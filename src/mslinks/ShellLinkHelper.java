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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

import mslinks.data.GUID;
import mslinks.data.ItemID;
import mslinks.data.ItemIDDrive;
import mslinks.data.ItemIDFS;
import mslinks.data.ItemIDRoot;
import mslinks.data.Registry;
import mslinks.data.VolumeID;

/**
 * Helper class to perform high level operations on links
 * ShellLink can be used directly for more detailed set up
 */
public class ShellLinkHelper {

	public enum Options {
		None,
		/**
		 * Sets flags marking the target as a directory
		 */
		ForceTypeDirectory,
		/**
		 * Sets flags marking the target as a file
		 */
		ForceTypeFile,
		/**
		 * Ignore any environment variables in the target path and save it as if it's a regular path
		 */
		IgnoreEnvVars,
		/**
		 * Resolve environment variables in the target path using caller's enviroment
		 */
		@Deprecated(since = "1.1.2", forRemoval = true)
		ResolveEnvVars
	}

	protected ShellLink link;

	public ShellLinkHelper(ShellLink l) {
		link = l;
	}

	public ShellLink getLink() { return link; }

	/**
	 * Sets LAN target path
	 * @param path is an absolute in the form '\\host\share\path\to\target'
	 * @throws ShellLinkException
	 */
	public ShellLinkHelper setNetworkTarget(String path, Options ... options) throws ShellLinkException {
		if (!path.startsWith("\\"))
			path = "\\" + path;
		if (!path.startsWith("\\\\"))
			path = "\\" + path;

		var optionsList = Arrays.asList(options);
		if (!optionsList.contains(Options.IgnoreEnvVars) && hasEnvVars(path)) {
			throw new ShellLinkException("Paths with environment variables should be set using setEnvironmentVariableTarget");
		}

		int p1 = path.indexOf('\\', 2); // hostname
		int p2 = path.indexOf('\\', p1+1); // share name
		
		if (p1 != -1) {
			LinkInfo info = link.getHeader().getLinkFlags().hasLinkInfo() ? link.getLinkInfo() : link.createLinkInfo();
			if (p2 != -1) {
				info.createCommonNetworkRelativeLink().setNetName(path.substring(0, p2));
				info.setCommonPathSuffix(path.substring(p2+1));
			} else {
				info.createCommonNetworkRelativeLink().setNetName(path);
				info.setCommonPathSuffix("");
			}

			link.getHeader().getFileAttributesFlags().setDirecory();

			boolean forceFile = optionsList.contains(Options.ForceTypeFile);
			boolean forceDirectory = optionsList.contains(Options.ForceTypeDirectory);
			if (forceFile || !forceDirectory && Files.isRegularFile(Paths.get(path))) {
				link.getHeader().getFileAttributesFlags().clearDirecory();
			}
		} else {
			link.getHeader().getFileAttributesFlags().clearDirecory();
		}

		link.getHeader().getLinkFlags().setHasExpString();
		link.getEnvironmentVariable().setVariable(path);
		return this;
	}

	/**
	 * Sets target on local computer, e.g. "C:\path\to\target"
	 * @param drive is a letter part of the path, e.g. "C" or "D"
	 * @param absolutePath is a path in the specified drive, e.g. "path\to\target"
	 * @throws ShellLinkException
	 */
	public ShellLinkHelper setLocalTarget(String drive, String absolutePath, Options... options) throws ShellLinkException {
		var optionsList = Arrays.asList(options);
		if (!optionsList.contains(Options.IgnoreEnvVars) && hasEnvVars(absolutePath)) {
			throw new ShellLinkException("Paths with environment variables should be set using setEnvironmentVariableTarget");
		}

		link.getHeader().getLinkFlags().setHasLinkTargetIDList();
		var idList = link.createTargetIdList();
		// root is computer
		idList.add(new ItemIDRoot().setClsid(Registry.CLSID_COMPUTER));

		// drive
		// windows usually creates TYPE_DRIVE_MISC here but TYPE_DRIVE_FIXED also works fine
		var driveItem = new ItemIDDrive(ItemID.TYPE_DRIVE_MISC).setName(drive);
		idList.add(driveItem);

		// each segment of the path is directory
		absolutePath = absolutePath.replaceAll("^(\\\\|\\/)", "");
		String absoluteTargetPath = driveItem.getName() + absolutePath;
		String[] path = absolutePath.split("\\\\|\\/");
		for (String i : path)
			idList.add(new ItemIDFS(ItemID.TYPE_FS_DIRECTORY).setName(i));
		
		LinkInfo info = link.getHeader().getLinkFlags().hasLinkInfo() ? link.getLinkInfo() : link.createLinkInfo();
		info.createVolumeID().setDriveType(VolumeID.DRIVE_FIXED);
		info.setLocalBasePath(absoluteTargetPath);

		link.getHeader().getFileAttributesFlags().setDirecory();

		boolean forceFile = optionsList.contains(Options.ForceTypeFile);
		boolean forceDirectory = optionsList.contains(Options.ForceTypeDirectory);
		if (forceFile || !forceDirectory && Files.isRegularFile(Paths.get(absoluteTargetPath))) {
			link.getHeader().getFileAttributesFlags().clearDirecory();
			idList.getLast().setTypeFlags(ItemID.TYPE_FS_FILE);
		}

		return this;
	}

