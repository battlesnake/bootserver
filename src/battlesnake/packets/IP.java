package battlesnake.packets;

import java.net.InetAddress;

/* IPv4 address conversion utilities */
public class IP {

	public static byte[] IntToBytes(int a) {
		byte[] b = new byte[4];
		b[0] = (byte) ((a >> 24) & 0xff);
		b[1] = (byte) ((a >> 16) & 0xff);
		b[2] = (byte) ((a >> 8) & 0xff);
		b[3] = (byte) (a & 0xff);
		return b;
	}

	public static int BytesToInt(byte[] a) {
		return ((a[0] << 24) & 0xff000000) | ((a[1] << 16) & 0x00ff0000)
				| ((a[2] << 8) & 0x0000ff00) | (a[3] & 0x000000ff);
	}

	public static String IntToStr(int a) {
		return BytesToStr(IntToBytes(a));
	}

	public static String BytesToStr(byte[] a) {
		try {
			return (a[0] & 0xff) + "." + (a[1] & 0xff) + "." + (a[2] & 0xff)
					+ "." + (a[3] & 0xff);
		} catch (Exception e) {
			return "";
		}
	}

	public static int StrToInt(String a) {
		return BytesToInt(StrToBytes(a));
	}

	public static byte[] StrToBytes(String a) {
		try {
			return InetAddress.getByName(a).getAddress();
		} catch (Exception e) {
			return new byte[] { 0, 0, 0, 0 };
		}
	}
}
