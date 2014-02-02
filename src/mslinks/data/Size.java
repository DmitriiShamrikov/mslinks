package mslinks.data;

import io.ByteWriter;

import java.io.IOException;

import mslinks.Serializable;

public class Size implements Serializable{
	private int x, y;
	
	public Size() {
		x = y = 0;
	}
	
	public Size(int _x, int _y) {
		x = _x;
		y = _y;
	}

	public int getX() {
		return x;
	}

	public Size setX(int x) {
		this.x = x;
		return this;
	}

	public int getY() {
		return y;
	}

	public Size setY(int y) {
		this.y = y;
		return this;
	}

	@Override
	public void serialize(ByteWriter bw) throws IOException {
		bw.write2bytes(x);
		bw.write2bytes(y);
	}	
}
