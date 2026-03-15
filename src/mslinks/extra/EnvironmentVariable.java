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
import mslinks.ShellLinkException;

public class EnvironmentVariable implements Serializable {

	public static final int signature = 0xA0000001;
	public static final int size = 0x314;
	
	private String variable;
	
	public EnvironmentVariable() {
		variable = "";
	}

	public EnvironmentVariable(ByteReader br, int sz) throws ShellLinkException, IOException {
		this(new Serializer<ByteReader>(br), sz);
	}
	
	public EnvironmentVariable(Serializer<ByteReader> serializer, int sz) throws ShellLinkException, IOException {
		if (sz != size)
			throw new ShellLinkException();
		
		int pos = serializer.getPosition();
		variable = serializer.readString(260, "variable name");
		serializer.seekTo(pos + 260);
		
		pos = serializer.getPosition();
		String unicodeStr = serializer.readUnicodeStringNullTerm(260, "variable name (unicode)");
		serializer.seekTo(pos + 520);
		if (unicodeStr != null && !unicodeStr.equals(""))
			variable = unicodeStr;
	}
	
	@Override
	public void serialize(Serializer<ByteWriter> serializer) throws IOException {
		serializer.write(size, 4, Serializer.BLOCK_SIZE_NAME);
		serializer.write(signature, 4, "signature");
		serializer.writeStringFixedSize(variable, 260, "variable name");	
		serializer.writeUnicodeStringFixedSize(variable, 520, "variable name");
	}
	
	public String getVariable() { return variable; }
	public EnvironmentVariable setVariable(String s) { variable = s; return this; }

}
