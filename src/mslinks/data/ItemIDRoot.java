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

import io.ByteReader;
import io.Serializer;
import mslinks.ShellLinkException;
import mslinks.UnsupportedItemIDException;

public class ItemIDRoot extends ItemIDRegItem {

	public ItemIDRoot() {
		super(GROUP_ROOT | TYPE_ROOT_REGITEM);
	}

	public ItemIDRoot(int flags) throws UnsupportedItemIDException {
		super(flags | GROUP_ROOT);

		int subType = typeFlags & ID_TYPE_INGROUPMASK;
		if (subType != TYPE_ROOT_REGITEM)
			throw new UnsupportedItemIDException(typeFlags);
	}

	@Override
	public void load(Serializer<ByteReader> serializer, int maxSize) throws IOException, ShellLinkException {
		int endPos = serializer.getPosition() + maxSize;
		super.load(serializer, maxSize);
		serializer.seekTo(endPos);
	}

	@Override
	public String toString() {
		if (clsid.equals(Registry.CLSID_COMPUTER))
			return "";
		return super.toString();
	}
}
