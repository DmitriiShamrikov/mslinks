package mslinks.data;

import io.ByteReader;
import io.ByteWriter;

import java.io.IOException;

import mslinks.Serializable;

public class Decimal implements Serializable {
	
	public byte scale;
	public boolean negative;
	public int hi32;
	public long low64;
	
	public Decimal(ByteReader br) throws IOException {
		br.read2bytes();
		scale = (byte)br.read();
		negative = (byte)br.read() == 0x80;
		hi32 = (int)br.read4bytes();
		low64 = br.read8bytes();
	}
	
	public Decimal(byte sc, boolean n, int hi, long lo) {
		scale = sc; negative = n; hi32 = hi; low64 = lo;
	}

	@Override
	public void serialize(ByteWriter bw) throws IOException {
		bw.write2bytes(0);
		bw.write(scale);
		bw.write(negative ? 0x80 : 0);
		bw.write4bytes(hi32);
		bw.write8bytes(low64);
	}

}
