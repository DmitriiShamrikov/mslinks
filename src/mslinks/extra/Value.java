package mslinks.extra;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;

import mslinks.data.Currency;
import mslinks.data.Date;
import mslinks.data.Decimal;
import mslinks.data.Filetime;
import mslinks.data.GUID;
import mslinks.data.Hresult;
import io.ByteReader;

/** unsigned values not impleneted */
public class Value {
	
	public static final HashMap<Integer, Method> valuetypes = new HashMap<Integer, Method>() {{
		for (Method m : Value.class.getMethods()) {
			ValueType vt = m.getAnnotation(ValueType.class);
			if (vt != null)
				put(vt.type(), m);
		}
	}};
	
	@ValueType(type = 0x0)
	public static void empty(ByteReader br) {}
	
	@ValueType(type = 0x1)
	public static void Null(ByteReader br) {}
	
	@ValueType(type = 0x2)
	public static short i2(ByteReader br) throws IOException {
		return (short)br.read2bytes();
	}
	
	@ValueType(type = 0x3)
	public static int i4(ByteReader br) throws IOException {
		return (int)br.read4bytes();
	}
	
	@ValueType(type = 0x4)
	public static float r4(ByteReader br) throws IOException {
		int t = (int)br.read4bytes();
		return Float.intBitsToFloat(t);
	}
	
	@ValueType(type = 0x5)
	public static double r8(ByteReader br) throws IOException {
		long t = br.read8bytes();
		return Double.longBitsToDouble(t);
	}
	
	@ValueType(type = 0x6)
	public static Currency currency(ByteReader br) throws IOException {
		return new Currency(br);
	}
	
	@ValueType(type = 0x7)
	public static Date date(ByteReader br) throws IOException {
		return new Date(br);
	}
	
	@ValueType(type = 0x8)
	public static String string(ByteReader br) throws IOException {
		int sz = (int)br.read4bytes();
		byte[] buf = new byte[sz];
		for (int i=0; i<sz; i++)
			buf[i] = (byte)br.read();
		return new String(buf, 0, buf[sz-1]==0? sz-1 : sz); 
	}
	
	@ValueType(type = 0xa)
	public static Hresult hresult(ByteReader br) throws IOException {
		return new Hresult(br);
	}
	
	@ValueType(type = 0xb)
	public static boolean bool(ByteReader br) throws IOException {
		 return 0 == br.read2bytes();
	}
	
	@ValueType(type = 0xe)
	public static Decimal decimal(ByteReader br) throws IOException {
		 return new Decimal(br);
	}
	
	@ValueType(type = 0x10)
	public static byte i1(ByteReader br) throws IOException {
		return (byte)br.read();
	}
	
	@ValueType(type = 0x11)
	public static byte ui1(ByteReader br) throws IOException {
		return (byte)br.read();
	}
	
	@ValueType(type = 0x12)
	public static short ui2(ByteReader br) throws IOException {
		return (short)br.read2bytes();
	}
	
	@ValueType(type = 0x13)
	public static int ui4(ByteReader br) throws IOException {
		return (int)br.read4bytes();
	}
	
	@ValueType(type = 0x14)
	public static long i8(ByteReader br) throws IOException {
		return br.read8bytes();
	}
	
	@ValueType(type = 0x15)
	public static long ui8(ByteReader br) throws IOException {
		return br.read8bytes();
	}
	
	@ValueType(type = 0x16)
	public static int _int(ByteReader br) throws IOException {
		return (int)br.read4bytes();
	}
	
	@ValueType(type = 0x17)
	public static int uint(ByteReader br) throws IOException {
		return (int)br.read4bytes();
	}
	
	@ValueType(type = 0x1e)
	public static String _string(ByteReader br) throws IOException {
		return string(br); 
	}
	
	@ValueType(type = 0x1f)
	public static String unicodeString(ByteReader br) throws IOException {
		int sz = (int)br.read4bytes();
		char[] buf = new char[sz];
		for (int i=0; i<sz; i++)
			buf[i] = (char)br.read2bytes();
		return new String(buf, 0, buf[sz-1]==0? sz-1 : sz);  
	}
	
	@ValueType(type = 0x40)
	public static Filetime filetime(ByteReader br) throws IOException {
		return new Filetime(br);
	}
	
	@ValueType(type = 0x41)
	public static byte[] blob(ByteReader br) throws IOException {
		int sz = (int)br.read4bytes();
		byte[] buf = new byte[sz];
		for (int i=0; i<sz; i++)
			buf[i] = (byte)br.read();
		return buf;
	}
	
	/** not implemented */
	@ValueType(type = 0x42)
	public static void shit1(ByteReader br) throws IOException {
		br.seek((int)br.read4bytes());
	}
	
	/** not implemented */
	@ValueType(type = 0x43)
	public static void shit2(ByteReader br) throws IOException {
		br.seek((int)br.read4bytes());
	}
	
	/** not implemented */
	@ValueType(type = 0x44)
	public static void shit3(ByteReader br) throws IOException {
		br.seek((int)br.read4bytes());
	}
	
	/** not implemented */
	@ValueType(type = 0x45)
	public static void shit4(ByteReader br) throws IOException {
		br.seek((int)br.read4bytes());
	}
	
	@ValueType(type = 0x46)
	public static byte[] blob2(ByteReader br) throws IOException {
		return blob(br);
	}
	
	/** not implemented */
	@ValueType(type = 0x47)
	public static void clipboarddata(ByteReader br) throws IOException {
		br.seek((int)br.read4bytes());
	}
	
	@ValueType(type = 0x48)
	public static GUID clsid(ByteReader br) throws IOException {
		return new GUID(br);
	}
	
	/** not implemented */
	@ValueType(type = 0x49)
	public static void shit5(ByteReader br) throws IOException {
		br.seek(16);
		br.seek((int)br.read4bytes());
	}
}


