package mslinks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.GregorianCalendar;

import mslinks.data.Filetime;

public class Main {
	public static void main(String[] args) throws IOException, ShellLinkException {
		//for (String i : Charset.availableCharsets().keySet())
		//	System.out.println(i);
		//if (true) return;
		
		ShellLink link = new ShellLink("testlink4.lnk");
		//link.setName("Test name");
		Filetime ft = link.getHeader().getWriteTime();
		System.out.println(String.format("%d:%d:%d %d.%d.%d", ft.get(GregorianCalendar.HOUR_OF_DAY), ft.get(GregorianCalendar.MINUTE), ft.get(GregorianCalendar.SECOND),
				ft.get(GregorianCalendar.DAY_OF_MONTH), ft.get(GregorianCalendar.MONTH) + 1, ft.get(GregorianCalendar.YEAR)));
		
		//link.getConsoleData().setTextColor(2);
		
		link.serialize(Files.newOutputStream(Paths.get("test.lnk")));
	}
}
