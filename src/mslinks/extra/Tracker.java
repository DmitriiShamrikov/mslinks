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
import mslinks.data.GUID;

public class Tracker implements Serializable {
	
	public static final int signature = 0xA0000003;
	public static final int size = 0x60;
	
	private String netbios;
	private GUID d1;
	private GUID d2;
	private GUID db1;
	private GUID db2;
	
	public Tracker() {
		netbios = "localhost";
		d1 = db1 = new GUID();
		d2 = db2 = new GUID("539D9DC6-8293-11E3-8FB0-005056C00008");
	}

	public Tracker(ByteReader br, int sz) throws ShellLinkException, IOException {
		this(new Serializer<ByteReader>(br), sz);
	}
	
	public Tracker(Serializer<ByteReader> serializer, int sz) throws ShellLinkException, IOException {
		if (sz != size)
			throw new ShellLinkException();
		int len = (int)serializer.read(4, "length");
		if (len != 0x58)
			throw new ShellLinkException();
		serializer.read(4, "version");
		int pos = serializer.getPosition();
		netbios = serializer.readString(16, "netbios name");
		serializer.seek(pos + 16 - serializer.getPosition());
		d1 = new GUID(serializer);
		d2 = new GUID(serializer);
		db1 = new GUID(serializer);
		db2 = new GUID(serializer);
	}

	@Override
	public void serialize(Serializer<ByteWriter> serializer) throws IOException {
		serializer.write(size, 4, Serializer.BLOCK_SIZE_NAME);
		serializer.write(signature, 4, "signature");
		serializer.write(0x58, 4, "length");
		serializer.write(0, 4, "version");
		serializer.writeStringFixedSize(netbios, 16, "netbios");
		d1.serialize(serializer);
		d2.serialize(serializer);
		db1.serialize(serializer);
		db2.serialize(serializer);
	}
	
	public String getNetbiosName() { return netbios; }
	public Tracker setNetbiosName(String s) throws ShellLinkException {
		if (s.length() > 16)
			throw new ShellLinkException("netbios name length must be <= 16");
		netbios = s;
		return this;
	}
}
