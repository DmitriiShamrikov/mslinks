package mslinks;

import io.ByteReader;
import io.ByteWriter;

import java.io.IOException;
import java.util.LinkedList;

public class LinkTargetIDList implements Serializable {
	private LinkedList<byte[]> list = new LinkedList<>();
	
	public LinkTargetIDList(ByteReader data) throws IOException, ShellLinkException {
		int size = (int)data.read2bytes();
		
		int pos = data.getPosition(); 
		
		int s = (int)data.read2bytes();
		while (s != 0) {
			s -= 2;
			byte[] b = new byte[s];
			for (int i=0; i<s; i++)
				b[i] = (byte)data.read();
			list.add(b);
			s = (int)data.read2bytes();
		}
		
		pos = data.getPosition() - pos;
		if (pos != size) 
			throw new ShellLinkException();
	}

	public void serialize(ByteWriter bw) throws IOException {
		int size = 2;
		for (byte[] i : list)
			size += i.length + 2;
		bw.write2bytes(size);
		for (byte[] i : list) {
			bw.write2bytes(i.length + 2);
			for (byte j : i)
				bw.write(j);
		}
		bw.write2bytes(0);
	}
}
