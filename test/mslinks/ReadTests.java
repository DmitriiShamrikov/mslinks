/*
	https://github.com/DmitriiShamrikov/mslinks
	
	Copyright (c) 2022 Dmitrii Shamrikov

	Licensed under the WTFPL
	You may obtain a copy of the License at
 
	http://www.wtfpl.net/about/
 
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/
package mslinks;

import org.junit.Test;

import io.ByteReader;
import mslinks.data.CNRLink;
import mslinks.data.VolumeID;
import mslinks.extra.ConsoleData;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class ReadTests {
	private ShellLink createLink(byte[] data) throws IOException, ShellLinkException {
		var reader = new ByteReader(new ByteArrayInputStream(data));
		reader.setLittleEndian();
		return new ShellLink(reader);
	}

	// =====================================
	// === General link properties tests ===
	// =====================================

	@Test
	public void TestLinkProperties() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.consolelink);

		String expectedTarget = "C:\\linktest\\folder\\pause.bat";

		assertTrue(link.getTargetIdList().canBuildAbsolutePath());
		assertEquals(expectedTarget, link.getTargetIdList().buildPath());
		assertEquals(expectedTarget, link.resolveTarget());
		assertEquals("%SystemRoot%\\System32\\SHELL32.dll", link.getIconLocation());
		assertEquals("cmdarg", link.getCMDArgs());
		assertEquals("C:\\Windows", link.getWorkingDir());
		assertEquals(".\\folder\\pause.bat", link.getRelativePath());
		assertEquals(null, link.getName());
	}

	@Test
	public void TestLinkHeaderProperties() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.consolelink);
		var header = link.getHeader();

		assertEquals(ShellLinkHeader.SW_SHOWNORMAL, header.getShowCommand());
		assertEquals(128, header.getIconIndex());
		assertEquals(20, header.getFileSize());
		assertEquals("22:09:50 13.06.2022", header.getWriteTime().toString());
		assertEquals("22:09:55 13.06.2022", header.getAccessTime().toString());
		assertEquals("22:09:18 13.06.2022", header.getCreationTime().toString());
	}

	@Test
	public void TestLinkFlags() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.consolelink);
		var flags = link.getHeader().getLinkFlags();

		assertTrue(flags.hasLinkTargetIDList());
		assertTrue(flags.hasLinkInfo());
		assertFalse(flags.hasName());
		assertTrue(flags.hasRelativePath());
		assertTrue(flags.hasWorkingDir());
		assertTrue(flags.hasArguments());
		assertTrue(flags.hasIconLocation());
		assertTrue(flags.isUnicode());
		assertFalse(flags.forceNoLinkInfo());
		assertFalse(flags.hasExpString());
		assertFalse(flags.runInSeparateProcess());
		assertFalse(flags.hasDarwinID());
		assertFalse(flags.runAsUser());
		assertFalse(flags.hasExpIcon());
		assertFalse(flags.noPidlAlias());
		assertFalse(flags.runWithShimLayer());
		assertFalse(flags.forceNoLinkTrack());
		assertTrue(flags.enableTargetMetadata());
		assertFalse(flags.disableLinkPathTracking());
		assertFalse(flags.disableKnownFolderTracking());
		assertFalse(flags.disableKnownFolderAlias());
		assertFalse(flags.allowLinkToLink());
		assertFalse(flags.unaliasOnSave());
		assertFalse(flags.preferEnvironmentPath());
		assertFalse(flags.keepLocalIDListForUNCTarget());
	}

	@Test
	public void TestFileAttributesFlags() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.consolelink);
		var flags = link.getHeader().getFileAttributesFlags();

		assertFalse(flags.isReadonly());
		assertFalse(flags.isHidden());
		assertFalse(flags.isSystem());
		assertFalse(flags.isDirecory());
		assertTrue(flags.isArchive());
		assertFalse(flags.isNormal());
		assertFalse(flags.isTemporary());
		assertFalse(flags.isSparseFile());
		assertFalse(flags.isReparsePoint());
		assertFalse(flags.isCompressed());
		assertFalse(flags.isOffline());
		assertFalse(flags.isNotContentIndexed());
		assertFalse(flags.isEncypted());
	}

	@Test
	public void TestLinkInfo() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.consolelink);
		var linkInfo = link.getLinkInfo();

		assertEquals("C:\\linktest\\folder\\pause.bat", linkInfo.buildPath());
		assertEquals("C:\\linktest\\folder\\pause.bat", linkInfo.getLocalBasePath());
		assertEquals(null, linkInfo.getCommonPathSuffix());
		assertNull(linkInfo.getCommonNetworkRelativeLink());

		var vid = linkInfo.getVolumeID();
		assertEquals(VolumeID.DRIVE_FIXED, vid.getDriveType());
		assertEquals(-159486725, vid.getSerialNumber());
		assertEquals("", vid.getLabel());
	}

	// =====================================
	// ======= Extra stuff tests ===========
	// =====================================

	@Test
	public void TestConsoleProperties() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.consolelink);
		var consoleData = link.getConsoleData();

		int[] colors = {
			ConsoleData.rgb(12, 12, 12),
			ConsoleData.rgb(0, 55, 218),
			ConsoleData.rgb(19, 161, 14),
			ConsoleData.rgb(58, 150, 221),
			ConsoleData.rgb(197, 15, 31),
			ConsoleData.rgb(136, 23, 152),
			ConsoleData.rgb(193, 156, 0),
			ConsoleData.rgb(204, 204, 204),
			ConsoleData.rgb(118, 118, 118),
			ConsoleData.rgb(59, 120, 255),
			ConsoleData.rgb(22, 198, 12),
			ConsoleData.rgb(97, 214, 214),
			ConsoleData.rgb(231, 72, 86),
			ConsoleData.rgb(180, 0, 158),
			ConsoleData.rgb(249, 241, 165),
			ConsoleData.rgb(242, 242, 242),
		};
		assertArrayEquals(colors, consoleData.getColorTable());

		assertEquals(2, consoleData.getTextColor());
		assertEquals(0, consoleData.getTextBackground());
		assertEquals(5, consoleData.getPopupTextColor());
		assertEquals(15, consoleData.getPopupTextBackground());

		assertEquals(80, consoleData.getBufferSize().getX());
		assertEquals(9000, consoleData.getBufferSize().getY());
		assertEquals(80, consoleData.getWindowSize().getX());
		assertEquals(20, consoleData.getWindowSize().getY());
		assertEquals(0, consoleData.getWindowPos().getX());
		assertEquals(0, consoleData.getWindowPos().getY());

		assertEquals(18, consoleData.getFontSize());
		assertEquals(ConsoleData.Font.LucidaConsole, consoleData.getFont());
		assertEquals(ConsoleData.CursorSize.Small, consoleData.getCursorSize());

		assertEquals(50, consoleData.getHistorySize());
		assertEquals(4, consoleData.getHistoryBuffers());

		var flags = consoleData.getConsoleFlags();
		assertFalse(flags.isBoldFont());
		assertFalse(flags.isFullscreen());
		assertTrue(flags.isQuickEdit());
		assertTrue(flags.isInsertMode());
		assertTrue(flags.isAutoPosition());
		assertFalse(flags.isHistoryDup());
	}

	@Test
	public void TestExtras() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.consolelink);
		
		var env = link.getEnvironmentVariable();
		assertEquals("", env.getVariable());

		var tracker = link.getTracker();
		assertEquals("desktop-4c56d9j", tracker.getNetbiosName());

		var vista = link.getVistaIDList();
		assertEquals("", vista.toString());
	}

	// =====================================
	// ======= Special cases tests =========
	// =====================================

	@Test
	public void TestRunAsAdmin() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.consolelink_admin);
		var flags = link.getHeader().getLinkFlags();
		assertTrue(flags.runAsUser());
	}

	@Test
	public void TestExeLinkProperties() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.exelink);

		String expectedTarget = "C:\\Program Files\\7-Zip\\7zFM.exe";

		assertTrue(link.getTargetIdList().canBuildAbsolutePath());
		assertEquals(expectedTarget, link.getTargetIdList().buildPath());
		assertEquals(expectedTarget, link.resolveTarget());
		assertEquals(null, link.getIconLocation());
		assertEquals("C:\\Program Files\\7-Zip", link.getWorkingDir());
		assertEquals("..\\Program Files\\7-Zip\\7zFM.exe", link.getRelativePath());
	}

	@Test
	public void TestMediaFileLinkProperties() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.textfilelink);

		String expectedTarget = "C:\\linktest\\folder\\textfile.txt";

		assertTrue(link.getTargetIdList().canBuildAbsolutePath());
		assertEquals(expectedTarget, link.getTargetIdList().buildPath());
		assertEquals(expectedTarget, link.resolveTarget());
		assertEquals(null, link.getIconLocation());
		assertEquals("C:\\linktest\\folder", link.getWorkingDir());
		assertEquals(".\\folder\\textfile.txt", link.getRelativePath());
	}

	@Test
	public void TestUnicodePath() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.unicodetest);

		String expectedTarget = "C:\\linktest\\folder\\\u03B1\u03B1\u03B1.txt";

		assertTrue(link.getTargetIdList().canBuildAbsolutePath());
		assertEquals(expectedTarget, link.getTargetIdList().buildPath());
		assertEquals(expectedTarget, link.resolveTarget());
	}

	@Test
	public void TestNetworkSharePath() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.sharedfilelink);
		var header = link.getHeader();
		var linkInfo = link.getLinkInfo();

		String expectedTarget = "\\\\LAPTOP\\SHARE\\testfile.txt";

		assertFalse(header.getLinkFlags().hasLinkTargetIDList());
		assertNull(link.getTargetIdList());

		assertTrue(header.getLinkFlags().hasLinkInfo());
		assertNotNull(linkInfo);

		assertEquals(expectedTarget, linkInfo.buildPath());
		assertEquals(expectedTarget, link.resolveTarget());
	}

	@Test
	public void TestNetworkShareLinkProperties() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.sharedfilelink);
		var linkInfo = link.getLinkInfo();

		assertNull(linkInfo.getVolumeID());
		assertEquals(null, linkInfo.getLocalBasePath());
		assertEquals("testfile.txt", linkInfo.getCommonPathSuffix());

		var netLink = linkInfo.getCommonNetworkRelativeLink();
		assertNotNull(netLink);
		assertEquals(CNRLink.WNNC_NET_DECORB, netLink.getNetworkType());
		assertEquals("\\\\LAPTOP\\SHARE", netLink.getNetName());
		assertEquals(null, netLink.getDeviceName());
	}

	@Test
	public void TestNetworkDrivePath() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.networkdrivefilelink);

		String expectedTarget = "Z:\\testfile.txt";

		assertTrue(link.getTargetIdList().canBuildAbsolutePath());
		assertEquals(expectedTarget, link.getTargetIdList().buildPath());
		assertEquals(expectedTarget, link.resolveTarget());
	}

	@Test
	public void TestNetworkDriveLinkProperties() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.networkdrivefilelink);
		var linkInfo = link.getLinkInfo();

		String expectedTarget = "\\\\laptop\\share\\testfile.txt";

		assertNull(linkInfo.getVolumeID());
		assertEquals(null, linkInfo.getLocalBasePath());
		assertEquals("testfile.txt", linkInfo.getCommonPathSuffix());
		assertEquals(expectedTarget, linkInfo.buildPath());

		var netLink = linkInfo.getCommonNetworkRelativeLink();
		assertNotNull(netLink);
		assertEquals(CNRLink.WNNC_NET_DECORB, netLink.getNetworkType());
		assertEquals("\\\\laptop\\share", netLink.getNetName());
		assertEquals("Z:", netLink.getDeviceName());
	}

	@Test
	public void TestNetworkShareUnicodePath() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.sharedunicodelink);
		var header = link.getHeader();
		var linkInfo = link.getLinkInfo();

		String expectedTarget = "\\\\LAPTOP\\\u0391\u0391\u0391\\\u03B1\u03B1\u03B1.txt";

		assertFalse(header.getLinkFlags().hasLinkTargetIDList());
		assertNull(link.getTargetIdList());

		assertTrue(header.getLinkFlags().hasLinkInfo());
		assertNotNull(linkInfo);

		assertEquals(expectedTarget, linkInfo.buildPath());
		assertEquals(expectedTarget, link.resolveTarget());
	}

	@Test
	public void TestDirectoryLink() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.directorylink);

		String expectedTarget = "C:\\linktest\\folder\\";

		assertTrue(link.getHeader().getFileAttributesFlags().isDirecory());
		assertTrue(link.getTargetIdList().canBuildAbsolutePath());
		assertEquals(expectedTarget, link.getTargetIdList().buildPath());
		assertEquals(expectedTarget, link.resolveTarget());
	}

	@Test
	public void TestWindowsXPLink() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.consolelink_winxp);

		String expectedTarget = "C:\\linktest\\folder\\pause.bat";

		assertTrue(link.getTargetIdList().canBuildAbsolutePath());
		assertEquals(expectedTarget, link.getTargetIdList().buildPath());
		assertEquals(expectedTarget, link.resolveTarget());
		assertEquals("%SystemRoot%\\system32\\SHELL32.dll", link.getIconLocation());
		assertEquals(null, link.getCMDArgs());
		assertEquals("C:\\Windows", link.getWorkingDir());
		assertEquals(".\\folder\\pause.bat", link.getRelativePath());
		assertEquals(null, link.getName());
	}

	@Test
	public void TestDesktopScreenLinkWin10() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.desktop_win10);
		
		String expectedTarget = "<Desktop>\\pause.bat";

		assertFalse(link.getTargetIdList().canBuildAbsolutePath());
		assertTrue(link.getTargetIdList().canBuildPath());
		assertEquals(1, link.getTargetIdList().size());
		assertEquals(expectedTarget, link.getTargetIdList().buildPath());
		assertEquals("C:\\Users\\admin\\Desktop\\pause.bat", link.resolveTarget());
	}

	@Test
	public void TestDesktopFolderLinkWin10() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.desktop_win10_folder);
		
		String expectedTarget = "<Desktop>\\pause.bat";

		assertFalse(link.getTargetIdList().canBuildAbsolutePath());
		assertTrue(link.getTargetIdList().canBuildPath());
		assertEquals(3, link.getTargetIdList().size());
		assertEquals(expectedTarget, link.getTargetIdList().buildPath());
		assertEquals("C:\\Users\\admin\\Desktop\\pause.bat", link.resolveTarget());
	}

	@Test
	public void TestDesktopScreenLinkWinXP() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.desktop_winxp);
		
		String expectedTarget = "<Desktop>\\pause.bat";
		String absoluteTarget = "C:\\Documents and Settings\\admin\\\u0420\u0430\u0431\u043e\u0447\u0438\u0439 \u0441\u0442\u043e\u043b\\pause.bat";
		absoluteTarget = new String(absoluteTarget.getBytes(Charset.forName("windows-1251")));

		assertFalse(link.getTargetIdList().canBuildAbsolutePath());
		assertTrue(link.getTargetIdList().canBuildPath());
		assertEquals(1, link.getTargetIdList().size());
		assertEquals(expectedTarget, link.getTargetIdList().buildPath());
		assertEquals(absoluteTarget, link.resolveTarget());
	}

	@Test
	public void TestDesktopFolderLinkWinXP() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.desktop_winxp_folder);
		
		String expectedTarget = "C:\\Documents and Settings\\admin\\\u0420\u0430\u0431\u043e\u0447\u0438\u0439 \u0441\u0442\u043e\u043b\\pause.bat";

		assertTrue(link.getTargetIdList().canBuildAbsolutePath());
		assertTrue(link.getTargetIdList().canBuildPath());
		assertEquals(6, link.getTargetIdList().size());
		assertEquals(expectedTarget, link.getTargetIdList().buildPath());
		assertEquals(expectedTarget, link.resolveTarget());
	}

	@Test
	public void TestDocumentsLinkWin10() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.documents_win10);
		
		String expectedTarget = "<LocalDocuments>\\pause.bat";

		assertFalse(link.getTargetIdList().canBuildAbsolutePath());
		assertTrue(link.getTargetIdList().canBuildPath());
		assertEquals(3, link.getTargetIdList().size());
		assertEquals(expectedTarget, link.getTargetIdList().buildPath());
		assertEquals("C:\\Users\\admin\\Documents\\pause.bat", link.resolveTarget());
	}

	@Test
	public void TestDocumentsLinkWinXP() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.documents_winxp);
		
		String expectedTarget = "<Documents>\\pause.bat";
		String absoluteTarget = "C:\\Documents and Settings\\admin\\\u041c\u043e\u0438 \u0434\u043e\u043a\u0443\u043c\u0435\u043d\u0442\u044b\\pause.bat";
		absoluteTarget = new String(absoluteTarget.getBytes(Charset.forName("windows-1251")));

		assertFalse(link.getTargetIdList().canBuildAbsolutePath());
		assertTrue(link.getTargetIdList().canBuildPath());
		assertEquals(2, link.getTargetIdList().size());
		assertEquals(expectedTarget, link.getTargetIdList().buildPath());
		assertEquals(absoluteTarget, link.resolveTarget());
	}

	@Test
	public void TestDownloadsLinkWin10() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.downloads_win10);
		
		String expectedTarget = "<LocalDownloads>\\pause.bat";

		assertFalse(link.getTargetIdList().canBuildAbsolutePath());
		assertTrue(link.getTargetIdList().canBuildPath());
		assertEquals(3, link.getTargetIdList().size());
		assertEquals(expectedTarget, link.getTargetIdList().buildPath());
		assertEquals("C:\\Users\\admin\\Downloads\\pause.bat", link.resolveTarget());
	}

	@Test
	public void TestDownloadsLinkWinXP() throws IOException, ShellLinkException {
		var link = createLink(ReadTestData.downloads_winxp);
		
		String expectedTarget = "<Documents>\\Downloads\\pause.bat";
		String absoluteTarget = "C:\\Documents and Settings\\admin\\\u041c\u043e\u0438 \u0434\u043e\u043a\u0443\u043c\u0435\u043d\u0442\u044b\\Downloads\\pause.bat";
		absoluteTarget = new String(absoluteTarget.getBytes(Charset.forName("windows-1251")));

		assertFalse(link.getTargetIdList().canBuildAbsolutePath());
		assertTrue(link.getTargetIdList().canBuildPath());
		assertEquals(3, link.getTargetIdList().size());
		assertEquals(expectedTarget, link.getTargetIdList().buildPath());
		assertEquals(absoluteTarget, link.resolveTarget());
	}
}
