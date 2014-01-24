package mslinks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.GregorianCalendar;

public class Main {
	public static void main(String[] args) throws IOException, ShellLinkException {
		ShellLink link = new ShellLink("testlink.lnk");
		Filetime ft = link.getWriteTime();
		System.out.println(String.format("%d:%d:%d %d.%d.%d", ft.get(GregorianCalendar.HOUR_OF_DAY), ft.get(GregorianCalendar.MINUTE), ft.get(GregorianCalendar.SECOND),
				ft.get(GregorianCalendar.DAY_OF_MONTH), ft.get(GregorianCalendar.MONTH) + 1, ft.get(GregorianCalendar.YEAR)));
		
		link.serialize(Files.newOutputStream(Paths.get("test.lnk")));
	}
}
