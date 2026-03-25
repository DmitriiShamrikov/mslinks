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
package mslinks.extra;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

import io.ByteReader;
import io.ByteWriter;
import io.Serializer;
import mslinks.Serializable;
import mslinks.ShellLinkException;
import mslinks.data.Filetime;
import mslinks.data.GUID;
import mslinks.data.Registry;

public class PropertyStore implements Serializable
{
	public static final int signature = 0xA0000009;
	public static final int minsize = 0x0c;
	public static final int version = 0x53505331;

	public static final int CODEPAGE_PROPERTY_IDENTIFIER = 0x00000001;

	public static final int VT_VECTOR = 0x1000;
	public static final int VT_ARRAY = 0x2000;
	public static final int VT_TYPE_MASK = 0x0fff;

	public static final int VT_EMPTY            = 0x0000;
	public static final int VT_NULL             = 0x0001;
	public static final int VT_I2               = 0x0002; // +VT_VECTOR +VT_ARRAY
	public static final int VT_I4               = 0x0003; // +VT_VECTOR +VT_ARRAY
	public static final int VT_R4               = 0x0004; // +VT_VECTOR +VT_ARRAY
	public static final int VT_R8               = 0x0005; // +VT_VECTOR +VT_ARRAY
	public static final int VT_CY               = 0x0006; // +VT_VECTOR +VT_ARRAY
	public static final int VT_DATE             = 0x0007; // +VT_VECTOR +VT_ARRAY
	public static final int VT_BSTR             = 0x0008; // +VT_VECTOR +VT_ARRAY
	public static final int VT_ERROR            = 0x000A; // +VT_VECTOR +VT_ARRAY
	public static final int VT_BOOL             = 0x000B; // +VT_VECTOR +VT_ARRAY
	public static final int VT_VARIANT          = 0x000C; // VT_VECTOR VT_ARRAY (this can't be outside vector or array)
	public static final int VT_DECIMAL          = 0x000E; // +VT_ARRAY

	public static final int VT_I1               = 0x0010; // +VT_VECTOR +VT_ARRAY
	public static final int VT_UI1              = 0x0011; // +VT_VECTOR +VT_ARRAY
	public static final int VT_UI2              = 0x0012; // +VT_VECTOR +VT_ARRAY
	public static final int VT_UI4              = 0x0013; // +VT_VECTOR +VT_ARRAY
	public static final int VT_I8               = 0x0014; // +VT_VECTOR
	public static final int VT_UI8              = 0x0015; // +VT_VECTOR
	public static final int VT_INT              = 0x0016; // +VT_ARRAY
	public static final int VT_UINT             = 0x0017; // +VT_ARRAY

	public static final int VT_LPSTR            = 0x001E; // +VT_VECTOR
	public static final int VT_LPWSTR           = 0x001F; // +VT_VECTOR

	public static final int VT_FILETIME         = 0x0040; // +VT_VECTOR
	public static final int VT_BLOB             = 0x0041;
	public static final int VT_STREAM           = 0x0042;
	public static final int VT_STORAGE          = 0x0043;
	public static final int VT_STREAMED_OBJECT  = 0x0044;
	public static final int VT_STORED_OBJECT    = 0x0045;
	public static final int VT_BLOB_OBJECT      = 0x0046;
	public static final int VT_CF               = 0x0047; // +VT_VECTOR
	public static final int VT_CLSID            = 0x0048; // +VT_VECTOR
	public static final int VT_VERSIONED_STREAM = 0x0049;

	private static final GregorianCalendar BASE_DATE;

