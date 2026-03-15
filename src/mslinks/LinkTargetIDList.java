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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import mslinks.data.ItemID;
import mslinks.data.ItemIDDrive;
import mslinks.data.ItemIDFS;
import mslinks.data.ItemIDRoot;
import mslinks.data.ItemIDUnknown;
import mslinks.data.Registry;

public class LinkTargetIDList extends LinkedList<ItemID> implements Serializable {
	
	public LinkTargetIDList() {}
	
	public LinkTargetIDList(ByteReader data) throws IOException, ShellLinkException {
		this(new Serializer<ByteReader>(data));
	}

	public LinkTargetIDList(Serializer<ByteReader> serializer) throws IOException, ShellLinkException {
		try (var block = serializer.beginBlock("LinkTargetIDList")) {
			int size = (int)serializer.read(2, Serializer.BLOCK_SIZE_NAME);
			int pos = serializer.getPosition(); 
			
			while (true) {
				try (var itemBlock = serializer.beginBlock("ItemBlock")) {
					int itemSize = (int)serializer.read(2, Serializer.BLOCK_SIZE_NAME);
					if (itemSize == 0)
						break;

					int typeFlags = serializer.read("typeFlags", ItemID::typeFlagsToLog);
					var item = ItemID.createItem(typeFlags);
					item.load(serializer, itemSize - 3);
					add(item);
				}
			}
			
			pos = serializer.getPosition() - pos;
			if (pos != size) 
				throw new ShellLinkException("unexpected size of LinkTargetIDList");
		}
	}

	@Override
	public void serialize(Serializer<ByteWriter> serializer) throws IOException {
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
		
		try (var block = serializer.beginBlock("LinkTargetIDList")) {
			serializer.write(size, 2, Serializer.BLOCK_SIZE_NAME);
			for (int j = 0; j < this.size(); ++j) {
				try (var itemBlock = serializer.beginBlock("ItemBlock")) {
					serializer.write(b[j].length + 2, 2, Serializer.BLOCK_SIZE_NAME);
					this.get(j).serialize(serializer);
				}
			}
			serializer.write(0, 2, Serializer.BLOCK_SIZE_NAME);
		}
	}

	/**
	 * @deprecated Equivalent of {@link #canBuildPath()} method
	 */
	@Deprecated(since = "1.0.9", forRemoval = true)
	public boolean isCorrect() {
		return canBuildPath();
	}
	
	public boolean canBuildPath() {
		for (ItemID i : this)
			if (i instanceof ItemIDUnknown)
				return false;
		return true;
	}

	public boolean canBuildAbsolutePath() {
		if (size() < 2)
			return false;

		var firstId = getFirst();
		if (!(firstId instanceof ItemIDRoot))
			return false;
		
		var rootId = (ItemIDRoot) firstId;
		if (!rootId.getClsid().equals(Registry.CLSID_COMPUTER))
			return false;

		var secondId = get(1);
		return secondId instanceof ItemIDDrive;
	}

	public String buildPath() {
		var path = new StringBuilder();
		if (!isEmpty()) {
			// when a link created by drag'n'drop menu from desktop, id list starts from filename directly
			var firstId = getFirst();
			if (firstId instanceof ItemIDFS)
				path.append("<Desktop>\\");

			for (ItemID i : this) {
				path.append(i.toString());
			}
		}
		return path.toString();
	}
}
