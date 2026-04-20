package mslinks.data;

import java.io.IOException;

import io.ByteReader;
import io.ByteWriter;
import io.Serializer;
import mslinks.ShellLinkException;
import mslinks.UnsupportedCLSIDException;

public class ItemIDKnownFolder extends ItemID
{
	private GUID m_clsid;

	@SuppressWarnings("removal")
	public ItemIDKnownFolder()
	{
		super(0);
	}

	@Override
	public void load(Serializer<ByteReader> serializer, int maxSize) throws IOException, ShellLinkException
	{
		int startPos = serializer.getPosition();
		super.load(serializer, maxSize);

		serializer.seek(11);
		m_clsid = new GUID(serializer);
		serializer.seekTo(startPos + maxSize);
	}

	@Override
	public void serialize(ByteWriter bw) throws IOException
	{
		serialize(new Serializer<>(bw));
	}

	public void serialize(Serializer<ByteWriter> serializer) throws IOException
	{
		super.serialize(serializer);
		serializer.write(0, "unknown");
		serializer.write(0x1a, 2, "unknown");
		serializer.write(0x23febbee, 4, "unknown");
		serializer.write(0, 2, "unknown");
		serializer.write(0x10, 2, "unknown");
		m_clsid.serialize(serializer);
		serializer.write(0, "unknown");
	}

	public GUID getClsid()
	{
		return m_clsid;
	}

	public ItemIDKnownFolder setClsid(GUID clsid) throws UnsupportedCLSIDException
	{
		if (!Registry.canUseClsidIn(clsid, this.getClass()))
		{
			throw new UnsupportedCLSIDException(clsid);
		}

		m_clsid = clsid;
		return this;
	}

	@Override
	public String toString()
	{
		String name;
		try
		{
			name = Registry.getName(m_clsid); 
		}
		catch (UnsupportedCLSIDException e)
		{
			name = "{" + m_clsid.toString() + "}";
		}

		return "<" + name + ">\\";
	}
}
