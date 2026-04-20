package mslinks.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import mslinks.ShellLinkException;
import mslinks.ShellLinkHeader;
import mslinks.UnsupportedCLSIDException;
import mslinks.extra.PropertyStore;

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

	public static final int CLSID_FLAG_CONTROL = 1;

	private static class Entry {
		public GUID clsid;
		public String name;
		public int flags;
		public Class<?>[] allowedClasses;
	}

	private static ArrayList<Entry> registry = new ArrayList<>();
	private static HashMap<GUID, Entry> indexClsids = new HashMap<>();
	private static HashMap<String, ArrayList<Entry>> indexNames = new HashMap<>();

	public static void registerClsid(GUID clsid, String name, Class<?>... allowedItemIdTypes) {
		registerClsid(clsid, name, 0, allowedItemIdTypes);
	}

	public static void registerClsid(GUID clsid, String name, int flags, Class<?>... allowedItemIdTypes) {
		registerClsidInternal(clsid, name, 0, allowedItemIdTypes);
	}

	public static GUID registerClsid(String clsid, String name, Class<?>... allowedItemIdTypes) {
		return registerClsid(clsid, name, 0, allowedItemIdTypes);
	}

	public static GUID registerClsid(String clsid, String name, int flags, Class<?>... allowedItemIdTypes) {
		var guid = new GUID(clsid);
		registerClsidInternal(guid, name, flags, allowedItemIdTypes);
		return guid;
	}

	private static void registerClsidInternal(GUID clsid, String name, int flags, Class<?>... allowedClasses) {
		var entry = indexClsids.get(clsid);
		if (entry != null) {
			if (!name.equals(entry.name)) {
				System.out.println("WARNING: Registry already contains " + clsid.toString() +
					" with name " + entry.name + ". Renaming it to " + name);

				var oldNameEntries = indexNames.get(entry.name.toLowerCase());
				if (oldNameEntries != null) {
					oldNameEntries.remove(entry);
				}
				
				entry.name = name;

				var newNameEntries = indexNames.get(name.toLowerCase());
				if (newNameEntries == null) {
					newNameEntries = new ArrayList<>();
					indexNames.put(name.toLowerCase(), newNameEntries);
				}
				newNameEntries.add(entry);
			}
			entry.flags = flags;
			return;
		}
		
		entry = new Entry();
		entry.clsid = clsid;
		entry.name = name;
		entry.flags = flags;
		if (allowedClasses.length > 0)
			entry.allowedClasses = allowedClasses;
		else
			entry.allowedClasses = new Class<?>[] {ItemIDRegItem.class};
		registry.add(entry);
		indexClsids.put(clsid, entry);

		var nameEntries = indexNames.get(name.toLowerCase());
		if (nameEntries == null) {
			nameEntries = new ArrayList<>();
			indexNames.put(name.toLowerCase(), nameEntries);
		}
		nameEntries.add(entry);
	}

	public static String getName(GUID clsid) throws UnsupportedCLSIDException {
		if (!indexClsids.containsKey(clsid))
			throw new UnsupportedCLSIDException(clsid);
		
		var entry = indexClsids.get(clsid);
		return entry.name;
	}

	public static GUID getClsid(String name) throws ShellLinkException {
		return getClsid(name, 0);
	}

	public static GUID getClsid(String name, int idx) throws ShellLinkException {
		name = name.toLowerCase();
		if (!indexNames.containsKey(name))
			throw new ShellLinkException(name + " is not found");
		
		var entries = indexNames.get(name);
		return entries.size() > 0 ? entries.get(idx).clsid : null;
	}

	public static boolean canUseClsidIn(GUID clsid, Class<?> itemClass) {
		if (!indexClsids.containsKey(clsid))
			return false;
		
		var entry = indexClsids.get(clsid);
		for (var i : entry.allowedClasses) {
			if (i.isAssignableFrom(itemClass))
				return true;
		}
		return false;
	}

	public static boolean isKnownFolderClsid(GUID clsid) {
		if (!indexClsids.containsKey(clsid))
			return false;

		var entry = indexClsids.get(clsid);
		return Arrays.stream(entry.allowedClasses).anyMatch(cls -> cls == ItemIDKnownFolder.class);
	}

	public static boolean isControlClsid(GUID clsid) {
		return hasFlag(clsid, CLSID_FLAG_CONTROL);
	}

	public static boolean hasFlag(GUID clsid, int flag) {
		if (!indexClsids.containsKey(clsid))
			return false;

		var entry = indexClsids.get(clsid);
		return (entry.flags & flag) != 0;
	}

	public static Iterable<GUID> asIterable() {
		return new RegistryEnumeration();
	}

	public static final GUID CLSID_COMPUTER;
	public static final GUID CLSID_DESKTOP;
	public static final GUID CLSID_DOCUMENTS;
	public static final GUID CLSID_DOWLOADS;
	public static final GUID CLSID_USERFOLDER;
	
	public static final GUID CLSID_LINK_HEADER;
	public static final GUID PROPERTY_STORAGE_FORMAT_STRING;

	static {
		CLSID_LINK_HEADER =
		registerClsid("{00021401-0000-0000-C000-000000000046}", "LinkHeader", ShellLinkHeader.class);

		PROPERTY_STORAGE_FORMAT_STRING =
		registerClsid("{D5CDD505-2E9C-101B-9397-08002B2CF9AE}", "StringStorageType", PropertyStore.class);
		
		CLSID_COMPUTER =
		registerClsid("{20d04fe0-3aea-1069-a2d8-08002b30309d}", "Computer", ItemIDRoot.class);


		// Windows XP only
		registerClsid("D20EA4E1-3957-11D2-A40B-0C5020524152", "Fonts");
		registerClsid("208D2C60-3AEA-1069-A2D7-08002B30309D", "NetHood");
		registerClsid("2227A280-3AEA-1069-A2DE-08002B30309D", "PrintHood");

		// Windows XP+
		registerClsid("D20EA4E1-3957-11D2-A40B-0C5020524153", "CommonAdministrativeTools");
		CLSID_DOCUMENTS=
		registerClsid("450D8FBA-AD25-11D0-98A8-0800361B1103", "Documents");
		registerClsid("645FF040-5081-101B-9F08-00AA002F954E", "RecycleBin");

		// Windows Vista only
		registerClsid("FD228CB7-AE11-4AE3-864C-16F3910AB8FE", "Fonts", ItemIDKnownFolder.class);
		registerClsid("D9DC8A3B-B784-432E-A781-5A1130A75963", "History", ItemIDKnownFolder.class);

		// Windows Vista+
		registerClsid("724EF170-A42D-4FEF-9F26-B60E846FBA4F", "AdminTools", ItemIDKnownFolder.class);
		registerClsid("9E52AB10-F80D-49DF-ACB8-4330F5687855", "CDBurning", ItemIDKnownFolder.class);
		registerClsid("D0384E7D-BAC3-4797-8F14-CBA229B392B5", "CommonAdminTools", ItemIDKnownFolder.class);
		registerClsid("0139D44E-6AFE-49F2-8690-3DAFCAE6FFB8", "CommonPrograms", ItemIDKnownFolder.class);
		registerClsid("A4115719-D62E-491D-AA7C-E74B8BE3B067", "CommonStartMenu", ItemIDKnownFolder.class);
		registerClsid("82A5EA35-D9CD-47C5-9629-E15D2F714E6E", "CommonStartup", ItemIDKnownFolder.class);
		registerClsid("B94237E7-57AC-4347-9151-B08C6C32D1F7", "CommonTemplates", ItemIDKnownFolder.class);
		registerClsid("56784854-C6CB-462B-8169-88E350ACB882", "Contacts", ItemIDKnownFolder.class);
		registerClsid("2B0F765D-C0E9-4171-908E-08A611B84FF6", "Cookies", ItemIDKnownFolder.class);
		CLSID_DESKTOP=
		registerClsid("B4BFCC3A-DB2C-424C-B029-7FE99A87C641", "Desktop", ItemIDRegItem.class, ItemIDKnownFolder.class);
		registerClsid("FDD39AD0-238F-46AF-ADB4-6C85480369C7", "Documents", ItemIDKnownFolder.class);
		CLSID_DOWLOADS=
		registerClsid("374DE290-123F-4565-9164-39C4925E467B", "Downloads", ItemIDRegItem.class, ItemIDKnownFolder.class);
		registerClsid("1777F761-68AD-4D8A-87BD-30B759FA33DD", "Favorites", ItemIDKnownFolder.class);
		registerClsid("A75D362E-50FC-4FB7-AC2C-A8BEAA314493", "Gadgets", ItemIDKnownFolder.class);
		registerClsid("054FAE61-4DD8-4787-80B6-090220C4B700", "GameExplorer", ItemIDKnownFolder.class);
		registerClsid("352481E8-33BE-4251-BA85-6007CAEDCF9D", "InternetCache", ItemIDKnownFolder.class);
		registerClsid("D34A6CA6-62C2-4C34-8A7C-14709C1AD938", "Links");
		registerClsid("BFB9D5E0-C6A9-404C-B2B2-AE6DB6AF4968", "Links", ItemIDKnownFolder.class);
		registerClsid("F1B32785-6FBA-4FCF-9D55-7B8E7F157091", "Local", ItemIDKnownFolder.class);
		registerClsid("A520A1A4-1780-4FF6-BD18-167343C5AF16", "LocalLow", ItemIDKnownFolder.class);
		registerClsid("4BD8D571-6D19-48D3-BE97-422220080E43", "Music", ItemIDKnownFolder.class);
		registerClsid("B155BDF8-02F0-451E-9A26-AE317CFD7779", "NetHood");
		registerClsid("C5ABBF53-E17F-4121-8900-86626FC2C973", "NetHood", ItemIDKnownFolder.class);
		registerClsid("33E28130-4E1E-4676-835A-98395C3BC3BB", "Pictures", ItemIDKnownFolder.class);
		registerClsid("ED50FC29-B964-48A9-AFB3-15EBB9B97F36", "PrintHood");
		registerClsid("9274BD8D-CFD1-41C3-B35E-B13F55A758F4", "PrintHood", ItemIDKnownFolder.class);
		registerClsid("5E6C858F-0E22-4760-9AFE-EA3317B67173", "Profile", ItemIDKnownFolder.class);
		registerClsid("62AB5D82-FDC1-4DC3-A9DD-070D1D495D97", "ProgramData", ItemIDKnownFolder.class);
		registerClsid("905E63B6-C1BF-494E-B29C-65B732D3D21A", "ProgramFiles", ItemIDKnownFolder.class);
		registerClsid("F7F1ED05-9F6D-47A2-AAAE-29D317C6F066", "ProgramFilesCommon", ItemIDKnownFolder.class);
		registerClsid("6365D5A7-0F0D-45E5-87F6-0DA56B6A4F7D", "ProgramFilesCommonX64", ItemIDKnownFolder.class);
		registerClsid("DE974D24-D9C6-4D3E-BF91-F4455120B917", "ProgramFilesCommonX86", ItemIDKnownFolder.class);
		registerClsid("6D809377-6AF0-444B-8957-A3773F02200E", "ProgramFilesX64", ItemIDKnownFolder.class);
		registerClsid("7C5A40EF-A0FB-4BFC-874A-C0F2E0B9FA8E", "ProgramFilesX86", ItemIDKnownFolder.class);
		registerClsid("A77F5D77-2E2B-44C3-A6A2-ABA601054A51", "Programs", ItemIDKnownFolder.class);
		registerClsid("4336A54D-038B-4685-AB02-99BB52D3FB8B", "Public");
		registerClsid("DFDF76A2-C82A-4D63-906A-5644AC457385", "Public", ItemIDKnownFolder.class);
		registerClsid("C4AA340D-F20F-4863-AFEF-F87EF2E6BA25", "PublicDesktop", ItemIDKnownFolder.class);
		registerClsid("ED4824AF-DCE4-45A8-81E2-FC7965083634", "PublicDocuments", ItemIDKnownFolder.class);
		registerClsid("3D644C9B-1FB8-4F30-9B45-F670235F79C0", "PublicDownloads", ItemIDKnownFolder.class);
		registerClsid("DEBF2536-E1A8-4C59-B6A2-414586476AEA", "PublicGameExplorer", ItemIDKnownFolder.class);
		registerClsid("3214FAB5-9757-4298-BB61-92A9DEAA44FF", "PublicMusic", ItemIDKnownFolder.class);
		registerClsid("B6EBFB86-6907-413C-9AF7-4FC2ABF07CC5", "PublicPictures", ItemIDKnownFolder.class);
		registerClsid("2400183A-6185-49FB-A2D8-4A392A602BA3", "PublicVideos", ItemIDKnownFolder.class);
		registerClsid("52A4F021-7B75-48A9-9F6B-4B87A210BC8F", "QuickLaunch", ItemIDKnownFolder.class);
		registerClsid("22877A6D-37A1-461A-91B0-DBDA5AAEBC99", "Recent");
		registerClsid("AE50C081-EBD2-438A-8655-8A092E34987A", "Recent", ItemIDUnknown.class);
		registerClsid("8AD10C31-2ADB-4296-A8F7-E4701232C972", "Resources", ItemIDKnownFolder.class);
		registerClsid("3EB685DB-65F9-4CF6-A03A-E3EF65729F3D", "Roaming", ItemIDKnownFolder.class);
		registerClsid("B250C668-F57D-4EE1-A63C-290EE7D1AA1F", "SampleMusic", ItemIDKnownFolder.class);
		registerClsid("C4900540-2379-4C75-844B-64E6FAF8716B", "SamplePictures", ItemIDKnownFolder.class);
		registerClsid("859EAD94-2E85-48AD-A71A-0969CB56A6CD", "SampleVideos", ItemIDKnownFolder.class);
		registerClsid("4C5C32FF-BB9D-43B0-B5B4-2D72E54EAAA4", "SavedGames", ItemIDKnownFolder.class);
		registerClsid("7D1D3A04-DEBB-4115-95CF-2F29DA2920DA", "Searches", ItemIDKnownFolder.class);
		registerClsid("8983036C-27C0-404B-8F08-102D10DCFD74", "SendTo", ItemIDKnownFolder.class);
		registerClsid("7B396E54-9EC5-4300-BE0A-2482EBAE1A26", "SidebarDefaultParts", ItemIDKnownFolder.class);
		registerClsid("625B53C3-AB48-4EC1-BA1F-A1EF4146FC19", "StartMenu", ItemIDKnownFolder.class);
		registerClsid("B97D20BB-F46A-4C97-BA10-5E3608430854", "Startup", ItemIDKnownFolder.class);
		registerClsid("1AC14E77-02E7-4E5D-B744-2EB1AE5198B7", "System", ItemIDKnownFolder.class);
		registerClsid("D65231B0-B2F1-4857-A4CE-A8E7C6EA7D27", "SystemX86", ItemIDKnownFolder.class);
		registerClsid("A63293E8-664E-48DB-A079-DF759E0509F7", "Templates", ItemIDKnownFolder.class);
		CLSID_USERFOLDER=
		registerClsid("59031A47-3F72-44A7-89C5-5595FE6B30EE", "UserFolder", CLSID_FLAG_CONTROL);
		registerClsid("9C73F5E5-7AE7-4E32-A8E8-8D23B85255BF", "UserFolder", CLSID_FLAG_CONTROL);
		registerClsid("0762D272-C50A-4BB0-A382-697DCD729B80", "UserProfiles", ItemIDKnownFolder.class);
		registerClsid("18989B1D-99B5-455B-841C-AB7C74E4DDFC", "Videos", ItemIDKnownFolder.class);
		registerClsid("F38BF404-1D43-42F2-9305-67DE0B28FC23", "Windows", ItemIDKnownFolder.class);

		// Windows 7+
		registerClsid("5CE4A5E9-E4EB-479D-B89F-130C02886155", "DeviceMetadataStore", ItemIDKnownFolder.class);
		registerClsid("BCB5256F-79F6-4CEE-B725-DC34E402FD46", "ImplicitAppShortcuts", ItemIDKnownFolder.class);
		registerClsid("1B3EA5DC-B587-4786-B4EF-BD1DC332AEAE", "Libraries", ItemIDKnownFolder.class);
		registerClsid("48DAF80B-E6CF-4F4E-B800-0E69D84EE384", "PublicLibraries", ItemIDKnownFolder.class);
		registerClsid("E555AB60-153B-4D17-9F04-A5FE99FC15EC", "PublicRingtones", ItemIDKnownFolder.class);
		registerClsid("C870044B-F49E-4126-A9C3-B52A1FF411E8", "Ringtones", ItemIDKnownFolder.class);
		registerClsid("9E3995AB-1F9C-4F13-B827-48B24B6C7174", "UserPinned", ItemIDKnownFolder.class);
		registerClsid("1F3427C8-5C10-4210-AA03-2EE45287D668", "UserPinned");

		// Windows 8 only
		registerClsid("0D4C3DB6-03A3-462F-A0E6-08924C41B5D4", "History", ItemIDKnownFolder.class);
		registerClsid("7E636BFE-DFA9-4D5E-B456-D7B39851D8A9", "Templates", ItemIDKnownFolder.class);

		// Windows 8+
		registerClsid("008CA0B1-55B4-4C56-B8A8-4DE4B299D3BE", "AccountPictures", ItemIDKnownFolder.class);
		registerClsid("1CF1260C-4DD0-4EBB-811F-33C572699FDE", "MyMusic");
		registerClsid("3ADD1653-EB32-4CB0-BBD7-DFA0ABB5ACCA", "MyPictures");
		registerClsid("A0953C92-50DC-43BF-BE83-3742FED03C9C", "MyVideo");
		registerClsid("A8CDFF1C-4878-43BE-B5FD-F8091C1C60D0", "Personal");
		registerClsid("00BCFC5A-ED94-4E48-96A1-3F6217F21990", "RoamingTiles", ItemIDKnownFolder.class);

		// Windows 10 only
		registerClsid("31C0DD25-9439-4F12-BF41-7FF4EDA38722", "3DObjects", ItemIDKnownFolder.class);
		registerClsid("AB5FB87B-7CE2-4F83-915D-550846C9537B", "CameraRoll", ItemIDKnownFolder.class);
		registerClsid("0DB7E03F-FC29-4DC6-9020-FF41B59E513A", "3DObjects");
		registerClsid("A3918781-E5F2-4890-B3D9-A7E54332328C", "ApplicationShortcuts", ItemIDKnownFolder.class);
		registerClsid("D3162B92-9365-467A-956B-92703ACA08AF", "LocalDocuments");
		registerClsid("088E3905-0323-4B02-9826-5D99428E115F", "LocalDownloads");
		registerClsid("3DFDF296-DBEC-4FB4-81D1-6A3438BCF4DE", "LocalMusic");
		registerClsid("24AD3AD4-A569-4530-98E1-AB02F9417AA8", "LocalPictures");
		registerClsid("F86FA3AB-70D2-4FC7-9C99-FCBF05467F3A", "LocalVideos");
		registerClsid("018D5C66-4533-4307-9B53-224DE2ED1FE6", "OneDrive");
		registerClsid("A52BBA46-E9E1-435F-B3D9-28DAA648C0F6", "OneDrive", ItemIDKnownFolder.class);
		registerClsid("F8278C54-A712-415B-B593-B77A2BE0DDA9", "Profile");
		registerClsid("5B934B42-522B-4C34-BBFE-37A3EF7B9C90", "Public");
		registerClsid("0482AF6C-08F1-4C34-8C90-E17EC98B1E17", "PublicUserTiles", ItemIDKnownFolder.class);
		registerClsid("4564B25E-30CD-4787-82BA-39E73A750B14", "Recent");

		// Windows 11
		registerClsid("5CD7AEE2-2219-4A67-B85D-6C9CE15660CB", "Programs", ItemIDKnownFolder.class);
	}
}
