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

import org.junit.Test;

import mslinks.ShellLinkHelper.Options;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Examples {
	@Test
	public void WindowsExample() throws IOException, ShellLinkException {
		assumeTrue(isRunningOnWindows());

		var sl = new ShellLink()
			.setWorkingDir(Paths.get("..").toAbsolutePath().normalize().toString())
			.setIconLocation("%SystemRoot%\\system32\\SHELL32.dll");
		sl.getHeader().setIconIndex(128);
		sl.getConsoleData()
			.setFont(mslinks.extra.ConsoleData.Font.Consolas)
			.setFontSize(24)
			.setTextColor(5);

		Path targetPath = Paths.get("pause.bat").toAbsolutePath();
		String root = targetPath.getRoot().toString();
		String pathWithoutRoot = targetPath.subpath(0, targetPath.getNameCount()).toString();

		new ShellLinkHelper(sl)
			.setLocalTarget(root, pathWithoutRoot, Options.ForceTypeFile)
			.saveTo("testlink.lnk");
		
		Path workingDir = Paths.get("").toAbsolutePath();
		assertEquals(workingDir.getParent().toString(), sl.getWorkingDir());
		assertEquals(targetPath.toString(), sl.resolveTarget());
	}

	@Test
	public void LinuxExample() throws IOException, ShellLinkException {
		assumeFalse(isRunningOnWindows());

		String workingDir = Paths.get("").toAbsolutePath().toString();
		String driveLetter = getWslDriveLetter(workingDir);
		assertNotNull("this test supposed to run in WSL", driveLetter);
		workingDir = workingDir.replace("/mnt/" + driveLetter + "/", driveLetter.toUpperCase() + ":\\").replaceAll("\\/", "\\\\");
		String linkWorkingDir = workingDir.substring(0, workingDir.lastIndexOf('\\'));

		var sl = new ShellLink()
			.setWorkingDir(linkWorkingDir)
			.setIconLocation("%SystemRoot%\\system32\\SHELL32.dll");
		sl.getHeader().setIconIndex(128);
		sl.getConsoleData()
			.setFont(mslinks.extra.ConsoleData.Font.Consolas)
			.setFontSize(24)
			.setTextColor(5);
		
		new ShellLinkHelper(sl)
			.setLocalTarget(driveLetter, workingDir + "\\" + "pause.bat", Options.ForceTypeFile)
			.saveTo("testlink.lnk");

		assertEquals(linkWorkingDir, sl.getWorkingDir());
		assertEquals(driveLetter + ":\\" + workingDir + "\\" + "pause.bat", sl.resolveTarget());
	}

	private String getWslDriveLetter(String path) {
		path = path.toLowerCase();
		for (int i = 0; i < 26; ++i) {
			String letter = Character.toString('a' + i);
			if (path.startsWith("/mnt/" + letter + "/"))
				return letter;
		}
		return null;
	}

	private boolean isRunningOnWindows() {
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}
}
