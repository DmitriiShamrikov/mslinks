/*
	https://github.com/BlackOverlord666/mslinks
	
	Copyright (c) 2015 Dmitrii Shamrikov

	Licensed under the WTFPL
	You may obtain a copy of the License at
 
	http://www.wtfpl.net/about/
 
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/
package mslinks.data;

import java.io.IOException;
import java.lang.reflect.Field;

import mslinks.Serializable;
import mslinks.ShellLinkException;
import io.ByteReader;
import io.ByteWriter;
import io.Serializer;

public class CNRLink implements Serializable {
	
	public static final int WNNC_NET_AVID = 0x001A000;
	public static final int WNNC_NET_DOCUSPACE = 0x001B000;
	public static final int WNNC_NET_MANGOSOFT = 0x001C000;
	public static final int WNNC_NET_SERNET = 0x001D000;
	public static final int WNNC_NET_RIVERFRONT1 = 0X001E000;
	public static final int WNNC_NET_RIVERFRONT2 = 0x001F000;
	public static final int WNNC_NET_DECORB = 0x0020000;
	public static final int WNNC_NET_PROTSTOR = 0x0021000;
	public static final int WNNC_NET_FJ_REDIR = 0x0022000;
	public static final int WNNC_NET_DISTINCT = 0x0023000;
	public static final int WNNC_NET_TWINS = 0x0024000;
	public static final int WNNC_NET_RDR2SAMPLE = 0x0025000;
	public static final int WNNC_NET_CSC = 0x0026000;
	public static final int WNNC_NET_3IN1 = 0x0027000;
	public static final int WNNC_NET_EXTENDNET = 0x0029000;
	public static final int WNNC_NET_STAC = 0x002A000;
	public static final int WNNC_NET_FOXBAT = 0x002B000;
	public static final int WNNC_NET_YAHOO = 0x002C000;
	public static final int WNNC_NET_EXIFS = 0x002D000;
	public static final int WNNC_NET_DAV = 0x002E000;
	public static final int WNNC_NET_KNOWARE = 0x002F000;
	public static final int WNNC_NET_OBJECT_DIRE = 0x0030000;
	public static final int WNNC_NET_MASFAX = 0x0031000;
	public static final int WNNC_NET_HOB_NFS = 0x0032000;
	public static final int WNNC_NET_SHIVA = 0x0033000;
	public static final int WNNC_NET_IBMAL = 0x0034000;
	public static final int WNNC_NET_LOCK = 0x0035000;
	public static final int WNNC_NET_TERMSRV = 0x0036000;
	public static final int WNNC_NET_SRT = 0x0037000;
	public static final int WNNC_NET_QUINCY = 0x0038000;
	public static final int WNNC_NET_OPENAFS = 0x0039000;
	public static final int WNNC_NET_AVID1 = 0X003A000;
	public static final int WNNC_NET_DFS = 0x003B000;
	public static final int WNNC_NET_KWNP = 0x003C000;
	public static final int WNNC_NET_ZENWORKS = 0x003D000;
	public static final int WNNC_NET_DRIVEONWEB = 0x003E000;
	public static final int WNNC_NET_VMWARE = 0x003F000;
	public static final int WNNC_NET_RSFX = 0x0040000;
	public static final int WNNC_NET_MFILES = 0x0041000;
	public static final int WNNC_NET_MS_NFS = 0x0042000;
	public static final int WNNC_NET_GOOGLE = 0x0043000;
	
	private CNRLinkFlags flags; 
	private int nptype;
	private String netname, devname;

	public CNRLink() {
		flags = new CNRLinkFlags(0).setValidNetType();
		nptype = WNNC_NET_DECORB;
		netname = "";
	}

	public CNRLink(ByteReader data) throws ShellLinkException, IOException {
		this(new Serializer<ByteReader>(data));
	}
	
	public CNRLink(Serializer<ByteReader> serializer) throws ShellLinkException, IOException {
		try (var block = serializer.beginBlock("CNRLink")) {
			int pos = serializer.getPosition();
			int size = (int)serializer.read(4, Serializer.BLOCK_SIZE_NAME);
			if (size < 0x14)
				throw new ShellLinkException();
			flags = new CNRLinkFlags(serializer);
			int nnoffset = (int)serializer.read(4, "net name offset");
			int dnoffset = (int)serializer.read(4, "dev name offset");
			if (!flags.isValidDevice())
				dnoffset = 0;
			nptype = (int)serializer.read(4, "nptype", CNRLink::npTypeToLog);
			if (flags.isValidNetType())
				checkNptype(nptype);
			else nptype = 0;
			
			int nnoffset_u = 0, dnoffset_u = 0;
			if (nnoffset > 0x14) {
				nnoffset_u = (int)serializer.read(4, "net name offset (unicode)");
				dnoffset_u = (int)serializer.read(4, "dev name offset (unicode)");
			}
			
			serializer.seek(pos + nnoffset - serializer.getPosition());
			netname = serializer.readString(pos + size - serializer.getPosition(), "netname");
			if (dnoffset != 0) {
				serializer.seek(pos + dnoffset - serializer.getPosition());
				devname = serializer.readString(pos + size - serializer.getPosition(), "devname");
			}

			if (nnoffset_u != 0) {
				serializer.seek(pos + nnoffset_u - serializer.getPosition());
				netname = serializer.readUnicodeStringNullTerm(pos + size - serializer.getPosition(), "netname");
			}
			
			if (dnoffset_u != 0) {
				serializer.seek(pos + dnoffset_u - serializer.getPosition());
				devname = serializer.readUnicodeStringNullTerm(pos + size - serializer.getPosition(), "devname");
			}
		}
	}

	private static String npTypeToLog(long value) {
		Field f = Serializer.findConstField(CNRLink.class, (int)value);
		return f != null ? f.getName() : "UNKNOWN";
	}
	
	private static void checkNptype(int type) throws ShellLinkException {
		if (Serializer.findConstField(CNRLink.class, type) == null) {
			throw new ShellLinkException("incorrect network type");
		}
	}

	@Override
	public void serialize(ByteWriter bw) throws IOException {
		serialize(new Serializer<>(bw));
	}

	public void serialize(Serializer<ByteWriter> serializer) throws IOException {
		try (var block = serializer.beginBlock("CNRLink")) {
			int size = 28;
			byte[] netname_b = null, devname_b = null;
			netname_b = netname.getBytes();
			if (devname != null) devname_b = devname.getBytes();
			size += netname_b.length + 1;
			if (devname_b != null) size += devname_b.length + 1;
			size += netname.length() * 2 + 2;
			if (devname != null) size += devname.length() * 2 + 2;
			serializer.write(size, 4, Serializer.BLOCK_SIZE_NAME);
			flags.serialize(serializer);
			int off = 28;
			serializer.write(off, 4, "net name offset");
			off += netname_b.length + 1;
			if (devname_b != null) {
				serializer.write(off, 4, "dev name offset");
				off += devname_b.length + 1;
			} else {
				serializer.write(0, 4, "dev name offset");
			}
			serializer.write(nptype, 4, "nptype", CNRLink::npTypeToLog);
			serializer.write(off, 4, "net netame offset (unicode)");
			off += netname.length() * 2 + 2;
			if (devname != null) {
				serializer.write(off, 4, "dev name offset (unicode)");
				off += devname.length() * 2 + 2;
			} else {
				serializer.write(0, 4, "dev name offset (unicode)");
			}
			serializer.writeString(netname, "netname");
			if (devname_b != null) {
				serializer.writeString(devname, "devname");
			}
			serializer.writeUnicodeStringNullTerm(netname, "netname (unicode)");
			if (devname != null) {
				serializer.writeUnicodeStringNullTerm(devname, "devname (unicode)");
			}
		}
	}
	
	public int getNetworkType() { return nptype; }
	/**
	 * pass zero to switch off network type
	 */
	public CNRLink setNetworkType(int n) throws ShellLinkException {
		if (n == 0) {
			flags.clearValidNetType();
			nptype = n;
		} else {
			checkNptype(n);
			flags.setValidNetType();
			nptype = n;
		}
		return this;
	}
	
	public String getNetName() { return netname; }
	/** 
	 * if s is null take no effect
	 */
	public CNRLink setNetName(String s) {
		if (s != null)
			netname = s; 
		return this;
	}
	
	public String getDeviceName() { return devname; }
	/**
	 * pass null to switch off device info
	 */
	public CNRLink setDeviceName(String s) {
		if (s == null) {
			devname = null;
			flags.clearValidDevice();
		} else {
			devname = s;
			flags.setValidDevice();
		}
		return this;
	}
}
