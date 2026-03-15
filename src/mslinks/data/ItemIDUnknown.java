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
import io.Serializer;
import mslinks.ShellLinkException;

public class ItemIDUnknown extends ItemID {

	protected byte[] data;

	@SuppressWarnings("removal")
	public ItemIDUnknown(int flags) {
		super(flags);
	}

	@Override
	public void load(Serializer<ByteReader> serializer, int maxSize) throws IOException, ShellLinkException {
		int startPos = serializer.getPosition();
		
		super.load(serializer, maxSize);
		
		int bytesRead = serializer.getPosition() - startPos;
		data = new byte[maxSize - bytesRead];
		serializer.read(data, 0, data.length, "data");
	}

	@Override
	public void serialize(ByteWriter bw) throws IOException {
		serialize(new Serializer<>(bw));
	}

	public void serialize(Serializer<ByteWriter> serializer) throws IOException {
		super.serialize(serializer);
		serializer.write(data, "data");
	}

	@Override
	public String toString() {
		return String.format("<ItemIDUnknown 0x%02X>", typeFlags);
	}
}
