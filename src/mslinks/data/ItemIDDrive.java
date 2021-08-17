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
import mslinks.ShellLinkException;
import mslinks.UnsupportedItemIDException;

public class ItemIDDrive extends ItemID {

	protected String name;

	public ItemIDDrive(int flags) throws UnsupportedItemIDException {
		super(flags | GROUP_COMPUTER);

		int subType = typeFlags & ID_TYPE_INGROUPMASK;
		if (subType == 0)
			throw new UnsupportedItemIDException(typeFlags);
	}
	
	@Override
	public void load(ByteReader br, int maxSize) throws IOException, ShellLinkException {
		int startPos = br.getPosition();
		int endPos = startPos + maxSize;

		super.load(br, maxSize);

		int stringEndPos = br.getPosition() + 4;
		setName(br.readString(4));
		br.seek(stringEndPos - br.getPosition());

		// 8 bytes: drive size
		// 8 bytes: drive free size
		// 1 byte: 0/1 - has drive extension
		// 1 byte: 0/1 - drive extension has class id
		// 16 bytes: clsid - only possible value is CDBurn
		br.seek(endPos - br.getPosition());
	}

	@Override
	public void serialize(ByteWriter bw) throws IOException {
		super.serialize(bw);

		byte[] b = name.getBytes();
		bw.write(b);
		// TODO: check if this stuff is really needed 
		for (int i=0; i<22-b.length; i++)
			bw.write(0);
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}
	
	public ItemIDDrive setName(String s) throws ShellLinkException {
		if (s == null) 
			return this;
		
		if (Pattern.matches("\\w+:\\\\", s))
			name = s;
		else if (Pattern.matches("\\w+:", s))
			name = s + "\\";
		else if (Pattern.matches("\\w+", s))
			name = s + ":\\";
		else 
			throw new ShellLinkException("wrong drive name: " + s);
		
		return this;
	}
}
