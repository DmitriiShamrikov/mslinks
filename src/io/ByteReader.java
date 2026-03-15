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
import java.io.InputStream;
import java.nio.ByteOrder;

public class ByteReader extends InputStream implements SerializerStream<ByteReader> {
	private boolean le = ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN);
	
	private InputStream stream;
	private int pos = 0;

	private byte[] buffer = new byte[8];
	
	
	public ByteReader(InputStream in) {
		stream = in;
	}

	@Override
	public int getPosition() {
		return pos;
	}

	@Override
	public ByteReader changeEndiannes() {
		le = !le;
		return this;
	}

	@Override
	public ByteReader setLittleEndian() {
		le = true;
		return this;
	}

	@Override
	public ByteReader setBigEndian() {
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
	
	public boolean seek(int n) throws IOException {
		if (n < 0) return false;
		for (int i=0; i<n; i++)
			read();
		return true;
	}

	public boolean seekTo(int newPos) throws IOException {
		return seek(newPos - pos);
	}

	@Override
	public void close() throws IOException
	{
		stream.close();
		super.close();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		int result = stream.read(b, off, len);
		pos += result;
		return result;
	}
	
	@Override
	public int read() throws IOException {
		pos++;
		return stream.read();
	}

	public long read(int numBytes) throws IOException
	{
		if (numBytes > buffer.length)
		{
			throw new IOException(String.format("Can't read %d bytes at a time", numBytes));
		}

		int numBytesRead = read(buffer, 0, numBytes);
		if (numBytesRead != numBytes)
		{
			throw new IOException(String.format("Can't read %d bytes, reached end of file", numBytes));
		}

		int start = 0;
		int end = numBytes;
		int step = 1;
		if (!le)
		{
			start = numBytes - 1;
			end = -1;
			step = -1;
		}

		long result = 0;
		byte shift = 0;
		for (int i = start; i != end; i += step, ++shift)
		{
			result |= (long)(buffer[i] & 0xff) << (shift * 8);
		}

		return result;
	}
	
	/**
	 * reads 0-terminated string in default code page
	 * @param sz - maximum size in bytes
	 */
	public String readString(int sz) throws IOException {
		if (sz == 0) return null;
		byte[] buf = new byte[sz];
		int i = 0;
		for (; i<sz; i++) {
			int b = read();
			if (b == 0) break;
			buf[i] = (byte)b;
		}
		return new String(buf, 0, i);
	}
	
	/**
	 * reads 0-terminated string in unicode
	 * @param sz - maximum size in charcters
	 */
	public String readUnicodeStringNullTerm(int sz) throws IOException {
		if (sz == 0) return null;
		char[] buf = new char[sz];
		int i = 0;
		for (; i<sz; i++) {
			char c = (char)read(2);
			if (c == 0) break;
			buf[i] = c;
		}
		return new String(buf, 0, i);
	}
	
	/**
	 * reads unicode string that has 2 bytes at start indicates length of string
	 */
	public String readUnicodeStringSizePadded() throws IOException {
		int c = (int)read(2);
		char[] buf = new char[c];
		for (int i=0; i<c; i++)
			buf[i] = (char)read(2);
		return new String(buf);
	}
}