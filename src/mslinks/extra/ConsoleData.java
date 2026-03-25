/*
	https://github.com/BlackOverlord666/mslinks
	
	Copyright (c) 2015 Dmitrii Shamrikov

	Licensed under the WTFPL
	You may obtain a copy of the License at
 
	http://www.wtfpl.net/about/
 
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/
package mslinks.extra;

import io.ByteReader;
import io.ByteWriter;
import io.Serializer;

import java.io.IOException;

import mslinks.Serializable;
import mslinks.ShellLinkException;
import mslinks.data.ConsoleFlags;
import mslinks.data.Size;

public class ConsoleData implements Serializable {
	
	public static final int signature = 0xA0000002;
	public static final int size = 0xcc;
	
	public static int rgb(int r, int g, int b) {
		return (r & 0xff) | ((g & 0xff) << 8) | ((b & 0xff) << 16);
	}	
	public static int r(int rgb) { return rgb & 0xff; }
	public static int g(int rgb) { return (rgb & 0xff00) >> 8; }
	public static int b(int rgb) { return (rgb & 0xff0000) >> 16; }
	
	private ConsoleFlags flags = new ConsoleFlags(0);
	private int textFG;
	private int textBG;
	private int popupFG;
	private int popupBG;
	private Size buffer;
	private Size window;
	private Size windowpos;
	private int fontsize;
	private Font font;
	private CursorSize cursize;
	private int historysize;
	private int historybuffers;
	private int[] colors = new int[16];
	
	public ConsoleData() {
		textFG = 7;
		textBG = 0;
		popupFG = 5;
		popupBG = 15;
		buffer = new Size(80, 300);
		window = new Size(80, 25);
		windowpos = new Size();
		fontsize = 14;
		font = Font.Terminal;
		cursize = CursorSize.Small;
		historysize = 50;
		historybuffers = 4;
		flags.setInsertMode();
		flags.setAutoPosition();
		
		int i = 0;
		colors[i++] = rgb(0,   0,   0);
		colors[i++] = rgb(0,   0,   128);
		colors[i++] = rgb(0,   128, 0);
		colors[i++] = rgb(0,   128, 128);
		colors[i++] = rgb(128, 0,   0);
		colors[i++] = rgb(128, 0,   128);
		colors[i++] = rgb(128, 128,   0);
		colors[i++] = rgb(192, 192, 192);
		colors[i++] = rgb(128, 128, 128);
		colors[i++] = rgb(0,   0,   255);
		colors[i++] = rgb(0,   255, 0);
		colors[i++] = rgb(0,   255, 255);
		colors[i++] = rgb(255, 0,   0);
		colors[i++] = rgb(255, 0,   255);
		colors[i++] = rgb(255, 255,   0);
		colors[i++] = rgb(255, 255, 255);
	}
	
	public ConsoleData(ByteReader br, int sz) throws ShellLinkException, IOException {
		this(new Serializer<ByteReader>(br), sz);
	}
	
	public ConsoleData(Serializer<ByteReader> serializer, int sz) throws ShellLinkException, IOException {
		if (sz != size) throw new ShellLinkException();
		int t = (int)serializer.read(2, "text color", ConsoleData::colorIndexToLog);
		textFG = t & 0xf;
		textBG = (t & 0xf0) >> 4;
		t = (int)serializer.read(2, "popup color", ConsoleData::colorIndexToLog);
		popupFG = t & 0xf;
		popupBG = (t & 0xf0) >> 4;
		buffer = new Size(serializer, "buffer");
		window = new Size(serializer, "window size");
		windowpos = new Size(serializer, "window pos");
		serializer.read(8, "unused space");
		

		fontsize = ((int)serializer.read(4, "font height and width", ConsoleData::fontSizeToLog)) >>> 16;
		serializer.read(4, "font family");
		if ((int)serializer.read(4, "font weight") >= 700) 
			flags.setBoldFont();

		int pos = serializer.getPosition();
		String fontName = serializer.readUnicodeStringNullTerm(32, "font name");
		switch (fontName.charAt(0)) {
			case 'T': font = Font.Terminal; break;
			case 'L': font = Font.LucidaConsole; break;
			case 'C': font = Font.Consolas; break;
			default: throw new ShellLinkException("unknown font type");
		}
		serializer.seek(64 - serializer.getPosition() + pos);
		
		t = (int)serializer.read(4, "cursor size", ConsoleData::cursorSizeToLog);
		if (t <= 25) cursize = CursorSize.Small;
		else if (t <= 50) cursize = CursorSize.Medium;
		else cursize = CursorSize.Large;
		
		if ((int)serializer.read(4, "is fullscreen") != 0)
			flags.setFullscreen();
		if ((int)serializer.read(4, "is quckedit") != 0)
			flags.setQuickEdit();
		if ((int)serializer.read(4, "is insert mode") != 0)
			flags.setInsertMode();
		if ((int)serializer.read(4, "is auto-position enabled") != 0)
			flags.setAutoPosition();
		historysize = (int)serializer.read(4, "historysize");
		historybuffers = (int)serializer.read(4, "number of historybuffers");
		if ((int)serializer.read(4, "allow duplicates in history") != 0)
			flags.setHistoryDup();
		for (int i=0; i<16; i++)
			colors[i] = (int)serializer.read(4, String.format("color table color[%d]", i), ConsoleData::colorToLog);
	}

	@Override
	public void serialize(Serializer<ByteWriter> serializer) throws IOException {
		serializer.write(size, 4, Serializer.BLOCK_SIZE_NAME);
		serializer.write(signature, 4, "signature", v -> getClass().getName());
		serializer.write(textFG | (textBG << 4), 2, "text color", ConsoleData::colorIndexToLog);
		serializer.write(popupFG | (popupBG << 4), 2, "popup color", ConsoleData::colorIndexToLog);
		buffer.serialize(serializer, "buffer");
		window.serialize(serializer, "window");
		windowpos.serialize(serializer, "window pos");
		serializer.write(0, 8, "unused space");
		serializer.write(fontsize << 16, 4, "font height and width", ConsoleData::fontSizeToLog);
		serializer.write(font == Font.Terminal? 0x30 : 0x36, 4, "font family");
		serializer.write(flags.isBoldFont() ? 700 : 0, 4, "font weight");
		
		String fn = "";
		switch (font) {
			case Terminal: fn = "Terminal"; break;
			case LucidaConsole: fn = "Lucida Console"; break;
			case Consolas: fn = "Consolas"; break;
		}
		serializer.writeUnicodeStringFixedSize(fn, 64, "font name");
		
		int curSizeValue = 0;
		switch (cursize) {
			case Small: curSizeValue = 0; break;
			case Medium: curSizeValue = 26; break;
			case Large: curSizeValue = 51; break;
		}
		serializer.write(curSizeValue, 4, "cursor size", ConsoleData::cursorSizeToLog);
		serializer.write(flags.isFullscreen()? 1 : 0, 4, "is fullscreen");
		serializer.write(flags.isQuickEdit()? 1 : 0, 4, "is quckedit");
		serializer.write(flags.isInsertMode()? 1 : 0, 4, "is insert mode");
		serializer.write(flags.isAutoPosition()? 1 : 0, 4, "is auto-position enabled");
		serializer.write(historysize, 4, "historysize");
		serializer.write(historybuffers, 4, "number of historybuffers");
		serializer.write(flags.isHistoryDup()? 1 : 0, 4, "allow duplicates in history");
		for (int i=0; i<16; i++)
			serializer.write(colors[i], 4, String.format("color table color[%d]", i), ConsoleData::colorToLog);
	}

	private static String colorIndexToLog(long value) {
		return String.format("color idx: %d, background idx: %d", value & 0xf, ((value & 0xf0) >> 8));
	}

	private static String fontSizeToLog(long value) {
		return String.format("height: %d, width: %d", value >> 16, value & 0xffff);
	}

	private static String cursorSizeToLog(long value) {
		String name;
		if (value <= 25) {
			name = "Small";
		}
		else if (value <= 50) {
			name = "Medium";
		}
		else {
			name = "Large";
		}
		return String.format("%d (%s)", value, name);
	}

	private static String colorToLog(long value) {
		int color = (int)value;
		return String.format("#%02X%02X%02X", r(color), g(color), b(color));
	}
	
	public int[] getColorTable() { return colors; }
	
	/** get index in array returned by getColorTable() method */
	public int getTextColor() { return textFG; }
	/** set index in array returned by getColorTable() method */
	public ConsoleData setTextColor(int n) { textFG = n; return this; }	
	/** get index in array returned by getColorTable() method */
	public int getTextBackground() { return textBG; }
	/** set index in array returned by getColorTable() method */
	public ConsoleData setTextBackground(int n) { textBG = n; return this; }
	
	/** get index in array returned by getColorTable() method */
	public int getPopupTextColor() { return popupFG; }
	/** set index in array returned by getColorTable() method */
	public ConsoleData setPopupTextColor(int n) { popupFG = n; return this; }
	/** get index in array returned by getColorTable() method */
	public int getPopupTextBackground() { return popupBG; }
	/** set index in array returned by getColorTable() method */
	public ConsoleData setPopupTextBackground(int n) { popupBG = n; return this; }
	
	public Size getBufferSize() { return buffer; }
	public Size getWindowSize() { return window; }
	public Size getWindowPos() { return windowpos; }
	
	public ConsoleFlags getConsoleFlags() { return flags; }
	
	public int getFontSize() { return fontsize; }
	public ConsoleData setFontSize(int n) { fontsize = n; return this; } 
	
	public Font getFont() { return font; }
	public ConsoleData setFont(Font f) { font = f; return this; }
	
	public CursorSize getCursorSize() { return cursize; }
	public ConsoleData setCursorSize(CursorSize cs) { cursize = cs; return this; }
	
	public int getHistorySize() { return historysize; }
	public ConsoleData setHistorySize(int n) { historysize = n; return this; }
	
	public int getHistoryBuffers() { return historybuffers; }
	public ConsoleData setHistoryBuffers(int n) { historybuffers = n; return this; }
	
	/**
	 * only this fonts are working...
	 */
	public enum Font {
		Terminal, LucidaConsole, Consolas
	}
	
	public enum CursorSize {
		Small, Medium, Large
	}
}
