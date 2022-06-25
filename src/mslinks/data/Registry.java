package mslinks.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import mslinks.ShellLinkException;
import mslinks.UnsupportedCLSIDException;

public class Registry {
	private static class RegistryEnumeration implements Iterable<GUID> {
		@Override
		public Iterator<GUID> iterator() {
			return new RegistryIterator();
		}
	}

	private static class RegistryIterator implements Iterator<GUID> {
		private int idx = 0;

		@Override
		public boolean hasNext() {
			return idx < registry.size() ;
		}

		@Override
		public GUID next() {
			return registry.get(idx++).clsid;
		}
	} 

	private static class Entry {
		public GUID clsid;
		public String name;
		public Class<?>[] allowedItemIdTypes;
	}

	private static ArrayList<Entry> registry = new ArrayList<>();
	private static HashMap<GUID, Entry> indexClsids = new HashMap<>();
	private static HashMap<String, Entry> indexNames = new HashMap<>();

	public static void registerClsid(GUID clsid, String name, Class<?>... allowedItemIdTypes) throws ShellLinkException {
		if (indexClsids.containsKey(clsid))
			throw new ShellLinkException("Registry already contains " + clsid.toString());
		
		if (indexNames.containsKey(name))
			throw new ShellLinkException("Registry already contains " + name);

		registerClsidInternal(clsid, name, allowedItemIdTypes);
	}

	private static GUID registerClsid(String clsid, String name, Class<?>... allowedItemIdTypes) {
		var guid = new GUID(clsid);
		registerClsidInternal(guid, name, allowedItemIdTypes);
		return guid;
	}

	private static void registerClsidInternal(GUID clsid, String name, Class<?>... allowedItemIdTypes) {
		var entry = new Entry();
		entry.clsid = clsid;
		entry.name = name;
		if (allowedItemIdTypes.length > 0)
			entry.allowedItemIdTypes = allowedItemIdTypes;
		else
			entry.allowedItemIdTypes = new Class<?>[] {ItemIDRegItem.class};
			registry.add(entry);
		indexClsids.put(clsid, entry);
		indexNames.put(name.toLowerCase(), entry);
	}

	public static String getName(GUID clsid) throws UnsupportedCLSIDException {
		if (!indexClsids.containsKey(clsid))
			throw new UnsupportedCLSIDException(clsid);
		
		var entry = indexClsids.get(clsid);
		return entry.name;
	}

	public static GUID getClsid(String name) throws ShellLinkException {
		name = name.toLowerCase();
		if (!indexNames.containsKey(name))
			throw new ShellLinkException(name + " is not found");
		
		var entry = indexNames.get(name);
		return entry.clsid;
	}

	public static boolean canUseClsidIn(GUID clsid, Class<?> itemIdClass) {
		if (!indexClsids.containsKey(clsid))
			return false;
		
		var entry = indexClsids.get(clsid);
		for (var i : entry.allowedItemIdTypes) {
			if (i.isAssignableFrom(itemIdClass))
				return true;
		}
		return false;
	}

	public static Iterable<GUID> asIterable() {
		return new RegistryEnumeration();
	}

	public static final GUID CLSID_COMPUTER;
	public static final GUID CLSID_DESKTOP;
	public static final GUID CLSID_DOCUMENTS;
	public static final GUID CLSID_DOWLOADS;

	static {
		CLSID_COMPUTER =
		registerClsid("{20d04fe0-3aea-1069-a2d8-08002b30309d}", "Computer", ItemIDRoot.class);

		// Windows XP+
		registerClsid("{D20EA4E1-3957-11D2-A40B-0C5020524153}", "CommonAdministrativeTools");
		CLSID_DOCUMENTS =
		registerClsid("{450D8FBA-AD25-11D0-98A8-0800361B1103}", "Documents");
		registerClsid("{645FF040-5081-101B-9F08-00AA002F954E}", "RecycleBin");
		registerClsid("{D20EA4E1-3957-11D2-A40B-0C5020524152}", "Fonts"); // WinXP ONLY

		// Windows 7+
		registerClsid("{D34A6CA6-62C2-4C34-8A7C-14709C1AD938}", "Links");
		registerClsid("{B155BDF8-02F0-451E-9A26-AE317CFD7779}", "NetHood");
		registerClsid("{ED50FC29-B964-48A9-AFB3-15EBB9B97F36}", "PrintHood");
		registerClsid("{4336A54D-038B-4685-AB02-99BB52D3FB8B}", "Public");
		registerClsid("{1F3427C8-5C10-4210-AA03-2EE45287D668}", "UserPinned");

		// Windows 10+
		registerClsid("{0DB7E03F-FC29-4DC6-9020-FF41B59E513A}", "3DObjects");
		CLSID_DESKTOP =
		registerClsid("{B4BFCC3A-DB2C-424C-B029-7FE99A87C641}", "Desktop");
		CLSID_DOWLOADS =
		registerClsid("{374DE290-123F-4565-9164-39C4925E467B}", "Downloads");
		registerClsid("{D3162B92-9365-467A-956B-92703ACA08AF}", "LocalDocuments");
		registerClsid("{088E3905-0323-4B02-9826-5D99428E115F}", "LocalDownloads");
		registerClsid("{3DFDF296-DBEC-4FB4-81D1-6A3438BCF4DE}", "LocalMusic");
		registerClsid("{24AD3AD4-A569-4530-98E1-AB02F9417AA8}", "LocalPictures");
		registerClsid("{F86FA3AB-70D2-4FC7-9C99-FCBF05467F3A}", "LocalVideos");
		registerClsid("{1CF1260C-4DD0-4EBB-811F-33C572699FDE}", "MyMusic");
		registerClsid("{3ADD1653-EB32-4CB0-BBD7-DFA0ABB5ACCA}", "MyPictures");
		registerClsid("{A0953C92-50DC-43BF-BE83-3742FED03C9C}", "MyVideo");
		registerClsid("{018D5C66-4533-4307-9B53-224DE2ED1FE6}", "OneDrive");
		registerClsid("{A8CDFF1C-4878-43BE-B5FD-F8091C1C60D0}", "Personal");
		registerClsid("{F8278C54-A712-415B-B593-B77A2BE0DDA9}", "Profile");
		registerClsid("{5B934B42-522B-4C34-BBFE-37A3EF7B9C90}", "Public_1");
	}
}
