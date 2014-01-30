package mslinks;

import java.io.IOException;

import io.ByteWriter;

public interface Serializable {
	void serialize(ByteWriter bw) throws IOException;
}
