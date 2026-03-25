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
package mslinks;

import io.ByteReader;
import io.ByteWriter;
import io.Serializer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import mslinks.data.LinkFlags;
import mslinks.extra.ConsoleData;
import mslinks.extra.ConsoleFEData;
import mslinks.extra.Darwin;
import mslinks.extra.EnvironmentVariable;
import mslinks.extra.IconEnvironment;
import mslinks.extra.KnownFolder;
import mslinks.extra.Shim;
import mslinks.extra.SpecialFolder;
import mslinks.extra.Stub;
import mslinks.extra.Tracker;
import mslinks.extra.VistaIDList;

public class ShellLink {

	public static final String VERSION = "1.1.2";
	
	private static HashMap<Integer, Class<? extends Serializable>> extraTypes = new HashMap<>(Map.of(
		ConsoleData.signature, ConsoleData.class,
		ConsoleFEData.signature, ConsoleFEData.class,
		Tracker.signature, Tracker.class,
		VistaIDList.signature, VistaIDList.class,
		EnvironmentVariable.signature, EnvironmentVariable.class,
		Darwin.signature, Darwin.class,
		IconEnvironment.signature, IconEnvironment.class,
		KnownFolder.signature, KnownFolder.class,
		SpecialFolder.signature, SpecialFolder.class,
		Shim.signature, Shim.class
	));
	
	
	private ShellLinkHeader header;
	private LinkTargetIDList idlist;
	private LinkInfo info;
	private String name;
	private String relativePath;
	private String workingDir;
	private String cmdArgs;
	private String iconLocation;
	private HashMap<Integer, Serializable> extra = new HashMap<>();
	
	private Path linkFileSource;
	
	public ShellLink() {
		header = new ShellLinkHeader();
		header.getLinkFlags().setIsUnicode();
	}
	
	public ShellLink(String file) throws IOException, ShellLinkException {
		this(Paths.get(file));
	}
	
	public ShellLink(File file) throws IOException, ShellLinkException {
		this(file.toPath());
	}
	
	public ShellLink(Path file) throws IOException, ShellLinkException {
		parse(file);
	}

	private void parse(Path file) throws ShellLinkException, IOException {
		try (InputStream in = Files.newInputStream(file)) {
			parse(in);
		}
		linkFileSource = file.toAbsolutePath();
	}
	
	public ShellLink(InputStream in) throws IOException, ShellLinkException {
		parse(in);
	}

	private void parse(InputStream in) throws ShellLinkException, IOException {
		parse(new ByteReader(in));
	}

	public ShellLink(ByteReader reader) throws IOException, ShellLinkException {
		parse(reader);
	}

	private void parse(ByteReader reader) throws ShellLinkException, IOException {
		parse(new Serializer<>(reader));
	}

	public ShellLink(Serializer<ByteReader> serializer) throws IOException, ShellLinkException {
		parse(serializer);
	}
	
	private void parse(Serializer<ByteReader> serializer) throws ShellLinkException, IOException {
		header = new ShellLinkHeader(serializer);
		LinkFlags lf = header.getLinkFlags();
		if (lf.hasLinkTargetIDList()) 
			idlist = new LinkTargetIDList(serializer);
		if (lf.hasLinkInfo())
			info = new LinkInfo(serializer);
		if (lf.hasName())
			name = serializer.readUnicodeStringSizePadded("name");
		if (lf.hasRelativePath())
			relativePath = serializer.readUnicodeStringSizePadded("relativePath");
		if (lf.hasWorkingDir()) 
			workingDir = serializer.readUnicodeStringSizePadded("workingDir");
		if (lf.hasArguments()) 
			cmdArgs = serializer.readUnicodeStringSizePadded("cmdArgs");
		if (lf.hasIconLocation())
			iconLocation = serializer.readUnicodeStringSizePadded("iconLocation");
		
		while (true) {
			try (var block = serializer.beginBlock("ExtraBlock")) {
				int size = (int)serializer.read(4, Serializer.BLOCK_SIZE_NAME);
				if (size < 4) break;
				int sign = (int)serializer.read(4, "signature", v -> extraTypes.get((int)(long)v) != null ? extraTypes.get((int)(long)v).getTypeName() : "UNKNOWN");
				try {
					Class<?> cl = extraTypes.get(sign);
					if (cl == KnownFolder.class)
						extra.put(sign, new KnownFolder(serializer, size, this));
					else if (cl == SpecialFolder.class)
						extra.put(sign, new SpecialFolder(serializer, size, this));
					else if (cl != null)
						extra.put(sign, (Serializable)cl.getConstructor(Serializer.class, int.class).newInstance(serializer, size));
					else
						extra.put(sign, new Stub(serializer, size, sign));
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException	| SecurityException e) {	
					e.printStackTrace();
				}
			}
		}
	}

