package battlesnake.bootserver;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import battlesnake.logging.Log;
import battlesnake.packets.DHCPPacket;
import battlesnake.packets.IP;
import battlesnake.simpleservers.UDPServer;
import battlesnake.utils.Path;

/* DHCP server */
public class DHCP extends UDPServer {

	/* Map of client name <==> address */
	private Map<String, Integer> clients = new HashMap<String, Integer>();

	/*
	 * Allocate an address from the pool for client
	 * identified by id, or
	 * return its current address if one is assigned
	 */
	private int AllocateIP(String id) {
		int tryip;
		if (clients.containsKey(id))
			return clients.get(id);
		else {
			for (int offset = Configuration.dhcp_pool_offset; offset < Configuration.dhcp_pool_offset + Configuration.dhcp_pool_length; offset++) {
				tryip = Configuration.dhcp_pool_address + offset;
				try {
					if (CheckIPAvail(tryip, "<no reassingment>"))
						return tryip;
				}
				catch (Exception e) {
					/*
					 * Dirty coding, ignores errors caused
					 * by invalid addresses or unavailable
					 * addresses
					 */
				}
			}
			return 0;
		}
	}

	/* Append the DHCP options in the reply packet with the boot parameters */
	private void AppendBootOptions(DHCPPacket pkt, DHCPPacket rep) {
		/*
		 * If a boot image does not exist, don't bother setting boot
		 * parameters
		 */
		if (Configuration.boot_file != "") {
			/* Set boot image filename */
			rep.AppendOptionNTS(DHCPPacket.OPTION_BOOTFILE_NAME, Configuration.boot_file);
			pkt.bootp_file = Configuration.boot_file + "\0";
			/*
			 * Set boot image size from configuration or otherwise
			 * calculate it on the fly
			 */
			File bootfile = new File(Path.Join(new String[] { Configuration.tftp_root_folder, Configuration.boot_file }));
			if (Configuration.boot_file_size != 0)
				rep.AppendOption4B(DHCPPacket.OPTION_BOOTFILESIZE, Configuration.boot_file_size);
			else if (bootfile.exists())
				rep.AppendOption4B(DHCPPacket.OPTION_BOOTFILESIZE, (int) bootfile.length());
			/* Set address of boot server */
			int bootaddr = 0;
			if (Configuration.tftp_enabled && Configuration.network_address != 0
					&& (Configuration.dhcp_opt_boot_server_address == 0 || Configuration.dhcp_opt_boot_server_address == Configuration.network_address))
				bootaddr = Configuration.network_address;
			else if (Configuration.dhcp_opt_boot_server_address != 0)
				bootaddr = Configuration.dhcp_opt_boot_server_address;
			/* Set address of TFTP server */
			if (bootaddr != 0) {
				rep.AppendOption4B(DHCPPacket.OPTION_TFTP_SERVER, bootaddr);
				pkt.bootp_host = IP.IntToStr(bootaddr) + "\0";
			}
		}
	}

	/* Append a client's identity to the reply packet */
	private void AppendClientIdent(DHCPPacket pkt, DHCPPacket rep) {
		DHCPPacket.Option clident = pkt.Options(DHCPPacket.OPTION_CLIENT_IDENT);
		/* If the identity is available from the source packet, use it */
		if (clident != null && clident.len > 0)
			rep.AppendOption(clident);
		else {
			byte[] clid = new byte[pkt.hlen];
			for (int i = 0; i < pkt.hlen; i++)
				clid[i] = pkt.chaddr[i];
			rep.AppendOption(DHCPPacket.OPTION_CLIENT_IDENT, clid);
		}
	}

	/* Append the lease options to the reply packet */
	private void AppendLeaseOptions(DHCPPacket pkt, DHCPPacket rep) {
		rep.AppendOption4B(DHCPPacket.OPTION_SERVER_IDENT, Configuration.network_address);
		rep.AppendOption4B(DHCPPacket.OPTION_REBIND_TIME, 86400 * 10);
		rep.AppendOption4B(DHCPPacket.OPTION_RENEWAL_TIME, 86400 * 10);
		rep.AppendOption4B(DHCPPacket.OPTION_LEASE_TIME, 86400 * 10);
	}

