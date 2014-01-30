package mslinks.data;

import io.ByteReader;

import java.io.IOException;

public class LinkFlags extends BitSet32 {
	
	public LinkFlags(int n) {
		super(n);
		reset();
	}
	
	public LinkFlags(ByteReader data) throws IOException {
		super(data);
		reset();
	}
	
	private void reset() {
		clear(11);
		clear(16);
		for (int i=27; i<32; i++)
			clear(i);
	}
	
	public boolean hasLinkTargetIDList() 			{ return get(0); }	
	public boolean hasLinkInfo() 					{ return get(1); }	
	public boolean hasName() 						{ return get(2); }	
	public boolean hasRelativePath() 				{ return get(3); }	
	public boolean hasWorkingDir() 					{ return get(4); }	
	public boolean hasArguments() 					{ return get(5); }	
	public boolean hasIconLocation() 				{ return get(6); }	
	public boolean isUnicode() 						{ return get(7); }	
	public boolean forceNoLinkInfo() 				{ return get(8); }	
	public boolean hasExpString() 					{ return get(9); }	
	public boolean runInSeparateProcess() 			{ return get(10); }	
	public boolean hasDarwinID() 					{ return get(12); }	
	public boolean runAsUser() 						{ return get(13); }	
	public boolean hasExpIcon() 					{ return get(14); }	
	public boolean noPidlAlias() 					{ return get(15); }	
	public boolean runWithShimLayer() 				{ return get(17); }	
	public boolean forceNoLinkTrack() 				{ return get(18); }	
	public boolean enableTargetMetadata() 			{ return get(19); }	
	public boolean disableLinkPathTracking() 		{ return get(20); }	
	public boolean disableKnownFolderTracking() 	{ return get(21); }	
	public boolean disableKnownFolderAlias() 		{ return get(22); }	
	public boolean allowLinkToLink() 				{ return get(23); }	
	public boolean unaliasOnSave() 					{ return get(24); }	
	public boolean preferEnvironmentPath() 			{ return get(25); }	
	public boolean keepLocalIDListForUNCTarget() 	{ return get(26); }
	
	public void setHasLinkTargetIDList() 			{ set(0); }	
	public void setHasLinkInfo() 					{ set(1); }	
	public void setHasName() 						{ set(2); }	
	public void setHasRelativePath() 				{ set(3); }	
	public void setHasWorkingDir() 					{ set(4); }	
	public void setHasArguments() 					{ set(5); }	
	public void setHasIconLocation() 				{ set(6); }	
	public void setIsUnicode() 						{ set(7); }	
	public void setForceNoLinkInfo() 				{ set(8); }	
	public void setHasExpString() 					{ set(9); }	
	public void setRunInSeparateProcess() 			{ set(10); }	
	public void setHasDarwinID() 					{ set(12); }	
	public void setRunAsUser() 						{ set(13); }	
	public void setHasExpIcon() 					{ set(14); }	
	public void setNoPidlAlias() 					{ set(15); }	
	public void setRunWithShimLayer() 				{ set(17); }	
	public void setForceNoLinkTrack() 				{ set(18); }	
	public void setEnableTargetMetadata() 			{ set(19); }	
	public void setDisableLinkPathTracking() 		{ set(20); }	
	public void setDisableKnownFolderTracking() 	{ set(21); }	
	public void setDisableKnownFolderAlias() 		{ set(22); }	
	public void setAllowLinkToLink() 				{ set(23); }	
	public void setUnaliasOnSave() 					{ set(24); }	
	public void setPreferEnvironmentPath() 			{ set(25); }	
	public void setKeepLocalIDListForUNCTarget() 	{ set(26); }
	
	public void clearHasLinkTargetIDList() 			{ clear(0); }	
	public void clearHasLinkInfo() 					{ clear(1); }	
	public void clearHasName() 						{ clear(2); }	
	public void clearHasRelativePath() 				{ clear(3); }	
	public void clearHasWorkingDir() 				{ clear(4); }	
	public void clearHasArguments() 				{ clear(5); }	
	public void clearHasIconLocation() 				{ clear(6); }	
	public void clearIsUnicode() 					{ clear(7); }	
	public void clearForceNoLinkInfo() 				{ clear(8); }	
	public void clearHasExpString() 				{ clear(9); }	
	public void clearRunInSeparateProcess() 		{ clear(10); }	
	public void clearHasDarwinID() 					{ clear(12); }	
	public void clearRunAsUser() 					{ clear(13); }	
	public void clearHasExpIcon() 					{ clear(14); }	
	public void clearNoPidlAlias() 					{ clear(15); }	
	public void clearRunWithShimLayer() 			{ clear(17); }	
	public void clearForceNoLinkTrack() 			{ clear(18); }	
	public void clearEnableTargetMetadata() 		{ clear(19); }	
	public void clearDisableLinkPathTracking() 		{ clear(20); }	
	public void clearDisableKnownFolderTracking() 	{ clear(21); }	
	public void clearDisableKnownFolderAlias() 		{ clear(22); }	
	public void clearAllowLinkToLink() 				{ clear(23); }	
	public void clearUnaliasOnSave() 				{ clear(24); }	
	public void clearPreferEnvironmentPath() 		{ clear(25); }	
	public void clearKeepLocalIDListForUNCTarget() 	{ clear(26); }
}
