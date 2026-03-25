/*
	https://github.com/BlackOverlord666/mslinks
	
	Copyright (c) 2026 Dmitrii Shamrikov

	Licensed under the WTFPL
	You may obtain a copy of the License at
 
	http://www.wtfpl.net/about/
 
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/
package mslinks.extra;

import java.io.IOException;

import io.ByteReader;
import io.ByteWriter;
import io.Serializer;
import mslinks.Serializable;
import mslinks.ShellLinkException;

public abstract class StringDataBlock implements Serializable
{
	public static final int size = 0x314;
	protected String m_Value = "";

	public StringDataBlock()
	{
	}

	public StringDataBlock(Serializer<ByteReader> serializer, int sz, String name) throws ShellLinkException, IOException
	{
		if (sz != size)
			throw new ShellLinkException();
		
		int pos = serializer.getPosition();
		m_Value = serializer.readString(260, name);
		serializer.seekTo(pos + 260);
		
		pos = serializer.getPosition();
		String unicodeStr = serializer.readUnicodeStringNullTerm(260, name + " (unicode)");
		serializer.seekTo(pos + 520);
		if (unicodeStr != null && !unicodeStr.equals(""))
			m_Value = unicodeStr;
	}
	
	
	protected void serialize(Serializer<ByteWriter> serializer, int signature, String name) throws IOException
	{
		serializer.write(size, 4, Serializer.BLOCK_SIZE_NAME);
		serializer.write(signature, 4, "signature", v -> getClass().getName());
		serializer.writeStringFixedSize(m_Value, 260, name);	
		serializer.writeUnicodeStringFixedSize(m_Value, 520, name + " (unicode)");
	}
}
