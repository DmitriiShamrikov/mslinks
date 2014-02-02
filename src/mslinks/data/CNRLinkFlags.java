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
	
	public CNRLinkFlags setValidDevice() 		{ set(0); return this; }	
	public CNRLinkFlags setValidNetType()		{ set(1); return this; }
	
	public CNRLinkFlags clearValidDevice() 		{ clear(0); return this; }	
	public CNRLinkFlags clearValidNetType()		{ clear(1); return this; }

}
