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
package mslinks.data;

import io.ByteReader;
import io.ByteWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import mslinks.Serializable;

public class HotKeyFlags implements Serializable {
	private static HashMap<Byte, String> keys = new HashMap<>(Map.ofEntries(
		Map.entry((byte)0x30, "0"),
		Map.entry((byte)0x31, "1"),
		Map.entry((byte)0x32, "2"),
		Map.entry((byte)0x33, "3"),
		Map.entry((byte)0x34, "4"),
		Map.entry((byte)0x35, "5"),
		Map.entry((byte)0x36, "6"),
		Map.entry((byte)0x37, "7"),
		Map.entry((byte)0x38, "8"),
		Map.entry((byte)0x39, "9"),
		Map.entry((byte)0x41, "A"),
		Map.entry((byte)0x42, "B"),
		Map.entry((byte)0x43, "C"),
		Map.entry((byte)0x44, "D"),
		Map.entry((byte)0x45, "E"),
		Map.entry((byte)0x46, "F"),
		Map.entry((byte)0x47, "G"),
		Map.entry((byte)0x48, "H"),
		Map.entry((byte)0x49, "I"),
		Map.entry((byte)0x4A, "J"),
		Map.entry((byte)0x4B, "K"),
		Map.entry((byte)0x4C, "L"),
		Map.entry((byte)0x4D, "M"),
		Map.entry((byte)0x4E, "N"),
		Map.entry((byte)0x4F, "O"),
		Map.entry((byte)0x50, "P"),
		Map.entry((byte)0x51, "Q"),
		Map.entry((byte)0x52, "R"),
		Map.entry((byte)0x53, "S"),
		Map.entry((byte)0x54, "T"),
		Map.entry((byte)0x55, "U"),
		Map.entry((byte)0x56, "V"),
		Map.entry((byte)0x57, "W"),
		Map.entry((byte)0x58, "X"),
		Map.entry((byte)0x59, "Y"),
		Map.entry((byte)0x5A, "Z"),
		Map.entry((byte)0x70, "F1"),
		Map.entry((byte)0x71, "F2"),
		Map.entry((byte)0x72, "F3"),
		Map.entry((byte)0x73, "F4"),
		Map.entry((byte)0x74, "F5"),
		Map.entry((byte)0x75, "F6"),
		Map.entry((byte)0x76, "F7"),
		Map.entry((byte)0x77, "F8"),
		Map.entry((byte)0x78, "F9"),
		Map.entry((byte)0x79, "F10"),
		Map.entry((byte)0x7A, "F11"),
		Map.entry((byte)0x7B, "F12"),
		Map.entry((byte)0x7C, "F13"),
		Map.entry((byte)0x7D, "F14"),
		Map.entry((byte)0x7E, "F15"),
		Map.entry((byte)0x7F, "F16"),
		Map.entry((byte)0x80, "F17"),
		Map.entry((byte)0x81, "F18"),
		Map.entry((byte)0x82, "F19"),
		Map.entry((byte)0x83, "F20"),
		Map.entry((byte)0x84, "F21"),
		Map.entry((byte)0x85, "F22"),
		Map.entry((byte)0x86, "F23"),
		Map.entry((byte)0x87, "F24"),
		Map.entry((byte)0x90, "NUM LOCK"),
		Map.entry((byte)0x91, "SCROLL LOCK"),
		Map.entry((byte)0x01, "SHIFT"),
		Map.entry((byte)0x02, "CTRL"),
		Map.entry((byte)0x04, "ALT")
	));
	
	private static HashMap<String, Byte> keysr = new HashMap<>();
	
	static {
		for (var i : keys.entrySet())
			keysr.put(i.getValue(), i.getKey());
	}
	
	private byte low;
	private byte high;
	
	public HotKeyFlags() {
		low = high = 0;
	}
	
	public HotKeyFlags(ByteReader data) throws IOException {
		low = (byte)data.read();
		high = (byte)data.read();
	}
	
	public String getKey() {
		return keys.get(low);
	}
	
	public HotKeyFlags setKey(String k) {
		if (k != null && !k.equals(""))
			low = keysr.get(k);
		return this;
	}
	
	public boolean isShift() { return (high & 1) != 0; }
	public boolean isCtrl() { return (high & 2) != 0; }
	public boolean isAlt() { return (high & 4) != 0; }
	
	public HotKeyFlags setShift() { high = (byte)(1 | (high & 6)); return this; }
	public HotKeyFlags setCtrl() { high = (byte)(2 | (high & 5)); return this; }
	public HotKeyFlags setAlt() { high = (byte)(4 | (high & 3)); return this; }
	
	public HotKeyFlags clearShift() { high = (byte)(high & 6); return this; }
	public HotKeyFlags clearCtrl() { high = (byte)(high & 5); return this; }
	public HotKeyFlags clearAlt() { high = (byte)(high & 3); return this; }

	public void serialize(ByteWriter bw) throws IOException {
		bw.write(low);
		bw.write(high);
	}
}
