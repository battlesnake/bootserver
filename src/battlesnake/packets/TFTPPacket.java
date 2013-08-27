package battlesnake.packets;

import java.nio.ByteBuffer;
import java.util.Map;

import battlesnake.utils.CaseInsensitiveHashMap;

/* A TFTP packet */
public class TFTPPacket extends Packet {

	/*
	 * TFTP opcodes:
	 * - RRQ = Read ReQuest
	 * - WRQ = Write ReQuest
	 * - ACK = Acknowledge
	 * - OACK = Option Acknowledge
	 */
	public static final int OP_RRQ = 1, OP_WRQ = 2, OP_DATA = 3, OP_ACK = 4, OP_ERROR = 5, OP_OACK = 6;
	public int op;
	/* Used for RRQ/WRQ */
	public String filename;
	public String mode;
	public CaseInsensitiveHashMap<String> options = new CaseInsensitiveHashMap<String>();
	/* Used for ACK/DATA */
	public int block_id;
	/* Used for DATA */
	public ByteBuffer data;
	/* Used for ERROR */
	public int ErrorCode;
	public String ErrorMessage;

	public TFTPPacket() {
	}

	/* Decode a binary packet into this object */
	@Override
	public void Decode(ByteBuffer source) {
		op = source.getShort();
		if (op == OP_RRQ || op == OP_WRQ) {
			filename = DecodeNTS(source);
			mode = DecodeNTS(source);
			while (source.remaining() > 0) {
				String k = DecodeNTS(source);
				String v = DecodeNTS(source);
				options.put(k, v);
			}
		}
		else if (op == OP_DATA) {
			block_id = source.getShort();
			data.put(source);
			data.flip();
		}
		else if (op == OP_ACK)
			block_id = source.getShort();
		else if (op == OP_ERROR) {
			ErrorCode = source.getShort();
			ErrorMessage = DecodeNTS(source);
		}
		else if (op == OP_OACK)
			while (source.remaining() > 0) {
				String k = DecodeNTS(source);
				String v = DecodeNTS(source);
				options.put(k, v);
			}
	}

	/* Decode a binary packet into a new object */
	public TFTPPacket(ByteBuffer source) {
		Decode(source);
	}

	/* Encode this packet to a binary blob */
	@Override
	public ByteBuffer Encode() {
		int len = 1024;
		if (data != null) {
			len += data.limit();
			data.rewind();
		}
		ByteBuffer pkt = ByteBuffer.allocate(len);
		pkt.putShort((short) op);
		if (op == OP_RRQ || op == OP_WRQ) {
			pkt.put(filename.getBytes(Encoding));
			pkt.put((byte) 0);
			pkt.put(mode.getBytes(Encoding));
			pkt.put((byte) 0);
			if (options != null && options.size() > 0)
				for (Map.Entry<String, String> opt : options.entrySet()) {
					pkt.put(opt.getKey().getBytes(Encoding));
					pkt.put((byte) 0);
					pkt.put(opt.getValue().getBytes(Encoding));
					pkt.put((byte) 0);
				}
		}
		else if (op == OP_DATA) {
			pkt.putShort((short) block_id);
			if (data != null)
				pkt.put(data);
		}
		else if (op == OP_ACK)
			pkt.putShort((short) block_id);
		else if (op == OP_ERROR) {
			pkt.putShort((short) ErrorCode);
			pkt.put(ErrorMessage.getBytes(Encoding));
			pkt.put((byte) 0);
		}
		else if (op == OP_OACK)
			for (Map.Entry<String, String> opt : options.entrySet()) {
				String k = opt.getKey();
				String v = opt.getValue();
				pkt.put(k.getBytes(Encoding));
				pkt.put((byte) 0);
				pkt.put(v.getBytes(Encoding));
				pkt.put((byte) 0);
			}
		pkt.flip();
		return pkt;
	}

	/* Create a TFTP packet */
	private TFTPPacket(int opcode, String filename, String mode, int block_id, ByteBuffer data, int ErrorCode, String ErrorMessage) {
		this.op = opcode;
		this.filename = filename;
		this.mode = mode;
		this.block_id = block_id;
		this.data = data;
		this.ErrorCode = ErrorCode;
		this.ErrorMessage = ErrorMessage;
	}

	/* Set a load of options */
	private TFTPPacket ParseOpts(String[] k, String[] v) throws Exception {
		if (k.length != v.length)
			throw new Exception("TFTP option list does not match length of value list");
		for (int i = 0; i < k.length; i++)
			options.put(k[i], v[i]);
		return this;
	}

	/* Create a read request packet */
	public static TFTPPacket RRQ(String filename, String mode) {
		return new TFTPPacket(OP_RRQ, filename, mode, 0, null, 0, null);
	}

	/* Create a write request packet */
	public static TFTPPacket WRQ(String filename, String mode) {
		return new TFTPPacket(OP_WRQ, filename, mode, 0, null, 0, null);
	}

	/* Create a read request packet with options */
	public static TFTPPacket RRQ(String filename, String mode, String[] options, String[] values) throws Exception {
		return new TFTPPacket(OP_RRQ, filename, mode, 0, null, 0, null).ParseOpts(options, values);
	}

