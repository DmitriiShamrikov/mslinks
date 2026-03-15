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
package mslinks.data;

import io.ByteReader;
import io.ByteWriter;
import io.Serializer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import mslinks.Serializable;

public class BitSet32 implements Serializable {
	private int d;

	public BitSet32() {
	}
	
	public BitSet32(int n) {
		d = n;
	}

	public BitSet32(ByteReader data) throws IOException {
		this(new Serializer<>(data));
	}

	public BitSet32(Serializer<ByteReader> serializer) throws IOException {
		parse(serializer);
	}

	protected void parse(Serializer<ByteReader> serializer) throws IOException {
		d = (int)serializer.read(4, "bitset");
	}

	protected String toLog() {
		StringBuilder builder = new StringBuilder();
		for(var method : getClass().getDeclaredMethods()) {
			if (method.getReturnType() == boolean.class && method.getParameterCount() == 0) {
				try {
					if ((boolean)method.invoke(this)) {
						if (builder.length() != 0) {
							builder.append(" | ");
						}
						builder.append(method.getName());
					}
				}
				catch (IllegalAccessException | InvocationTargetException e) {

				}
			}
		}

		return builder.toString();
	}
	
	protected boolean get(int i) {
		return (d & (1 << i)) != 0;
	}
	
	protected void set(int i) {
		d = (d & ~(1 << i)) | (1 << i);
	}
	
	protected void clear(int i) {
		d = d & ~(1 << i);
	}

	public void serialize(ByteWriter bw) throws IOException {
		serialize(new Serializer<>(bw));
	}

	public void serialize(Serializer<ByteWriter> serializer) throws IOException {
		serializer.write(d, 4, "bitset");
	}
}
