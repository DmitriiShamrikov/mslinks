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
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import mslinks.Serializable;
import mslinks.ShellLinkException;

public abstract class ItemID implements Serializable {

	// from NT\shell\shell32\shitemid.h

	public static final int ID_TYPE_JUNCTION = 0x80;
	public static final int ID_TYPE_GROUPMASK = 0x70;
	public static final int ID_TYPE_INGROUPMASK = 0x0f;

	public static final int GROUP_ROOT = 0x10;
	public static final int GROUP_COMPUTER = 0x20;
	public static final int GROUP_FS = 0x30;
	public static final int GROUP_NET = 0x40;
	public static final int GROUP_LOC = 0x50;
	public static final int GROUP_CONTROLPANEL = 0x70;

	// GROUP_ROOT
	public static final int TYPE_ROOT_REGITEM = 0x0f;

	// GROUP_COMPUTER
	public static final int TYPE_DRIVE_RESERVED_1 = 0x1;
	public static final int TYPE_DRIVE_REMOVABLE = 0x2;
	public static final int TYPE_DRIVE_FIXED = 0x3;
	public static final int TYPE_DRIVE_REMOTE = 0x4;
	public static final int TYPE_DRIVE_CDROM = 0x5;
	public static final int TYPE_DRIVE_RAMDISK = 0x6;
	public static final int TYPE_DRIVE_RESERVED_7 = 0x7;
	public static final int TYPE_DRIVE_DRIVE525 = 0x8;
	public static final int TYPE_DRIVE_DRIVE35 = 0x9;
	public static final int TYPE_DRIVE_NETDRIVE = 0xa;    // Network drive
	public static final int TYPE_DRIVE_NETUNAVAIL = 0xb;  // Network drive that is not restored.
	public static final int TYPE_DRIVE_RESERVED_C = 0xc;
	public static final int TYPE_DRIVE_RESERVED_D = 0xd;
	public static final int TYPE_DRIVE_REGITEM = 0xe;     // Controls, Printers, ... Do not confuse with TYPE_ROOT_REGITEM
	public static final int TYPE_DRIVE_MISC = 0xf;

	// GROUP_FS - these values can be combined
	public static final int TYPE_FS_DIRECTORY = 0x1;
	public static final int TYPE_FS_FILE = 0x2;
	public static final int TYPE_FS_UNICODE = 0x4;
	public static final int TYPE_FS_COMMON = 0x8;

	// GROUP_NET
	public static final int TYPE_NET_DOMAIN = 0x1;
	public static final int TYPE_NET_SERVER = 0x2;
	public static final int TYPE_NET_SHARE = 0x3;
	public static final int TYPE_NET_FILE = 0x4;
	public static final int TYPE_NET_GROUP = 0x5;
	public static final int TYPE_NET_NETWORK = 0x6;
	public static final int TYPE_NET_RESTOFNET = 0x7;
	public static final int TYPE_NET_SHAREADMIN = 0x8;
	public static final int TYPE_NET_DIRECTORY = 0x9;
	public static final int TYPE_NET_TREE = 0xa;
	public static final int TYPE_NET_NDSCONTAINER = 0xb;
	public static final int TYPE_NET_REGITEM = 0xd;
	public static final int TYPE_NET_REMOTEREGITEM = 0xe;
	public static final int TYPE_NET_PRINTER = 0xf;

	// GROUP_LOC - ???
	
	// GROUP_CONTROLPANEL
	public static final int TYPE_CONTROL_REGITEM = 0x0;
	public static final int TYPE_CONTROL_REGITEM_EX = 0x1;

	public static ItemID createItem(int typeFlags) throws ShellLinkException {
		if ((typeFlags & ID_TYPE_JUNCTION) != 0)
			throw new ShellLinkException("junctions are not supported");

		int group = typeFlags & ID_TYPE_GROUPMASK;
		switch (group) {
		case GROUP_ROOT:
			return new ItemIDRoot(typeFlags);
		case GROUP_COMPUTER: 
			return new ItemIDDrive(typeFlags);
		case GROUP_FS:
			return new ItemIDFS(typeFlags);
		default:
			return new ItemIDUnknown(typeFlags);
		}

		//throw new UnsupportedItemIDException(typeFlags);
	}


	protected int typeFlags;
	
	protected ItemID(int flags) {
		this.typeFlags = flags;
	}

	public void load(ByteReader br, int maxSize) throws IOException, ShellLinkException {
		// DO NOT read type flags here as they have already been read
		// in order to determine the type of this item id
	}

	@Override
	public void serialize(ByteWriter bw) throws IOException {
		bw.write(typeFlags);
	}

	@Override
	public String toString() {
		return "";
	}

	public int getTypeFlags() { return typeFlags; }
	public ItemID setTypeFlags(int flags) throws ShellLinkException {
		if ((flags & ID_TYPE_GROUPMASK) != 0) {
			throw new ShellLinkException("ItemID group cannot be changed. " +
				"Create a new instance of an appropriate type instead.");
		}

		if ((flags & ID_TYPE_JUNCTION) != 0)
			throw new ShellLinkException("Junctions are not supported");

		typeFlags = (typeFlags & ID_TYPE_GROUPMASK) | (flags & ID_TYPE_INGROUPMASK);
		return this;
	}

	protected static boolean isLongFilename(String filename) {
		if (filename.charAt(0) == '.' || filename.charAt(filename.length() - 1) == '.')
			return true;

		if (!filename.matches("^\\p{ASCII}+$"))
			return true;

		// no matter whether it is file or directory
		int dotIdx = filename.lastIndexOf('.');
		String baseName = dotIdx == -1 ? filename : filename.substring(0, dotIdx);
		String ext = dotIdx == -1 ? "" : filename.substring(dotIdx + 1);

		String wrongSymbolsPattern = ".*[\\.\"\\/\\\\\\[\\]:;=, ]+.*";
		return baseName.length() > 8 || ext.length() > 3 || baseName.matches(wrongSymbolsPattern) || ext.matches(wrongSymbolsPattern);
	}

	protected static String generateShortName(String longname) {
		// assume that it is actually long, don't check it again
		longname = longname.replaceAll("\\.$|^\\.", "");

		int dotIdx = longname.lastIndexOf('.');
		String baseName = dotIdx == -1 ? longname : longname.substring(0, dotIdx);
		String ext = dotIdx == -1 ? "" : longname.substring(dotIdx + 1);

		ext = ext.replace(" ", "").replaceAll("[\\.\"\\/\\\\\\[\\]:;=,\\+]", "_");
		ext = ext.substring(0, Math.min(3, ext.length()));

		baseName = baseName.replace(" ", "").replaceAll("[\\.\"\\/\\\\\\[\\]:;=,\\+]", "_");
		baseName = baseName.substring(0, Math.min(6, baseName.length()));

		// well, for same short names we should use "~2", "~3" and so on,
		// but actual index is generated by os while creating a file and stored in filesystem
		// so it is not possible to get actual one
		StringBuilder shortname = new StringBuilder(baseName + "~1" + (ext.isEmpty() ? "" : "." + ext));

		// i have no idea how non-asci symbols are converted in dos names
		CharsetEncoder asciiEncoder = StandardCharsets.US_ASCII.newEncoder();
		for (int i = 0; i < shortname.length(); ++i)
		{
			if (!asciiEncoder.canEncode(shortname.charAt(i)))
				shortname.setCharAt(i, '_');
		}

		return shortname.toString().toUpperCase();
	}
}

