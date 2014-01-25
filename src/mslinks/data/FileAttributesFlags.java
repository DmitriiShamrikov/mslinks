package mslinks.data;

import io.ByteReader;

import java.io.IOException;

public class FileAttributesFlags extends BitSet32 {
	public FileAttributesFlags(int n) {
		super(n);
	}
	
	public FileAttributesFlags(ByteReader data) throws IOException {
		super(data);
	}
	
	public boolean isReadonly() 			{ return get(0); }
	public boolean isHidden() 				{ return get(1); }
	public boolean isSystem() 				{ return get(2); }
	public boolean isDirecory() 			{ return get(4); }
	public boolean isArchive() 				{ return get(5); }
	public boolean isNormal() 				{ return get(7); }
	public boolean isTemporary() 			{ return get(8); }
	public boolean isSparseFile() 			{ return get(9); }
	public boolean isReparsePoint() 		{ return get(10); }
	public boolean isCompressed() 			{ return get(11); }
	public boolean isOffline() 				{ return get(12); }
	public boolean isNotContentIndexed() 	{ return get(13); }
	public boolean isEncypted() 			{ return get(14); }
	
	public void setReadonly() 			{ set(0); }
	public void setHidden() 			{ set(1); }
	public void setSystem() 			{ set(2); }
	public void setDirecory() 			{ set(4); }
	public void setArchive() 			{ set(5); }
	public void setNormal() 			{ set(7); }
	public void setTemporary() 			{ set(8); }
	public void setSparseFile() 		{ set(9); }
	public void setReparsePoint() 		{ set(10); }
	public void setCompressed() 		{ set(11); }
	public void setOffline() 			{ set(12); }
	public void setNotContentIndexed() 	{ set(13); }
	public void setEncypted() 			{ set(14); }
	
	public void clearReadonly() 			{ clear(0); }
	public void clearHidden() 				{ clear(1); }
	public void clearSystem() 				{ clear(2); }
	public void clearDirecory() 			{ clear(4); }
	public void clearArchive() 				{ clear(5); }
	public void clearNormal() 				{ clear(7); }
	public void clearTemporary() 			{ clear(8); }
	public void clearSparseFile() 			{ clear(9); }
	public void clearReparsePoint() 		{ clear(10); }
	public void clearCompressed() 			{ clear(11); }
	public void clearOffline() 				{ clear(12); }
	public void clearNotContentIndexed() 	{ clear(13); }
	public void clearEncypted() 			{ clear(14); }
	
}
