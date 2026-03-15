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
package mslinks.extra;

import io.ByteReader;
import io.ByteWriter;
import io.Serializer;

import java.io.IOException;
import java.util.LinkedList;

import mslinks.Serializable;
import mslinks.ShellLinkException;

public class VistaIDList implements Serializable {

	public static final int signature = 0xA000000C;
	
	private LinkedList<byte[]> list = new LinkedList<>();

	public VistaIDList() {
	}

	public VistaIDList(ByteReader br, int size) throws ShellLinkException, IOException {
		this(new Serializer<ByteReader>(br), size);
	}
	
	public VistaIDList(Serializer<ByteReader> serializer, int size) throws ShellLinkException, IOException {
		if (size < 0xa)
			throw new ShellLinkException();
		
		int s = (int)serializer.read(2, "item id size");
		while (s != 0) {
			s -= 2;
			byte[] b = new byte[s];
			serializer.read(b, 0, s, "item id data");
			list.add(b);
			s = (int)serializer.read(2, "item id size");
		}		
	}
	
	@Override
	public void serialize(Serializer<ByteWriter> serializer) throws IOException {
		int size = 10;
		for (byte[] i : list)
			size += i.length + 2;

		serializer.write(size, 4, Serializer.BLOCK_SIZE_NAME);
		serializer.write(signature, 4, "signature");
		for (byte[] i : list) {
			serializer.write(i.length, 2, "item id size");
			serializer.write(i, "item id data");
		}
		serializer.write(0, 2, "item id size");
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (byte[] b : list)
			sb.append(new String(b) + "\n");
		return sb.toString();
	}
}
