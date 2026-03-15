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
package mslinks;

import io.ByteReader;
import io.ByteWriter;
import io.Serializer;

import java.io.IOException;

import mslinks.data.FileAttributesFlags;
import mslinks.data.Filetime;
import mslinks.data.GUID;
import mslinks.data.HotKeyFlags;
import mslinks.data.LinkFlags;
import mslinks.data.Registry;

public class ShellLinkHeader implements Serializable {
	private static final int headerSize = 0x0000004C;
	
	public static final int SW_SHOWNORMAL = 1;
	public static final int SW_SHOWMAXIMIZED = 3;
	public static final int SW_SHOWMINNOACTIVE = 7;	
	
	private LinkFlags lf;
	private FileAttributesFlags faf;
	private Filetime creationTime;
	private Filetime accessTime;
	private Filetime writeTime;
	private int fileSize;
	private int iconIndex;
	private int showCommand;
	private HotKeyFlags hkf;
	
	public ShellLinkHeader() {
		lf = new LinkFlags(0);
		faf = new FileAttributesFlags(0);
		creationTime = new Filetime();
		accessTime = new Filetime();
		writeTime = new Filetime();
		showCommand = SW_SHOWNORMAL;
		hkf = new HotKeyFlags();
	}

	public ShellLinkHeader(ByteReader data) throws ShellLinkException, IOException {
		this(new Serializer<>(data));
	}

	public ShellLinkHeader(Serializer<ByteReader> serializer) throws ShellLinkException, IOException {
		try (var block = serializer.beginBlock("ShellLinkHeader")) {
			int size = (int)serializer.read(4, Serializer.BLOCK_SIZE_NAME);
			if (size != headerSize)
				throw new ShellLinkException();
			GUID g = new GUID(serializer);
			if (!g.equals(Registry.CLSID_LINK_HEADER))
				throw new ShellLinkException();
			lf = new LinkFlags(serializer);
			faf = new FileAttributesFlags(serializer);
			creationTime = new Filetime(serializer, "creationTime");
			accessTime = new Filetime(serializer, "accessTime");
			writeTime = new Filetime(serializer, "writeTime");
			fileSize = (int)serializer.read(4, "fileSize");
			iconIndex = (int)serializer.read(4, "iconIndex");
			showCommand = (int)serializer.read(4, "showCommand", ShellLinkHeader::commandToLog);
			if (showCommand != SW_SHOWNORMAL && showCommand != SW_SHOWMAXIMIZED && showCommand != SW_SHOWMINNOACTIVE) {
				showCommand = SW_SHOWNORMAL;
			}
			hkf = new HotKeyFlags(serializer);
			serializer.read(2, "reserved1");
			serializer.read(4, "reserved2");
			serializer.read(4, "reserved3");
		}
	}

	private static String commandToLog(long value) {
		// ffs... java can't switch on long
		if (value == SW_SHOWNORMAL) {
			return "SW_SHOWNORMAL";
		} else if (value == SW_SHOWMAXIMIZED) {
			return "SW_SHOWMAXIMIZED";
		} else if (value == SW_SHOWMINNOACTIVE) {
			return "SW_SHOWMINNOACTIVE";
		}
		return "UNKNOWN";
	}
	
	public LinkFlags getLinkFlags() { return lf; }
	public FileAttributesFlags getFileAttributesFlags() { return faf; }
	public Filetime getCreationTime() { return creationTime; }
	public Filetime getAccessTime() { return accessTime; }
	public Filetime getWriteTime() { return writeTime; }
	public HotKeyFlags getHotKeyFlags() { return hkf; }
	
	public int getFileSize() { return fileSize; }
	public ShellLinkHeader setFileSize(long n) { fileSize = (int)n; return this; }
	
	public int getIconIndex() { return iconIndex; }
	public ShellLinkHeader setIconIndex(int n) { iconIndex = n; return this; }
	
	public int getShowCommand() { return showCommand; }
	public ShellLinkHeader setShowCommand(int n) throws ShellLinkException { 
		if (n == SW_SHOWNORMAL || n == SW_SHOWMAXIMIZED || n == SW_SHOWMINNOACTIVE) {
			showCommand = n;
			return this; 
		} else 
			throw new ShellLinkException();
	}

	@Override
	public void serialize(Serializer<ByteWriter> serializer) throws IOException {
		try (var block = serializer.beginBlock("ShellLinkHeader")) {
			serializer.write(headerSize, 4, Serializer.BLOCK_SIZE_NAME);
			Registry.CLSID_LINK_HEADER.serialize(serializer);
			lf.serialize(serializer);
			faf.serialize(serializer);
			creationTime.serialize(serializer, "creationTime");
			accessTime.serialize(serializer, "accessTime");
			writeTime.serialize(serializer, "writeTime");
			serializer.write(fileSize, 4, "fileSize");
			serializer.write(iconIndex, 4, "iconIndex");
			serializer.write(showCommand, 4, "showCommand", ShellLinkHeader::commandToLog);
			hkf.serialize(serializer);
			serializer.write(0, 2, "reserved1");
			serializer.write(0,4, "reserved2");
			serializer.write(0,4, "reserved3");
		}
	}
}