	/**
	 * Sets target relative to a special folder defined by a GUID.
	 * Use {@link Registry} class to get an available GUID by name or predefined constants.
	 * Note that you can add your own GUIDs available on your system using {@link Registry#registerClsid(String clsid, String clsid, String name, Class<?>... allowedItemIdTypes)}
	 * @param root a GUID defining a special folder, e.g. Registry.CLSID_DOCUMENTS. Must be registered in the {@link Registry}
	 * @param path a path relative to the special folder, e.g. "path\to\target"
	 * @throws ShellLinkException
	 */
	public ShellLinkHelper setSpecialFolderTarget(GUID root, String path, Options... options) throws ShellLinkException {
		var optionsList = Arrays.asList(options);
		if (!optionsList.contains(Options.ForceTypeFile) && !optionsList.contains(Options.ForceTypeDirectory)) {
			throw new ShellLinkException("The type of target is not specified");
		}

		if (!optionsList.contains(Options.IgnoreEnvVars) && hasEnvVars(path)) {
			throw new ShellLinkException("Environment variables are not supported combined with special folders");
		}
		
		link.getHeader().getLinkFlags().setHasLinkTargetIDList();
		var idList = link.createTargetIdList();
		// although later systems use ItemIDRoot(computer) + ItemIDRegFolder(root clsid) pair, always set root clsid as ItemIDRoot for simplicity
		idList.add(new ItemIDRoot().setClsid(root));

		// each segment of the path is directory
		path = path.replaceAll("^(\\\\|\\/)", "");
		String[] pathSegments = path.split("\\\\|\\/");
		for (String i : pathSegments)
			idList.add(new ItemIDFS(ItemID.TYPE_FS_DIRECTORY).setName(i));

		link.getHeader().getFileAttributesFlags().setDirecory();

		if (optionsList.contains(Options.ForceTypeFile)) {
			link.getHeader().getFileAttributesFlags().clearDirecory();
			idList.getLast().setTypeFlags(ItemID.TYPE_FS_FILE);
		}

		return this;
	}

	/**
	 * Sets target relative to desktop directory of the user opening the link. This method is universal 
	 * because it works without Registry.CLSID_DESKTOP which is available only on later systems
	 * @param path a path relative to the desktop, e.g. "path\to\target"
	 * @throws ShellLinkException
	 */
	public ShellLinkHelper setDesktopRelativeTarget(String path, Options... options) throws ShellLinkException {
		var optionsList = Arrays.asList(options);
		if (!optionsList.contains(Options.ForceTypeFile) && !optionsList.contains(Options.ForceTypeDirectory)) {
			throw new ShellLinkException("The type of target is not specified");
		}

		if (!optionsList.contains(Options.IgnoreEnvVars) && hasEnvVars(path)) {
			throw new ShellLinkException("Environment variables are not supported combined with special folders");
		}
		
		link.getHeader().getLinkFlags().setHasLinkTargetIDList();
		var idList = link.createTargetIdList();

		// no root item here

		// each segment of the path is directory
		path = path.replaceAll("^(\\\\|\\/)", "");
		String[] pathSegments = path.split("\\\\|\\/");
		for (String i : pathSegments)
			idList.add(new ItemIDFS(ItemID.TYPE_FS_DIRECTORY).setName(i));

		link.getHeader().getFileAttributesFlags().setDirecory();

		if (optionsList.contains(Options.ForceTypeFile)) {
			link.getHeader().getFileAttributesFlags().clearDirecory();
			idList.getLast().setTypeFlags(ItemID.TYPE_FS_FILE);
		}

		return this;
	}

