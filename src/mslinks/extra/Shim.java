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

public class Shim implements Serializable
{
	public static final int signature = 0xA0000008;
	public static final int minsize = 0x88;

	private String m_LayerName;

	public Shim()
	{
	}

	public Shim(ByteReader br, int sz) throws ShellLinkException, IOException
	{
		this(new Serializer<ByteReader>(br), sz);
	}
	
	public Shim(Serializer<ByteReader> serializer, int sz) throws ShellLinkException, IOException
	{
		if (sz < minsize)
		{
			throw new ShellLinkException();
		}
		
		int stringSize = sz - 4 - 4; // minus signature and block size
		m_LayerName = serializer.readUnicodeStringNullTerm(stringSize / 2, "layer name");
	}

	@Override
	public void serialize(Serializer<ByteWriter> serializer) throws IOException
	{
		String layerName = m_LayerName != null ? m_LayerName : "";
		int size = Math.max(4 + 4 + layerName.length() * 2, minsize);

		serializer.write(size, 4, Serializer.BLOCK_SIZE_NAME);
		serializer.write(signature, 4, "signature", v -> getClass().getName());
		serializer.writeUnicodeStringFixedSize(layerName, size, "layer name");
	}

	public String getLayerName()
	{
		return m_LayerName;
	}
	public Shim setLayerName(String layerName)
	{
		m_LayerName = layerName;
		return this;
	}
}