	/* (Re-)assign an IP address to a client */
	private void AssignIP(int ip, String id) {
		if (clients.containsKey(id))
			if (clients.get(id) == ip)
				;
			else
				Log.Add(this, Log.TYPE_INFO, "DHCP: Re-assigning " + IP.IntToStr(ip) + " to " + id);
		else
			Log.Add(this, Log.TYPE_INFO, "DHCP: Assigning " + IP.IntToStr(ip) + " to " + id);
		clients.put(id, ip);
	}

	/* The address to use for broadcasts */
	private int BroadcastAddr() {
		return 0xffffffff;
	}

	/* Check if an IP address is available */
	private boolean CheckIPAvail(int tryip, String clientHwAddress) {
		try {
			for (Map.Entry<String, Integer> e : clients.entrySet())
				if (e.getValue() == tryip)
					return e.getKey().equals(clientHwAddress);
			return tryip != Configuration.network_address
					&& !InetAddress.getByAddress(IP.IntToBytes(tryip)).isReachable(NetworkInterface.getByName(Configuration.network_interface), 6, 400);
		}
		catch (Exception e) {
			return true;
		}
	}

	/* Validate an IP address (is it on our subnet?) */
	private boolean CheckIPValid(int tryip) {
		if (Configuration.dhcp_enforce_subnet)
			return (tryip & Configuration.dhcp_subnet_mask) == (Configuration.dhcp_pool_address & Configuration.dhcp_subnet_mask);
		else
			return true;
	}

	/* Replace loopback address with server address */
	private int ForwardLoopback(int ip) {
		if (ip == 0x7f000001)
			return Configuration.network_address;
		else
			return ip;
	}

	/* Return the address of the server */
	@Override
	protected int getInterfaceAddress() {
		return Configuration.network_address;
	}

	/* Return the port that the server is listening on */
	@Override
	protected int getPort() {
		return Configuration.dhcp_server_port;
	}

	/* Handles DHCP DISCOVER */
	private boolean OnDiscover(DHCPPacket pkt, String clientHwAddress, String clientHumanReadableId, String clientHostname) throws Exception {
		if (Configuration.dhcp_log_packets)
			Log.Add(this, Log.TYPE_INFO, "DHCP Discover from " + clientHumanReadableId);
		/* Validate requested IP address */
		Integer tryip = 0;
		tryip = pkt.Options(DHCPPacket.OPTION_REQUESTED_IP, null).data4B();
		if (tryip != 0 && !CheckIPValid(tryip)) {
			Log.Add(this, Log.TYPE_WARN, "Reqested IP " + IP.IntToStr(tryip) + " is not on enforced subnet " + IP.IntToStr(Configuration.network_address & Configuration.dhcp_subnet_mask)
					+ "/" + IP.IntToStr(Configuration.dhcp_subnet_mask) + " for " + clientHumanReadableId);
			if (Configuration.dhcp_authoritive) {
				DHCPPacket Nak = pkt.MakeResponse(DHCPPacket.MSGTYPE_DHCPNAK);
				Send(Nak);
			}
			return true;
		}
		if (tryip != 0 && !CheckIPAvail(tryip, clientHwAddress)) {
			Log.Add(this, Log.TYPE_WARN, "Reqested IP " + IP.IntToStr(tryip) + " is already in use so cannot be assigned to " + clientHumanReadableId);
			tryip = 0;
		}
		/* Allocate an IP address if needed */
		if (tryip == 0)
			tryip = AllocateIP(clientHwAddress);
		if (tryip == 0) {
			Log.Add(this, Log.TYPE_ERR, "Failed to allocate IP for " + clientHumanReadableId);
			return false;
		}
		/* Store the IP in the assignment table */
		AssignIP(tryip, clientHwAddress);
		/* Create offer (reply packet) */
		DHCPPacket rep = pkt.MakeResponse(DHCPPacket.DHCP_REPLY);
		rep.yiaddr = tryip;
		rep.AppendOption1B(DHCPPacket.OPTION_DHCP_MESSAGE_TYPE, DHCPPacket.MSGTYPE_DHCPOFFER);
		ParseRequestList(pkt, rep);
		AppendBootOptions(pkt, rep);
		AppendLeaseOptions(pkt, rep);
		AppendClientIdent(pkt, rep);
		if (Configuration.dhcp_log_packets)
			Log.Add(this, Log.TYPE_INFO, "Sending DHCP offer to " + clientHumanReadableId);
		rep.flags = (short) 0x8000;
		/* Send reply */
		Send(rep);
		return false;
	}

