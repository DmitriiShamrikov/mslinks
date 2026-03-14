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
package mslinks;

import io.ByteReader;
import io.ByteWriter;
import io.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import mslinks.data.*;

public class LinkInfo implements Serializable {
	private LinkInfoFlags lif;
	private VolumeID vid;
	private String localBasePath;
	private CNRLink cnrlink;
	private String commonPathSuffix;
	
	public LinkInfo() {
		lif = new LinkInfoFlags(0);
	}

	public LinkInfo(ByteReader data) throws IOException, ShellLinkException {
		this(new Serializer<>(data));
	}
	
	public LinkInfo(Serializer<ByteReader> serializer) throws IOException, ShellLinkException {
		int pos = serializer.getPosition();
		int size = (int)serializer.read(4, "size");
		int hsize = (int)serializer.read(4, "hsize");
		lif = new LinkInfoFlags(serializer);
		int vidoffset = (int)serializer.read(4, "vidoffset");
		int lbpoffset = (int)serializer.read(4, "lbpoffset");
		int cnrloffset = (int)serializer.read(4, "cnrloffset");
		int cpsoffset = (int)serializer.read(4, "cpsoffset");
		int lbpoffset_u = 0, cpfoffset_u = 0;
		if (hsize >= 0x24) {
			lbpoffset_u = (int)serializer.read(4, "lbpoffset (unicode)");
			cpfoffset_u = (int)serializer.read(4, "cpfoffset (unicode)");
		}
		
		if (lif.hasVolumeIDAndLocalBasePath()) {
			serializer.seek(pos + vidoffset - serializer.getPosition());
			vid = new VolumeID(serializer);
			serializer.seek(pos + lbpoffset - serializer.getPosition());
			localBasePath = serializer.readString(pos + size - serializer.getPosition(), "localBasePath");
		}
		if (lif.hasCommonNetworkRelativeLinkAndPathSuffix()) {
			serializer.seek(pos + cnrloffset - serializer.getPosition());
			cnrlink = new CNRLink(serializer);
			serializer.seek(pos + cpsoffset - serializer.getPosition());
			commonPathSuffix = serializer.readString(pos + size - serializer.getPosition(), "commonPathSuffix");
		}
		if (lif.hasVolumeIDAndLocalBasePath() && lbpoffset_u != 0) {
			serializer.seek(pos + lbpoffset_u - serializer.getPosition());
			localBasePath = serializer.readUnicodeStringNullTerm((pos + size - serializer.getPosition()) / 2, "localBasePath");
		}
		if (lif.hasCommonNetworkRelativeLinkAndPathSuffix() && cpfoffset_u != 0) {
			serializer.seek(pos + cpfoffset_u - serializer.getPosition());
			commonPathSuffix = serializer.readUnicodeStringNullTerm((pos + size - serializer.getPosition()) / 2, "commonPathSuffix");
		}
		
		serializer.seek(pos + size - serializer.getPosition());
	}

