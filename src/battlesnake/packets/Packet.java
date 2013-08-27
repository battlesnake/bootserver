package battlesnake.packets;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/* Base class for a packet */
public abstract class Packet {

	public static Charset Encoding = Charset.forName("US-ASCII");

	/* Decode a raw binary packet into this object */
	public abstract void Decode(ByteBuffer source);

	/* Encode this object to a raw binary packet */
	public abstract ByteBuffer Encode();

	/* Decode a null terminated string */
	protected String DecodeNTS(ByteBuffer source) {
		CharBuffer str = CharBuffer.allocate(source.limit());
		while (source.remaining() > 0) {
			byte b = source.get();
			if (b == (byte) 0)
				break;
			str.put((char) b);
		}
		str.flip();
		return str.toString();
	}

	/* Decode a fixed length string */
	protected String DecodeFLS(ByteBuffer source, int length) {
		CharBuffer str = CharBuffer.allocate(length);
		int remaining = length;
		while (source.remaining() > 0 && remaining > 0) {
			byte b = source.get();
			str.put((char) b);
			remaining--;
		}
		str.flip();
		return str.toString();
	}

}
