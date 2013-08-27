package battlesnake.bootserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;

import battlesnake.logging.Log;
import battlesnake.packets.IP;

/*
 * Maintains the server configuration in static fields, and contains a
 * Serializer class which can load/save the contents of these fields to a
 * configuration file
 */
public class Configuration {
	/* Device to bind to */
	public static String network_interface = "";
	public static int network_address = 0;
	/* DHCP server parameters */
	public static boolean dhcp_enabled = true;
	public static boolean dhcp_log_packets = false;
	public static boolean dhcp_authoritive = true;
	public static short dhcp_server_port = 67;
	public static short dhcp_client_port = 68;
	public static int dhcp_pool_address = 0;
	public static int dhcp_pool_offset = 0;
	public static int dhcp_pool_length = 0;
	public static int dhcp_subnet_mask = 0;
	public static boolean dhcp_enforce_subnet = true;
	public static boolean dhcp_broadcasts_only = true;
	public static int dhcp_reply_delay = 0;
	/* Other DHCP parameters */
	public static int dhcp_opt_time_server_address = 0;
	public static int dhcp_opt_gateway_address = 0;
	public static int dhcp_opt_dns_server_address = 0;
	public static String dhcp_opt_dns_domain = "";
	public static int dhcp_opt_boot_server_address = 0;
	/* TFTP server */
	public static boolean tftp_enabled = true;
	public static short tftp_server_port = 69;
	public static boolean tftp_log_packets = false;
	/* TFTP root folder */
	public static String tftp_root_folder = "/tftp";
	/*
	 * Windows symlink hack for TFTP server:
	 * 
	 * Some Linux network install tarballs contain symbolic links, which
	 * obviously don't work on Windows are will just appear to be text files
	 * containing the path to the target file. This symlink hack attempts to
	 * identify and follow such files, allowing network installers for
	 * various Linux distributions to be served from a Windows host.
	 * 
	 * The heuristic to detect these symlinks is as follows:
	 * - File size < 200 bytes
	 * - File contents ~= /^[A-Za-z0-9-_\.]+$/
	 * 
	 * The contents of a matching file are treated as a relative path and
	 * the link is followed by the server.
	 * 
	 * -TODO: ensure that the path parser follows each path element as it
	 * decodes them, so it can follow symlinks to directories.
	 */
	public static boolean tftp_windows_symlink_hack = true;
	/* BOOTP/DHCP filename */
	public static String boot_file = "pxelinux.0";
	public static int boot_file_size = 0;

	public static boolean Load(String filename) {
		return Serializer.Load(filename);
	}

	public static boolean Save(String filename) {
		return Serializer.Save(filename);
	}

	/* Serializer for static fields */
	private static class Serializer {

		private Map<String, Object> fields = new TreeMap<String, Object>();

		/* Field represents an IP address (or something else with the dotted decimal format) */
		static boolean isIp(String k, Class<?> c) {
			return (k.endsWith("_address") || k.endsWith("net_mask")) && (c.equals(int.class) || c.equals(Integer.class));
		}

		/* Save configuration to file */
		public static boolean Save(String filename) {
			try {
				Serializer ser = Serializer.ReadSettingsFromObject();
				BufferedWriter buf = new BufferedWriter(new FileWriter(filename));
				for (Map.Entry<String, Object> f : ser.fields.entrySet()) {
					String k = f.getKey();
					Object v = f.getValue();
					/* Handle IP addresses */
					if (isIp(k, v.getClass())) {
						v = IP.IntToStr((Integer) v);
					}
					buf.write(k + "=" + v.toString());
					buf.newLine();
				}
				buf.close();
				return true;
			}
			catch (Exception e) {
				Log.Add("Configuration", Log.TYPE_ERR, e.getMessage());
				return false;
			}
		}

		/* Load configuration from file */
		public static boolean Load(String filename) {
			try {
				Serializer ser = Serializer.ReadSettingsFromObject();
				BufferedReader buf = new BufferedReader(new FileReader(filename));
				String s;
				while ((s = buf.readLine()) != null) {
					String[] f = s.split("=", 2);
					if (ser.fields.containsKey(f[0])) {
						ser.fields.put(f[0], f[1]);
						Log.Add("Configuration", Log.TYPE_INFO, f[0] + " = " + f[1]);
					}
					else {
						Log.Add("Configuration", Log.TYPE_ERR, "Configuration key " + f[0] + " not found");
					}
				}
				buf.close();
				ser.WriteSettingsToObject();
				return true;
			}
			catch (Exception e) {
				Log.Add("Configuration", Log.TYPE_ERR, e.getMessage());
				return false;
			}
		}

		/* Get the name of a field */
		private static String fieldname(Field field) {
			return field.getName();
		}

		/* Create a serializer and set the values to those of the configuration */
		private static Serializer ReadSettingsFromObject() {
			Serializer me = new Serializer();
			for (Field field : Configuration.class.getFields()) {
				try {
					if ((field.getModifiers() & Modifier.PUBLIC) != 0 && (field.getType().isPrimitive() || field.getType().equals(String.class))) {
						me.fields.put(fieldname(field), field.get(Configuration.class));
					}
				}
				catch (Exception e) {
					Log.Add(me, Log.TYPE_ERR, e.getMessage());
				}
			}
			return me;
		}

		/* Set the configuration */ 
		private void WriteSettingsToObject() {
			for (Field field : Configuration.class.getFields()) {
				try {
					String k = fieldname(field);
					if (fields.containsKey(k)) {
						Object v = fields.get(k);
						// Handle IP addresses
						if (isIp(k, field.getType())) {
							v = IP.StrToInt((String) v);
						}
						Class<?> c = field.getType();
						if (c.equals(double.class)) {
							field.set(Configuration.class, Double.valueOf(v.toString()));
						}
						else if (c.equals(float.class)) {
							field.set(Configuration.class, Float.valueOf(v.toString()));
						}
						else if (c.equals(long.class)) {
							field.set(Configuration.class, Long.valueOf(v.toString()));
						}
						else if (c.equals(int.class)) {
							field.set(Configuration.class, Integer.valueOf(v.toString()));
						}
						else if (c.equals(short.class)) {
							field.set(Configuration.class, Short.valueOf(v.toString()));
						}
						else if (c.equals(byte.class)) {
							field.set(Configuration.class, Byte.valueOf(v.toString()));
						}
						else if (c.equals(boolean.class)) {
							field.set(Configuration.class, Boolean.valueOf(v.toString()));
						}
						else if (c.equals(char.class)) {
							field.set(Configuration.class, (char) (int) Integer.valueOf(v.toString()));
						}
						else if (c.equals(String.class)) {
							field.set(Configuration.class, v.toString());
						}
						else {
							Log.Add("Configuration", Log.TYPE_ERR, "Unknown data type: " + c.getSimpleName());
						}
					}
				}
				catch (Exception e) {
					Log.Add(this, Log.TYPE_ERR, e.getMessage());
				}
			}
		}
	}

}
