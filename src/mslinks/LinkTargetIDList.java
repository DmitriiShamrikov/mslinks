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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import mslinks.data.ItemID;
import mslinks.data.ItemIDUnknown;

public class LinkTargetIDList extends LinkedList<ItemID> implements Serializable {
	
	public LinkTargetIDList() {}
	
	public LinkTargetIDList(ByteReader data) throws IOException, ShellLinkException {
		int size = (int)data.read2bytes();
		int pos = data.getPosition(); 
		
		while (true) {
			int itemSize = (int)data.read2bytes();
			if (itemSize == 0)
				break;

			int typeFlags = data.read();
			var item = ItemID.createItem(typeFlags);
			item.load(data, itemSize - 3);
			add(item);
		}
		
		pos = data.getPosition() - pos;
		if (pos != size) 
			throw new ShellLinkException("unexpected size of LinkTargetIDList");
	}

	public void serialize(ByteWriter bw) throws IOException {
		int size = 2;
		byte[][] b = new byte[size()][];
		int i = 0;
		for (ItemID j : this) {
			ByteArrayOutputStream ba = new ByteArrayOutputStream();
			ByteWriter w = new ByteWriter(ba);
			
			j.serialize(w);
			b[i++] = ba.toByteArray();
		}
		for (byte[] j : b)
			size += j.length + 2;
		
		bw.write2bytes(size);
		for (byte[] j : b) {
			bw.write2bytes(j.length + 2);
			bw.write(j);
		}
		bw.write2bytes(0);
	}
	
	public boolean isCorrect() {
		for (ItemID i : this)
			if (i instanceof ItemIDUnknown)
				return false;
		return true;
	}

	public String buildPath() {
		var path = new StringBuilder();
		for (ItemID i : this) {
			path.append(i.toString());
		}
		return path.toString();
	}
}
