package mslinks;

import io.ByteReader;
import io.ByteWriter;
import io.Bytes;

import java.io.IOException;

import mslinks.data.FileAttributesFlags;
import mslinks.data.Filetime;
import mslinks.data.GUID;
import mslinks.data.HotKeyFlags;
import mslinks.data.LinkFlags;

public class ShellLinkHeader implements Serializable {
	private static byte b(int i) { return (byte)i; }
	private static int headerSize = 0x0000004C;
	private static GUID clsid = new GUID(new byte[] {
			b(0x01),  b(0x14),  b(0x02),  b(0x00),  
			b(0x00),  b(0x00),  
			b(0x00),  b(0x00),  
			b(0xc0), b(0x00),  
			b(0x00),  b(0x00),  b(0x00),  b(0x00),  b(0x00),  b(0x46) });
	
	public static final int SW_SHOWNORMAL = 1;
	public static final int SW_SHOWMAXIMIZED = 3;
	public static final int SW_SHOWMINNOACTIVE = 7;	
	
	private LinkFlags lf;
	private FileAttributesFlags faf;
	private Filetime creationTime, accessTime, writeTime;
	private int fileSize, iconIndex, showCommand;
	private HotKeyFlags hkf;
	
	
	
	public ShellLinkHeader(ByteReader data) throws ShellLinkException, IOException {
		int size = (int)data.read4bytes();
		if (size != headerSize) {
			size = Bytes.reverse(size);			
			if (size != headerSize) 
				throw new ShellLinkException();
			data.changeEndiannes();
		}
		
		GUID g = new GUID(data);
		if (!g.equals(clsid))
			throw new ShellLinkException();
		lf = new LinkFlags(data);
		faf = new FileAttributesFlags(data);
		creationTime = new Filetime(data);
		accessTime = new Filetime(data);
		writeTime = new Filetime(data);
		fileSize = (int)data.read4bytes();
		iconIndex = (int)data.read4bytes();
		showCommand = (int)data.read4bytes();
		if (showCommand != SW_SHOWNORMAL && showCommand != SW_SHOWMAXIMIZED && showCommand != SW_SHOWMINNOACTIVE)
			throw new ShellLinkException();
		hkf = new HotKeyFlags(data);
		data.read2bytes();
		data.read8bytes();
	}
	
	public LinkFlags getLinkFlags() { return lf; }
	public FileAttributesFlags getFileAttributesFlags() { return faf; }
	public Filetime getCreationTime() { return creationTime; }
	public Filetime getAccessTime() { return accessTime; }
	public Filetime getWriteTime() { return writeTime; }
	public HotKeyFlags getHotKeyFlags() { return hkf; }
	
	public int getFileSize() { return fileSize; }
	public void setFileSize(long n) { fileSize = (int)n; }
	
	public int getIconIndex() { return iconIndex; }
	public void setIconIndex(int n) { iconIndex = n; }
	
	public int getShowCommand() { return showCommand; }
	public void setShowCommand(int n) throws ShellLinkException { 
		if (n == SW_SHOWNORMAL || n == SW_SHOWMAXIMIZED || n == SW_SHOWMINNOACTIVE)
			showCommand = n;
		else 
			throw new ShellLinkException();
	}

	public void serialize(ByteWriter bw) throws IOException {
		bw.write4bytes(headerSize);
		clsid.serialize(bw);
		lf.serialize(bw);
		faf.serialize(bw);
		creationTime.serialize(bw);
		accessTime.serialize(bw);
		writeTime.serialize(bw);
		bw.write4bytes(fileSize);
		bw.write4bytes(iconIndex);
		bw.write4bytes(showCommand);
		hkf.serialize(bw);
		bw.write2bytes(0);
		bw.write8bytes(0);
	}
}
