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
import io.Bytes;
import io.Serializer;

import java.io.IOException;
import java.util.Random;

import mslinks.Serializable;
import mslinks.UnsupportedCLSIDException;

public class GUID implements Serializable {
	private static Random r = new Random();
	
	private int d1;
	private short d2;
	private short d3;
	private byte[] d4 = new byte[8];
	
	public GUID() {
		d1 = r.nextInt();
		d2 = (short)r.nextInt();
		d3 = (short)r.nextInt();
		for (int i = 0; i < d4.length; ++i) {
			d4[i] = (byte)r.nextInt();
		}
	}
	
	public GUID(byte[] d) {
		d1 = Bytes.makeIntL(d[0], d[1], d[2], d[3]);
		d2 = Bytes.makeShortL(d[4], d[5]);
		d3 = Bytes.makeShortL(d[6], d[7]);
		for (int i = 0; i < d4.length; ++i) {
			d4[i] = d[8 + i];
		}
	}

	public GUID(ByteReader data) throws IOException {
		this(new Serializer<>(data));
	}
	
	public GUID(Serializer<ByteReader> serializer) throws IOException {
		try (var block = serializer.beginBlock("GUID", this::toLog)) {
			d1 = (int)serializer.read(4, "d1", null);
			d2 = (short)serializer.read(2, "d2", null);
			d3 = (short)serializer.read(2, "d3", null);
			serializer.read(d4, 0, d4.length, "d4");
		}
	}
	
	public GUID(String s) {
		if (s.charAt(0) == '{' && s.charAt(s.length()-1) == '}')
			s = s.substring(1, s.length() - 1);
		String[] p = s.split("-");
		
		byte[] b = parse(p[0]);
		d1 = Bytes.makeIntB(b[0], b[1], b[2], b[3]);
		b = parse(p[1]);
		d2 = Bytes.makeShortB(b[0], b[1]);
		b = parse(p[2]);
		d3 = Bytes.makeShortB(b[0], b[1]);
		d4[0] = (byte)Long.parseLong(p[3].substring(0, 2), 16);
		d4[1] = (byte)Long.parseLong(p[3].substring(2, 4), 16);
		d4[2] = (byte)Long.parseLong(p[4].substring(0, 2), 16);
		d4[3] = (byte)Long.parseLong(p[4].substring(2, 4), 16);
		d4[4] = (byte)Long.parseLong(p[4].substring(4, 6), 16);
		d4[5] = (byte)Long.parseLong(p[4].substring(6, 8), 16);
		d4[6] = (byte)Long.parseLong(p[4].substring(8, 10), 16);
		d4[7] = (byte)Long.parseLong(p[4].substring(10, 12), 16);
	}
	
	private byte[] parse(String s) {
		byte[] b = new byte[s.length() / 2];
		for (int i=0, j=0; j<s.length(); i++, j+=2)
			b[i] = (byte)Long.parseLong(s.substring(j, j+2), 16);
		return b;
	}
	
	public String toString() {
		return String.format("%08X-%04X-%04X-%04X-%012X", d1, d2, d3,
			Bytes.makeShortB(d4[0], d4[1]), Bytes.makeLongB((byte)0, (byte)0, d4[2], d4[3], d4[4], d4[5], d4[6], d4[7]));
	}

	private String toLog() {
		String str = toString();
		try  {
			return String.format("%s (%s)", str, Registry.getName(this));
		}
		catch (UnsupportedCLSIDException e) {
			return String.format("%s (unknown)", str);
		}
	}
	
	public boolean equals(Object o) {
		if (o == this)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		GUID g = (GUID)o;
		for (int i = 0; i < d4.length; ++i) {
			if (d4[i] != g.d4[i])
				return false;
		}

		return d1 == g.d1 && d2 == g.d2 && d3 == g.d3;
	}

	@Override
	public int hashCode()
	{
		return (int)(d1 ^ d2 ^ d3 ^ Bytes.makeIntL(d4[0], d4[1], d4[2], d4[3]) ^ Bytes.makeIntL(d4[4], d4[5], d4[6], d4[7]));
	}

	public void serialize(ByteWriter bw) throws IOException {
		serialize(new Serializer<>(bw));
	}

	public void serialize(Serializer<ByteWriter> serializer) throws IOException {
		try (var block = serializer.beginBlock("GUID", this::toLog)) {
			serializer.write(d1, 4, "d1");
			serializer.write(d2, 2, "d2");
			serializer.write(d3, 2, "d3");
			serializer.write(d4, "d4");
		}
	}
}
