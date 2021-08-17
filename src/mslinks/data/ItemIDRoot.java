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
import io.ByteWriter;
import mslinks.ShellLinkException;
import mslinks.UnsupportedItemIDException;

public class ItemIDRoot extends ItemID {

	public static final GUID CLSID_COMPUTER = new GUID("20d04fe0-3aea-1069-a2d8-08002b30309d");

	private GUID clsid;

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
	public void load(ByteReader br, int maxSize) throws IOException, ShellLinkException {
		super.load(br, maxSize);
		br.read(); // order
		clsid = new GUID(br);
	}

	@Override
	public void serialize(ByteWriter bw) throws IOException {
		super.serialize(bw);
		bw.write(0);
		clsid.serialize(bw);
	}

	public GUID getClsid() {
		return clsid;
	}

	public ItemIDRoot setClsid(GUID clsid) {
		// TODO: check validity?
		this.clsid = clsid;
		return this;
	}
}