	public void serialize(ByteWriter bw) throws IOException {
		int pos = bw.getPosition();
		int hsize = 28;
		if (localBasePath != null || commonPathSuffix != null) 
			hsize += 8;
		
		byte[] vid_b = null, localBasePath_b = null, cnrlink_b = null, commonPathSuffix_b = null;
		if (lif.hasVolumeIDAndLocalBasePath()) {
			vid_b = toByteArray(vid);
			localBasePath_b = localBasePath.getBytes();
			commonPathSuffix_b = new byte[0];
		}
		if (lif.hasCommonNetworkRelativeLinkAndPathSuffix()) {
			cnrlink_b = toByteArray(cnrlink);
			commonPathSuffix_b = commonPathSuffix.getBytes();
		}
		
		int size = hsize
				+ (vid_b == null? 0 : vid_b.length)
				+ (localBasePath_b == null? 0 : localBasePath_b.length + 1)
				+ (cnrlink_b == null? 0 : cnrlink_b.length)
				+ commonPathSuffix_b.length + 1;
		
		if (hsize > 28) {
			if (lif.hasVolumeIDAndLocalBasePath()) {
				size += localBasePath.length() * 2 + 2;
				size += 1;
			}
			if (lif.hasCommonNetworkRelativeLinkAndPathSuffix())
				size += commonPathSuffix.length() * 2;
			size += 2;
		}
		
		
		bw.write4bytes(size);
		bw.write4bytes(hsize);
		lif.serialize(bw);
		int off = hsize;
		if (lif.hasVolumeIDAndLocalBasePath()) {
			bw.write4bytes(off); // volumeid offset
			off += vid_b.length;
			bw.write4bytes(off); // localBasePath offset
			off += localBasePath_b.length + 1;
		} else {
			bw.write4bytes(0); // volumeid offset
			bw.write4bytes(0); // localBasePath offset
		}
		if (lif.hasCommonNetworkRelativeLinkAndPathSuffix()) {
			bw.write4bytes(off); // CommonNetworkRelativeLink offset 
			off += cnrlink_b.length;
			bw.write4bytes(off); // commonPathSuffix
			off += commonPathSuffix_b.length + 1;
		} else {
			bw.write4bytes(0); // CommonNetworkRelativeLinkOffset
			bw.write4bytes(size - (hsize > 28 ? 4 : 1)); // fake commonPathSuffix offset 
		}
		if (hsize > 28) {
			if (lif.hasVolumeIDAndLocalBasePath()) {
				bw.write4bytes(off); // LocalBasePathOffsetUnicode
				off += localBasePath.length() * 2 + 2;
				bw.write4bytes(size - 2); // fake CommonPathSuffixUnicode offset
			} else  {
				bw.write4bytes(0);
				bw.write4bytes(off); // CommonPathSuffixUnicode offset 
				off += commonPathSuffix.length() * 2 + 2;
			}
		}
		
		if (lif.hasVolumeIDAndLocalBasePath()) {
			bw.write(vid_b);
			bw.write(localBasePath_b);
			bw.write(0);
		}
		if (lif.hasCommonNetworkRelativeLinkAndPathSuffix()) {
			bw.write(cnrlink_b);
			bw.write(commonPathSuffix_b);
			bw.write(0);
		}
		
		if (hsize > 28) {
			if (lif.hasVolumeIDAndLocalBasePath()) {
				bw.writeUnicodeStringNullTerm(localBasePath);
			}
			if (lif.hasCommonNetworkRelativeLinkAndPathSuffix()) {
				bw.writeUnicodeStringNullTerm(commonPathSuffix);
			}
		}
		
		while (bw.getPosition() < pos + size)
			bw.write(0);
	}
	
	private byte[] toByteArray(Serializable o) throws IOException {
		ByteArrayOutputStream arr = new ByteArrayOutputStream();
		ByteWriter bt = new ByteWriter(arr);
		o.serialize(bt);
		return arr.toByteArray();
	}
	
	public VolumeID getVolumeID() { return vid; }
	/**
	 * Creates VolumeID and LocalBasePath that is empty string
	 */
	public VolumeID createVolumeID() {	
		vid = new VolumeID();
		localBasePath = "";
		lif.setVolumeIDAndLocalBasePath();
		return vid;
	}
	
	public String getLocalBasePath() { return localBasePath; }
	/**
	 * Set LocalBasePath and creates new VolumeID (if it not exists)
	 * If s is null takes no effect 
	 */
	public LinkInfo setLocalBasePath(String s) {
		if (s == null) return this;
		
		localBasePath = s;
		if (vid == null) vid = new VolumeID();
		lif.setVolumeIDAndLocalBasePath();
		return this;
	}
	
	public CNRLink getCommonNetworkRelativeLink() { return cnrlink; }
	/**
	 * Creates CommonNetworkRelativeLink and CommonPathSuffix that is empty string
	 */
	public CNRLink createCommonNetworkRelativeLink() {
		cnrlink = new CNRLink();
		commonPathSuffix = "";
		lif.setCommonNetworkRelativeLinkAndPathSuffix();
		return cnrlink;
	}
	
	public String getCommonPathSuffix() { return commonPathSuffix; }
	/**
	 * Set CommonPathSuffix and creates new CommonNetworkRelativeLink (if it not exists)
	 * If s is null takes no effect 
	 */
	public LinkInfo setCommonPathSuffix(String s) {
		if (s == null) return this;
		commonPathSuffix = s;
		if (cnrlink == null) cnrlink = new CNRLink();
		lif.setCommonNetworkRelativeLinkAndPathSuffix();
		return this;
	}

	public String buildPath() {
		if (localBasePath != null) {
			String path = localBasePath;
			if (commonPathSuffix != null && !commonPathSuffix.equals("")) {
				if (path.charAt(path.length() - 1) != File.separatorChar)
					path += File.separatorChar;
				path += commonPathSuffix;
			}
			return path;
		}
		
		if (cnrlink != null && commonPathSuffix != null)
			return cnrlink.getNetName() + "\\" + commonPathSuffix;

		return null;
	}
}