	/* Create a write request packet with options */
	public static TFTPPacket WRQ(String filename, String mode, String[] options, String[] values) throws Exception {
		return new TFTPPacket(OP_WRQ, filename, mode, 0, null, 0, null).ParseOpts(options, values);
	}

	/* Create a data packet */
	public static TFTPPacket DATA(int block_id, ByteBuffer data) {
		return new TFTPPacket(OP_DATA, null, null, block_id, data, 0, null);
	}

	/* Create an acknowledgement packet */
	public static TFTPPacket ACK(int block_id) {
		return new TFTPPacket(OP_ACK, null, null, block_id, null, 0, null);
	}

	/* Create an error packet */
	public static TFTPPacket ERROR(int ErrorCode, String ErrorMessage) {
		return new TFTPPacket(OP_ERROR, null, null, 0, null, ErrorCode, ErrorMessage);
	}

	/* Create an error packet */
	public static TFTPPacket ERROR(int ErrorCode) {
		return new TFTPPacket(OP_ERROR, null, null, 0, null, ErrorCode, GetErrorText(ErrorCode));
	}

	/* Create an option acknowledgement packet */
	public static TFTPPacket OACK(String[] options, String[] values) throws Exception {
		return new TFTPPacket(OP_OACK, null, null, 0, null, 0, null).ParseOpts(options, values);
	}

	/* TFTP error codes */
	public static final int ERROR_UNDEFINED = 0, ERROR_FILE_NOT_FOUND = 1, ERROR_ACCESS_VIOLATION = 2, ERROR_DISK_FULL = 3, ERROR_ILLEGAL_OPERATION = 4, ERROR_UNKNOWN_TID = 5,
			ERROR_FILE_ALREADY_EXISTS = 6, ERROR_NO_SUCH_USER = 7, ERROR_OPTION_NEGOTIATION = 8;

	/* Error code to string */
	public static String GetErrorText(int ErrorCode) {
		String[] Errors = new String[] { "Not defined, see error message (if any)", "File not found", "Access violation", "Disk full or allocation exceeded", "Illegal TFTP operation",
				"Unknown transfer ID", "File already exists", "No such user", "Option negotiation error" };
		if (ErrorCode < Errors.length && ErrorCode >= 0)
			return Errors[ErrorCode];
		else
			return "Unknown TFTP error code <" + ErrorCode + ">";
	}

	/* Opcode to string */
	public static String GetOpText(int op) {
		String[] ops = new String[] { "Read request (RRQ)", "Write request (WRQ)", "Data (DATA)", "Acknowledgment (ACK)", "Error (ERROR)", "Option acknowledgement (OACK)" };
		op--;
		if (op < ops.length && op >= 0)
			return ops[op];
		else
			return "opcode <" + op + ">";
	}

	/* String representation of packet, useful for debugging */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("op=%s (%d)\n", GetOpText(op), op));
		if (op == OP_RRQ || op == OP_WRQ) {
			sb.append(String.format("filename=%s\n", filename));
			sb.append(String.format("mode=%s\n", mode));
		}
		else if (op == OP_DATA) {
			sb.append(String.format("blockid=%d\n", block_id));
			if (data.limit() > 0) {
				String hexbytes = "";
				String ascbytes = "";
				for (int i = 0; i < data.limit() && i < 32; i++) {
					byte b = data.get(i);
					hexbytes += (i != 0 ? "," : "") + Integer.toHexString(0x100 | b & 0xff).substring(1);
					ascbytes += b >= 32 && b < 128 ? (char) b : "?";
				}
				sb.append(String.format("data=%d bytes [ASCII: %s] [HEX: %s]\n", data.limit(), ascbytes, hexbytes));
			}
			else
				sb.append("data=0 bytes");
		}
		else if (op == OP_ACK)
			sb.append(String.format("blockid=%d\n", block_id));
		else if (op == OP_ERROR) {
			sb.append(String.format("errorcode=%d\n", ErrorCode));
			sb.append(String.format("errortext=%s\n", ErrorMessage));
		}
		if ((op == OP_OACK || op == OP_RRQ || op == OP_WRQ) && options.size() > 0) {
			sb.append("options=\n");
			for (Map.Entry<String, String> opt : options.entrySet())
				sb.append(String.format("\t%s=%s\n", opt.getKey(), opt.getValue()));
		}
		return sb.toString();
	}

	/* Set the block-size option */ 
	public void SetBlockSize(int BlockSize) {
		if (op == OP_RRQ || op == OP_WRQ || op == OP_OACK)
			options.put(OPTION_BLOCKSIZE, ((Integer) BlockSize).toString());
	}

	/* Does the packet have any options set? */
	public boolean hasOptions() {
		return options != null && options.size() > 0;
	}

	/* Does the packet a specific options set? */
	public boolean hasOption(String name) {
		return options != null && options.containsKey(name);
	}

	/* Get the value of an option */
	public String getOption(String name) {
		return options.get(name);
	}

	/* Set the value of an option */
	public void setOption(String name, String value) {
		options.put(name, value);

	}

	/* ??? */
	public final static String OPTION_FILESIZE = "tsize";
	public final static String OPTION_BLOCKSIZE = "blksize";
	/* Ethernet MTU */
	public final static int BLOCKSIZE_ETHERNET = 1428;

}
