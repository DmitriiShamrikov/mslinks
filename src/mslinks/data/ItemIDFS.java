/*
	https://github.com/DmitriiShamrikov/mslinks
	
	Copyright (c) 2022 Dmitrii Shamrikov

	Licensed under the WTFPL
	You may obtain a copy of the License at
 
	http://www.wtfpl.net/about/
 
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/
package mslinks.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.ByteReader;
import io.ByteWriter;
import io.Serializer;
import mslinks.ShellLinkException;
import mslinks.UnsupportedItemIDException;

public class ItemIDFS extends ItemID {

	// from NT\shell\inc\idhidden.h

	protected static final int HIDDEN_ID_EMPTY = 0;
	protected static final int HIDDEN_ID_URLFRAGMENT = 1;         //  Fragment IDs on URLs (#anchors)
	protected static final int HIDDEN_ID_URLQUERY = 2;            //  Query strings on URLs (?query+info)
	protected static final int HIDDEN_ID_JUNCTION = 3;            //  Junction point data
	protected static final int HIDDEN_ID_IDFOLDEREX = 4;          //  IDFOLDEREX, extended data for CFSFolder
	protected static final int HIDDEN_ID_DOCFINDDATA = 5;         //  DocFind's private attached data (not persisted)
	protected static final int HIDDEN_ID_PERSONALIZED = 6;        //  personalized like (My Docs/Zeke's Docs)
	protected static final int HIDDEN_ID_recycle2 = 7;            //  recycle
	protected static final int HIDDEN_ID_RECYCLEBINDATA = 8;      //  RecycleBin private data (not persisted)
	protected static final int HIDDEN_ID_RECYCLEBINORIGINAL = 9;  //  the original unthunked path for RecycleBin items
	protected static final int HIDDEN_ID_PARENTFOLDER = 10;       //  merged folder uses this to encode the source folder.
	protected static final int HIDDEN_ID_STARTPANEDATA = 11;      //  Start Pane's private attached data
	protected static final int HIDDEN_ID_NAVIGATEMARKER = 12;     //  Used by Control Panel's 'Category view'

	// from NT\public\sdk\inc\ntioapi.h

	public static final int FILE_ATTRIBUTE_READONLY = 0x00000001;
	public static final int FILE_ATTRIBUTE_HIDDEN = 0x00000002;
	public static final int FILE_ATTRIBUTE_SYSTEM = 0x00000004;

	public static final int FILE_ATTRIBUTE_DIRECTORY = 0x00000010;
	public static final int FILE_ATTRIBUTE_ARCHIVE = 0x00000020;
	public static final int FILE_ATTRIBUTE_DEVICE = 0x00000040;
	public static final int FILE_ATTRIBUTE_NORMAL = 0x00000080;

	public static final int FILE_ATTRIBUTE_TEMPORARY = 0x00000100;
	public static final int FILE_ATTRIBUTE_SPARSE_FILE = 0x00000200;
	public static final int FILE_ATTRIBUTE_REPARSE_POINT = 0x00000400;
	public static final int FILE_ATTRIBUTE_COMPRESSED = 0x00000800;

	public static final int FILE_ATTRIBUTE_OFFLINE = 0x00001000;
	public static final int FILE_ATTRIBUTE_NOT_CONTENT_INDEXED = 0x00002000;
	public static final int FILE_ATTRIBUTE_ENCRYPTED = 0x00004000;


	protected int size;
	protected short attributes;
	protected String shortname;
	protected String longname;

	@SuppressWarnings("removal")
	public ItemIDFS(int flags) throws UnsupportedItemIDException {
		super(flags | GROUP_FS);
		onTypeFlagsChanged();
	}

	private void onTypeFlagsChanged() throws UnsupportedItemIDException {
		int subType = typeFlags & ID_TYPE_INGROUPMASK;
		if ((subType & TYPE_FS_DIRECTORY) == 0 && (subType & TYPE_FS_FILE) == 0)
			throw new UnsupportedItemIDException(typeFlags);

		// don't allow flipping unicode flag at will to avoid inconsistency 
		if (longname != null) {
			if (isLongFilename(longname))
				typeFlags |= TYPE_FS_UNICODE;
			else
				typeFlags &= ~TYPE_FS_UNICODE;
		}

		// attribute directory flag should match the typeFlag directory flag
		if ((subType & TYPE_FS_DIRECTORY) != 0)
			attributes |= FILE_ATTRIBUTE_DIRECTORY;
		else
			attributes &= ~FILE_ATTRIBUTE_DIRECTORY;
	}
	
	@Override
	public void load(Serializer<ByteReader> serializer, int maxSize) throws IOException, ShellLinkException {
		// 3 bytes are the size (2) and the type (1) initially parsed in LinkTargetIDList
		// but they are considered part of the ItemID for calculating offsets
		int startPos = serializer.getPosition() - 3;
		int endPos = startPos + maxSize + 3;

		super.load(serializer, maxSize);

		serializer.read("padding ?"); // IDFOLDER struct doesn't have this byte but it does exist in data. Probably it's just padding
		size = (int)serializer.read(4, "file size");
		serializer.read(2, "date modified");
		serializer.read(2, "time modified");
		attributes = (short)serializer.read(2, "attributes", ItemIDFS::attributesToLog);

		if ((typeFlags & TYPE_FS_UNICODE) != 0) {
			longname = serializer.readUnicodeStringNullTerm(endPos - serializer.getPosition(), "longname");
		}
		shortname = serializer.readString(endPos - serializer.getPosition(), "shortname");

		int restOfDataSize = endPos - serializer.getPosition();
		if (restOfDataSize <= 2) {
			serializer.seek(restOfDataSize);
			return;
		}

		// last 2 bytes are the offset to the hidden list
		// someone had a briliant fucking idea to put offset in the end of the block, here goes the mess
		int bytesParsed = serializer.getPosition() - startPos;
		byte[] dataChunk = new byte[restOfDataSize];

		serializer.setSuspendLogging(true);
		serializer.read(dataChunk, 0, dataChunk.length - 2, "hidden chunk");
		int hiddenOffset = (int)serializer.read(2, "hiddenOffset");
		serializer.setSuspendLogging(false);

		if (hiddenOffset == 0 || hiddenOffset < bytesParsed) {
			return;
		}

		if (serializer.isLoggingActive()) {
			var outStream = new ByteArrayOutputStream();
			var bw = new ByteWriter(outStream);
			if (serializer.isLittleEndian()) {
				bw.setLittleEndian();
			} else {
				bw.setBigEndian();
			}
			bw.write(hiddenOffset, 2);
			bw.close();
			byte[] arr = outStream.toByteArray();
			dataChunk[dataChunk.length - 2] = arr[0];
			dataChunk[dataChunk.length - 1] = arr[1];
		}

		int offsetInDataChunk = hiddenOffset - bytesParsed;
		var hs = new Serializer<>(new ByteReader(new ByteArrayInputStream(dataChunk, 0, dataChunk.length)));
		hs.seek(offsetInDataChunk);
		loadHiddenPart(hs, dataChunk.length);
		hs.read(2, "hiddenOffset");
	}

	protected void loadHiddenPart(Serializer<ByteReader> serializer, int maxSize) throws IOException {
		while (true) {
			int startPos = serializer.getPosition();
			int hiddenSize = (int)serializer.read(2, "hiddenSize");
			int hiddenVersion = (int)serializer.read(2, "hiddenVersion");
			int hiddenIdField = (int)serializer.read(4, "hiddenIdField", ItemIDFS::hiddenIdToLog);
			int hiddenIdMagic = (hiddenIdField & 0xFFFF0000) >>> 16;
			int hiddenId = hiddenIdField & 0xFFFF;

			int hiddenEndPos = startPos + hiddenSize;
			if (hiddenEndPos > maxSize) {
				break;
			}
			
			if (hiddenIdMagic != 0xBEEF) {
				serializer.seek(hiddenSize - 8);
				continue;
			}

			if (hiddenId == HIDDEN_ID_IDFOLDEREX && hiddenVersion >= 3) { // IDFX_V1
				serializer.read(4, "date & time created");
				serializer.read(4, "date & time accessed");
				int offsetNameUnicode = (int)serializer.read(2, "offsetNameUnicode");
				serializer.read(2, "offResourceA"); // offResourceA
				int unicodeNamePos = startPos + offsetNameUnicode;
				serializer.seek(unicodeNamePos - serializer.getPosition());
				longname = serializer.readUnicodeStringNullTerm(startPos + hiddenSize - serializer.getPosition(), "longname");

				// we don't serialize hidden parts so add unicode flag
				if (!longname.equals(shortname))
					typeFlags |= TYPE_FS_UNICODE;
				break;
			}
		}
	}

	@Override
	public void serialize(ByteWriter bw) throws IOException {
		serialize(new Serializer<>(bw));
	}

	public void serialize(Serializer<ByteWriter> serializer) throws IOException {
		super.serialize(serializer);
		serializer.write(0, "padding");
		serializer.write(size, 4, "file size");
		serializer.write(0, 2, "date modified");
		serializer.write(0, 2, "time modified");
		serializer.write(attributes, 2, "attributes", ItemIDFS::attributesToLog);

		if ((typeFlags & TYPE_FS_UNICODE) != 0) {
			serializer.writeUnicodeStringNullTerm(longname, "longname");
			serializer.writeString(shortname, "shortname");
		} else {
			serializer.writeString(shortname,"shortname");
			serializer.write(0, "");
		}
	}

	private static String attributesToLog(long value) {
		var builder = new StringBuilder();
		Serializer.iterateOverClassConsts(ItemIDFS.class, (field, constValue) -> {
			if (field.getName().startsWith("FILE_ATTRIBUTE_") && (constValue & value) != 0) {
				if (!builder.isEmpty()) {
					builder.append(" | ");
				}
				builder.append(field.getName());
			}
			return true;
		});
		return builder.toString();
	}

	private static String hiddenIdToLog(long value) {
		int hiddenId = (int)(value & 0xFFFF);
		var f = Serializer.findConstField(ItemIDFS.class, hiddenId, field -> field.getName().startsWith("HIDDEN_ID_"));
		return f != null ? f.getName() : "UNKNOWN";
	}

	@Override
	public String toString() {
		String name = (typeFlags & TYPE_FS_UNICODE) != 0 ? longname : shortname;
		if ((typeFlags & TYPE_FS_DIRECTORY) != 0)
			name += "\\";
		return name;
	}

	@Override
	public ItemID setTypeFlags(int flags) throws ShellLinkException {
		super.setTypeFlags(flags);
		onTypeFlagsChanged();
		return this;
	}

	@Override
	@SuppressWarnings("removal")
	public int getSize() { return size; }
	@Override
	@SuppressWarnings("removal")
	public ItemID setSize(int s) {
		size = s;
		return this;
	}

	public short getAttributes() { return attributes; }
	public ItemIDFS setAttributes(short attr) throws ShellLinkException {
		attributes = attr;
		if ((attr & FILE_ATTRIBUTE_DIRECTORY) != 0) {
			typeFlags |= TYPE_FS_DIRECTORY;
			typeFlags &= ~TYPE_FS_FILE;
		} else {
			typeFlags &= ~TYPE_FS_DIRECTORY;
			typeFlags |= TYPE_FS_FILE;
		}
		return this;
	}

	@Override
	@SuppressWarnings("removal")
	public String getName() {
		if (longname != null && !longname.equals(""))
			return longname;
		return shortname;
	}
	
	@Override
	@SuppressWarnings("removal")
	public ItemID setName(String s) throws ShellLinkException {
		if (s == null) 
			return this;
		
		if (s.contains("\\"))
			throw new ShellLinkException("wrong ItemIDFS name: " + s);

		longname = s;
		if (isLongFilename(s)) {
			shortname = generateShortName(s);
			typeFlags |= TYPE_FS_UNICODE;
		} else {
			shortname = s;
			typeFlags &= ~TYPE_FS_UNICODE;
		}

		return this;
	}
}
