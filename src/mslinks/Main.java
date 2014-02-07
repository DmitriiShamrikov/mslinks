package mslinks;

import java.io.IOException;
import mslinks.extra.ConsoleData.Font;

public class Main {
	public static void main(String[] args) throws IOException, ShellLinkException {			
		ShellLink sl = ShellLink.createLink("pause.bat")
				.setWorkingDir("..");
		sl.getConsoleData()
			.setFont(Font.LucindaConsole)
			.setFontSize(24)
			.setTextColor(5);
				
		sl.saveTo("testlink.lnk");
		System.out.println(sl.getWorkingDir());
		
	}
}
