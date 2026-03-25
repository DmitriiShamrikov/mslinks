/*
	https://github.com/BlackOverlord666/mslinks
	
	Copyright (c) 2026 Dmitrii Shamrikov

	Licensed under the WTFPL
	You may obtain a copy of the License at
 
	http://www.wtfpl.net/about/
 
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/
package mslinks.extra;

import java.io.IOException;

import io.ByteReader;
import io.ByteWriter;
import io.Serializer;
import mslinks.Serializable;
import mslinks.ShellLink;
import mslinks.ShellLinkException;
import mslinks.data.GUID;
import mslinks.data.ItemID;

public class KnownFolder implements Serializable
{
	public static final int signature = 0xA000000B;
	public static final int size = 0x1c;

	private GUID m_FolderId;
	private ShellLink m_OwnerLink;
	private ItemID m_ItemID;
	
	public KnownFolder()
	{
	}

	public KnownFolder(ByteReader br, int sz, ShellLink link) throws ShellLinkException, IOException
	{
		this(new Serializer<ByteReader>(br), sz, link);
	}
	
	public KnownFolder(Serializer<ByteReader> serializer, int sz, ShellLink link) throws ShellLinkException, IOException
	{
		if (sz != size)
		{
			throw new ShellLinkException();
		}
		
		m_OwnerLink = link;
		m_FolderId = new GUID(serializer);
		int offset = (int)serializer.read(4, "offset to itemID", this::itemIDOffsetToLog);
		for (ItemID item : link.getTargetIdList())
		{
			if (item.getOffset() == offset)
			{
				m_ItemID = item;
			}
		}
	}

	@Override
	public void serialize(Serializer<ByteWriter> serializer) throws IOException
	{
		serializer.write(size, 4, Serializer.BLOCK_SIZE_NAME);
		serializer.write(signature, 4, "signature", v -> getClass().getName());
		m_FolderId.serialize(serializer);

		// make sure the object is still there
		// the offset of the ItemID is updated during serialization
		// as TargetItemIDList is serialized before this
		if (m_OwnerLink.getTargetIdList().contains(m_ItemID))
		{
			serializer.write(m_ItemID.getOffset(), 4, "offset to itemID", this::itemIDOffsetToLog);
		}
		else
		{
			serializer.write(0, 4, "offset to itemID (invalid)");
		}
	}

	private String itemIDOffsetToLog(long offset)
	{
		var idlist = m_OwnerLink.getTargetIdList();
		for (int i = 0; i < idlist.size(); ++i)
		{
			if (idlist.get(i).getOffset() == (int)offset)
			{
				return String.format("item id #%d", i);
			}
		}
		return "item id not found";
	}

	public GUID getFolderId()
	{
		return m_FolderId;
	}
	public KnownFolder setFolderId(GUID guid)
	{
		m_FolderId = guid;
		return this;
	}

	public ItemID getItemID()
	{
		return m_ItemID;
	}
	public KnownFolder setItemID(ItemID itemID)
	{
		m_ItemID = itemID;
		return this;
	}
}
