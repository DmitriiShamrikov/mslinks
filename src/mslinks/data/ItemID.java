/*
	https://github.com/BlackOverlord666/mslinks
	
	Copyright (c) 2015 Dmitrii Shamrikov

	Licensed under the WTFPL
	You may obtain a copy of the License at
 
	http://www.wtfpl.net/about/
 
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/
package mslinks.data;

import io.ByteReader;
import io.ByteWriter;

import java.io.IOException;
import java.util.regex.Pattern;

import mslinks.Serializable;
import mslinks.ShellLinkException;
import mslinks.UnsupportedCLSIDException;

public class ItemID implements Serializable {
	
	private static final GUID mycomputer = new GUID("20d04fe0-3aea-1069-a2d8-08002b30309d");
	private static byte[] ub1 = new byte[] {4, 0, -17, -66}; // unknown bytes
	private static byte[] ub2 = new byte[] {42, 0, 0, 0}; // unknown bytes

	private static final int EXT_VERSION_WINXP = 3;
	private static final int EXT_VERSION_VISTA = 7;
	private static final int EXT_VERSION_WIN7 = 8;
	private static final int EXT_VERSION_WIN8 = 9; // same for win10
	
	public static final int TYPE_UNKNOWN = 0;
	public static final int TYPE_FILE = 0x32;
	public static final int TYPE_DIRECTORY = 0x31;
	public static final int TYPE_DRIVE = 0x2f;
	public static final int TYPE_CLSID = 0x1f;
	
	private int type;
	private int size;
	private String shortname, longname;
	private GUID clsid;
	private byte[] data;
	
	public ItemID() {
		shortname = "";
		longname = "";
	}
	
	public ItemID(byte[] d) {
		data = d;
	}
	
	public ItemID(ByteReader br) throws IOException, ShellLinkException {
		int pos = br.getPosition();
		type = br.read();
		if (type == TYPE_DRIVE) {
			setName(br.readString(22));
			br.seek(pos + 23 - br.getPosition());
		} else if (type == TYPE_FILE || type == TYPE_DIRECTORY) {
			br.read(); // unknown
			size = (int)br.read4bytes();
			br.read4bytes(); //last modified
			br.read2bytes(); // folder attributes
			shortname = br.readString(13);
			if (((br.getPosition() - pos) & 1) != 0)
				br.read();
			pos = br.getPosition();
			int sz = (int)br.read2bytes();
			int extensionVersion = (int)br.read2bytes();
			br.read4bytes(); // unknown
			br.read4bytes(); // date created
			br.read4bytes(); // last accessed
			// unknown blocks depended on os version
			switch (extensionVersion) {
				case EXT_VERSION_WINXP: br.seek(4); break;
				case EXT_VERSION_VISTA: br.seek(22); break;
				case EXT_VERSION_WIN7: br.seek(26); break;
				case EXT_VERSION_WIN8: br.seek(30); break;
				default: throw new ShellLinkException("Unknown extension version");
			}
			longname = br.readUnicodeString(pos + sz - br.getPosition());
			br.seek(pos + sz - br.getPosition()); // unknown
		} else if (type == TYPE_CLSID) {
			br.read(); // unknown
			clsid = new GUID(br);
			if (!clsid.equals(mycomputer)) 
				throw new UnsupportedCLSIDException();
		} else 
			throw new ShellLinkException("unsupported ItemID type");
	}

	@Override
	public void serialize(ByteWriter bw) throws IOException {
		if (data != null) {
			bw.writeBytes(data);
			return;
		}
			
		int pos = bw.getPosition();
		bw.write(type);
		int attr = 0;
		switch (type) {
			case TYPE_CLSID:
				bw.write(0);
				clsid.serialize(bw);				
				return;
			case TYPE_DRIVE:
				byte[] b = getName().getBytes();
				bw.write(b);
				for (int i=0; i<22-b.length; i++)
					bw.write(0);
				return;
			case TYPE_DIRECTORY:
				bw.write(0);
				bw.write4bytes(0);
				attr = 0x10;
				break;
			case TYPE_FILE:
				bw.write(0);
				bw.write4bytes(size);
				break;
		}
		
		bw.write4bytes(0); // last modified
		bw.write2bytes(attr);
		bw.writeBytes(shortname.getBytes());
		bw.write(0);
		if (((bw.getPosition() - pos) & 1) != 0) 
			bw.write(0);
		
		bw.write2bytes(2 + 2 + ub1.length + 4 + 4 + ub2.length + 4 + (longname.length() + 1) * 2 + 2);
		bw.write2bytes(EXT_VERSION_WINXP);
		bw.writeBytes(ub1);
		bw.write4bytes(0); // date created
		bw.write4bytes(0); // last accessed
		bw.writeBytes(ub2);
		bw.write4bytes(0); // unknown block depended on os version (always use WinXP)
		bw.writeUnicodeString(longname, true);
		bw.write2bytes((shortname.length() & ~1) + 16);
	}
	
	public String getName() {
		if (longname != null && !longname.equals(""))
			return longname;
		return shortname;
	}
	
	public ItemID setName(String s) throws ShellLinkException {
		if (s == null) 
			return this;
		
		if (type == TYPE_FILE || type == TYPE_DIRECTORY) {
			if (s.contains("\\"))
				throw new ShellLinkException("wrong name");
			
			longname = s;
			
			String name, ext = "";			
			int dot = s.lastIndexOf('.');
			if (dot != -1) {
				name = s.substring(0, dot);
				ext = s.substring(name.length());
			} else name = s;
			
			if (name.length() > 8)
				name = name.substring(0, 6) + "~1";
			
			shortname = name + ext;			
		}
		if (type == TYPE_DRIVE) {
			if (Pattern.matches("\\w+:\\\\", s))
				shortname = longname = s;
			else if (Pattern.matches("\\w+:", s))
				shortname = longname = s + "\\";
			else if (Pattern.matches("\\w+", s))
				shortname = longname = s + ":\\";
			else throw new ShellLinkException("wrong name");
		}
		return this;
	}
	
	public int getSize() { return size; }
	public ItemID setSize(int s) throws ShellLinkException {
		if (type != TYPE_FILE)
			throw new ShellLinkException("only files has size");
		size = s;
		return this;
	}
	
	public int getType() { return type; }
	public ItemID setType(int t) throws ShellLinkException {
		if (t == TYPE_CLSID) {
			type = t;
			clsid = mycomputer;
			return this;
		}
		if (t == TYPE_FILE || t == TYPE_DIRECTORY || t == TYPE_DRIVE) {
			type = t;
			return this;
		}
		throw new ShellLinkException("wrong type");
	}

}

