package mslinks;

import io.ByteReader;
import io.ByteWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ShellLink {
	
	private boolean le;
	private ShellLinkHeader header;
	private LinkTargetIDList idlist;
	private LinkInfo info;
	
	public ShellLink(String file) throws IOException, ShellLinkException {
		this(Paths.get(file));
	}
	
	public ShellLink(File file) throws IOException, ShellLinkException {
		this(file.toPath());
	}
	
	public ShellLink(Path file) throws IOException, ShellLinkException {
		this(Files.newInputStream(file));
	}
	
	public ShellLink(InputStream in) throws IOException, ShellLinkException {
		this(new ByteReader(in));
	}
	
	private ShellLink(ByteReader data) throws ShellLinkException, IOException {
		header = new ShellLinkHeader(data);
		if (header.getLinkFlags().hasLinkTargetIDList()) 
			idlist = new LinkTargetIDList(data);
		if (header.getLinkFlags().hasLinkInfo())
			info = new LinkInfo(data);
		le = data.isLitteEndian();
	}
		
	public void serialize(OutputStream out) throws IOException {
		ByteWriter bw = new ByteWriter(out);
		if (le) bw.setLittleEndian();
		else bw.setBigEndian();
		header.serialize(bw);
		if (header.getLinkFlags().hasLinkTargetIDList())
			idlist.serialize(bw);
		if (header.getLinkFlags().hasLinkInfo())
			info.serialize(bw);
	}
	
	public ShellLinkHeader getHeader() { return header; }
	
	public LinkInfo getLinkInfo() { return info; }
	public void createLinkInfo() {
		info = new LinkInfo();
		header.getLinkFlags().setHasLinkInfo();
	}
}