	public void serialize(OutputStream out) throws IOException {
		serialize(new ByteWriter(out));
	}

	public void serialize(ByteWriter bw) throws IOException {
		serialize(new Serializer<>(bw));
	}

	public void serialize(Serializer<ByteWriter> serializer) throws IOException {
		LinkFlags lf = header.getLinkFlags();
		header.serialize(serializer);
		if (lf.hasLinkTargetIDList())
			idlist.serialize(serializer);
		
		if (lf.hasLinkInfo())
			info.serialize(serializer);
		if (lf.hasName())
			serializer.writeUnicodeStringSizePadded(name, "name");
		if (lf.hasRelativePath())
			serializer.writeUnicodeStringSizePadded(relativePath, "relativePath");
		if (lf.hasWorkingDir()) 
			serializer.writeUnicodeStringSizePadded(workingDir, "workingDir");
		if (lf.hasArguments()) 
			serializer.writeUnicodeStringSizePadded(cmdArgs, "cmdArgs");
		if (lf.hasIconLocation())
			serializer.writeUnicodeStringSizePadded(iconLocation, "iconLocation");
		
		for (Serializable i : extra.values()) {
			try (var block = serializer.beginBlock("ExtraBlock")) {
				i.serialize(serializer);
			}
		}
		
		serializer.write(0, 4, Serializer.BLOCK_SIZE_NAME);
	}
	
	public ShellLinkHeader getHeader() { return header; }
	
	public LinkInfo getLinkInfo() { return info; }
	public LinkInfo createLinkInfo() {
		info = new LinkInfo();
		header.getLinkFlags().setHasLinkInfo();
		return info;
	}
	public ShellLink removeLinkInfo() {
		info = null;
		header.getLinkFlags().clearHasLinkInfo();
		return this;
	}

	public LinkTargetIDList getTargetIdList() { return idlist; }
	public LinkTargetIDList createTargetIdList() {
		if (idlist == null) {
			idlist = new LinkTargetIDList();
			header.getLinkFlags().setHasLinkTargetIDList();
		}
		return idlist;
	}
	public ShellLink removeTargetIdList() {
		idlist = null;
		header.getLinkFlags().clearHasLinkTargetIDList();
		return this;
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
		else { 
			header.getLinkFlags().setHasRelativePath();
			if (!s.startsWith("."))
				s = ".\\" + s;
		}
		relativePath = s;
		return this;
	}
	
	public String getWorkingDir() { return workingDir; }
	public ShellLink setWorkingDir(String s) {
		if (s == null) 
			header.getLinkFlags().clearHasWorkingDir();
		else {
			header.getLinkFlags().setHasWorkingDir();
		}
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
		else {
			header.getLinkFlags().setHasIconLocation();
		}
		iconLocation = s;
		return this;
	}

	public String getLanguage() { 
		return getConsoleFEData().getLanguage();
	}
	
	public ShellLink setLanguage(String s) { 
		getConsoleFEData().setLanguage(s);
		return this;
	}
	
	public ConsoleData getConsoleData() {
		return (ConsoleData)getExtraDataBlock(ConsoleData.signature);
	}
	public ShellLink removeConsoleData() {
		extra.remove(ConsoleData.signature);
		return this;
	}

	public ConsoleFEData getConsoleFEData() {
		return (ConsoleFEData)getExtraDataBlock(ConsoleFEData.signature);
	}
	public ShellLink removeConsoleFEData() {
		extra.remove(ConsoleFEData.signature);
		return this;
	}

	public EnvironmentVariable getEnvironmentVariable() {
		return (EnvironmentVariable)getExtraDataBlock(EnvironmentVariable.signature);
	}
	public ShellLink removeEnvironmentVariable() {
		extra.remove(EnvironmentVariable.signature);
		return this;
	}

	public Darwin getDarwin() {
		return (Darwin)getExtraDataBlock(Darwin.signature);
	}
	public ShellLink removeDarwin() {
		extra.remove(Darwin.signature);
		return this;
	}

	public IconEnvironment getIconEnvironment() {
		return (IconEnvironment)getExtraDataBlock(IconEnvironment.signature);
	}
	public ShellLink removeIconEnvironment() {
		extra.remove(IconEnvironment.signature);
		return this;
	}

