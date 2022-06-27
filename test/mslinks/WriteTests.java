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

import io.ByteWriter;
import mslinks.ShellLinkHelper.Options;
import mslinks.extra.ConsoleData.Font;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class WriteTests {

	// need some real path to be able to test links in the OS
	// also keep it static as the test data is static
	private static final String PROJECT_PATH = "C:\\Programming\\Java\\mslinks";
	private static final String PROJECT_DRIVE = "C";
	private static final String PROJECT_DIR = "Programming\\Java\\mslinks";
	private static final String RELATIVE_PATH = "..\\..";

	private ShellLinkHelper createLink() {
		var link = new ShellLink();
		var header = link.getHeader();

		header.getAccessTime().clear();
		header.getAccessTime().set(2000, 1, 1);
		header.getCreationTime().clear();
		header.getCreationTime().set(2000, 1, 1);
		header.getWriteTime().clear();
		header.getWriteTime().set(2000, 1, 1);

		return new ShellLinkHelper(link);
	}

	private byte[] serializeLink(ShellLink link) throws IOException {
		var stream = new ByteArrayOutputStream();
		var writer = new ByteWriter(stream);
		writer.setLittleEndian();
		link.serialize(writer);
		return stream.toByteArray();
	}

	@Test
	public void TestBasicLink() throws ShellLinkException, IOException {
		var link = createLink();
		link.setLocalTarget(PROJECT_DRIVE, PROJECT_DIR + "\\pause.bat", Options.ForceTypeFile)
			.getLink()
				.setRelativePath(RELATIVE_PATH + "\\pause.bat")
				.setWorkingDir(PROJECT_PATH);

		assertArrayEquals(WriteTestData.basiclink, serializeLink(link.getLink()));
	}

	@Test
	public void TestConsoleLink() throws ShellLinkException, IOException {
		var link = createLink();
		link.setLocalTarget(PROJECT_DRIVE, PROJECT_DIR + "\\pause.bat", Options.ForceTypeFile)
			.getLink()
				.setRelativePath(RELATIVE_PATH + "\\pause.bat")
				.setWorkingDir("C:\\Windows")
				.setCMDArgs("arg1 arg2")
				.getConsoleData()
					.setTextBackground(15)
					.setTextColor(2)
					.setFontSize(16)
					.setFont(Font.LucidaConsole)
					.getWindowSize()
						.setX(40)
						.setY(20);
		
		assertArrayEquals(WriteTestData.consolelink, serializeLink(link.getLink()));
	}

	@Test
	public void TestLinkIcon() throws ShellLinkException, IOException {
		var link = createLink();
		link.setLocalTarget(PROJECT_DRIVE, PROJECT_DIR + "\\pause.bat", Options.ForceTypeFile)
			.getLink()
				.setRelativePath(RELATIVE_PATH + "\\pause.bat")
				.setWorkingDir(PROJECT_PATH)
				.setIconLocation("%SystemRoot%\\System32\\SHELL32.dll")
				.getHeader()
					.setIconIndex(64);
		
		assertArrayEquals(WriteTestData.linkicon, serializeLink(link.getLink()));
	}

	@Test
	public void TestRunAsAdmin() throws ShellLinkException, IOException {
		var link = createLink();
		link.setLocalTarget(PROJECT_DRIVE, PROJECT_DIR + "\\pause.bat", Options.ForceTypeFile)
			.getLink()
				.setRelativePath(RELATIVE_PATH + "\\pause.bat")
				.setWorkingDir(PROJECT_PATH)
				.getHeader().getLinkFlags().setRunAsUser();

		assertArrayEquals(WriteTestData.adminlink, serializeLink(link.getLink()));
	}

	@Test
	public void TestUnicodePath() throws ShellLinkException, IOException {
		var link = createLink();
		link.setLocalTarget(PROJECT_DRIVE, PROJECT_DIR + "\\\u03B1\u03B1\u03B1.bat", Options.ForceTypeFile)
			.getLink()
				.setRelativePath(RELATIVE_PATH + "\\pause.bat")
				.setWorkingDir(PROJECT_PATH);

		assertArrayEquals(WriteTestData.unicodepathlink, serializeLink(link.getLink()));
	}

	@Test
	public void TestNetworkSharePath() throws ShellLinkException, IOException {
		var link = createLink();
		link.setNetworkTarget("\\\\laptop\\share\\testfile.txt", Options.ForceTypeFile)
			.getLink()
				.setRelativePath(RELATIVE_PATH + "\\pause.bat")
				.setWorkingDir(PROJECT_PATH);

		assertArrayEquals(WriteTestData.networksharelink, serializeLink(link.getLink()));
	}

	@Test
	public void TestNetworkDrivePathAsLocalPath() throws ShellLinkException, IOException {
		var link = createLink();
		link.setLocalTarget("Z", "testfile.txt", Options.ForceTypeFile)
			.getLink()
				.setRelativePath(RELATIVE_PATH + "\\pause.bat")
				.setWorkingDir(PROJECT_PATH);

		assertArrayEquals(WriteTestData.networkdrivelink, serializeLink(link.getLink()));
	}

	@Test
	public void TestNetworkDrivePath() throws ShellLinkException, IOException {
		var link = createLink();
		// network mapped drive has both local and network path
		link.setLocalTarget("Z", "testfile.txt", Options.ForceTypeFile)
			.setNetworkTarget("\\\\laptop\\share\\testfile.txt", Options.ForceTypeFile)
			.getLink()
				.setRelativePath(RELATIVE_PATH + "\\pause.bat")
				.setWorkingDir(PROJECT_PATH)
				.getLinkInfo().getCommonNetworkRelativeLink().setDeviceName("Z:");

		assertArrayEquals(WriteTestData.networkdrivefulllink, serializeLink(link.getLink()));
	}

	@Test
	public void TestDirectoryLink() throws ShellLinkException, IOException {
		var link = createLink();
		link.setLocalTarget("C", "Windows", Options.ForceTypeDirectory)
			.getLink()
				.setRelativePath(RELATIVE_PATH + "\\pause.bat");

		assertArrayEquals(WriteTestData.directorylink, serializeLink(link.getLink()));
	}

	@Test
	public void TestNetworkShareUnicodePath() throws ShellLinkException, IOException {
		var link = createLink();
		link.setNetworkTarget("\\\\laptop\\\u03B1\u03B1\u03B1\\\u03B1\u03B1\u03B1.txt", Options.ForceTypeFile)
			.getLink()
				.setRelativePath(RELATIVE_PATH + "\\pause.bat")
				.setWorkingDir(PROJECT_PATH);

		assertArrayEquals(WriteTestData.networkshareunicodelink, serializeLink(link.getLink()));
	}
}
