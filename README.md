# mslinks

[![Check](https://github.com/DmitriiShamrikov/mslinks/actions/workflows/check.yml/badge.svg)](https://github.com/DmitriiShamrikov/mslinks/actions/workflows/check.yml)

## Summary

Library for parsing and creating Windows shortcut files (.lnk)

## Description

This is an implementation of [Shell Link (.LNK) Binary File Format](http://msdn.microsoft.com/en-us/library/dd871305.aspx) plus some reverse engineering. The library doesn't depend on any Windows-specific API, so it can be used in any environment.

This library allows opening existing .lnk files as well as creating new ones from scrath. You can set up many properties of the link such as working directory, command line arguments, icon, console text color, etc. The supported targets include:

* Files and directories on local filesystem with a drive letter as a root (absolute paths like `C:\path\to\target`)
* Files and directories in Samba shares (network paths like `\\host\share\path\to\target`)
* Files and directories in special Windows folders, such as Desktop, Documents, Downloads, etc. See available [GUIDs](https://github.com/DmitriiShamrikov/mslinks/wiki/GUIDs-table)
* Files and directories on local filesystem or in Samba shares with paths containing environment variables (e.g. `%appdata%\path\to\target` or `\\host\\share\%somevar%\path\to\target`)

Planned features:

* Non-filesystem targets like Control Panel, Printers, etc

The composition of classes reflect the data layout described in the format [specification](http://msdn.microsoft.com/en-us/library/dd871305.aspx), so it's recommended to take a look there if you are looking for something specific or want a detailed explanation of flags and constants. Otherwise, you can use `ShellLinkHelper` class that provides methods for some general tasks.

## Examples

The easiest way to create a link with default parameters: `ShellLinkHelper.createLink("C:\\path\\to\\targetfile", "linkfile.lnk")`

The following example shows creation of a link for a local .bat file and sets up a working directory, icon and console font. Note that, the path has to be absolute and the drive letter has to be a separate parameter to enforce Windows-style paths. For simplicity, this example uses `java.nio.file.Path` to prepare the path assuming it runs on Windows. Take a look at [Examples.java](https://github.com/DmitriiShamrikov/mslinks/blob/master/test/mslinks/Examples.java) for a Linux compatible example.

```java
package mslinks;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

The next example shows creation of a link to a text file in a network share.

```java
package mslinks;

import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException {
		var link = new ShellLinkHelper(new ShellLink());
		link.setNetworkTarget("\\\\host\\share\\testfile.txt", Options.ForceTypeFile);
		link.saveTo("testlink.lnk");
	}
}
```

The next example shows the usage of special folders. This allows setting a path relative to a system-known location which can be different for different users. See a [list](https://github.com/DmitriiShamrikov/mslinks/wiki/GUIDs-table) of available folders.

```java
package mslinks;

import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException {
		var link = new ShellLinkHelper(new ShellLink());
		link.setSpecialFolderTarget(Registry.CLSID_DOCUMENTS, "testfile.txt", Options.ForceTypeFile);
		link.saveTo("testlink.lnk");
	}
}
```

Here is an example using environment variables. Note that Windows adds absolute path to the link as soon as the link is double-clicked and the target file is successfully located (this doesn't inlude inspecting link properties). The EV path still works at this point if the EV value changes but it's not very stable. If you want to avoid this kind of behaviour and prevent Windows changing links, you can set a read-only flag on the link file

```java
package mslinks;

import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException {
		var link = new ShellLinkHelper(new ShellLink());
		link.setEnvironmentVariableTarget("%appdata%\\path\\to\\testfile.txt", Options.ForceTypeFile);
		link.saveTo("testlink.lnk");
		new java.io.File("testlink.lnk").setReadOnly();
	}
}
```

## FAQ

Q: How do I make the target file run as administrator?<br/>
A: `link.getHeader().getLinkFlags().setRunAsUser()`

Q: Can I have a link to another link?<br/>
A: Yes. Set this flag: `link.getHeader().getLinkFlags().setAllowLinkToLink()`

Q: Can I create a link with a relative (to the link file) path?<br/>
A: Unfortunately, no. Despite ShellLink having a relative path field, it doesn't work on its own. I haven't found a way to setup a link with relative path only.

Q: Can I see what values are actually being read or written?<br/>
A: You can manually create a Serializer object with logging enabled (STDOUT). E.g.:
```
boolean enableLogging = true;
var serializer = new Serializer<>(new ByteReader(Files.newInputStream(Path.of("linkfile.lnk"))), enableLogging);
var link = new ShellLink(serializer);
```

Q: I created a link from Windows but when I open it in mslinks, the target path is in a wrong encoding.<br/>
A: This could happen when Windows locale doesn't match the default encoding of your JVM.
Unicode characters are allowed in path, however Windows does not use Unicode format *if the characters can be encoded with the system locale* and it doesn't keep codepage information inside .lnk files. Before Java 18 the default encoding was the same as system locale, since Java 18 UTF-8 is the default encoding (unless you override it with `-Dfile.encoding` JVM arg). mslinks always forces Unicode when saving links.

Q: I have a link with a an unsupported GUID. Is there a way around it?<br/>
A: If you are sure that the GUID corresponds to a valid known folder you can add it manually using `Registry.registerClsid("{GUID}", "FolderName");`

## Download

* [releases page](https://github.com/DmitriiShamrikov/mslinks/releases)
* [Maven Central Repository](https://central.sonatype.com/artifact/org.jabref/mslinks/versions)