	public KnownFolder getKnownFolder() {
		return (KnownFolder)getExtraDataBlock(KnownFolder.signature);
	}
	public ShellLink removeKnownFolder() {
		extra.remove(KnownFolder.signature);
		return this;
	}

	public Tracker getTracker() {
		return (Tracker)getExtraDataBlock(Tracker.signature);
	}
	public ShellLink removeTracker() {
		extra.remove(Tracker.signature);
		return this;
	}

	public VistaIDList getVistaIDList() {
		return (VistaIDList)getExtraDataBlock(VistaIDList.signature);
	}
	public ShellLink removeVistaIDList() {
		extra.remove(VistaIDList.signature);
		return this;
	}

	/**
	 * linkFileSource is the location where the lnk file is stored
	 * used only to build relative path and is not serialized
	 */
	public Path getLinkFileSource() { return linkFileSource; }
	public ShellLink setLinkFileSource(Path path) {
		linkFileSource = path;
		return this;
	}
	
	public String resolveTarget() {
		if (header.getLinkFlags().hasLinkTargetIDList() && idlist != null && idlist.canBuildAbsolutePath())
			return idlist.buildPath();
		
		if (header.getLinkFlags().hasLinkInfo() && info != null) {
			String path = info.buildPath();
			if (path != null)
				return path;
		}
		
		if (linkFileSource != null && header.getLinkFlags().hasRelativePath() && relativePath != null) 
			return linkFileSource.resolveSibling(relativePath).normalize().toString();

		var envBlock = (EnvironmentVariable)extra.get(EnvironmentVariable.signature);
		if (envBlock != null && !envBlock.getVariable().isEmpty())
			return envBlock.getVariable();

		if (header.getLinkFlags().hasLinkTargetIDList() && idlist != null && idlist.canBuildPath())
			return idlist.buildPath();
		
		return "<unknown>";
	}

	private Serializable getExtraDataBlock(int signature) {
		Serializable block = extra.get(signature);
		if (block == null) {
			Class<?> type = extraTypes.get(signature);
			try {
				block = (Serializable)type.getConstructor().newInstance();
				extra.put(signature, block);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException	| SecurityException e) {
				e.printStackTrace();
			}
		}
		return block;
	}

	/**
	 * @deprecated Use new ShellLinkHelper API: {@link ShellLinkHelper#saveTo(String path) }
	 */
	@Deprecated(since = "1.0.7", forRemoval = true)
	public ShellLink saveTo(String path) throws IOException {
		new ShellLinkHelper(this).saveTo(path);
		return this;
	}

	/**
	 * Set path to target file or directory. Function accepts local paths and network paths.
	 * Environment variables are accepted but resolved here and aren't kept in the link.
	 * @deprecated Use new ShellLinkHelper API: {@link ShellLinkHelper#setNetworkTarget(String path)} or {@link ShellLinkHelper#setLocalTarget(String drive, String absolutePath)}
	 */
	@Deprecated(since = "1.0.7", forRemoval = true)
	public ShellLink setTarget(String target) {
		target = ShellLinkHelper.resolveEnvVariables(target);
		String targetAbsPath = Paths.get(target).toAbsolutePath().toString();

		try {
			var helper = new ShellLinkHelper(this);
			if (targetAbsPath.startsWith("\\\\")) {
				helper.setNetworkTarget(targetAbsPath);
			} else {
				String[] parts = targetAbsPath.split(":");
				if (parts.length == 2)
					helper.setLocalTarget(parts[0], parts[1]);
			}
		} catch (ShellLinkException e) {}
		
		return this;
	}

	/**
	 * @deprecated Use new ShellLinkHelper API: {@link ShellLinkHelper#setNetworkTarget(String path)} or {@link ShellLinkHelper#setLocalTarget(String drive, String absolutePath)}
	 */
	@Deprecated(since = "1.0.7", forRemoval = true)
	public static ShellLink createLink(String target) {
		ShellLink sl = new ShellLink();
		sl.setTarget( target );
		return sl;
	}

	/**
	 * @deprecated Use new ShellLinkHelper API: {@link ShellLinkHelper#createLink(String target, String linkpath)}
	 */
	@Deprecated(since = "1.0.7", forRemoval = true)
	public static ShellLink createLink(String target, String linkpath) throws IOException {
		return createLink(target).saveTo(linkpath);
	}
}
