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

import mslinks.Serializable;
import mslinks.ShellLinkException;

public class Size implements Serializable{
	private int x, y;
	
	public Size() {
		x = y = 0;
	}
	
	public Size(int _x, int _y) {
		x = _x;
		y = _y;
	}

	public Size(ByteReader br) throws ShellLinkException, IOException {
		this(new Serializer<ByteReader>(br), "");
	}
	
	public Size(Serializer<ByteReader> serializer, String name) throws ShellLinkException, IOException {
		try (var block = serializer.beginBlock(name)) {
			x = (int)serializer.read(2, "X");
			y = (int)serializer.read(2, "Y");
		}
	}

	public int getX() {
		return x;
	}

	public Size setX(int x) {
		this.x = x;
		return this;
	}

	public int getY() {
		return y;
	}

	public Size setY(int y) {
		this.y = y;
		return this;
	}

	@Override
	public void serialize(Serializer<ByteWriter> serializer) throws IOException {
		serialize(serializer, "Size");
	}

	public void serialize(Serializer<ByteWriter> serializer, String name) throws IOException {
		try (var block = serializer.beginBlock(name)) { 
			serializer.write(x, 2, "X");
			serializer.write(y, 2, "Y");
		}
	}	
}
