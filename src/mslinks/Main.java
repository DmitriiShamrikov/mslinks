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
import java.nio.file.Path;
import java.nio.file.Paths;

import mslinks.ShellLinkHelper.Options;

public class Main {
	public static void main(String[] args) throws IOException, ShellLinkException {
		var sl = new ShellLink()
			.setWorkingDir("..")
			.setIconLocation("%SystemRoot%\\system32\\SHELL32.dll");
		sl.getHeader().setIconIndex(128);
		sl.getConsoleData()
			.setFont(mslinks.extra.ConsoleData.Font.Consolas)
			.setFontSize(24)
			.setTextColor(5);

		Path targetPath = Paths.get("pause.bat").toAbsolutePath();
		String root = targetPath.getRoot().toString();
		String pathWithoutRoot = targetPath.subpath(0, targetPath.getNameCount()).toString();

		var helper = new ShellLinkHelper(sl)
			.setLocalTarget(root, pathWithoutRoot, Options.None)
			.saveTo("testlink.lnk");
		System.out.println(sl.getWorkingDir());
		System.out.println(sl.resolveTarget());
	}
}