	/* Handles DHCP REQUEST */
	private boolean OnRequest(DHCPPacket pkt, String clientHwAddress, String clientHumanReadableId, String clientHostname, int clientIpAddress) throws Exception {
		/* Get target address of (broadcasted) packet */
		int target = pkt.Options(DHCPPacket.OPTION_SERVER_IDENT, null).data4B();
		if (target == 0)
			target = pkt.siaddr;
		/* Ensure that the packet is not intended for another server */
		if (target != Configuration.network_address && target != 0) {
			Log.Add(this, Log.TYPE_INFO, "DHCP Request from " + clientHumanReadableId + " to " + IP.IntToStr(target) + " (ignored)");
			return false;
		}
		if (Configuration.dhcp_log_packets)
			Log.Add(this, Log.TYPE_INFO, "DHCP Request from " + clientHumanReadableId);
		/* Check requested IP address */
		Integer tryip = clientIpAddress != 0 ? clientIpAddress : pkt.Options(DHCPPacket.OPTION_REQUESTED_IP, null).data4B();
		/* Validate address */
		if (tryip != 0)
			/* Is requested address not valid? */
			if (!CheckIPValid(tryip)) {
				Log.Add(this, Log.TYPE_WARN,
						"Requested IP " + IP.IntToStr(tryip) + " is not on enforced subnet " + IP.IntToStr(Configuration.network_address & Configuration.dhcp_subnet_mask)
								+ "/" + IP.IntToStr(Configuration.dhcp_subnet_mask) + " for " + clientHumanReadableId);
				/*
				 * Only NACK if this server is authoritive,
				 * otherwise let another server handle the
				 * request
				 */
				if (Configuration.dhcp_authoritive) {
					DHCPPacket Nak = pkt.MakeResponse(DHCPPacket.MSGTYPE_DHCPNAK);
					Send(Nak);
				}
				return true;
			}
			/* Is requested address already taken? */
			else if (!CheckIPAvail(tryip, clientHwAddress)) {
				Log.Add(this, Log.TYPE_WARN, "Requested IP " + IP.IntToStr(tryip) + " is already taken, requested by " + clientHumanReadableId);
				tryip = 0;
			}
		/* No valid IP address */
		if (tryip == 0) {
			/* Create NACK */
			DHCPPacket rep = pkt.MakeResponse(DHCPPacket.DHCP_REPLY);
			rep.AppendOption1B(DHCPPacket.OPTION_DHCP_MESSAGE_TYPE, DHCPPacket.MSGTYPE_DHCPNAK);
			AppendClientIdent(pkt, rep);
			if (Configuration.dhcp_log_packets)
				Log.Add(this, Log.TYPE_INFO, "Sending DHCP negative-acknowledge for to " + clientHumanReadableId);
			rep.flags = (short) 0x8000;
			/* Send NACK */
			Send(rep);
			return false;
		}
		/* Success, reply to the request */
		else {
			/* Store the address assignment */
			AssignIP(tryip, clientHwAddress);
			/* Create acknowledgement */
			DHCPPacket rep = pkt.MakeResponse(DHCPPacket.DHCP_REPLY);
			rep.yiaddr = tryip;
			rep.AppendOption1B(DHCPPacket.OPTION_DHCP_MESSAGE_TYPE, DHCPPacket.MSGTYPE_DHCPACK);
			ParseRequestList(pkt, rep);
			AppendBootOptions(pkt, rep);
			AppendLeaseOptions(pkt, rep);
			AppendClientIdent(pkt, rep);
			if (Configuration.dhcp_log_packets)
				Log.Add(this, Log.TYPE_INFO, "Sending DHCP acknowledge for IP " + IP.IntToStr(tryip) + " to " + clientHumanReadableId);
			rep.flags = (short) 0x8000;
			/* Send acknowledgement */
			Send(rep);
			return false;
		}
	}

