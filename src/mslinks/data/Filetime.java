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
package mslinks.data;

import io.ByteReader;
import io.ByteWriter;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import mslinks.Serializable;

public class Filetime extends GregorianCalendar implements Serializable {
	private long residue;
	
	public Filetime() {
		super();
	}
	
	public Filetime(ByteReader data) throws IOException {
		this(data.read8bytes());
	}
	
	public Filetime(long time) {
		long t = time / 10000;
		residue = time - t;
		setTimeInMillis(t);
		add(Calendar.YEAR, -369);
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		if (!super.equals(o))
			return false;

		var obj = (Filetime)o;
		return residue == obj.residue;
	}

	@Override
	public int hashCode()
	{
		return (int)(super.hashCode() ^ ((residue & 0xffffffff00000000l) >> 32) ^ (residue & 0xffffffffl));
	}
	
	public long toLong() {
		GregorianCalendar tmp = (GregorianCalendar)clone();
		tmp.add(Calendar.YEAR, 369);
		return tmp.getTimeInMillis() + residue;
	}

	public void serialize(ByteWriter bw) throws IOException {
		bw.write8bytes(toLong());
	}
	
	public String toString() {
		return String.format("%d:%d:%d %d.%d.%d", 
				get(Calendar.HOUR_OF_DAY), get(Calendar.MINUTE), get(Calendar.SECOND),
				get(Calendar.DAY_OF_MONTH), get(Calendar.MONTH) + 1, get(Calendar.YEAR));
	}
}
