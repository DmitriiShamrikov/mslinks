package mslinks.data;

import io.ByteReader;
import io.ByteWriter;

import java.io.IOException;
import java.util.GregorianCalendar;

import mslinks.Serializable;

public class Date extends GregorianCalendar implements Serializable {

	public Date(ByteReader br) throws IOException {
		this(Double.longBitsToDouble(br.read8bytes()));
	}
	
	public Date(double value) {
		setTimeInMillis((long)(value * 24 * 60 * 60 * 1000));
		add(GregorianCalendar.YEAR, -71);
		add(GregorianCalendar.MONTH, 11);
		add(GregorianCalendar.DAY_OF_MONTH, 29);
	}
	
	public double toDouble() {
		add(GregorianCalendar.DAY_OF_MONTH, -29);
		add(GregorianCalendar.MONTH, -11);
		add(GregorianCalendar.YEAR, 71);
		double mill = (double)getTimeInMillis();
		return mill / 1000 / 60 / 60 / 24;  
	}
	
	public void serialize(ByteWriter bw) throws IOException {
		bw.write8bytes(Double.doubleToLongBits(toDouble()));		
	}
}
