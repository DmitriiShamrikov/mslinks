package mslinks;

import io.ByteReader;
import io.ByteWriter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import mslinks.data.LinkFlags;

public class ShellLink {
	
	private boolean le;
	private ShellLinkHeader header;
	private LinkTargetIDList idlist;
	private LinkInfo info;
	private String name, relativePath, workingDir, cmdArgs, iconLocation;
	
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
		LinkFlags lf = header.getLinkFlags();
		if (lf.hasLinkTargetIDList()) 
			idlist = new LinkTargetIDList(data);
		if (lf.hasLinkInfo())
			info = new LinkInfo(data);
		if (lf.hasName())
			name = data.readUnicodeString();
		if (lf.hasRelativePath())
			relativePath = data.readUnicodeString();
		if (lf.hasWorkingDir()) 
			workingDir = data.readUnicodeString();
		if (lf.hasArguments()) 
			cmdArgs = data.readUnicodeString();
		if (lf.hasIconLocation())
			iconLocation = data.readUnicodeString();
		
		le = data.isLitteEndian();
	}
		
	public void serialize(OutputStream out) throws IOException {
		LinkFlags lf = header.getLinkFlags();
		ByteWriter bw = new ByteWriter(out);
		if (le) bw.setLittleEndian();
		else bw.setBigEndian();
		header.serialize(bw);
		if (lf.hasLinkTargetIDList())
			idlist.serialize(bw);
		if (lf.hasLinkInfo())
			info.serialize(bw);
		if (lf.hasName())
			bw.writeUnicodeString(name);
		if (lf.hasRelativePath())
			bw.writeUnicodeString(relativePath);
		if (lf.hasWorkingDir()) 
			bw.writeUnicodeString(workingDir);
		if (lf.hasArguments()) 
			bw.writeUnicodeString(cmdArgs);
		if (lf.hasIconLocation())
			bw.writeUnicodeString(iconLocation);
	}
	
	public ShellLinkHeader getHeader() { return header; }
	
	public LinkInfo getLinkInfo() { return info; }
	public LinkInfo createLinkInfo() {
		info = new LinkInfo();
		header.getLinkFlags().setHasLinkInfo();
		return info;
	}
	
	public String getName() { return name; }
	public void setName(String s) {
		if (s == null) 
			header.getLinkFlags().clearHasName();
		else 
			header.getLinkFlags().setHasName();
		name = s;
	}
	
	public String getRelativePath() { return relativePath; }
	public void setRelativePath(String s) {
		if (s == null) 
			header.getLinkFlags().clearHasRelativePath();
		else 
			header.getLinkFlags().setHasRelativePath();
		relativePath = s;
	}
	
	public String getWorkingDir() { return workingDir; }
	public void setWorkingDir(String s) {
		if (s == null) 
			header.getLinkFlags().clearHasWorkingDir();
		else 
			header.getLinkFlags().setHasWorkingDir();
		workingDir = s;
	}
	
	public String getCMDArgs() { return cmdArgs; }
	public void setCMDArgs(String s) {
		if (s == null) 
			header.getLinkFlags().clearHasArguments();
		else 
			header.getLinkFlags().setHasArguments();
		cmdArgs = s;
	}
	
	public String getIconLocation() { return iconLocation; }
	public void setIconLocation(String s) {
		if (s == null) 
			header.getLinkFlags().clearHasIconLocation();
		else 
			header.getLinkFlags().setHasIconLocation();
		iconLocation = s;
	}
}
