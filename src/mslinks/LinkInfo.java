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
		try (var block = serializer.beginBlock("LinkInfo")) {
			int pos = serializer.getPosition();
			int size = (int)serializer.read(4, Serializer.BLOCK_SIZE_NAME);
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
				serializer.seekTo(pos + vidoffset);
				vid = new VolumeID(serializer);
				serializer.seekTo(pos + lbpoffset);
				localBasePath = serializer.readString(pos + size - serializer.getPosition(), "localBasePath");
			}
			if (lif.hasCommonNetworkRelativeLinkAndPathSuffix()) {
				serializer.seekTo(pos + cnrloffset);
				cnrlink = new CNRLink(serializer);
				serializer.seekTo(pos + cpsoffset);
				commonPathSuffix = serializer.readString(pos + size - serializer.getPosition(), "commonPathSuffix");
			}
			if (lif.hasVolumeIDAndLocalBasePath() && lbpoffset_u != 0) {
				serializer.seekTo(pos + lbpoffset_u);
				localBasePath = serializer.readUnicodeStringNullTerm((pos + size - serializer.getPosition()) / 2, "localBasePath (unicode)");
			}
			if (lif.hasCommonNetworkRelativeLinkAndPathSuffix() && cpfoffset_u != 0) {
				serializer.seekTo(pos + cpfoffset_u);
				commonPathSuffix = serializer.readUnicodeStringNullTerm((pos + size - serializer.getPosition()) / 2, "commonPathSuffix (unicode)");
			}
			
			serializer.seekTo(pos + size);
		}
	}

	@Override
	public void serialize(Serializer<ByteWriter> serializer) throws IOException {
		int pos = serializer.getPosition();
		int hsize = 28;
		if (localBasePath != null || commonPathSuffix != null) 
			hsize += 8;
		
		byte[] localBasePath_b = null, commonPathSuffix_b = null;
		int vidSize = 0;
		int cnrlinkSize = 0;
		if (lif.hasVolumeIDAndLocalBasePath()) {
			vidSize = calcSize(vid);
			localBasePath_b = localBasePath.getBytes();
			commonPathSuffix_b = new byte[0];
		}
		if (lif.hasCommonNetworkRelativeLinkAndPathSuffix()) {
			cnrlinkSize = calcSize(cnrlink);
			commonPathSuffix_b = commonPathSuffix.getBytes();
		}
		
		int size = hsize
				+ vidSize
				+ (localBasePath_b == null? 0 : localBasePath_b.length + 1)
				+ cnrlinkSize
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
		
		try (var block = serializer.beginBlock("LinkInfo")) {
			serializer.write(size, 4, Serializer.BLOCK_SIZE_NAME);
			serializer.write(hsize, 4, "hsize");
			lif.serialize(serializer);
			int off = hsize;
			if (lif.hasVolumeIDAndLocalBasePath()) {
				serializer.write(off, 4, "vidoffset"); // volumeid offset
				off += vidSize;
				serializer.write(off, 4, "lbpoffset"); // localBasePath offset
				off += localBasePath_b.length + 1;
			} else {
				serializer.write(0, 4, "vidoffset"); // volumeid offset
				serializer.write(0, 4, "lbpoffset"); // localBasePath offset
			}
			if (lif.hasCommonNetworkRelativeLinkAndPathSuffix()) {
				serializer.write(off, 4, "cnrloffset"); // CommonNetworkRelativeLink offset 
				off += cnrlinkSize;
				serializer.write(off, 4, "cpsloffset"); // commonPathSuffix
				off += commonPathSuffix_b.length + 1;
			} else {
				serializer.write(0, 4, "cnrloffset"); // CommonNetworkRelativeLinkOffset
				serializer.write(size - (hsize > 28 ? 4 : 1), 4, "cpsloffset"); // fake commonPathSuffix offset 
			}
			if (hsize > 28) {
				if (lif.hasVolumeIDAndLocalBasePath()) {
					serializer.write(off, 4, "lbpoffset (unicode)"); // LocalBasePathOffsetUnicode
					off += localBasePath.length() * 2 + 2;
					serializer.write(size - 2, 4, "cpfoffset (unicode)"); // fake CommonPathSuffixUnicode offset
				} else  {
					serializer.write(0, 4, "lbpoffset (unicode)");
					serializer.write(off, 4, "cpfoffset (unicode)"); // CommonPathSuffixUnicode offset 
					off += commonPathSuffix.length() * 2 + 2;
				}
			}
			
			if (lif.hasVolumeIDAndLocalBasePath()) {
				vid.serialize(serializer);
				serializer.writeString(localBasePath, "localBasePath");
			}
			if (lif.hasCommonNetworkRelativeLinkAndPathSuffix()) {
				cnrlink.serialize(serializer);
				serializer.writeString(commonPathSuffix, "commonPathSuffix");
			}
			
			if (hsize > 28) {
				if (lif.hasVolumeIDAndLocalBasePath()) {
					serializer.writeUnicodeStringNullTerm(localBasePath, "localBasePath (unicode)");
				}
				if (lif.hasCommonNetworkRelativeLinkAndPathSuffix()) {
					serializer.writeUnicodeStringNullTerm(commonPathSuffix, "commonPathSuffix (unicode)");
				}
			}
			
			while (serializer.getPosition() < pos + size)
				serializer.write(0, "padding");
		}
	}
	
	private int calcSize(Serializable o) throws IOException {
		ByteWriter bw = new ByteWriter(null);
		o.serialize(bw);
		return bw.getPosition();
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
				if (path.charAt(path.length() - 1) != '\\')
					path += '\\';
				path += commonPathSuffix;
			}
			return path;
		}
		
		if (cnrlink != null && commonPathSuffix != null)
			return cnrlink.getNetName() + "\\" + commonPathSuffix;

		return null;
	}
}
