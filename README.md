mslinks
=======
Library for parsing and creating Windows shortcut files (.lnk)
***

Partial implementation of [Shell Link (.LNK) Binary File Format](http://msdn.microsoft.com/en-us/library/dd871305.aspx)

You can edit most properties of the link such as working directory, tooltip text, icon, command line arguments, hotkeys, create links to LAN shared files and directories but followed features not implemented:

* extra data blocks: Darwin, IconEnvironment, KnownFolder, PropertyStore, Shim, SpecialFolder
* most options in LinkTargetIDList because it not documented, only key parts for resolving links implemented, others are zero stub
* relative path property not working, dont know why =(
* you can use environment variables and relative paths, but it resolves at creation time and not stores in the lnk file

Easiest way to create link with default parameters: `ShellLink.createLink("targetfile", "linkfile.lnk")`

Next sample demonstrates creating link for .bat file with setting working directory and tune font parameters for console
```
package mslinks;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {			
		ShellLink sl = ShellLink.createLink("pause.bat")
			.setWorkingDir("..");
		sl.getConsoleData()
			.setFont(mslinks.extra.ConsoleData.Font.Consolas)
			.setFontSize(24)
			.setTextColor(5);
				
		sl.saveTo("testlink.lnk");
		System.out.println(sl.getWorkingDir());
		System.out.println(sl.resolveTarget());
	}
}

```
