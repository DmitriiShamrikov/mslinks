package io;

import java.io.IOException;
import java.io.InputStream;

public class ByteReader extends InputStream {
	
	private InputStream stream;
	private Endianness end = Endianness.LITTLE_ENDIAN;
	private int pos = 0;
	
	
	public ByteReader(InputStream in) {
		stream = in;
	}
	
	public ByteReader setBigEndian() {
		end = Endianness.BIG_ENDIAN;
		return this;
	}
	
	public ByteReader setLittleEndian() {
		end = Endianness.LITTLE_ENDIAN;
		return this;
	}
	
	public ByteReader changeEndiannes() {
		if (isLitteEndian())
			setBigEndian();
		else 
			setLittleEndian();
		return this;
	}
	
	public boolean isBigEndian() {
		return end == Endianness.BIG_ENDIAN;
	}
	
	public boolean isLitteEndian() {
		return end == Endianness.LITTLE_ENDIAN;
	}
	
	public int getPosition() {
		return pos;
	}
	
	@Override
	public int read() throws IOException {
		pos++;
		return stream.read();
	}
	
	public long read2bytes() throws IOException {
		long b0 = read();
		long b1 = read();
		if (isLitteEndian())
			return b0 | (b1 << 8);
		else 
			return b1 | (b0 << 8);
	}
	
	public long read3bytes() throws IOException {
		long b0 = read();
		long b1 = read();
		long b2 = read();
		if (isLitteEndian())
			return b0 | (b1 << 8) | (b2 << 16);
		else 
			return b2 | (b1 << 8) | (b0 << 16);
	}
	
	public long read4bytes() throws IOException {
		long b0 = read();
		long b1 = read();
		long b2 = read();
		long b3 = read();
		if (isLitteEndian())
			return b0 | (b1 << 8) | (b2 << 16) | (b3 << 24);
		else 
			return b3 | (b2 << 8) | (b1 << 16) | (b0 << 24);
	}
	
	public long read5bytes() throws IOException {
		long b0 = read();
		long b1 = read();
		long b2 = read();
		long b3 = read();
		long b4 = read();
		if (isLitteEndian())
			return b0 | (b1 << 8) | (b2 << 16) | (b3 << 24) | (b4 << 32);
		else 
			return b4 | (b3 << 8) | (b2 << 16) | (b1 << 24) | (b0 << 32);
	}
	
	public long read6bytes() throws IOException {
		long b0 = read();
		long b1 = read();
		long b2 = read();
		long b3 = read();
		long b4 = read();
		long b5 = read();
		if (isLitteEndian())
			return b0 | (b1 << 8) | (b2 << 16) | (b3 << 24) | (b4 << 32) | (b5 << 40);
		else 
			return b5 | (b4 << 8) | (b3 << 16) | (b2 << 24) | (b1 << 32) | (b0 << 40);
	}
	
	public long read7bytes() throws IOException {
		long b0 = read();
		long b1 = read();
		long b2 = read();
		long b3 = read();
		long b4 = read();
		long b5 = read();
		long b6 = read();
		if (isLitteEndian())
			return b0 | (b1 << 8) | (b2 << 16) | (b3 << 24) | (b4 << 32) | (b5 << 40) | (b6 << 48);
		else 
			return b6 | (b5 << 8) | (b4 << 16) | (b3 << 24) | (b2 << 32) | (b1 << 40) | (b0 << 48);
	}
	
	public long read8bytes() throws IOException {
		long b0 = read();
		long b1 = read();
		long b2 = read();
		long b3 = read();
		long b4 = read();
		long b5 = read();
		long b6 = read();
		long b7 = read();
		if (isLitteEndian())
			return b0 | (b1 << 8) | (b2 << 16) | (b3 << 24) | (b4 << 32) | (b5 << 40) | (b6 << 48) | (b7 << 56);
		else 
			return b7 | (b6 << 8) | (b5 << 16) | (b4 << 24) | (b3 << 32) | (b2 << 40) | (b1 << 48) | (b0 << 56);
	}
}

enum Endianness {
	BIG_ENDIAN, LITTLE_ENDIAN
}