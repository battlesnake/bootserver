package battlesnake.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import battlesnake.packets.IP;

/* Network utilities */
public class Network {

	/* A route entry */
	public static class ActiveRoute {
		public final int dest;
		public final int subnet;
		public final int router;
		@SuppressWarnings("unused")
		private final int iface;
		@SuppressWarnings("unused")
		private final int metric;

		private ActiveRoute(String dest, String subnet, String router, String iface, String metric) {
			this.dest = IP.StrToInt(dest);
			this.subnet = IP.StrToInt(subnet);
			this.router = IP.StrToInt(router);
			this.iface = IP.StrToInt(iface);
			this.metric = Integer.valueOf(metric);
		}
	}

	/*
	 * Returns an array of routes
	 */
	public static ActiveRoute[] getActiveRoutes() {
		try {
			BufferedReader output = eval("netstat -rn");
			List<ActiveRoute> result = new ArrayList<ActiveRoute>();
			String line;
			final int MODE_LINUX = 1, MODE_WINDOWS = 2;
			int mode = 0;
			while ((line = output.readLine()) != null) {
				if (mode == 0) {
					/* Windows start of list */
					if (line.replace("\t", "").replace(" ", "").equalsIgnoreCase("NetworkDestinationNetmaskGatewayInterfaceMetric"))
						mode = MODE_WINDOWS;
					/* Linux start of list */
					if (line.replace("\t", "").replace(" ", "").equalsIgnoreCase("DestinationGatewayGenmaskFlagsMSSWindowirttIface"))
						mode = MODE_LINUX;
					continue;
				}
				String dest, subnet, gate, local, metric;
				if (mode == MODE_WINDOWS) {
					/* Windows end of list */
					if (line.toLowerCase().startsWith("default gateway:"))
						break;
					dest = line.substring(0, 17).trim();
					subnet = line.substring(17, 17).trim();
					gate = line.substring(34, 17).trim();
					local = line.substring(51, 17).trim();
					metric = line.substring(68).trim();
				}
				else if (mode == MODE_LINUX) {
					String[] fields = line.split("\t");
					dest = fields[0];
					gate = fields[1];
					subnet = fields[2];
					local = ""; /* Not given by Linux netstat */
					metric = ""; /* Use route -n if you must have the metric on Linux */
				}
				else
					continue;
				result.add(new ActiveRoute(dest, subnet, gate, local, metric));
			}
			return result.toArray(new ActiveRoute[] {});
		}
		catch (IOException e) {
			return null;
		}
	}

	/* Gets the default gateway */
	public static int getGateway() {
		try {
			BufferedReader output = eval("netstat -rn");
			String line;
			while ((line = output.readLine()) != null) {
				line = line.toLowerCase().replaceAll("\\s+", "\t");
				/* Windows */
				if (line.startsWith("default gateway:"))
					return IP.StrToInt(line.substring(17).trim());
				/* Unix, Linux, Crapintosh */
				else if (line.startsWith("0.0.0.0") || line.startsWith("default"))
					return IP.StrToInt(line.split("\t")[1]);
			}
			return 0;
		}
		catch (IOException e) {
			return 0;
		}
	}

	/* Runs a shell command */
	private static BufferedReader eval(String command) throws IOException {
		return new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(command).getInputStream()));
	}

}
