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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class TestUtils {

	public static void ExportLinkFiles(Path exportDir, Class<?> dataClass, boolean addExtension) throws IOException, IllegalArgumentException, IllegalAccessException {
		Files.createDirectories(exportDir);
		for (var field : dataClass.getDeclaredFields()) {
			if (field.getType() != byte[].class)
				continue;
				
			Path filePath = exportDir.resolve(field.getName() + (addExtension ? ".lnk" : ""));
			byte[] data = (byte[])field.get(null);
			
			Files.write(filePath, data, StandardOpenOption.CREATE);
		}
	}

	public static byte[] ByteArray(int... data) {
		var bytes = new byte[data.length];
		for (int i = 0; i < data.length; ++i)
			bytes[i] = (byte)data[i];
		return bytes;
	}
}
