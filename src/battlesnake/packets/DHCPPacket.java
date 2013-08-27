package battlesnake.packets;

// See http://support.microsoft.com/kb/169289 and the RFCs

// Note: DHCP op-codes are degenerate, use the <options> to determine the nature of the packet.

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import battlesnake.packets.dhcpdefs.OptionDefinitions;

/* Parses and generates DHCP packets */
public class DHCPPacket extends Packet {

	/* Packet fields */
	public byte op, htype, hlen, hops;
	public int xid;
	public short secs, flags;
	public int ciaddr, yiaddr, siaddr, giaddr;
	public byte[] chaddr = new byte[16];
	public String bootp_host = "";
	public String bootp_file = "";
	public int cookie = MAGIC_COOKIE;
	public List<Option> options = new ArrayList<Option>();

	/* DHCP magic cookie (see RFCs) */
	public static final int MAGIC_COOKIE = 0x63825363;

	/* Note: Packet types are degenerate */
	public static final byte DHCP_REQUEST = 1, DHCP_REPLY = 2;

	/*
	 * My ID   Qbytes   Fields
	 * #01     1        OP HTYPE HLEN HOPS
	 * #02     1        XID
	 * #03     1        SECS FLAGS
	 * #04     1        CIADDR (Client IP)
	 * #05     1        YIADDR (Your IP)
	 * #06     1        SIADDR (Server IP)
	 * #07     1        GIADDR (Gateway IP)
	 * #08     4        CHADDR (Client hardware address)
	 * #09     48       Additional BOOTP options (nulls)
	 * #10     1        Magic cookie
	 * #11     ?        DHCP options
	 */

	/* Create an empty packet */
	public DHCPPacket() {
	}

	/* Create a response with the specified opcode */
	public DHCPPacket MakeResponse(byte op) {
		DHCPPacket response = new DHCPPacket();
		response.op = op;
		response.htype = htype;
		response.hlen = hlen;
		response.hops = 0;
		response.xid = xid;
		response.secs = 0;
		response.flags = 0;
		response.ciaddr = 0;
		response.yiaddr = 0;
		response.siaddr = 0;
		response.chaddr = chaddr;
		response.cookie = cookie;
		return response;
	}

	/* Decode a DHCP packet and store it in the fields of this object */
	@Override
	public void Decode(ByteBuffer source) {
		this.op = source.get();
		this.htype = source.get();
		this.hlen = source.get();
		this.hops = source.get();
		this.xid = source.getInt();
		int secs_flags = source.getInt();
		this.secs = (short) ((secs_flags >>> 16) & 0xffff);
		this.flags = (short) (secs_flags & 0xffff);
		this.ciaddr = source.getInt();
		this.yiaddr = source.getInt();
		this.siaddr = source.getInt();
		this.giaddr = source.getInt();
		for (int i = 0; i < 16; i++)
			this.chaddr[i] = source.get();
		char[] bphost = new char[64];
		for (int i = 0; i < 64; i++)
			bphost[i] = (char) source.get();
		this.bootp_host = bphost.toString();
		char[] bpfile = new char[128];
		for (int i = 0; i < 128; i++)
			bpfile[i] = (char) source.get();
		this.bootp_file = bpfile.toString();
		this.cookie = source.getInt();
		if (this.cookie == MAGIC_COOKIE) {
			while (source.remaining() > 0) {
				byte op = source.get();
				if (op == (byte) 0)
					continue;
				else if (op == (byte) 0xff)
					break;
				int len = source.get();
				byte[] data = new byte[len];
				for (int i = 0; i < len; i++)
					data[i] = source.get();
				this.options.add(new Option(op, data));
			}
		}
	}

	/* Create a DHCP packet object from a raw packet */
	public DHCPPacket(ByteBuffer source) {
		Decode(source);
	}

