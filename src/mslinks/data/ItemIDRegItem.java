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
import mslinks.UnsupportedCLSIDException;

public abstract class ItemIDRegItem extends ItemID {
	
	protected GUID clsid;

	@SuppressWarnings("removal")
	public ItemIDRegItem(int flags) {
		super(flags);
	}

	@Override
	public void load(ByteReader br, int maxSize) throws IOException, ShellLinkException {
		super.load(br, maxSize);
		br.read(); // order
		setClsid(new GUID(br));
	}

	@Override
	public void serialize(ByteWriter bw) throws IOException {
		super.serialize(bw);
		bw.write(0); // order
		clsid.serialize(bw);
	}

	@Override
	public String toString() {
		String name;
		try {
			name = Registry.getName(clsid);
		} catch (UnsupportedCLSIDException e) {
			name = this.getClass().getSimpleName();
		}
		return "<" + name + ">\\";
	}

	public GUID getClsid() {
		return clsid;
	}

	public ItemIDRegItem setClsid(GUID clsid) throws UnsupportedCLSIDException {
		if (!Registry.canUseClsidIn(clsid, this.getClass()))
			throw new UnsupportedCLSIDException(clsid);
		this.clsid = clsid;
		return this;
	}
}
