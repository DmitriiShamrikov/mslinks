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

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public void serialize(ByteWriter bw) throws IOException {
		bw.write2bytes(x);
		bw.write2bytes(y);
	}	
}