	/* Encode the fields of the object into a raw binary packet */ 
	@Override
	public ByteBuffer Encode() {
		ByteBuffer pkt = ByteBuffer.allocate(65535);
		pkt.put(op);
		pkt.put(htype);
		pkt.put(hlen);
		pkt.put(hops);
		pkt.putInt(xid);
		pkt.putShort(secs);
		pkt.putShort(flags);
		pkt.putInt(ciaddr);
		pkt.putInt(yiaddr);
		pkt.putInt(siaddr);
		pkt.putInt(giaddr);
		for (int i = 0; i < 16; i++)
			pkt.put(chaddr[i]);
		byte[] bphost = bootp_host.getBytes(Encoding);
		for (int i = 0; i < 64; i++)
			if (i >= bphost.length)
				pkt.put((byte) 0);
			else
				pkt.put(bphost[i]);
		byte[] bpfile = bootp_file.getBytes(Encoding);
		for (int i = 0; i < 128; i++)
			if (i >= bpfile.length)
				pkt.put((byte) 0);
			else
				pkt.put(bpfile[i]);
		pkt.putInt(cookie);
		for (Option opt : options) {
			pkt.put(opt.opcode);
			pkt.put((byte) opt.data.length);
			pkt.put(opt.data);
			// padding
			if ((opt.data.length & 1) != 0)
				pkt.put((byte) 0);
		}
		pkt.put((byte) 0xff);
		pkt.flip();
		return pkt;
	}

	/* DHCP options */
	public static final byte OPTION_PADDING = 0, OPTION_SUBNET = 1,
			OPTION_ROUTER_ADDR = 3, OPTION_TIME_SERVER = 4,
			OPTION_NAME_SERVER = 5, OPTION_DNS_SERVER = 6,
			OPTION_LOG_SERVER = 7, OPTION_COOKIE_SERVER = 8,
			OPTION_HOSTNAME = 12, OPTION_BOOTFILESIZE = 13,
			OPTION_DNS_DOMAIN = 15, OPTION_ROOTPATH = 17, OPTION_TTL = 23,
			OPTION_MTU = 26, OPTION_REQUESTED_IP = 50, OPTION_LEASE_TIME = 51,
			OPTION_DHCP_MESSAGE_TYPE = 53, OPTION_SERVER_IDENT = 54,
			OPTION_PARAM_REQUEST_LIST = 55, OPTION_MESSAGE = 56,
			OPTION_MAX_MESSAGE_LEN = 57, OPTION_RENEWAL_TIME = 58,
			OPTION_REBIND_TIME = 59, OPTION_CLASS_IDENT = 60,
			OPTION_CLIENT_IDENT = 61, OPTION_TFTP_SERVER = 66,
			OPTION_BOOTFILE_NAME = 67, OPTION_AUTOCONF = 116,
			OPTION_END = (byte) 0xff;

	/* Append an option to the packet */
	public void AppendOption(Option op) {
		options.add(new Option(op.opcode, op.data));
	}

	/* Append an option to the packet (variable length data) */
	public void AppendOption(byte opcode, byte[] data) {
		options.add(new Option(opcode, data));
	}

	/* Append an option to the packet (no data) */
	public void AppendOption0B(byte opcode) {
		options.add(new Option(opcode, new byte[0]));
	}

	/* Append an option to the packet (byte data) */
	public void AppendOption1B(byte opcode, byte data) {
		options.add(new Option(opcode, new byte[] { data }));
	}

	/* Append an option to the packet (dbyte data) */
	public void AppendOption2B(byte opcode, short data) {
		options.add(new Option(opcode, new byte[] {
				(byte) ((data >> 8) & 0xff), (byte) (data & 0xff) }));
	}

	/* Append an option to the packet (qbyte data) */
	public void AppendOption4B(byte opcode, int data) {
		options.add(new Option(opcode, new byte[] {
				(byte) ((data >>> 24) & 0xff), (byte) ((data >>> 16) & 0xff),
				(byte) ((data >>> 8) & 0xff), (byte) (data & 0xff) }));
	}

	/* Append an option to the packet (null terminated string data) */
	public void AppendOptionNTS(byte opcode, String str) {
		byte[] data = new byte[str.length() + 1];
		for (int i = 0; i < str.length(); i++)
			data[i] = (byte) str.charAt(i);
		data[str.length()] = 0;
		options.add(new Option(opcode, data));
	}

	/* A DHCP option */
	public class Option {
		public final byte opcode;
		public final byte[] data;
		public final int len;
		private final ByteBuffer buff;

		/* Functions to read the raw data into various types */
		/* Byte */
		public byte data1B() {
			if (data.length >= 1)
				return buff.get(0);
			else
				return 0;
		}

		/* Dbyte */
		public short data2B() {
			if (data.length >= 2)
				return buff.getShort(0);
			else
				return 0;
		}

		/* Qbyte */
		public int data4B() {
			if (data.length >= 4)
				return buff.getInt(0);
			else
				return 0;
		}

