package mslinks.extra;

import io.ByteReader;
import io.ByteWriter;

import java.io.IOException;

import mslinks.Serializable;

public class Stub implements Serializable {
	
	private int sign;
	private byte[] data;

	public Stub(ByteReader br, int sz, int sgn) throws IOException {
		int len = sz - 8;
		sign = sgn;
		data = new byte[len];
		for (int i=0; i<len; i++)
			data[i] = (byte)br.read();
	}
	
	@Override
	public void serialize(ByteWriter bw) throws IOException {
		bw.write4bytes(data.length + 8);
		bw.write4bytes(sign);
		bw.writeBytes(data);
	}

}
