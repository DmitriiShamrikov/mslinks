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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Pattern;

import mslinks.data.ItemID;
import mslinks.data.VolumeID;

/**
 * Helper class to manipulate ShellLink properties in batches for common tasks
 * ShellLink can be used directly without helper for more detailed set up
 */
public class ShellLinkHelper {

	public enum Options {
		None,
		ForceTypeDirectory,
		ForceTypeFile,
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
	public ShellLinkHelper setNetworkTarget(String path) throws ShellLinkException {
		return setNetworkTarget(path, Options.None);
	}
	
	/**
	 * Sets LAN target path
	 * @param path is an absolute in the form '\\host\share\path\to\target'
	 * @throws ShellLinkException
	 */
	public ShellLinkHelper setNetworkTarget(String path, Options options) throws ShellLinkException {
		if (!path.startsWith("\\"))
			path = "\\" + path;
		if (!path.startsWith("\\\\"))
			path = "\\" + path;

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

			boolean forceFile = options == Options.ForceTypeFile;
			boolean forceDirectory = options == Options.ForceTypeDirectory;
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
	 * @return
	 * @throws ShellLinkException
	 */
	public ShellLinkHelper setLocalTarget(String drive, String absolutePath) throws ShellLinkException {
		return setLocalTarget(drive, absolutePath, Options.None);
	}

	/**
	 * Sets target on local computer, e.g. "C:\path\to\target"
	 * @param drive is a letter part of the path, e.g. "C" or "D"
	 * @param absolutePath is a path in the specified drive, e.g. "path\to\target"
	 * @return
	 * @throws ShellLinkException
	 */
	public ShellLinkHelper setLocalTarget(String drive, String absolutePath, Options options) throws ShellLinkException {
		link.getHeader().getLinkFlags().setHasLinkTargetIDList();
		var idList = link.createTargetIdList();
		idList.add(new ItemID().setType(ItemID.TYPE_CLSID)); // this computer - the only supported class id so far
		idList.add(new ItemID().setType(ItemID.TYPE_DRIVE).setName(drive));

		absolutePath = absolutePath.replaceAll("^(\\\\|\\/)", "");
		String absoluteTargetPath = idList.get(1).getName() + absolutePath;
		String[] path = absolutePath.split("\\\\|\\/");
		for (String i : path)
			idList.add(new ItemID().setType(ItemID.TYPE_DIRECTORY).setName(i));
		
		LinkInfo info = link.getHeader().getLinkFlags().hasLinkInfo() ? link.getLinkInfo() : link.createLinkInfo();
		info.createVolumeID().setDriveType(VolumeID.DRIVE_FIXED);
		info.setLocalBasePath(absoluteTargetPath);

		link.getHeader().getFileAttributesFlags().setDirecory();

		boolean forceFile = options == Options.ForceTypeFile;
		boolean forceDirectory = options == Options.ForceTypeDirectory;
		if (forceFile || !forceDirectory && Files.isRegularFile(Paths.get(absoluteTargetPath))) {
			link.getHeader().getFileAttributesFlags().clearDirecory();
			idList.getLast().setType(ItemID.TYPE_FILE);
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
		
		Path target = Paths.get(link.resolveTarget());
		if (!link.getHeader().getLinkFlags().hasRelativePath()) {
			Path savingDir = savingPath.getParent();
			// this will always be false on linux
			if (savingDir.getRoot().equals(target.getRoot()))
				link.setRelativePath(savingDir.relativize(target).toString());
		}
		
		if (!link.getHeader().getLinkFlags().hasWorkingDir()) {
			// this will always be false on linux
			if (Files.isRegularFile(target))
				link.setWorkingDir(target.getParent().toString());
		}
		
		link.serialize(Files.newOutputStream(savingPath));
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
	public static ShellLinkHelper createLink(String target, String linkpath) throws IOException, ShellLinkException {
		target = resolveEnvVariables(target);
		
		var helper = new ShellLinkHelper(new ShellLink());
		if (target.startsWith("\\\\")) {
			helper.setNetworkTarget(target);
		} else {
			String[] parts = target.split(":");
			if (parts.length != 2)
				throw new ShellLinkException("Wrong path '" + target + "'");
			helper.setLocalTarget(parts[0], parts[1]);
		}

		helper.saveTo(linkpath);
		return helper;
	}

	public static String resolveEnvVariables(String path) {
		for (var i : env.entrySet()) {
			String p = Pattern.quote(i.getKey());
			String r = i.getValue().replace("\\", "\\\\");
			path = Pattern.compile("%"+p+"%", Pattern.CASE_INSENSITIVE).matcher(path).replaceAll(r);
		}
		return path;
	}

	private static Map<String, String> env = System.getenv(); 
}
