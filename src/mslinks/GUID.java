package mslinks;

import java.io.IOException;

public class GUID {
	private int d1;
	private short d2, d3, d4;
	private long d5;
	
	public GUID(byte[] d) {
		d1 = ByteReader.makeIntL(d[0], d[1], d[2], d[3]);
		d2 = ByteReader.makeShortL(d[4], d[5]);
		d3 = ByteReader.makeShortL(d[6], d[7]);
		d4 = ByteReader.makeShortB(d[8], d[9]);
		d5 = ByteReader.makeLongB((byte)0, (byte)0, d[10], d[11], d[12], d[13], d[14], d[15]);
	}
	
	public GUID(ByteReader data) throws IOException {
		d1 = (int)data.read4bytes();
		d2 = (short)data.read2bytes();
		d3 = (short)data.read2bytes();
		data.changeEndiannes();
		d4 = (short)data.read2bytes();
		d5 = data.read6bytes();
		data.changeEndiannes();
	}
	
	public String toString() {
		return String.format("%08x-%04x-%04x-%04x-%012x", d1, d2, d3, d4, d5);
	}
	
	public boolean equals(Object o) {
		GUID g = (GUID)o;
		return d1 == g.d1 && d2 == g.d2 && d3 == g.d3 && d4 == g.d4 && d5 == g.d5;
	}

	public void serialize(ByteWriter bw) throws IOException {
		bw.write4bytes(d1);
		bw.write2bytes(d2);
		bw.write2bytes(d3);
		bw.changeEndiannes();
		bw.write2bytes(d4);
		bw.write6bytes(d5);
		bw.changeEndiannes();		
	}
}