	/* Handles received DHCP packets */
	@Override
	protected boolean OnReceive(InetSocketAddress sender, int remotePort, ByteBuffer data) throws Exception {
		/* Parse packet, ignore anything that isn't a REQUEST */
		DHCPPacket pkt = new DHCPPacket(data);
		if (pkt.op != DHCPPacket.DHCP_REQUEST)
			return true;
		/* Get client hardware address */
		String clientHwAddress = "";
		for (int i = 0; i < pkt.hlen; i++)
			clientHwAddress += Integer.toHexString(0x100 | pkt.chaddr[i] & 0xff).substring(1);
		/* Client identification string for logging purposes */
		String clientHumanReadableId = "client <hwaddress=";
		clientHumanReadableId += clientHwAddress;
		int clientIpAddress = IP.BytesToInt(sender.getAddress().getAddress());
		if (clientIpAddress != 0x00000000)
			clientHumanReadableId = clientHumanReadableId + "; address=" + IP.IntToStr(clientIpAddress);
		/* Client clientHostname */
		DHCPPacket.Option op = pkt.Options(DHCPPacket.OPTION_HOSTNAME, null);
		String clientHostname = op.dataNTS();
		if (clientHostname != "" && clientHostname != null)
			clientHumanReadableId = clientHumanReadableId + "; clientHostname=" + clientHostname;
		clientHumanReadableId += ">";
		/*
		 * Packet is not broadcasted: ignore it if the configuration
		 * specifies to
		 */
		if ((pkt.flags & 0x8000) == 0 && Configuration.dhcp_broadcasts_only) {
			if (Configuration.dhcp_log_packets)
				Log.Add(this, Log.TYPE_INFO, "Ignoring packet due to unset broadcast bit, from " + clientHumanReadableId);
			return true;
		}
		/*
		 * The seemingly redundant control logic (i.e. can only return
		 * true) is in place for in case we want to add fail conditions
		 * in future (kill the server by returning false)
		 */
		/* Packet type */
		switch (pkt.MessageType()) {
		case DHCPPacket.MSGTYPE_DHCPDISCOVER:
			if (OnDiscover(pkt, clientHwAddress, clientHumanReadableId, clientHostname))
				return true;
			break;
		case DHCPPacket.MSGTYPE_DHCPREQUEST:
			if (OnRequest(pkt, clientHwAddress, clientHumanReadableId, clientHostname, clientIpAddress))
				return true;
			break;
		default:
			return true;
		}
		return true;
	}

	/*
	 * Parses the request list of a DHCP packet, adding the requested
	 * information to the reply packet
	 */
	private void ParseRequestList(DHCPPacket pkt, DHCPPacket rep) {
		/* Get the parameter request list */
		byte[] prl = pkt.Options(DHCPPacket.OPTION_PARAM_REQUEST_LIST, null).data;
		/* Iterate over requests */
		for (byte element : prl)
			/* Append response to reply packet */
			switch (element) {
			case DHCPPacket.OPTION_SUBNET:
				rep.AppendOption4B(DHCPPacket.OPTION_SUBNET, Configuration.dhcp_subnet_mask);
				break;
			case DHCPPacket.OPTION_ROUTER_ADDR:
				if (Configuration.dhcp_opt_gateway_address != 0)
					rep.AppendOption4B(DHCPPacket.OPTION_ROUTER_ADDR, ForwardLoopback(Configuration.dhcp_opt_gateway_address));
				break;
			case DHCPPacket.OPTION_DNS_SERVER:
				if (Configuration.dhcp_opt_dns_server_address != 0)
					rep.AppendOption4B(DHCPPacket.OPTION_ROUTER_ADDR, ForwardLoopback(Configuration.dhcp_opt_dns_server_address));
				break;
			case DHCPPacket.OPTION_DNS_DOMAIN:
				if (Configuration.dhcp_opt_dns_domain != "")
					rep.AppendOptionNTS(DHCPPacket.OPTION_DNS_DOMAIN, Configuration.dhcp_opt_dns_domain);
				break;
			case DHCPPacket.OPTION_TIME_SERVER:
				if (Configuration.dhcp_opt_time_server_address != 0)
					rep.AppendOption4B(DHCPPacket.OPTION_TIME_SERVER, ForwardLoopback(Configuration.dhcp_opt_time_server_address));
				break;
			}
	}

	/* Broadcasts a DHCP packet */
	private void Send(DHCPPacket rep) throws Exception {
		if (Configuration.dhcp_reply_delay > 0)
			Thread.sleep(Configuration.dhcp_reply_delay);
		super.Send(Configuration.dhcp_client_port, InetAddress.getByAddress(IP.IntToBytes(BroadcastAddr())), rep.Encode());
	}

	/* Dumps the IP table to STDOUT */
	public void ShowMaps() {
		System.out.print("DHCP assignment table");
		for (Map.Entry<String, Integer> e : clients.entrySet())
			System.out.print(e.getKey() + " => " + IP.IntToStr(e.getValue()) + "\n");
	}
}