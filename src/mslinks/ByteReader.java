package mslinks;

import java.io.IOException;
import java.io.InputStream;

public class ByteReader extends InputStream {
	
	private InputStream stream;
	private Endianness end = Endianness.LITTLE_ENDIAN;
	
	
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
	
	@Override
	public int read() throws IOException {
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
	
	public static short reverse(short n) {
		return (short)(((n & 0xff) << 8) | ((n & 0xff00) >> 8));
	}
	
	public static int reverse(int n) {
		return ((n & 0xff) << 24) | ((n & 0xff00) << 8) | ((n & 0xff0000) >> 8) | ((n & 0xff000000) >>> 24);
	}
	
	public static long reverse(long n) {
		return ((n & 0xff) << 56) | ((n & 0xff00) << 40) | ((n & 0xff0000) << 24) | ((n & 0xff000000) << 8) |
				((n & 0xff00000000L) >> 8) | ((n & 0xff0000000000L) >> 24) | ((n & 0xff000000000000L) >> 40) | ((n & 0xff00000000000000L) >>> 56);
	}
	
	public static short makeShortB(byte b0, byte b1) {
		return (short)((i(b0) << 8) | i(b1));
	}
	
	public static int makeIntB(byte b0, byte b1, byte b2, byte b3) {
		return (i(b0) << 24) | (i(b1) << 16) | (i(b2) << 8) | i(b3);
	}
	
	public static long makeLongB(byte b0, byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7) {
		return (l(b0) << 56) | (l(b1) << 48) | (l(b2) << 40) | (l(b3) << 32) | (l(b4) << 24) | (l(b5) << 16) | (l(b6) << 8) | l(b7);
	}
	
	public static short makeShortL(byte b0, byte b1) {
		return (short)((i(b1) << 8) | i(b0));
	}
	
	public static int makeIntL(byte b0, byte b1, byte b2, byte b3) {
		return (i(b3) << 24) | (i(b2) << 16) | (i(b1) << 8) | i(b0);
	}
	
	public static long makeLongL(byte b0, byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7) {
		return (l(b7) << 56) | (l(b6) << 48) | (l(b5) << 40) | (l(b4) << 32) | (l(b3) << 24) | (l(b2) << 16) | (l(b1) << 8) | l(b0);
	}
		
	private static int i(byte b) {
		return b & 0xff;
	}
	
	private static long l(byte b) {
		return b & 0xffL;
	}
}

enum Endianness {
	BIG_ENDIAN, LITTLE_ENDIAN
}