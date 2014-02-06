package mslinks.data;

import java.io.IOException;

import mslinks.Serializable;
import io.ByteReader;
import io.ByteWriter;

public class Currency implements Serializable {
	long d; 
	
	public Currency(int n) {
		d = n;
	}
	
	public Currency(ByteReader br) throws IOException {
		d = br.read8bytes();
	}
	
	public long getLong() { return d; }
	public double getDouble() { return d / 10000.0; }
	
	public String toString() {
		long i = d / 10000;
		long f = d - i;
		return String.format("%d.%d", i, f);
	}

	@Override
	public void serialize(ByteWriter bw) throws IOException {
		bw.write8bytes(d);		
	}
}
