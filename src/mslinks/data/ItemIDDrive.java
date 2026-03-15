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

import java.io.IOException;
import java.util.regex.Pattern;

import io.ByteReader;
import io.ByteWriter;
import io.Serializer;
import mslinks.ShellLinkException;
import mslinks.UnsupportedItemIDException;

public class ItemIDDrive extends ItemID {

	protected String name;

	@SuppressWarnings("removal")
	public ItemIDDrive(int flags) throws UnsupportedItemIDException {
		super(flags | GROUP_COMPUTER);

		int subType = typeFlags & ID_TYPE_INGROUPMASK;
		if (subType == 0)
			throw new UnsupportedItemIDException(typeFlags);
	}

	@Override
	public void load(Serializer<ByteReader> serializer, int maxSize) throws IOException, ShellLinkException {
		int startPos = serializer.getPosition();
		int endPos = startPos + maxSize;

		super.load(serializer, maxSize);

		setName(serializer.readString(4, "drive name"));
		// 8 bytes: drive size
		// 8 bytes: drive free size
		// 1 byte: 0/1 - has drive extension
		// 1 byte: 0/1 - drive extension has class id
		// 16 bytes: clsid - only possible value is CDBurn
		serializer.seekTo(endPos);
	}

	@Override
	public void serialize(ByteWriter bw) throws IOException {
		serialize(new Serializer<>(bw));
	}

	public void serialize(Serializer<ByteWriter> serializer) throws IOException {
		super.serialize(serializer);

		serializer.writeString(name, "name");
		serializer.write(0, 8, "drive size");
		serializer.write(0, 8, "drive free size");
		serializer.write(0, "has drive extension");
		serializer.write(0, "has class id");
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	@SuppressWarnings("removal")
	public String getName() {
		return name;
	}
	
	@Override
	@SuppressWarnings("removal")
	public ItemIDDrive setName(String s) throws ShellLinkException {
		if (s == null) 
			return this;
		
		if (Pattern.matches("\\w:\\\\", s))
			name = s;
		else if (Pattern.matches("\\w:", s))
			name = s + "\\";
		else if (Pattern.matches("\\w", s))
			name = s + ":\\";
		else 
			throw new ShellLinkException("wrong drive name: " + s);
		
		return this;
	}
}
