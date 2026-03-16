/*
	https://github.com/BlackOverlord666/mslinks
	
	Copyright (c) 2026 Dmitrii Shamrikov

	Licensed under the WTFPL
	You may obtain a copy of the License at
 
	http://www.wtfpl.net/about/
 
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/
package io;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Serializer<T extends SerializerStream<T>> implements Closeable
{
	public static final String BLOCK_SIZE_NAME = "size";

	private T m_Stream;
	private boolean m_EnableLogging = false;
	private boolean m_SuspendLogging = false;
	private ArrayList<Block> m_Blocks = new ArrayList<>();

	private class Block
	{
		public int size;
		public int startPos;
		public Callable<String> formatter;

		public Block(Callable<String> formatter)
		{
			this.formatter = formatter;
		}
	}

	private class BlockScope implements Closeable
	{
		private Serializer<T> m_Serializer;

		public BlockScope(Serializer<T> serializer)
		{
			m_Serializer = serializer;
		}

		@Override
		public void close()
		{
			m_Serializer.endBlock();
		}
	}

	public Serializer()
	{
	}

	public Serializer(boolean enableLogging)
	{
		m_EnableLogging = enableLogging;
	}
	
	public Serializer(T stream)
	{
		this(stream, false);
	}

	public Serializer(T stream, boolean enableLogging)
	{
		this(enableLogging);
		m_Stream = stream;
	}

	public boolean getSuspendLogging()
	{
		return m_SuspendLogging;
	}

	public void setSuspendLogging(boolean value)
	{
		m_SuspendLogging = value;
	}

	public boolean isLoggingActive()
	{
		return m_EnableLogging && !m_SuspendLogging;
	}
	
	public int getPosition()
	{
		return m_Stream.getPosition();
	}
	
	public T changeEndiannes()
	{
		return m_Stream.changeEndiannes();
	}

	public boolean isLittleEndian()
	{
		return m_Stream.isLittleEndian();
	}

	public boolean isBigEndian()
	{
		return m_Stream.isBigEndian();
	}

	public T setLittleEndian()
	{
		return m_Stream.setLittleEndian();
	}

	public T setBigEndian()
	{
		return m_Stream.setBigEndian();
	}

	public Closeable beginBlock(String name)
	{
		return beginBlock(name, null);
	}

	public Closeable beginBlock(String name, Callable<String> formatter)
	{
		if (isLoggingActive())
		{
			logIndentation();
			System.out.println(name);
			var block = new Block(formatter);
			block.startPos = getPosition();
			m_Blocks.add(block);
		}

		return new BlockScope(this);
	}

	private void endBlock()
	{
		if (isLoggingActive())
		{
			boolean hasBeenFullySerialized = true;
			var block = m_Blocks.get(m_Blocks.size() - 1);
			if (block.size > 0)
			{
				int bytesSerialized = getPosition() - block.startPos;
				if (m_Stream instanceof ByteReader)
				{
					if (bytesSerialized < block.size)
					{
						hasBeenFullySerialized = false;

						int size = block.size - bytesSerialized;
						byte[] arr = new byte[size];
						try
						{
							read(arr, 0, size, "leftover data");
						}
						catch (Exception e)
						{
							System.out.printf("failed to read leftover data (%d bytes) because: %s", size, e.getMessage());
						}
					}
				}
				else
				{
					if (bytesSerialized != block.size)
					{
						System.out.printf("Written %d bytes while expeced %d. This will not work", bytesSerialized, block.size);
					}
				}
			}

			if (hasBeenFullySerialized && block.formatter != null)
			{
				try
				{
					logIndentation();
					System.out.printf("|-> %s\n", block.formatter.call());
				}
				catch (Exception ex)
				{
				}
			}
			m_Blocks.remove(m_Blocks.size() - 1);
		}
	}
	
	public boolean seek(int n) throws IOException
	{
		if (n < 0)
		{
			return false;
		}

		var builder = isLoggingActive() ? new StringBuilder() : null;
	
		var reader = asReader();
		for (int i = 0; i < n; i += 16)
		{
			if (isLoggingActive())
			{
				logIndentation();
				builder.delete(0, builder.length());
			}

			int j = 0;
			for (; j < 16 && j < n - i; ++j)
			{
				int value = reader.read();

				if (isLoggingActive())
				{
					System.out.printf("%02X ", value);
					builder.append(isPrintable((byte)value) ? (char)value : '.');
				}
			}
			
			if (isLoggingActive())
			{
				System.out.printf("| %s | skipped (%d bytes)\n", builder.toString(), j);
			}
		}

		return true;
	}

	public boolean seekTo(int newPos) throws IOException
	{
		return seek(newPos - getPosition());
	}

	@Override
	public void close() throws IOException
	{
		m_Stream.close();
	}

	public int read(byte[] b, int off, int len, String name) throws IOException
	{
		int numBytes = asReader().read(b, off, len);
		logArray(b, numBytes, name, null);
		return numBytes;
	}

	public int read(String name) throws IOException
	{
		return read(name, Serializer::defaultFormatter);
	}
	
	public int read(String name, Function<Long, String> formatter) throws IOException
	{	
		int value = asReader().read();
		logByte(value, name, formatter);
		if (isLoggingActive() && m_Blocks.size() > 0 && name.equals(BLOCK_SIZE_NAME))
		{
			m_Blocks.get(m_Blocks.size() - 1).size = value;
		}
		return value;
	}

	public long read(int numBytes, String name) throws IOException
	{
		return read(numBytes, name, Serializer::defaultFormatter);
	}

	public long read(int numBytes, String name, Function<Long, String> formatter) throws IOException
	{
		long value =  asReader().read(numBytes);
		logValue(value, numBytes, name, formatter);
		if (isLoggingActive() && m_Blocks.size() > 0 && name.equals(BLOCK_SIZE_NAME))
		{
			m_Blocks.get(m_Blocks.size() - 1).size = (int)value;
		}
		return value;
	}

	public String readString(int sz, String name) throws IOException
	{
		String str = asReader().readString(sz);

		if (isLoggingActive())
		{
			var memStream = new ByteArrayOutputStream();
			try (var writer = getTempWriter(memStream))
			{
				writer.writeString(str);
			}
			logArray(memStream.toByteArray(), memStream.size(), name, arr -> str);
		}

		return str;
	}
	
	public String readUnicodeStringNullTerm(int sz, String name) throws IOException
	{
		String str = asReader().readUnicodeStringNullTerm(sz);

		if (isLoggingActive())
		{
			var memStream = new ByteArrayOutputStream();
			try (var writer = getTempWriter(memStream))
			{
				writer.writeUnicodeStringNullTerm(str);
			}
			logArray(memStream.toByteArray(), memStream.size(), name, arr -> str);
		}

		return str;
	}
	
	public String readUnicodeStringSizePadded(String name) throws IOException
	{
		String str = asReader().readUnicodeStringSizePadded();

		if (isLoggingActive())
		{
			var memStream = new ByteArrayOutputStream();
			try (var writer = getTempWriter(memStream))
			{
				writer.writeUnicodeStringSizePadded(str);
			}
			logArray(memStream.toByteArray(), memStream.size(), name, arr -> str);
		}

		return str;
	}

	public void write(byte[] b, String name) throws IOException
	{
		write(b, 0, b.length, name);
	}

	public void write(byte[] b, int off, int len, String name) throws IOException
	{
		logArray(b, off, len, name, null);
		asWriter().write(b, off, len);
	}

	public void write(int b, String name) throws IOException
	{
		write(b, name, Serializer::defaultFormatter);
	}
	
	public void write(int b, String name, Function<Long, String> formatter) throws IOException
	{
		logByte(b, name, formatter);
		asWriter().write(b);
	}

	public void write(long value, int numBytes, String name) throws IOException
	{
		write(value, numBytes, name, Serializer::defaultFormatter);
	}

	public void write(long value, int numBytes, String name, Function<Long, String> formatter) throws IOException
	{
		logValue(value, numBytes, name, formatter);
		asWriter().write(value, numBytes);
	}

	public void writeString(String s, String name) throws IOException
	{
		writeString(s, -1, name);
	}

	public void writeStringFixedSize(String s, int maxSize, String name) throws IOException
	{
		writeString(s, maxSize, name);
	}

	private void writeString(String s, int maxSize, String name) throws IOException
	{
		if (isLoggingActive())
		{
			var memStream = new ByteArrayOutputStream();
			try (var writer = getTempWriter(memStream))
			{
				if (maxSize == -1)
				{
					writer.writeString(s);
				}
				else
				{
					writer.writeStringFixedSize(s, maxSize);
				}
			}
			logArray(memStream.toByteArray(), memStream.size(), name, arr -> s);
		}

		if (maxSize == -1)
		{
			asWriter().writeString(s);
		}
		else
		{
			asWriter().writeStringFixedSize(s, maxSize);
		}
	}

	public void writeUnicodeStringNullTerm(String s, String name) throws IOException
	{
		writeUnicodeStringNullTerm(s, -1, name);
	}

	public void writeUnicodeStringFixedSize(String s, int maxSize, String name) throws IOException
	{
		writeUnicodeStringNullTerm(s, maxSize, name);
	}

	private void writeUnicodeStringNullTerm(String s, int maxSize, String name) throws IOException
	{
		if (isLoggingActive())
		{
			var memStream = new ByteArrayOutputStream();
			try (var writer = getTempWriter(memStream))
			{
				if (maxSize == -1)
				{
					writer.writeUnicodeStringNullTerm(s);
				}
				else
				{
					writer.writeUnicodeStringFixedSize(s, maxSize);
				}
			}
			logArray(memStream.toByteArray(), memStream.size(), name, arr -> s);
		}

		if (maxSize == -1)
		{
			asWriter().writeUnicodeStringNullTerm(s);
		}
		else
		{
			asWriter().writeUnicodeStringFixedSize(s, maxSize);
		}
	}

	public void writeUnicodeStringSizePadded(String s, String name) throws IOException
	{
		if (isLoggingActive())
		{
			var memStream = new ByteArrayOutputStream();
			try (var writer = getTempWriter(memStream))
			{
				writer.writeUnicodeStringSizePadded(s);
			}
			logArray(memStream.toByteArray(), memStream.size(), name, arr -> s);
		}

		asWriter().writeUnicodeStringSizePadded(s);
	}

	private void logByte(long value, String name, Function<Long, String> formatter)
	{
		if (isLoggingActive())
		{
			logIndentation();
			if (formatter == null)
			{
				System.out.printf("%02X | %s\n", value, name);
			}
			else
			{
				System.out.printf("%02X | %s | %s\n", value, formatter.apply(value), name);
			}
		}
	}

	private void logValue(long value, int numBytes, String name, Function<Long, String> formatter)
	{
		if (isLoggingActive() && numBytes > 0)
		{
			logIndentation();
			int start = 0;
			int end = numBytes;
			int step = 1;
			if (m_Stream.isBigEndian())
			{
				start = numBytes - 1;
				end = -1;
				step = -1;
			}

			for (int i = start; i != end; i += step)
			{
				long shift = i * 8;
				long mask = 0xffL << shift;
				long b = ((value & mask) >>> shift) & 0xff;
				System.out.printf("%02X ", b);
			}

			if (formatter == null)
			{
				System.out.printf("| %s\n", name);
			}
			else
			{
				System.out.printf("| %s | %s\n", formatter.apply(value), name);
			}
		}
	}
	
	private void logArray(byte[] arr, int numBytes, String name, Function<byte[], String> formatter)
	{
		logArray(arr, 0, numBytes, name, formatter);
	}

	private void logArray(byte[] arr, int offset, int numBytes, String name, Function<byte[], String> formatter)
	{
		if (isLoggingActive())
		{
			for (int i = 0; i < numBytes; i += 16)
			{
				if (i % 16 == 0)
				{
					logIndentation();
				}

				for (int j = 0; j < 16 && j < numBytes - i; ++j)
				{
					System.out.printf("%02X ", arr[offset + i + j]);
				}

				System.out.print("| ");

 				for (int j = 0; j < 16 && j < numBytes - i; ++j)
				{
					byte b = arr[offset + i + j];
					System.out.printf("%c", isPrintable(b) ? b : '.');
				}

				System.out.printf(" | %s\n", name);
			}

			if (formatter != null)
			{
				logIndentation();
				System.out.printf("  |-> %s\n", formatter.apply(arr));
			}
		}
	}

	private void logIndentation()
	{
		System.out.printf("%4d: ", getPosition());
		for (int i = 0; i < m_Blocks.size(); ++i)
		{
			System.out.printf("   ");
		}
	}

	private static boolean isPrintable(byte b)
	{
		Character.UnicodeBlock block = Character.UnicodeBlock.of((char)b);
    	return b >= 0 && !Character.isISOControl(b) && block != null && block != Character.UnicodeBlock.SPECIALS;
	}

	private static String defaultFormatter(Long value)
	{
		return value.toString();
	}

	private ByteWriter getTempWriter(OutputStream stream)
	{
		var writer = new ByteWriter(stream);
		if (m_Stream.isLittleEndian())
		{
			writer.setLittleEndian();
		}
		else
		{
			writer.setBigEndian();
		}
		return writer;
	}

	private ByteReader asReader() throws IOException
	{
		if (m_Stream instanceof ByteReader)
		{
			return (ByteReader)m_Stream;
		}
		else
		{
			throw new IOException("Can't read from a writing stream!");
		}
	}

	private ByteWriter asWriter() throws IOException
	{
		if (m_Stream instanceof ByteWriter)
		{
			return (ByteWriter)m_Stream;
		}
		else
		{
			throw new IOException("Can't write to a reading stream!");
		}
	}

	public static void iterateOverClassConsts(Class<?> cl, BiFunction<Field, Integer, Boolean> lambda) {
		int mod = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
		for (var field : cl.getFields() ) {
			if ((field.getModifiers() & mod) == mod && field.getType() == int.class) {
				try {
					int constValue = (int)field.get(null);
					if (!lambda.apply(field, constValue)) {
						return;
					}
				} catch (IllegalAccessException e) {
				}
			}
		}
	}

	public static Field findConstField(Class<?> cl, int value, Function<Field, Boolean> filter) {
		// java can't do references to variables, therefore can't assign something in a lambda
		var arr = new Field[1];
		Serializer.iterateOverClassConsts(cl, (field, constValue) -> {
			if (constValue == value && (filter == null || filter.apply(field))) {
				arr[0] = field;
				return false;
			}
			return true;
		});
		return arr[0];
	}

	public static Field findConstField(Class<?> cl, int value) {
		return findConstField(cl, value, null);
	}
}
