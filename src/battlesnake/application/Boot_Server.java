package battlesnake.application;

import java.io.File;
import java.io.IOException;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import battlesnake.bootserver.Configuration;
import battlesnake.bootserver.DHCP;
import battlesnake.bootserver.TFTP;
import battlesnake.logging.Entry;
import battlesnake.logging.Listener;
import battlesnake.logging.Log;
import battlesnake.packets.IP;
import battlesnake.utils.Input;
import battlesnake.utils.Network;

public class Boot_Server {

	/* Server configuration file */
	static final String conf = "config.conf";

	/* Network interface and address to bind to */
	static NetworkInterface intf;
	static InterfaceAddress addr;

	/*
	 * Lists the available network interfaces and asks the user to select
	 * one
	 */
	static NetworkInterface GetConfigInterface() throws SocketException, IOException {
		Map<String, NetworkInterface> choices = new HashMap<String, NetworkInterface>();
		for (NetworkInterface intf : Collections.list(NetworkInterface.getNetworkInterfaces())) {
			choices.put(intf.getName() + " (" + intf.getDisplayName() + ")", intf);
		}
		intf = Input.Choice("Available network interfaces", "Select network interface to bind to", choices, null);
		Configuration.network_interface = intf.getName();
		return intf;
	}

	/*
	 * Lists the addresses bound to the selected interface and asks the user
	 * to select one
	 */
	static InterfaceAddress GetConfigAddress(NetworkInterface intf) throws SocketException, IOException {
		Map<String, InterfaceAddress> choices = new HashMap<String, InterfaceAddress>();
		for (InterfaceAddress addr : intf.getInterfaceAddresses()) {
			choices.put(addr.getAddress().getHostAddress() + "/" + addr.getNetworkPrefixLength(), addr);
		}
		addr = Input.Choice("Addresses bound to this interface", "Select address to bind to", choices, null);
		Configuration.network_address = IP.BytesToInt(addr.getAddress().getAddress());
		return addr;
	}

	/* Asks the user to choose the interface and the address to bind to */
	static void GetConfig() throws Exception {
		NetworkInterface intf = GetConfigInterface();
		InterfaceAddress addr = GetConfigAddress(intf);
		addr.toString();
	}

	public static void main(String[] args) {
		new Boot_Server().Run(args);
	}

	/* Automatic configuration, fills in the blanks */
	static void AutoConfig() {
		String app = "Auto-configure";
		/* Default DHCP server parameters */
		if (Configuration.dhcp_subnet_mask == 0) {
			Configuration.dhcp_subnet_mask = ~(0xffffffff >>> addr.getNetworkPrefixLength());
			Log.Add(app, Log.TYPE_INFO, "dhcp_subnet_mask = " + IP.IntToStr(Configuration.dhcp_subnet_mask));
		}
		if (Configuration.dhcp_pool_address == 0) {
			Configuration.dhcp_pool_address = Configuration.network_address & Configuration.dhcp_subnet_mask;
			Log.Add(app, Log.TYPE_INFO, "dhcp_pool_address = " + IP.IntToStr(Configuration.dhcp_pool_address));
		}
		if (Configuration.dhcp_pool_offset == 0) {
			Configuration.dhcp_pool_offset = 20;
			Log.Add(app, Log.TYPE_INFO, "dhcp_pool_offset = " + Configuration.dhcp_pool_offset);
		}
		if (Configuration.dhcp_pool_length == 0) {
			Configuration.dhcp_pool_length = 20;
			Log.Add(app, Log.TYPE_INFO, "dhcp_pool_length = " + Configuration.dhcp_pool_length);
		}
		/*
		 * Parses DHCP optional addresses, replacing 127.0.0.1 with the
		 * server IP address
		 */
		if (Configuration.dhcp_opt_gateway_address == 0x7f000001) {
			Configuration.dhcp_opt_gateway_address = Configuration.network_address;
			Log.Add(app, Log.TYPE_INFO, "dhcp_opt_gateway_address = " + IP.IntToStr(Configuration.dhcp_opt_gateway_address));
		}
		if (Configuration.dhcp_opt_dns_server_address == 0x7f000001) {
			Configuration.dhcp_opt_dns_server_address = Configuration.network_address;
			Log.Add(app, Log.TYPE_INFO, "dhcp_opt_dns_server_address = " + IP.IntToStr(Configuration.dhcp_opt_dns_server_address));
		}
		if (Configuration.dhcp_opt_time_server_address == 0x7f000001) {
			Configuration.dhcp_opt_time_server_address = Configuration.network_address;
			Log.Add(app, Log.TYPE_INFO, "dhcp_opt_time_server_address = " + IP.IntToStr(Configuration.dhcp_opt_time_server_address));
		}
		/*
		 * Parses DHCP optional addresses, replacing enabled-but-not-set
		 * (i.e. ==1) with the default gateway address
		 */
		int Gateway = Network.getGateway();
		if ((Gateway & Configuration.dhcp_subnet_mask) != (Configuration.network_address & Configuration.dhcp_subnet_mask)) {
			Gateway = 0;
		}
		if (Gateway != 0) {
			if (Configuration.dhcp_opt_gateway_address == 1) {
				Configuration.dhcp_opt_gateway_address = Gateway;
				Log.Add(app, Log.TYPE_INFO, "dhcp_opt_gateway_address = " + IP.IntToStr(Configuration.dhcp_opt_gateway_address));
			}
			if (Configuration.dhcp_opt_dns_server_address == 1) {
				Configuration.dhcp_opt_dns_server_address = Gateway;
				Log.Add(app, Log.TYPE_INFO, "dhcp_opt_dns_server_address = " + IP.IntToStr(Configuration.dhcp_opt_dns_server_address));
			}
			if (Configuration.dhcp_opt_time_server_address == 1) {
				Configuration.dhcp_opt_time_server_address = Gateway;
				Log.Add(app, Log.TYPE_INFO, "dhcp_opt_time_server_address = " + IP.IntToStr(Configuration.dhcp_opt_time_server_address));
			}
		}
		/*
		 * Default TFTP root folder = /TFTP or \TFTP depending on
		 * operating system
		 */
		if (Configuration.tftp_root_folder == "") {
			Configuration.tftp_root_folder = File.pathSeparator + "TFTP";
			Log.Add(app, Log.TYPE_INFO, "tftp_root_folder = " + Configuration.tftp_root_folder);
		}
	}

