package mslinks.data;

import io.ByteReader;

import java.io.IOException;

public class ConsoleFlags extends BitSet32 {

	public ConsoleFlags(int n) {
		super(n);
	}
	
	public ConsoleFlags(ByteReader data) throws IOException {
		super(data);
	}
	
	public boolean isBoldFont() { return get(0); }
	public boolean isFullscreen() { return get(1); }
	public boolean isQuickEdit() { return get(2); }
	public boolean isInsertMode() { return get(3); }
	public boolean isAutoPosition() { return get(4); }
	public boolean isHistoryDup() { return get(5); }
	
	public void setBoldFont() { set(0); }
	public void setFullscreen() { set(1); }
	public void setQuickEdit() { set(2); }
	public void setInsertMode() { set(3); }
	public void setAutoPosition() { set(4); }
	public void setHistoryDup() { set(5); }
	
	public void clearBoldFont() { clear(0); }
	public void clearFullscreen() { clear(1); }
	public void clearQuickEdit() { clear(2); }
	public void clearInsertMode() { clear(3); }
	public void clearAutoPosition() { clear(4); }
	public void clearHistoryDup() { clear(5); }

}
