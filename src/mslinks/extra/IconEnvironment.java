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
import mslinks.ShellLinkException;

public class IconEnvironment extends StringDataBlock
{
	public static final int signature = 0xA0000007;
	
	public IconEnvironment()
	{
	}

	public IconEnvironment(Serializer<ByteReader> serializer, int sz) throws ShellLinkException, IOException
	{
		super(serializer, sz, "icon path");
	}

	@Override
	public void serialize(Serializer<ByteWriter> serializer) throws IOException
	{
		super.serialize(serializer, signature, "icon path");
	}
	
	public String getIconPath() { return m_Value; }
	public IconEnvironment setIconPath(String s) { m_Value = s; return this; }
}
