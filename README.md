mslinks
=======

### Summary
Library for parsing and creating Windows shortcut files (.lnk)

### Description
Partial implementation of [Shell Link (.LNK) Binary File Format](http://msdn.microsoft.com/en-us/library/dd871305.aspx)

This library allows you create new and read and modify existing .lnk files. It can edit most properties of the link such as working directory, tooltip text, icon, command line arguments, hotkeys, create links to LAN shared files and directories but following features are not implemented:

* extra data blocks: Darwin, IconEnvironment, KnownFolder, PropertyStore, Shim, SpecialFolder
* some options in LinkTargetIDList because this section is not documented
* environment variables: you can use it in target path but it resolves while creating link and not stored in the lnk file

### Examples
Easiest way to create link with default parameters: `ShellLinkHelper.createLink("targetfile", "linkfile.lnk")`

Following example demonstrates creating link for .bat file and setting working directory, icon and setting up font parameters for console
```
package mslinks;

import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException {
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
		String path = targetPath.subpath(0, targetPath.getNameCount()).toString();

		new ShellLinkHelper(sl)
			.setLocalTarget(root, path, Options.ForceTypeFile)
			.saveTo("testlink.lnk");
	}
}

```

Final example creates cyclic link that blocks explorer on Windows 7 when trying to get into the containing directory
```
package mslinks;

import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException {
		var sl = new ShellLink();
		sl.getHeader().getLinkFlags().setAllowLinkToLink();

		Path targetPath = Paths.get("test.lnk").toAbsolutePath();
		String root = targetPath.getRoot().toString();
		String path = targetPath.subpath(0, targetPath.getNameCount()).toString();

		new ShellLinkHelper(sl)
			.setLocalTarget(root, path, Options.ForceTypeFile)
			.saveTo("testlink.lnk");
	}
}
```

### Download
* [releases page](https://github.com/DmitriiShamrikov/mslinks/releases)
* [Maven Central Repository](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.vatbub%22%20AND%20a%3A%22mslinks%22)
