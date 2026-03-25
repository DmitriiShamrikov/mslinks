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

import mslinks.Serializable;

public class Stub implements Serializable {
	
	private int sign;
	private byte[] data;

	public Stub(ByteReader br, int sz, int sgn) throws IOException {
		this(new Serializer<ByteReader>(br), sz, sgn);
	}

	public Stub(Serializer<ByteReader> serializer, int sz, int sgn) throws IOException {
		int len = sz - 8;
		sign = sgn;
		data = new byte[len];
		serializer.read(data, 0, data.length, "stub data");
	}
	
	@Override
	public void serialize(Serializer<ByteWriter> serializer) throws IOException {
		serializer.write(data.length + 8, 4, Serializer.BLOCK_SIZE_NAME);
		serializer.write(sign, 4, "signature", v -> getClass().getName());
		serializer.write(data, "data");
	}

}
