package battlesnake.packets;

import java.nio.ByteBuffer;

/*
 * A packet with a variable size payload, which could be another packet or just
 * some binary blob
 */
/*
 * Examples of where you would want to nest packets include: DHCP<-UDP<-IP where
 * the IP packet is the outer container
 */
public abstract class PayloadPacket extends Packet {

	/* The payload */
	public ByteBuffer payload = ByteBuffer.allocate(65536);

	public PayloadPacket() {

	}

	/* Create a payload using a raw binary blob */
	public PayloadPacket(ByteBuffer payload) {
		this.payload = payload;
	}

	/* Create a payload using another packet */
	public PayloadPacket(Packet payload) {
		this(payload.Encode());
	}

}