	/* Load and validate the configuration */
	private void prepareConfiguration() throws Exception {
		/*
		 * Load server configuration
		 */
		Log.Add(this, Log.TYPE_INFO, "Loading configuration from " + conf);
		Log.Indent();
		Configuration.Load(conf);
		Log.Unindent();
		System.out.print("\n");
		/*
		 * Get interface and address from user
		 */
		GetConfig();
		Log.Add(this, Log.TYPE_INFO, "Autoconfigure");
		Log.Indent();
		/*
		 * Fill in the remaining blanks of the configuration
		 */
		AutoConfig();
		Log.Unindent();
		/*
		 * Let the user know that they can save the configuration
		 */
		System.out.print("Press <S> then <ENTER> to save the configuration.\n\n");
		System.out.print("Press <C> then <ENTER> to exit.\n\n");
		/*
		 * Ensure that we actually have a server to run
		 */
		if (!(Configuration.dhcp_enabled || Configuration.tftp_enabled))
			throw new Exception("No servers are enabled in the configuration");
	}

	/* Start the necessary server(s) */
	private void startServers(DHCP dhcp, TFTP tftp) throws Exception {
		if (Configuration.dhcp_enabled) {
			dhcp.Start(true);
		}
		if (Configuration.tftp_enabled) {
			tftp.Start(true);
		}
	}

	/* Run until a server stops */
	private void runServers(DHCP dhcp, TFTP tftp) throws Exception {
		while ((dhcp.getRunning() || !Configuration.dhcp_enabled) && (tftp.getRunning() || !Configuration.tftp_enabled)) {
			Thread.sleep(100);
			char c = Character.toUpperCase((char) System.in.read());
			/* User wants to close the servers */
			if (c == 'C') {
				dhcp.Stop();
				tftp.Stop();
			}
			/* User wants to save the configuration */
			else if (c == 'S') {
				Configuration.Save(conf);
				Log.Add(this, Log.TYPE_INFO, "Configuration saved to " + conf);
			}
			/* User wants to see the DHCP map */
			else if (c == 'M') {
				if (dhcp.getRunning()) {
					dhcp.ShowMaps();
				}
				System.out.print("\n");
			}
		}
	}

	private void Run(String[] args) {
		/* Create a log listener to copy log entries to STDERR */
		Listener listener = new Listener() {
			@Override
			public void OnMessage(Entry msg) {
				System.err.print(msg.toString() + "\n");
			}
		};
		Log.Subscribe(listener);
		try {
			System.out.print("Mark's boot (DHCP+TFTP) server\n\n");
			try {
				/* Load and validate the configuration */
				prepareConfiguration();
				/* Create the servers */
				DHCP dhcp = new DHCP();
				TFTP tftp = new TFTP();
				/* Start the necessary server(s) */
				startServers(dhcp, tftp);
				/* Run until a server stops */
				runServers(dhcp, tftp);
			}
			/* Oops */
			catch (Exception e) {
				boolean DEBUG = true;
				Log.Add(this, Log.TYPE_FATAL, "Error: " + e.getMessage());
				if (DEBUG)
					e.printStackTrace();
			}
		}
		finally {
			Log.Unsubscribe(listener);
		}
	}
}
