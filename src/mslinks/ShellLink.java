package mslinks;

import io.ByteReader;
import io.ByteWriter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import mslinks.data.ConsoleData;
import mslinks.data.ConsoleFEData;
import mslinks.data.LinkFlags;

public class ShellLink {
	private static HashMap<Integer, Class> extraTypes = new HashMap<Integer, Class>() {{
		put(ConsoleData.signature, ConsoleData.class);
		put(ConsoleFEData.signature, ConsoleFEData.class);
	}};
	
	
	private boolean le;
	private ShellLinkHeader header;
	private LinkTargetIDList idlist;
	private LinkInfo info;
	private String name, relativePath, workingDir, cmdArgs, iconLocation;
	private HashMap<Integer, Serializable> extra = new HashMap<>();
	
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
				
		while (true) {
			int size = (int)data.read4bytes();
			if (size < 4) break;
			int sign = (int)data.read4bytes();
			try {
				Class cl = extraTypes.get(sign);
				if (cl != null)
					extra.put(sign, (Serializable)cl.getConstructor(ByteReader.class, int.class).newInstance(data, size));
				else 
					data.seek(size - 8);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException	| SecurityException e) {	
				e.printStackTrace();
			}			
		}
		
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
		
		for (Serializable i : extra.values())
			i.serialize(bw);
		bw.write4bytes(0);
	}
	
	public ShellLinkHeader getHeader() { return header; }
	
	public LinkInfo getLinkInfo() { return info; }
	public LinkInfo createLinkInfo() {
		info = new LinkInfo();
		header.getLinkFlags().setHasLinkInfo();
		return info;
	}
	
	public String getName() { return name; }
	public ShellLink setName(String s) {
		if (s == null) 
			header.getLinkFlags().clearHasName();
		else 
			header.getLinkFlags().setHasName();
		name = s;
		return this;
	}
	
	public String getRelativePath() { return relativePath; }
	public ShellLink setRelativePath(String s) {
		if (s == null) 
			header.getLinkFlags().clearHasRelativePath();
		else 
			header.getLinkFlags().setHasRelativePath();
		relativePath = s;
		return this;
	}
	
	public String getWorkingDir() { return workingDir; }
	public ShellLink setWorkingDir(String s) {
		if (s == null) 
			header.getLinkFlags().clearHasWorkingDir();
		else 
			header.getLinkFlags().setHasWorkingDir();
		workingDir = s;
		return this;
	}
	
	public String getCMDArgs() { return cmdArgs; }
	public ShellLink setCMDArgs(String s) {
		if (s == null) 
			header.getLinkFlags().clearHasArguments();
		else 
			header.getLinkFlags().setHasArguments();
		cmdArgs = s;
		return this;
	}
	
	public String getIconLocation() { return iconLocation; }
	public ShellLink setIconLocation(String s) {
		if (s == null) 
			header.getLinkFlags().clearHasIconLocation();
		else 
			header.getLinkFlags().setHasIconLocation();
		iconLocation = s;
		return this;
	}
	
	public ConsoleData getConsoleData() {
		ConsoleData cd = (ConsoleData)extra.get(ConsoleData.signature);
		if (cd == null) {
			cd = new ConsoleData();
			extra.put(ConsoleData.signature, cd);
		}
		return cd;
	}
	
	public String getLanguage() { 
		ConsoleFEData cd = (ConsoleFEData)extra.get(ConsoleFEData.signature);
		if (cd == null) {
			cd = new ConsoleFEData();
			extra.put(ConsoleFEData.signature, cd);
		}
		return cd.getLanguage();
	}
	
	public ShellLink setLanguage(String s) { 
		ConsoleFEData cd = (ConsoleFEData)extra.get(ConsoleFEData.signature);
		if (cd == null) {
			cd = new ConsoleFEData();
			extra.put(ConsoleFEData.signature, cd);
		}
		cd.setLanguage(s);
		return this;
	}
}
