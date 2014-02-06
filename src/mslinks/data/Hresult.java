package mslinks.data;

import io.ByteReader;
import io.ByteWriter;

import java.io.IOException;

import mslinks.Serializable;

public class Hresult implements Serializable {
	
	boolean ok;
	int facility, error;
	
	public Hresult(ByteReader br) throws IOException {
		this((int)br.read4bytes());
	}
	
	public Hresult(int n) {
		ok = (n & 1) == 0;
		facility = (n & 0xffe0) >> 5;
		error = n >>> 16;
	}
	
	public boolean isOk() { return ok; }
	public Hresult setOk(boolean v) { ok = v; return this; }
	
	public int getFacility() { return facility; }
	public Hresult setFacility(int v) { facility = v; return this; }
	
	public int getError() { return error; }
	public Hresult setError(int v) { error = v; return this; }

	@Override
	public void serialize(ByteWriter bw) throws IOException {
		bw.write4bytes((ok? 1 : 0) | (facility << 5) | (error << 16));
	}

}
