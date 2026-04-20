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
import mslinks.ShellLinkException;

import java.io.IOException;

public class EnvironmentVariable extends StringDataBlock
{
	public static final int signature = 0xA0000001;
	
	public EnvironmentVariable()
	{
	}

	public EnvironmentVariable(Serializer<ByteReader> serializer, int sz) throws ShellLinkException, IOException
	{
		super(serializer, sz, "variable");
	}

	@Override
	public void serialize(Serializer<ByteWriter> serializer) throws IOException
	{
		super.serialize(serializer, signature, "variable");
	}
	
	public String getVariable() { return m_Value; }
	public EnvironmentVariable setVariable(String s) throws ShellLinkException
	{
		if (s.length() > 260)
		{
			throw new ShellLinkException("Path must not be longer than 260 chars");
		}
		
		m_Value = s;
		return this;
	}
}