	static 
	{
		BASE_DATE = new GregorianCalendar(1899, 11, 30);
		BASE_DATE.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public static class MultiArray
	{
		private int[] m_DimSizes;
		private int[] m_IndexOffsets;
		private Object[] m_Data;

		public MultiArray(int[] dimSizes, int[] indexOffsets)
		{
			m_DimSizes = dimSizes;
			m_IndexOffsets = indexOffsets;
			
			int totalSize = 0;
			for (int i : dimSizes)
			{
				totalSize += i;
			}
			m_Data = new Object[totalSize];
		}

		public int[] getDimimensions()
		{
			return m_DimSizes;
		}

		public int[] getIndexOffsets()
		{
			return m_IndexOffsets;
		}

		public int size(int dimension)
		{
			return m_DimSizes[dimension];
		}

		public int totalSize()
		{
			return m_Data.length;
		}

		public Object get(int idx)
		{
			return m_Data[idx];
		}

		public void set(int idx, Object value)
		{
			m_Data[idx] = value;
		}

		public Object get(int[] indexes) throws ShellLinkException
		{
			return m_Data[getIndex(indexes)];
		}

		public void set(int[] indexes, Object value) throws ShellLinkException
		{
			m_Data[getIndex(indexes)] = value;
		}

		private int getIndex(int[] indexes) throws ShellLinkException
		{
			if (indexes.length != m_DimSizes.length)
			{
				throw new ShellLinkException(String.format("Wrong indexes amount for multi-array: expected %d, got %d", m_DimSizes.length, indexes.length));
			}

			int idx = 0;
			for (int i = 0; i < indexes.length; ++i)
			{
				int dimIdx = indexes[i] - m_IndexOffsets[i];
				if (dimIdx < 0 || dimIdx >= m_DimSizes[i])
				{
					throw new ShellLinkException(String.format("Index[%d] id out of bound: expected < %d, got %d", i, m_DimSizes.length, dimIdx));
				}

				for (int j = i + 1; j < indexes.length; ++j)
				{
					dimIdx *= m_DimSizes[j];
				}

				idx += dimIdx;
			}

			return idx;
		}
	}

	public static class VersionedStreamName
	{
		public GUID m_Guid;
		public String m_Name;
	}
	
	private Charset m_Charset;
	private ArrayList<HashMap<Object, Object>> m_Storages = new ArrayList<>();

	public ArrayList<HashMap<Object, Object>> getPropertyStorage()
	{
		return m_Storages;
	}

	public PropertyStore()
	{
	}

	public PropertyStore(ByteReader br, int sz) throws ShellLinkException, IOException
	{
		this(new Serializer<ByteReader>(br), sz);
	}
	
	public PropertyStore(Serializer<ByteReader> serializer, int sz) throws ShellLinkException, IOException
	{
		if (sz < minsize)
		{
			throw new ShellLinkException();
		}

		m_Charset = null;	
		while (true)
		{
			try (var block = serializer.beginBlock("Storage"))
			{
				int size = (int)serializer.read(4, Serializer.BLOCK_SIZE_NAME);
				if (size == 0)
				{
					break;
				}

				int ver = (int)serializer.read(4, "version");
				if (ver != version)
				{
					throw new ShellLinkException();
				}

				var formatId = new GUID(serializer, "format ID");
				boolean isStringStorage = formatId.equals(Registry.PROPERTY_STORAGE_FORMAT_STRING);

				HashMap<Object, Object> storage = new HashMap<>();
				while (true)
				{
					try (var propertyBlock = serializer.beginBlock("PropertyValue"))
					{
						int startPos = serializer.getPosition();
						int propSize = (int)serializer.read(4, Serializer.BLOCK_SIZE_NAME);
						if (propSize == 0)
						{
							break;
						}

						Object key;
						if (isStringStorage)
						{
							int nameSize = (int)serializer.read(4, "name size");
							serializer.read("reserved");
							key = serializer.readUnicodeStringNullTerm(nameSize, "name");
						}
						else
						{
							key = (int)serializer.read(4, "id", 
								v -> v == CODEPAGE_PROPERTY_IDENTIFIER ? String.format("%d (codepage)", v) : ((Long)v).toString());
							serializer.read("reserved");
						}

						Object value = readTypedValue(serializer, m_Charset);
						storage.put(key, value);

						if (!isStringStorage && (int)key == CODEPAGE_PROPERTY_IDENTIFIER && Integer.class.isAssignableFrom(value.getClass()))
						{
							m_Charset = getCharset((int)value);	
						}

						serializer.seekTo(startPos + propSize);
					}
				}

				m_Storages.add(storage);
			}
		}
	}

	private static Object readTypedValue(Serializer<ByteReader> serializer, Charset storeCharset) throws IOException, ShellLinkException
	{
		int valueType = (int)serializer.read(2, "type", PropertyStore::typeToLog);
		serializer.read(2, "padding");
		boolean isVector = (valueType & VT_VECTOR) != 0;
		boolean isArray = (valueType & VT_ARRAY) != 0;

		if (isVector)
		{
			try (var vectorBlock = serializer.beginBlock("Vector"))
			{
				int length = (int)serializer.read(4, "length");
				Object[] arr = new Object[length];
				for (int i = 0; i < length; ++i)
				{
					arr[i] = readValue(serializer, valueType, storeCharset);
				}
				return arr;
			}
		}
		else if (isArray)
		{
			try (var arrayBlock = serializer.beginBlock("Array"))
			{
				// oh boy...
				int type = (int)serializer.read(4, "type", PropertyStore::typeToLog);
				if (type != (valueType & ~VT_ARRAY))
				{
					throw new ShellLinkException();
				}
				
				int numDimension = (int)serializer.read(4, "numDimension");
				if (numDimension < 1 || numDimension > 31)
				{
					throw new ShellLinkException();
				}

				int[] dimSizes = new int[numDimension];
				int[] indexOffsets = new int[numDimension];
				for (int i = 0; i < numDimension; ++i)
				{
					dimSizes[i] = (int)serializer.read(4, "dimension size");
					indexOffsets[i] = (int)serializer.read(4, "index offset");
				}

				var marr = new PropertyStore.MultiArray(dimSizes, indexOffsets);
				for (int i = 0; i < marr.totalSize(); ++i)
				{
					Object value = readValue(serializer, valueType, storeCharset);
					marr.set(i, value);
				}

				return marr;
			}
		}
		else
		{
			return readValue(serializer, valueType, storeCharset);
		}
	}
	
	private static Object readValue(Serializer<ByteReader> serializer, int valueType, Charset charset) throws IOException, ShellLinkException
	{
		boolean isVector = (valueType & VT_VECTOR) != 0;
		boolean isArray = (valueType & VT_ARRAY) != 0;
		int type = valueType & VT_TYPE_MASK;
		switch (type)
		{
			case VT_EMPTY: 
			case VT_NULL:
				 return null;
			case VT_I1:
			case VT_UI1:
			{
				byte value = (byte)serializer.read("value");
				serializer.seek(3);
				return value;
			}
			case VT_I2:
			case VT_UI2:
			{
				short value = (short)serializer.read(2, "value");
				serializer.seek(2);
				return value;
			}
			case VT_BOOL:
			{
				short value = (short)serializer.read(2, "value", v -> v == 0 ? "false" : "true");
				serializer.seek(2);
				return value != 0;
			}
			case VT_I4:
			case VT_UI4:
			case VT_INT:
			case VT_UINT:
			case VT_ERROR:
				return (int)serializer.read(4, "value");
			case VT_I8:
			case VT_UI8:
				return serializer.read(8, "value");
			case VT_R4:
			{
				int value = (int)serializer.read(4, "value", v -> ((Float)Float.intBitsToFloat((int)(long)v)).toString());
				return Float.intBitsToFloat(value);
			}
			case VT_R8:
			{
				long value = serializer.read(8, "value", v -> ((Double)Double.longBitsToDouble(v)).toString());
				return Double.longBitsToDouble(value);
			}
			case VT_CY:
			{
				long value = serializer.read(8, "value", v -> new BigDecimal(BigInteger.valueOf(v), 4).toString());
				return new BigDecimal(BigInteger.valueOf(value), 4);
			}
			case VT_FILETIME:
			{
				return new Filetime(serializer, "Filetime");
			}
			case VT_CLSID:
			{
				return new GUID(serializer);
			}
			case VT_DATE:
			{
				long value = serializer.read(8, "value", v -> parseDate(v).toString());
				return parseDate(value);
			}
			case VT_BSTR:
			case VT_LPSTR:
			case VT_STREAM:
			case VT_STORAGE:
			case VT_STREAMED_OBJECT:
			case VT_STORED_OBJECT:
			{
				return readStringWithCharset(serializer, charset);
			}
			case VT_LPWSTR:
			{
				try (var block = serializer.beginBlock("String (Unicode)"))
				{
					int strLen = (int)serializer.read(4, "str length");
					String value = serializer.readUnicodeStringNullTerm(strLen, "str data");
					int strSize = strLen * 2;
					if ((strSize % 4) != 0)
					{
						serializer.seek(4 - (strSize % 4));
					}
					return value;
				}
			}
			case VT_DECIMAL:
			{
				var wrap = new BigDecimal[0];
				try (var block = serializer.beginBlock("Decimal", () -> wrap[0] != null ? wrap[0].toString() : "???"))
				{
					serializer.read(2, "reserved");
					long scale = serializer.read("scale");
					long sign = serializer.read("sign");
					long hi32 = serializer.read(4, "hi32");
					long lo64 = serializer.read(8, "lo64");

					if (scale < 0 || scale > 28)
					{
						throw new ShellLinkException();
					}

					var bint = BigInteger.valueOf(hi32);
					bint = bint.shiftLeft(64);
					bint = bint.or(BigInteger.valueOf(lo64 & 0x7fffffffffffffffL));
					if (lo64 < 0)
					{
						bint.setBit(63);
					}
					
					var value = new BigDecimal(bint, (int)scale);
					if (sign == 0x80)
					{
						value = value.negate();
					}

					wrap[0] = value;
					return value;
				}
			}
			case VT_CF:
			case VT_BLOB:
			case VT_BLOB_OBJECT:
			{
				String name = type == VT_CF ? "Clipboard" : "Blob";
				try (var block = serializer.beginBlock(name))
				{
					int size = (int)serializer.read(4, Serializer.BLOCK_SIZE_NAME);
					byte[] blob = new byte[size];
					serializer.read(blob, 0, size, "data");
					if ((size % 4) != 0)
					{
						serializer.seek(4 - (size % 4));
					}
					return blob;
				}
			}
			case VT_VERSIONED_STREAM:
			{
				try (var block = serializer.beginBlock("VersionedStream"))
				{
					var stream = new PropertyStore.VersionedStreamName();
					stream.m_Guid = new GUID(serializer);
					stream.m_Name = readStringWithCharset(serializer, charset);
					return stream;
				}
			}
			case VT_VARIANT:
			{
				if (isVector || isArray)
				{
					return readTypedValue(serializer, charset);
				}
			}
		}

		return null;
	}

	private static String readStringWithCharset(Serializer<ByteReader> serializer, Charset charset) throws IOException
	{
		var str = new String[1];
		try (var block = serializer.beginBlock(String.format("String (%d)", charset != null ? charset.name() : "unknow charset"), () -> str[0]))
		{
			int strSize = (int)serializer.read(4, Serializer.BLOCK_SIZE_NAME);
			byte[] data = new byte[strSize];
			serializer.read(data, 0, strSize, "str data");
			if ((strSize % 4) != 0)
			{
				serializer.seek(4 - (strSize % 4));
			}
			str[0] = charset != null ? charset.decode(ByteBuffer.wrap(data)).toString() : null;
		}
		return str[0];
	}

	private static Charset getCharset(int codePage)
	{
		// this is totally untested
		// is there a better way to convert [MS-UCODEREF] to
		// https://docs.oracle.com/javase/8/docs/technotes/guides/intl/encoding.doc.html

		switch (codePage)
		{
			case 37: return Charset.forName("cp037");
			case 708: return Charset.forName("ASMO-708");
			case 1200: return Charset.forName("UTF-16LE");
			case 1201: return Charset.forName("UTF-16BE");
			case 1361: return Charset.forName("ms1361");
			case 10000: return Charset.forName("MacRoman");
			case 10004: return Charset.forName("MacArabic");
			case 10005: return Charset.forName("MacHebrew");
			case 10006: return Charset.forName("MacGreek");
			case 10007: return Charset.forName("MacCyrillic");
			case 10010: return Charset.forName("MacRomania");
			case 10017: return Charset.forName("MacUkraine");
			case 10021: return Charset.forName("MacThai");
			case 10029: return Charset.forName("MacCentralEurope");
			case 10079: return Charset.forName("MacIceland");
			case 10081: return Charset.forName("MacTurkish");
			case 10082: return Charset.forName("MacCroatian");
			case 20886: return Charset.forName("koi8_r");
			case 21886: return Charset.forName("koi8_u");
			case 38598: return Charset.forName("ISO-8859-8");
			case 50222: return Charset.forName("ISO-2022-JP");
			case 50225: return Charset.forName("ISO-2022-KR");
			case 50227: return Charset.forName("ISO-2022-CN");
			case 50229: return Charset.forName("ISO-2022-CN");
			case 65001: return Charset.forName("UTF8");
		}

		if (codePage == 437 ||codePage == 500 || codePage >= 737 && codePage <= 1258)
		{
			return Charset.forName(String.format("cp%d", codePage));
		}
		else if (codePage >= 28591 && codePage <= 28605)
		{
			int page = codePage - 28590;
			return Charset.forName(String.format("ISO-8859-%d", page));
		}

		return null;
	}

	private static String typeToLog(long value)
	{
		StringBuilder builder = new StringBuilder();

		if ((value & VT_VECTOR) != 0)
		{
			builder.append("VT_VECTOR | ");
		}
		else if ((value & VT_ARRAY) != 0)
		{
			builder.append("VT_ARRAY | ");
		}

		Field f = Serializer.findConstField(PropertyStore.class, (int)value & VT_TYPE_MASK);
		if (f != null)
		{
			builder.append(f.getName());
		}
		else
		{
			builder.append("UNKNOWN");
		}
		return builder.toString();
	}

	public static GregorianCalendar parseDate(long value)
	{
		double days = Double.longBitsToDouble(value); // num days since December 30, 1899
		var calendar = (GregorianCalendar)BASE_DATE.clone();
		try
		{
			calendar.add(GregorianCalendar.DAY_OF_YEAR, (int)days);
			double hours = days - (int)days;
			calendar.add(GregorianCalendar.HOUR, (int)hours);
			double minutes = hours - (int)hours;
			calendar.add(GregorianCalendar.MINUTE, (int)minutes);
			double seconds = minutes - (int)minutes;
			calendar.add(GregorianCalendar.SECOND, (int)seconds);
			double millis = seconds - (int)seconds;
			calendar.add(GregorianCalendar.MILLISECOND, (int)millis);
		}
		catch (Exception ex)
		{
			return null;
		}
		return calendar;
	}

	@Override
	public void serialize(Serializer<ByteWriter> serializer) throws IOException
	{
		var sizeSerializer = new Serializer<ByteWriter>(new ByteWriter(null));
		if (serializer.isWritingData())
		{
			for (int i = 0; i < serializer.getPosition(); ++i)
			{
				sizeSerializer.write(0, "");
			}

			try (var block = sizeSerializer.beginBlock("PropertyStore"))
			{
				serialize(sizeSerializer);
			}
		}

		int size = sizeSerializer.getSize(serializer.getPosition());
		serializer.write(size, 4, Serializer.BLOCK_SIZE_NAME);
		serializer.write(signature, 4, "signature", v -> getClass().getName());

		for (var storage : m_Storages)
		{
			if (storage.isEmpty())
				continue;

			try (var block = serializer.beginBlock("Storage"))
			{
				size = sizeSerializer.getSize(serializer.getPosition());
				serializer.write(size, 4, Serializer.BLOCK_SIZE_NAME);
				serializer.write(version, 4, "version");

				boolean isStringStorage = storage.entrySet().iterator().next().getKey() instanceof String;
				if (isStringStorage)
				{
					Registry.PROPERTY_STORAGE_FORMAT_STRING.serialize(serializer, "format ID");
				}
				else
				{
					GUID.ZERO.serialize(serializer, "format ID");
				}

				for (var entry : storage.entrySet())
				{
					try (var propertyBlock = serializer.beginBlock("PropertyValue"))
					{
						size = sizeSerializer.getSize(serializer.getPosition());
						serializer.write(size, 4, Serializer.BLOCK_SIZE_NAME);

						if (isStringStorage)
						{
							String key = (String)entry.getKey();
							serializer.write(key.length() * 2 + 2, 4, "name size");
							serializer.write(0, "reserved");
							serializer.writeUnicodeStringNullTerm(key, "name"); 
						}
						else
						{
							int key = (int)entry.getKey();
							serializer.write(key, 4, "id");
							serializer.write(0, "reserved");
						}

						writeTypedValue(serializer, entry.getValue());
					}
				}

				serializer.write(0, 4, Serializer.BLOCK_SIZE_NAME);
			}
		}

		serializer.write(0, 4, Serializer.BLOCK_SIZE_NAME);
	}

	private void writeTypedValue(Serializer<ByteWriter> serializer, Object value) throws IOException
	{
		int valueType = 0;
		boolean isVector = false;
		boolean isArray = false;
		boolean isVariant = false;
		Class<?> valueClass = null;
		if (value.getClass() == MultiArray.class)
		{
			isArray = true;
			valueType = VT_ARRAY;

			var marr = (PropertyStore.MultiArray)value;
			for (int i = 0; i < marr.totalSize(); ++i)
			{
				if (marr.get(i) != null)
				{
					if (valueClass == null)
					{
						valueClass = marr.get(i).getClass();
					}
					else if (valueClass != marr.get(i).getClass())
					{
						isVariant = true;
						break;
					}
				}
			}
		}
		else if (value.getClass().isArray())
		{
			isVector = true;
			valueType = VT_VECTOR;

			Object[] arr = (Object[])value;
			for (Object v : arr)
			{
				if (v != null)
				{
					if (valueClass == null)
					{
						valueClass = v.getClass();
					}
					else if (valueClass != v.getClass())
					{
						isVariant = true;
						break;
					}
				}
			}
		}
		else
		{
			valueClass = value.getClass();
		}

		if (isVariant)
		{
			valueType |= VT_VARIANT;
		}
		else if (valueClass == null)
		{
			valueType |= VT_NULL;
		}
		else if (valueClass == Boolean.class)
		{
			valueType |= VT_BOOL;
		}
		else if (valueClass == Byte.class)
		{
			valueType |= VT_I1;
		}
		else if (valueClass == Short.class)
		{
			valueType |= VT_I2;
		}
		else if (valueClass == Integer.class)
		{
			valueType |= VT_I4;
		}
		else if (valueClass == Long.class)
		{
			valueType |= VT_I8;
		}
		else if (valueClass == Float.class)
		{
			valueType |= VT_R4;
		}
		else if (valueClass == Double.class)
		{
			valueType |= VT_R8;
		}
		else if (valueClass == BigDecimal.class)
		{
			valueType |= VT_DECIMAL;
		}
		else if (valueClass == Filetime.class)
		{
			valueType |= VT_FILETIME;
		}
		else if (valueClass == GUID.class)
		{
			valueType |= VT_CLSID;
		}
		else if (valueClass == GregorianCalendar.class)
		{
			valueType |= VT_DATE;
		}
		else if (valueClass == String.class)
		{
			valueType |= VT_LPWSTR;
		}
		else if (valueClass == byte[].class)
		{
			valueType |= VT_BLOB;
		}
		else if (valueClass == VersionedStreamName.class)
		{
			valueType |= VT_VERSIONED_STREAM;
		}

		serializer.write(valueType, 2, "type", PropertyStore::typeToLog);
		serializer.write(0,2, "padding");
		if (isVector)
		{
			try (var vectorBlock = serializer.beginBlock("Vector"))
			{
				Object[] arr = (Object[])value;
				serializer.write(arr.length, 4, "length");
				for (int i = 0; i < arr.length; ++i)
				{
					writeValue(serializer, arr[i], valueType);
				}
			}
		}
		else if (isArray)
		{
			try (var arrayBlock = serializer.beginBlock("Array"))
			{
				serializer.write(valueType & ~VT_ARRAY, 2, "type", PropertyStore::typeToLog);

				MultiArray marr = (MultiArray)value;
				var dims = marr.getDimimensions();
				var offsets = marr.getIndexOffsets();
				serializer.write(marr.getDimimensions().length, 4, "numDimension");
				for (int i = 0; i < dims.length; ++i)
				{
					serializer.write(dims[i], 4, "dimension size");
					serializer.write(offsets[i], 4, "index offset");
				}

				for (int i = 0; i < marr.totalSize(); ++i)
				{
					writeValue(serializer, marr.get(i), valueType & ~VT_ARRAY);
				}
			}
		}
		else
		{
			writeValue(serializer, value, valueType);
		}
	}

	private void writeValue(Serializer<ByteWriter> serializer, Object value, int valueType) throws IOException
	{
		int type = valueType & VT_TYPE_MASK;
		switch (type)
		{
			case VT_EMPTY: 
			case VT_NULL:
				 return;
			case VT_I1:
			case VT_UI1:
			{
				serializer.write((byte)value, "value");
				serializer.write(0, 3, "padding");
				return;
			}
			case VT_I2:
			case VT_UI2:
			{
				serializer.write((short)value, 2, "value");
				serializer.write(0, 2, "padding");
				return;
			}
			case VT_BOOL:
			{
				serializer.write((boolean)value ? 0xffff : 0, 2, "value", v -> value.toString());
				serializer.write(0, 2, "padding");
				return;
			}
			case VT_I4:
			case VT_UI4:
			case VT_INT:
			case VT_UINT:
			case VT_ERROR:
				serializer.write((int)value, 4, "value");
				return;
			case VT_I8:
			case VT_UI8:
				serializer.write((long)value, 8, "value");
				return;
			case VT_R4:
				serializer.write(Float.floatToIntBits((float)value), 4, "value", v -> value.toString());
				return;
			case VT_R8:
				serializer.write(Double.doubleToLongBits((double)value), 8, "value", v -> value.toString());
				return;
			case VT_CY:
			{
				var decimal = (BigDecimal)value;
				if (decimal.scale() != 4)
				{
					decimal = decimal.setScale(4);
				}
				decimal = decimal.multiply(new BigDecimal(10000));
				serializer.write(decimal.longValue(), 8, "value", v -> value.toString());
			}
			case VT_FILETIME:
				((Filetime)value).serialize(serializer);
				return;
			case VT_CLSID:
				((GUID)value).serialize(serializer);
				return;
			case VT_DATE:
			{
				var calendar = (GregorianCalendar)value;
				long seconds = (calendar.getTimeInMillis() - BASE_DATE.getTimeInMillis()) / 1000L;
				double days = seconds / (double)(60 * 60 * 24);
				serializer.write(Double.doubleToLongBits(days), 8, "value", v -> value.toString());
				return;
			}
			case VT_BSTR:
			case VT_LPSTR:
			case VT_STREAM:
			case VT_STORAGE:
			case VT_STREAMED_OBJECT:
			case VT_STORED_OBJECT:
				PropertyStore.writeStringWithCharset(serializer, (String)value, m_Charset != null ? m_Charset : Charset.defaultCharset());
				return;
			case VT_LPWSTR:
			{
				try (var block = serializer.beginBlock("String (Unicode)"))
				{
					String str = (String)value;
					serializer.write(str.length() + 1, 4, "str length");
					serializer.writeUnicodeStringNullTerm(str, "str data");
					int strSize = str.length() * 2 + 2;
					int padding = (strSize % 4) != 0 ? 4 - (strSize % 4) : 0;
					for (int i = 0; i < padding; ++i)
					{
						serializer.write(0, "padding");
					}
					return;
				}
			}
			case VT_DECIMAL:
			{
				try (var block = serializer.beginBlock("Decimal", () -> value.toString()))
				{
					serializer.write(0, 2, "reserved");
					
					var decimal = (BigDecimal)value;
					int scale = decimal.scale();
					if (scale < 0 || scale > 28)
					{
						// these check really should be at the point of setting the value to the storage
						throw new IOException("BigDecimal's scale must be between 0 and 28");
					}
					serializer.write(scale, "scale");
					serializer.write(decimal.signum() == -1 ? 0x80 : 0, "sing");

					if (decimal.signum() == -1)
					{
						decimal = decimal.negate();
					}

					var bint = decimal.movePointRight(scale).toBigIntegerExact();
					if (bint.bitCount() > 96)
					{
						throw new IOException("BigDecimal must fit in 96 bits");
					}

					serializer.write(bint.shiftRight(64).longValueExact(), 4, "hi32");
					serializer.write(bint.and(BigInteger.valueOf(0xffffffffffffffffL)).longValueExact(), 8, "lo64");
					return;
				}
			}
			case VT_CF:
			case VT_BLOB:
			case VT_BLOB_OBJECT:
			{
				String name = type == VT_CF ? "Clipboard" : "Blob";
				try (var block = serializer.beginBlock(name))
				{
					byte[] blob = (byte[])value;
					serializer.write(blob.length, 4, Serializer.BLOCK_SIZE_NAME);
					serializer.write(blob, "data");
					int padding = (blob.length % 4) != 0 ? 4 - (blob.length % 4) : 0;
					for (int i = 0; i < padding; ++i)
					{
						serializer.write(0, "padding");
					}
					return;
				}
			}
			case VT_VERSIONED_STREAM:
			{
				try (var block = serializer.beginBlock("VersionedStream"))
				{
					var stream = (VersionedStreamName)value;
					stream.m_Guid.serialize(serializer);
					PropertyStore.writeStringWithCharset(serializer, stream.m_Name, m_Charset != null ? m_Charset : Charset.defaultCharset());
					return;
				}
			}
			case VT_VARIANT:
			{
				if ((valueType & VT_VECTOR) != 0 || (valueType & VT_ARRAY) != 0)
				{
					writeTypedValue(serializer, value);
				}
				return;
			}
		}
	}

	private static void writeStringWithCharset(Serializer<ByteWriter> serializer, String value, Charset charset) throws IOException
	{
		try (var block = serializer.beginBlock(String.format("String (%d)", charset != null ? charset.name() : "unknow charset"), () -> value))
		{
			byte[] bytes = charset.encode(value).array();
			serializer.write(bytes.length + 1, 4, Serializer.BLOCK_SIZE_NAME);
			serializer.write(bytes, "str data");
			serializer.write(0,"");

			int paddingSize = (bytes.length + 1) % 4 != 0 ? 4 - (bytes.length + 1) % 4 : 0;
			for (int i = 0; i < paddingSize; ++i)
			{
				serializer.write(0, "padding");
			}
		}
	}
}
