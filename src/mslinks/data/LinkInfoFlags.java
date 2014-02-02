package mslinks.data;

import io.ByteReader;

import java.io.IOException;

public class LinkInfoFlags extends BitSet32 {
	
	public LinkInfoFlags(int n) {
		super(n);
		reset();
	}
	
	public LinkInfoFlags(ByteReader data) throws IOException {
		super(data);
		reset();
	}
	
	private void reset() {
		for (int i=2; i<32; i++)
			clear(i);
	}
	
	public boolean hasVolumeIDAndLocalBasePath() 				{ return get(0); }
	public boolean hasCommonNetworkRelativeLinkAndPathSuffix()	{ return get(1); }
	
	public LinkInfoFlags setVolumeIDAndLocalBasePath() 					{ set(0); return this; }	
	public LinkInfoFlags setCommonNetworkRelativeLinkAndPathSuffix()	{ set(1); return this; }
	
	public LinkInfoFlags clearVolumeIDAndLocalBasePath() 				{ clear(0); return this; }	
	public LinkInfoFlags clearCommonNetworkRelativeLinkAndPathSuffix()	{ clear(1); return this; }
}
