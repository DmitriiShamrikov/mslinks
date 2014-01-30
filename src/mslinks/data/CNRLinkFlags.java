package mslinks.data;

import io.ByteReader;

import java.io.IOException;

public class CNRLinkFlags extends BitSet32 {
	
	public CNRLinkFlags(int n) {
		super(n);
		reset();
	}

	public CNRLinkFlags(ByteReader data) throws IOException {
		super(data);
		reset();
	}
	
	private void reset() {
		for (int i=2; i<32; i++)
			clear(i);
	}
	
	public boolean isValidDevice() 		{ return get(0); }
	public boolean isValidNetType()		{ return get(1); }
	
	public void setValidDevice() 		{ set(0); }	
	public void setValidNetType()		{ set(1); }
	
	public void clearValidDevice() 		{ clear(0); }	
	public void clearValidNetType()		{ clear(1); }

}