	/**
	 * Sets target that can contain environment variables. Can be either local path or network path.
	 * @param path a path containing envronment variables, e.g. "%appdata%\path\to\target"
	 * @throws ShellLinkException
	 */
	public ShellLinkHelper setEnvironmentVariableTarget(String path, Options... options) throws ShellLinkException {
		var optionsList = Arrays.asList(options);
		link.getHeader().getLinkFlags().setHasExpString();
		link.getHeader().getLinkFlags().setPreferEnvironmentPath();
		link.getEnvironmentVariable().setVariable(path);
		if (optionsList.contains(Options.ForceTypeDirectory)) {
			link.getHeader().getFileAttributesFlags().setDirecory();
		}
		return this;
	}

	/**
	 * Serializes {@code ShellLink} to specified {@code path}. Sets appropriate relative path 
	 * and working directory if possible and if they are not already set
	 */
	public ShellLinkHelper saveTo(String path) throws IOException {
		Path savingPath = Paths.get(path).toAbsolutePath().normalize();
		if (Files.isDirectory(savingPath))
			throw new IOException("can't save ShellLink to \"" + savingPath + "\" because there is a directory with this name");

		link.setLinkFileSource(savingPath);
		
		Path savingDir = savingPath.getParent();
		try {
			Path target = Paths.get(link.resolveTarget());
			if (!link.getHeader().getLinkFlags().hasRelativePath()) {
				// this will always be false on linux
				if (savingDir.getRoot().equals(target.getRoot()))
					link.setRelativePath(savingDir.relativize(target).toString());
			}
			
			if (!link.getHeader().getLinkFlags().hasWorkingDir()) {
				// this will always be false on linux
				if (Files.isRegularFile(target))
					link.setWorkingDir(target.getParent().toString());
			}
		} catch (InvalidPathException e) {
			// skip automatic relative path and working dir if path is some special folder
		}
		
		Files.createDirectories(savingDir);
		try (var out = Files.newOutputStream(savingPath)) { 
			link.serialize(out);
		}
		return this;
	}

	/**
	 * Universal all-by-default creation of the link
	 * @param target - absolute path for the target file in windows format (e.g. C:\path\to\file.txt)
	 * @param linkpath - where to save link file
	 * @return
	 * @throws IOException
	 * @throws ShellLinkException
	 */
	public static ShellLinkHelper createLink(String target, String linkpath, Options... options) throws IOException, ShellLinkException {
		var optionsList = Arrays.asList(options);
		if (optionsList.contains(Options.ResolveEnvVars)) {
			target = resolveEnvVariables(target);
		}
		
		var helper = new ShellLinkHelper(new ShellLink());
		if (!optionsList.contains(Options.IgnoreEnvVars) && hasEnvVars(target)) {
			helper.setEnvironmentVariableTarget(target, options);
		} else if (target.startsWith("\\\\")) {
			helper.setNetworkTarget(target, options);
		} else {
			String[] parts = target.split(":");
			if (parts.length != 2)
				throw new ShellLinkException("Wrong path '" + target + "'");
			helper.setLocalTarget(parts[0], parts[1], options);
		}

		helper.saveTo(linkpath);
		return helper;
	}

	@Deprecated(since = "1.1.2", forRemoval = true)
	public static String resolveEnvVariables(String path) {
		for (var i : env.entrySet()) {
			String p = Pattern.quote(i.getKey());
			String r = i.getValue().replace("\\", "\\\\");
			path = Pattern.compile("%"+p+"%", Pattern.CASE_INSENSITIVE).matcher(path).replaceAll(r);
		}
		return path;
	}

	private static boolean hasEnvVars(String str) {
		String[] parts = str.split("\\\\");
		for (String part : parts) {
			int num = 0;
			for (int i = 0; i < part.length(); ) {
				i = part.indexOf("%", i + 1);
				if (i == -1) {
					break;
				}
				else {
					++num;
				}
			}

			if (num > 1) {
				return true;
			}
		}

		return false;
	}

	@Deprecated(since = "1.1.2", forRemoval = true)
	private static Map<String, String> env = System.getenv(); 
}
