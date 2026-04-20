package mslinks.data;

import java.io.IOException;

import io.ByteReader;
import io.ByteWriter;
import io.Serializer;
import mslinks.ShellLinkException;
import mslinks.UnsupportedItemIDException;

public class ItemIDControl extends ItemID
{
	private static final int s_Signature = 0x46534643;
	private static final GUID s_G1 = new GUID("{5E591A74-DF96-48D3-8D67-1733BCEE28BA}");
	private static final GUID s_G2 = new GUID("{DFFACDC5-679F-4156-8947-C5C76BC0B67F}");

	private String m_ShortName;
	private String m_LongName;
	private boolean m_IsFile;

	@SuppressWarnings("removal")
	public ItemIDControl()
	{
		super(GROUP_CONTROLPANEL | TYPE_CONTROL_SPECIAL_FOLDER);
	}

	@SuppressWarnings("removal")
	public ItemIDControl(int flags) throws UnsupportedItemIDException
	{
		super(flags | GROUP_CONTROLPANEL);
		int subType = typeFlags & ID_TYPE_INGROUPMASK;
		if (subType != TYPE_CONTROL_SPECIAL_FOLDER)
			throw new UnsupportedItemIDException(typeFlags);
	}

	@Override
	public void load(Serializer<ByteReader> serializer, int maxSize) throws IOException, ShellLinkException
	{
		int startPos = serializer.getPosition();
		int endPos = startPos + maxSize;

		serializer.seek(1); // could this be a padding like in ItemIDFS?
		int size = (int)serializer.read(2, "shortname block size");
		int sign = (int)serializer.read(4, "signature");
		if (sign == s_Signature)
		{
			serializer.read(2, "flags"); // bit 1 - hidden, bit 3 - file
			short flags = (short)serializer.read(2, "flags"); // 0x32 - file, 0x31 - folder
			m_IsFile = (flags & 0x2) != 0;

			serializer.seek(10);
			m_ShortName = serializer.readString(size - 20, "shortname");
			serializer.seekTo(startPos + 1 + size);

			serializer.seek(2);
			new GUID(serializer);
			new GUID(serializer);

			size = (int)serializer.read(2, "longname block size");
			int selector = (int)serializer.read(2, "type"); // seems to be dependent on the OS version
			serializer.read(2, "unknown");
			serializer.read(2, "beef");

			serializer.read(4, "creation time ?");
			serializer.read(4, "modification time ?");
			serializer.read(4, "type 2"); // this seeems to be tied to selector above

			// it looks like it's only possible to create a link with this structure using the wizard
			// but win xp doesn't do this and just creates normal absolute path
			int offset = 18;
			if (selector == 7)
			{
				// vista
			}
			else if (selector == 8)
			{
				offset += 4; // win7
			}
			else if (selector == 9)
			{
				offset += 8; // win 8, 10, 11
			}
			serializer.seek(offset);

			m_LongName = serializer.readUnicodeStringNullTerm(size - 20 - offset, "longname");
		}
		serializer.seekTo(endPos);
	}

	@Override
	public void serialize(ByteWriter bw) throws IOException
	{
		serialize(new Serializer<>(bw));
	}

	public void serialize(Serializer<ByteWriter> serializer) throws IOException
	{
		super.serialize(serializer);
		serializer.seek(1);

		int shortnameSize = m_ShortName.length() + 1; // + 0-term
		// pad to even size
		if (shortnameSize % 2 != 0)
		{
			shortnameSize++;
		}
		// size (2) + signature (4) + flags (2) + flags (2) + unknown (10) + shortname + nullterm(1)
		int blockSize = 2 + 4 + 2 + 2 + 10 + shortnameSize;
		serializer.write(blockSize, 2, "shortname block size");
		serializer.write(s_Signature, 4, "signature");

		// this seems to be dependant file/folder and if a shortname format is used
		// but it doesn't look like if specific bits control that
		short flags1 = 0;
		short flags2 = 0;
		if (m_LongName.equals(m_ShortName))
		{
			if (m_IsFile)
			{
				flags1 = 0x18;
				flags2 = 0x44;
			}
			else
			{
				flags1 = 0x16;
				flags2 = 0x42;
			}
		}
		else
		{
			if (m_IsFile)
			{
				flags1 = 0x1c;
				flags2 = 0x48;
			}
			else
			{
				flags1 = 0x18;
				flags2 = 0x44;
			}
		}

		serializer.write(flags1, 2, "flags");
		serializer.write(m_IsFile ? 0x0032 : 0x0031, 2, "flags");

		serializer.seek(10);
		serializer.writeStringFixedSize(m_ShortName, shortnameSize, "shortname");

		serializer.seek(2);
		s_G1.serialize(serializer);
		s_G2.serialize(serializer);

		blockSize = 2 + 2 + 2 + 2 + 8 + 4 + 18 + (m_LongName.length() + 1) * 2 + 2;
		serializer.write(blockSize, 2, "longname block size");
		serializer.write(7, 2, "type");
		serializer.write(4, 2, "unknown");
		serializer.write(0xbeef, 2, "beef");

		serializer.seek(8);
		serializer.write(0x26, 4, "type 2");
		serializer.seek(18);

		serializer.writeUnicodeStringNullTerm(m_LongName, "longname");

		serializer.write(flags2, 2,"flags");
	}

	@Override
	public String toString()
	{
		String name = m_LongName != null && m_LongName != "" ? m_LongName : m_ShortName;
		if (!m_IsFile)
		{
			name += "\\";
		}
		return name;
	}

	@SuppressWarnings("removal")
	public String getName()
	{
		return m_LongName != null ? m_LongName : m_ShortName;
	}
	
	@SuppressWarnings("removal")
	public ItemIDControl setName(String name)
	{
		m_LongName = name;
		m_ShortName = ItemID.isLongFilename(name) ? ItemID.generateShortName(name) : name;
		return this;
	}

	public boolean getIsFile()
	{
		return m_IsFile;
	}

	public ItemIDControl setIsFile(boolean isFile)
	{
		m_IsFile = isFile;
		return this;
	}
}
