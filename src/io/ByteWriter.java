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
package io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;

public class ByteWriter extends OutputStream implements SerializerStream<ByteWriter> {
	private boolean le = ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN);

	private OutputStream stream;
	private int pos = 0;
	
	
	public ByteWriter(OutputStream out) {
		stream = out;
	}

	@Override
	public int getPosition() {
		return pos;
	}

	@Override
	public ByteWriter setLittleEndian() {
		le = true;
		return this;
	}

	@Override
	public ByteWriter setBigEndian() {
		le = false;
		return this;
	}

	@Override
	public boolean isLittleEndian() {
		return le;
	}

	@Override
	public boolean isBigEndian() {
		return !le;
	}

	@Override
	public void close() throws IOException {
		if (stream != null) {
			stream.close();
		}
		super.close();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		pos += len;
		if (stream != null) {
			stream.write(b, off, len);
		}
	}
	
	@Override
	public void write(int b) throws IOException {
		pos++;
		if (stream != null) {
			stream.write(b);
		}
	}

	public void write(long value, int numBytes) throws IOException {
		if (numBytes > 8) {
			throw new IOException(String.format("Can't write %d bytes at a time", numBytes));
		}

		int start = 0;
		int end = numBytes;
		int step = 1;
		if (!le) {
			start = numBytes - 1;
			end = -1;
			step = -1;
		}

		for (int i = start; i != end; i += step) {
			long shift = i * 8;
			long mask = 0xffL << shift;
			long b = (value & mask) >> shift;
			write(b);
		}
	}

	private void write(long b) throws IOException {
		write((int)b);
	}
	
	/**
	 * writes 0-terminated string in default code page
	 */
	public void writeString(String s) throws IOException {
		write(s.getBytes());
		write(0);
	}

	/**
	 * writes 0-terminated string in default code page
	 */
	public void writeStringFixedSize(String s, int size) throws IOException {
		byte[] bytes = s.getBytes();
		write(bytes, 0, Math.min(bytes.length, size));
		int numToPad = size - bytes.length;
		for (int i = 0; i < numToPad; ++i) {
			write(0);
		}
	}

	/**
	 * writes 0-terminated string in unicode
	 */
	public void writeUnicodeStringNullTerm(String s) throws IOException {
		for (int i=0; i<s.length(); i++)
			write(s.charAt(i), 2);
		write(0, 2);
	}

	/**
	 * writes 0-terminated string in unicode
	 */
	public void writeUnicodeStringFixedSize(String s, int size) throws IOException {
		int maxChars = Math.min(s.length(),  size / 2);
		for (int i=0; i<maxChars; i++) {
			write(s.charAt(i), 2);
		}
		int numToPad = size - maxChars * 2;
		for (int i = 0; i < numToPad; ++i) {
			write(0);
		}
	}

	/**
	 * writes unicode string with 2 bytes at the start indicating the length of the string
	 */
	public void writeUnicodeStringSizePadded(String s) throws IOException {
		write(s.length(), 2);
		for (int i=0; i<s.length(); i++)
			write(s.charAt(i), 2);
	}
}