		/* Null terminated string */
		public String dataNTS() {
			char[] s = new char[data.length];
			int len = 0;
			for (int i = 0; i < data.length; i++)
				if (data[i] == 0)
					break;
				else {
					s[i] = (char) data[i];
					len++;
				}
			return new String(s, 0, len);
		}

		/* Create an option from raw binary data */
		public Option(byte opcode, byte[] data) {
			this.opcode = opcode;
			if (data == null) {
				this.data = new byte[0];
				this.len = 0;
			} else {
				this.data = data;
				this.len = data.length;
			}
			this.buff = ByteBuffer.wrap(this.data);
		}

	}

	/* Get an option or null if it doesn't exist */
	public Option Options(byte opcode) {
		for (Option op : options)
			if (op.opcode == opcode)
				return op;
		return null;
	}

	/* Get or create an option */
	public Option Options(byte opcode, byte[] defaultvalue) {
		for (Option op : options)
			if (op.opcode == opcode)
				return op;
		return new Option(opcode, defaultvalue);
	}

	/* DHCP message types */
	public static final byte MSGTYPE_DHCPDISCOVER = 1, MSGTYPE_DHCPOFFER = 2,
			MSGTYPE_DHCPREQUEST = 3, MSGTYPE_DHCPDECLINE = 4,
			MSGTYPE_DHCPACK = 5, MSGTYPE_DHCPNAK = 6, MSGTYPE_DHCPRELEASE = 7,
			MSGTYPE_DHCPINFORM = 8;

	/* Returns a MSGTYPE_* enum value or zero if no messagetype option was found */
	public byte MessageType() {
		Option op;
		if ((op = Options(OPTION_DHCP_MESSAGE_TYPE)) != null)
			if (op.data.length == 1)
				return op.data[0];
		return 0;
	}

	/* String representation of the packet, for debugging */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("op=%d, htype=%d, hlen=%d, hops=%d\n", op,
				htype, hlen, hops));
		sb.append(String.format("xid=%s\n",
				Long.toHexString(0x100000000L | ((xid) & 0xffffffffL))
						.substring(1)));
		sb.append(String.format("secs=%s, flags=%s\n",
				Long.toHexString(0x10000L | ((secs) & 0xffffL)).substring(1),
				Long.toHexString(0x10000L | ((flags) & 0xffffL)).substring(1)));
		sb.append(String.format("ciaddr=%s\n", IP.IntToStr(ciaddr)));
		sb.append(String.format("yiaddr=%s\n", IP.IntToStr(yiaddr)));
		sb.append(String.format("siaddr=%s\n", IP.IntToStr(siaddr)));
		sb.append(String.format("giaddr=%s\n", IP.IntToStr(giaddr)));
		String chaddrs = "";
		for (int i = 0; i < 4; i++)
			chaddrs += Long.toHexString(
					0x100000000L | ((chaddr[i]) & 0xffffffffL)).substring(1);
		sb.append(String.format("chaddr=%s\n", chaddrs));
		sb.append(String.format("bootp_host=%s\n", bootp_host));
		sb.append(String.format("bootp_file=%s\n", bootp_file));
		sb.append(String.format("cookie=%s\n",
				Long.toHexString(0x100000000L | ((cookie) & 0xffffffffL))
						.substring(1)));
		/* Options */
		sb.append("options=\n");
		for (Option opt : options) {
			sb.append(String.format("\tcode=%d - %s\n", opt.opcode,
					OptionDefinitions.Name(opt.opcode)));
			sb.append(String.format("\tlength=%d\n", opt.buff.limit()));
			if (opt.len > 0) {
				String hexbytes = "";
				for (int i = 0; i < opt.len; i++)
					hexbytes += ((i != 0) ? "," : "")
							+ Integer.toHexString(
									0x100 | ((opt.data[i]) & 0xff))
									.substring(1);
				sb.append(String.format("\tdata=%s", hexbytes));
				if (opt.len == 4) {
					sb.append(" / " + IP.IntToStr(opt.data4B()));
					sb.append(" / " + opt.data4B());
				} else if (opt.len == 2)
					sb.append(" / " + opt.data2B());
				else if (opt.len == 1)
					sb.append(" / " + opt.data1B());
				sb.append(" / " + opt.dataNTS());
			}
			sb.append("\n\n");
		}
		return sb.toString();
	}

}
