package mslinks.extra;

import io.ByteReader;
import io.ByteWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Currency;
import java.util.HashMap;

import mslinks.Serializable;
import mslinks.ShellLinkException;
import mslinks.data.*;


public class PropertyStore extends HashMap implements Serializable {
	
	public static final int signature = 0xA0000009;
	public static final int version = 0x53505331;
	public static final GUID stringGUID = new GUID("{D5CDD505-2E9C-101B-9397-08002B2CF9AE}");
		
	private boolean strings;
	
	public PropertyStore() {
		super();
	}	
	
	public PropertyStore(ByteReader br, int size) throws IOException, ShellLinkException {
		if (size < 0xc) 
			throw new ShellLinkException();
		int pos = br.getPosition();
		int sz = (int)br.read4bytes();
		int ver = (int)br.read4bytes();
		if (ver != version)
			throw new ShellLinkException("unsupported version of property storage");
		GUID formatid = new GUID(br);
		boolean str = formatid.equals(stringGUID);
		
		while (true) {
			int vs = (int)br.read4bytes();
			if (vs == 0) break;
			Object key;
			
			if (str) {
				int ns = (int)br.read4bytes();
				br.read(); //reserved;
				key = br.readUnicodeString(ns);
			} else {
				key = (int)br.read4bytes();
				br.read(); //reserved;
			}
			
			int type = (int)br.read2bytes();
			int gtype = type >> 8;
			type &= 0xff;
			br.read2bytes();
			try {
				int p = br.getPosition();
				if (gtype == 0) {
					Method m = Value.valuetypes.get(type);
					put(key, m.invoke(null, br));
				} else if (gtype == 1) {
					int len = (int)br.read4bytes();
					Object[] vector = new Object[len];
					Method m = Value.valuetypes.get(type);
					for (int i=0; i<len; i++)
						vector[i] = m.invoke(null, br);
					put(key, vector);
				} else {
					br.read4bytes(); // shit
					int numd = (int)br.read4bytes(); //1-31
					int[] sizes = new int[numd];
					for (int i=0; i<numd; i++) {
						sizes[i] = (int)br.read4bytes();
						br.read4bytes(); // shit
					}
					
					Object[][] arr = new Object[numd][];
					Method m = Value.valuetypes.get(type);
					for (int i=0; i<numd; i++) {
						arr[i] = new Object[sizes[i]];
						for (int j=0; j<sizes[i]; j++)
							arr[i][j] = m.invoke(null, br);
					}
					put(key, arr);
				}
				// padding
				int len = br.getPosition() - p;
				br.seek((4 - (len % 4)) % 4);
			} catch (Exception e) { e.printStackTrace(); }
		}
		
		br.seek(pos + sz - br.getPosition());
	}

	@Override
	public void serialize(ByteWriter bw) throws IOException {
		int size = 24;
		for (Object k : keySet()) {
			
		}
	}
	
	private byte[] serializeValue(boolean le, Object k) throws IOException {
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		ByteWriter bw = new ByteWriter(ba);
		if (le) bw.setLittleEndian();
		else bw.setBigEndian();
		
		
		// а как же векторы и массивы?
		int sz = 9;
		if (strings)
			sz += ((String)k).length() * 2 + 2;
		Object v = get(k);
		sz += 4;
		int pad = 0;
		if (v != null) {
			int s = sizeof(v);			
			pad = (4 - (s % 4)) % 4;
			sz += s + pad;
		}
		
		bw.write4bytes(sz);
		bw.write4bytes(strings ? ((String)k).length() : (int)k);
		bw.write(0);
		
		for (Method m : Value.class.getMethods()) {
			ValueType vt = m.getAnnotation(ValueType.class);
			if (vt != null && m.getReturnType().isAssignableFrom(v.getClass())) {
				bw.write2bytes(vt.type());
				bw.write2bytes(0);
				break;
			}				
		}
		
		if (strings) 
			bw.writeUnicodeString((String)k, true);
		
		if (v instanceof Byte)
			bw.write(((byte)v) & 0xff);
		else if (v instanceof Short)
			bw.write2bytes((short)v);
		else if (v instanceof Integer)
			bw.write4bytes((int)v);
		else if (v instanceof Long) 
			bw.write8bytes((long)v);
		else if (v instanceof Boolean) {
			if ((boolean)v)
				bw.write2bytes(0xffff);
			else 
				bw.write2bytes(0);
		} else if (v instanceof String) {
			bw.write4bytes(((String)v).length());
			bw.writeUnicodeString((String)v, true);
		} else if (v instanceof byte[]) {
			bw.write4bytes(((byte[])v).length);
			bw.writeBytes((byte[])v);
		} else ((Serializable)v).serialize(bw);
		
		for (int i=0; i<pad; i++)
			bw.write(0);
		
		return ba.toByteArray();
	}
	
	private int sizeof(Object v) {
		if (v.getClass().isArray()) {
			if (v instanceof byte[])
				return 4 + ((byte[])v).length;
			
			Object[] arr = (Object[])v;
			if (arr.length == 0) return 0;
			if (arr instanceof Object[][]) {
				int sz = 8;
				for (int i=0; i<arr.length; i++)
					sz += 4 + sizeof(arr[i]);
					
			} else {
				int sum = 0;
				for (int i=0; i<arr.length; i++)
					sum += sizeof(arr[i]);
				
				return 4 + sum;				
			}
		}
		
		if (v instanceof Byte)
			return 1;
		if (v instanceof Short || v instanceof Boolean)
			return 2;
		if (v instanceof Integer || v instanceof Hresult)
			return 4;
		if (v instanceof Long || v instanceof mslinks.data.Currency || v instanceof Date || v instanceof Filetime)
			return 8;
		if (v instanceof GUID || v instanceof Decimal)
			return 16;
		if (v instanceof String)
			return 4 + ((String)v).length() * 2 + 2;
		
		return 0;
	}
	
	public boolean isStringNames() {
		return strings;
	}
	
	public Object put(Object key, Object value) {
		boolean s = key instanceof String;
		if (size() == 0 || s == strings) {
			strings = s;
			return super.put(key, value);
		}		
		throw new RuntimeException("all keys in property storage must be the same type");
	}
	
}
