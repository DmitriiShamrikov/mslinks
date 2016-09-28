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
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
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
	public static final int TYPE_FILE_OLD = 0x36;
	public static final int TYPE_DIRECTORY_OLD = 0x35;
	public static final int TYPE_FILE = 0x32;
	public static final int TYPE_DIRECTORY = 0x31;
	public static final int TYPE_DRIVE_OLD = 0x23;
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
	
	public ItemID(ByteReader br, int maxSize) throws IOException, ShellLinkException {
		int pos = br.getPosition();
		int endPos = pos + maxSize;
		type = br.read();
		if (type == TYPE_DRIVE || type == TYPE_DRIVE_OLD) {
			setName(br.readString(maxSize - 1));
			br.seek(endPos - br.getPosition());
		} else if (type == TYPE_FILE_OLD || type == TYPE_DIRECTORY_OLD) {
			br.read(); // unknown
			size = (int)br.read4bytes();
			br.read4bytes(); //last modified
			br.read2bytes(); // folder attributes
			longname = br.readUnicodeString(endPos - br.getPosition());
			shortname = br.readString(endPos - br.getPosition());
			br.seek(endPos - br.getPosition());
		} else if (type == TYPE_FILE || type == TYPE_DIRECTORY) {
			br.read(); // unknown
			size = (int)br.read4bytes();
			br.read4bytes(); //last modified
			br.read2bytes(); // folder attributes
			shortname = br.readString(endPos - br.getPosition());
			if (isLongFilename(shortname)) {
				longname = shortname;
				shortname = br.readString(endPos - br.getPosition());
				br.seek(pos + maxSize - br.getPosition());
				return;
			}
			if (endPos - br.getPosition() <= 2) {
				longname = shortname;
				br.seek(endPos - br.getPosition());
				return;
			}

			if (((br.getPosition() - pos) & 1) != 0)
				br.read();
			pos = br.getPosition();
			int extSize = (int)br.read2bytes();
			int extensionVersion = (int)br.read2bytes();
			br.read4bytes(); // unknown
			br.read4bytes(); // date created
			br.read4bytes(); // last accessed
			// unknown blocks depending on os version
			switch (extensionVersion) {
				case EXT_VERSION_WINXP: br.seek(4); break;
				case EXT_VERSION_VISTA: br.seek(22); break;
				case EXT_VERSION_WIN7: br.seek(26); break;
				case EXT_VERSION_WIN8: br.seek(30); break;
				default: throw new ShellLinkException("Unknown extension version");
			}
			longname = br.readUnicodeString(pos + extSize - br.getPosition());
			br.seek(pos + extSize - br.getPosition()); // unknown
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

		boolean unicodeName = longname != null && !longname.equals(shortname);
			
		int pos = bw.getPosition();
		//bw.write(type);
		int attr = 0;
		switch (type) {
			case TYPE_CLSID:
				bw.write(type);
				bw.write(0);
				clsid.serialize(bw);
				return;
			case TYPE_DRIVE:
			case TYPE_DRIVE_OLD:
				bw.write(type);
				byte[] b = getName().getBytes();
				bw.write(b);
				for (int i=0; i<22-b.length; i++)
					bw.write(0);
				return;
			case TYPE_DIRECTORY:
			case TYPE_DIRECTORY_OLD:
				bw.write(unicodeName ? TYPE_DIRECTORY_OLD : TYPE_DIRECTORY);
				bw.write(0);
				bw.write4bytes(0);
				attr = 0x10;
				break;
			case TYPE_FILE:
			case TYPE_FILE_OLD:
				bw.write(unicodeName ? TYPE_FILE_OLD : TYPE_FILE);
				bw.write(0);
				bw.write4bytes(size);
				break;
		}
		
		bw.write4bytes(0); // last modified
		bw.write2bytes(attr);
		// use simple old format without extension used in versions before xp
		// it seems like there are no problems on newer systems, also it supports long unicode names on old ones
		if (unicodeName) {
			bw.writeUnicodeString(longname, true);
			bw.writeBytes(shortname.getBytes());
			bw.write(0);
		} else {
			bw.writeBytes(shortname.getBytes());
			bw.write(0);
			bw.write(0);
		}

		/*
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
		bw.write4bytes(0); // unknown block depending on os version (always use WinXP)
		bw.writeUnicodeString(longname, true);
		bw.write2bytes((shortname.length() & ~1) + 16);
		*/
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
			shortname = isLongFilename(s) ? generateShortName(s) : s;
		}
		if (type == TYPE_DRIVE || type == TYPE_DRIVE_OLD) {
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
		if (t == TYPE_FILE || t == TYPE_DIRECTORY || t == TYPE_DRIVE || t == TYPE_DRIVE_OLD) {
			type = t;
			return this;
		}
		throw new ShellLinkException("wrong type");
	}

	private static boolean isLongFilename( String filename )
	{
		if( filename.charAt( 0 ) == '.' || filename.charAt( filename.length() - 1 ) == '.' )
			return true;

		if( !filename.matches( "^\\p{ASCII}+$" ) )
			return true;

		// no matter whether it is file or directory
		int dotIdx = filename.lastIndexOf( '.' );
		String baseName = dotIdx == -1 ? filename : filename.substring( 0, dotIdx );
		String ext = dotIdx == -1 ? "" : filename.substring( dotIdx + 1 );

		String wrongSymbolsPattern = ".*[\\.\"\\/\\\\\\[\\]:;=, ]+.*";
		return baseName.length() > 8 || ext.length() > 3 || baseName.matches( wrongSymbolsPattern ) || ext.matches( wrongSymbolsPattern );
	}

	private static String generateShortName( String longname )
	{
		// assume that it is actually long, don't check it again
		longname = longname.replaceAll( "\\.$|^\\.", "" );

		int dotIdx = longname.lastIndexOf( '.' );
		String baseName = dotIdx == -1 ? longname : longname.substring( 0, dotIdx );
		String ext = dotIdx == -1 ? "" : longname.substring( dotIdx + 1 );

		ext = ext.replaceAll( " ", "" ).replaceAll( "[\\.\"\\/\\\\\\[\\]:;=,\\+]", "_" );
		ext = ext.substring( 0, Math.min( 3, ext.length() ) );

		baseName = baseName.replaceAll( " ", "" ).replaceAll( "[\\.\"\\/\\\\\\[\\]:;=,\\+]", "_" );
		baseName = baseName.substring( 0, Math.min( 6, baseName.length() ) );

		// well, for same short names we should use "~2", "~3" and so on,
		// but actual index is generated by os while creating a file and stored in filesystem
		// so it is not possible to get actual one
		StringBuilder shortname = new StringBuilder( baseName + "~1" + ( ext.isEmpty() ? "" : "." + ext ) );

		// i have no idea how non-asci symbols are converted in dos names
		CharsetEncoder asciiEncoder = Charset.forName( "US-ASCII" ).newEncoder();
		for( int i = 0; i < shortname.length(); ++i )
		{
			if( !asciiEncoder.canEncode( shortname.charAt( i ) ) )
				shortname.setCharAt( i, '_' );
		}

		return shortname.toString().toUpperCase();
	}
}

