package battlesnake.packets.dhcpdefs;

import java.util.HashMap;
import java.util.Map;

/* Holds DHCP option definitions, primarily used when debugging */
public class OptionDefinitions {

	private static Map<Integer, OptionDefinition> Definitions = new HashMap<Integer, OptionDefinition>();

	public static OptionDefinition get(int code) {
		code &= 0xff;
		if (Definitions.containsKey(code))
			return Definitions.get(code);
		else
			return null;
	}

	public static String Name(int code) {
		Initialise();
		code &= 0xff;
		if (Definitions.containsKey(code))
			return Definitions.get(code).Name;
		else
			return "opcode" + Integer.toString(code);
	}

	public static String RFCs(int code) {
		Initialise();
		code &= 0xff;
		if (Definitions.containsKey(code))
			return Definitions.get(code).RFCs;
		else
			return "Unknown";
	}

	private static void Initialise() {
		if (Definitions.size() != 0)
			return;
		Definitions.put(0, new OptionDefinition(0, "Pad", "RFC 2132"));
		Definitions.put(1, new OptionDefinition(1, "Subnet Mask", "RFC 2132"));
		Definitions.put(2, new OptionDefinition(2, "Time Offset (deprecated)",
				"RFC 2132"));
		Definitions.put(3, new OptionDefinition(3, "Router", "RFC 2132"));
		Definitions.put(4, new OptionDefinition(4, "Time Server", "RFC 2132"));
		Definitions.put(5, new OptionDefinition(5, "Name Server", "RFC 2132"));
		Definitions.put(6, new OptionDefinition(6, "Domain Name Server",
				"RFC 2132"));
		Definitions.put(7, new OptionDefinition(7, "Log Server", "RFC 2132"));
		Definitions.put(8, new OptionDefinition(8, "Quote Server", "RFC 2132"));
		Definitions.put(9, new OptionDefinition(9, "LPR Server", "RFC 2132"));
		Definitions.put(10, new OptionDefinition(10, "Impress Server",
				"RFC 2132"));
		Definitions.put(11, new OptionDefinition(11,
				"Resource Location Server", "RFC 2132"));
		Definitions.put(12, new OptionDefinition(12, "Host Name", "RFC 2132"));
		Definitions.put(13, new OptionDefinition(13, "Boot File Size",
				"RFC 2132"));
		Definitions.put(14, new OptionDefinition(14, "Merit Dump File",
				"RFC 2132"));
		Definitions
				.put(15, new OptionDefinition(15, "Domain Name", "RFC 2132"));
		Definitions
				.put(16, new OptionDefinition(16, "Swap Server", "RFC 2132"));
		Definitions.put(17, new OptionDefinition(17, "Root Path", "RFC 2132"));
		Definitions.put(18, new OptionDefinition(18, "Extensions Path",
				"RFC 2132"));
		Definitions.put(19, new OptionDefinition(19,
				"IP Forwarding enable/disable", "RFC 2132"));
		Definitions.put(20, new OptionDefinition(20,
				"Non-local Source Routing enable/disable", "RFC 2132"));
		Definitions.put(21, new OptionDefinition(21, "Policy Filter",
				"RFC 2132"));
		Definitions.put(22, new OptionDefinition(22,
				"Maximum Datagram Reassembly Size", "RFC 2132"));
		Definitions.put(23, new OptionDefinition(23, "Default IP Time-to-live",
				"RFC 2132"));
		Definitions.put(24, new OptionDefinition(24, "Path MTU Aging Timeout",
				"RFC 2132"));
		Definitions.put(25, new OptionDefinition(25, "Path MTU Plateau Table",
				"RFC 2132"));
		Definitions.put(26, new OptionDefinition(26, "Interface MTU",
				"RFC 2132"));
		Definitions.put(27, new OptionDefinition(27, "All Subnets are Local",
				"RFC 2132"));
		Definitions.put(28, new OptionDefinition(28, "Broadcast Address",
				"RFC 2132"));
		Definitions.put(29, new OptionDefinition(29, "Perform Mask Discovery",
				"RFC 2132"));
		Definitions.put(30, new OptionDefinition(30, "Mask supplier",
				"RFC 2132"));
		Definitions.put(31, new OptionDefinition(31,
				"Perform router discovery", "RFC 2132"));
		Definitions.put(32, new OptionDefinition(32,
				"Router solicitation address", "RFC 2132"));
		Definitions.put(33, new OptionDefinition(33, "Static routing table",
				"RFC 2132"));
		Definitions.put(34, new OptionDefinition(34, "Trailer encapsulation",
				"RFC 2132"));
		Definitions.put(35, new OptionDefinition(35, "ARP cache timeout",
				"RFC 2132"));
		Definitions.put(36, new OptionDefinition(36, "Ethernet encapsulation",
				"RFC 2132"));
		Definitions.put(37, new OptionDefinition(37, "Default TCP TTL",
				"RFC 2132"));
		Definitions.put(38, new OptionDefinition(38, "TCP keepalive interval",
				"RFC 2132"));
		Definitions.put(39, new OptionDefinition(39, "TCP keepalive garbage",
				"RFC 2132"));
		Definitions.put(40, new OptionDefinition(40,
				"Network Information Service Domain", "RFC 2132"));
		Definitions.put(41, new OptionDefinition(41,
				"Network Information Servers", "RFC 2132"));
		Definitions
				.put(42, new OptionDefinition(42, "NTP servers", "RFC 2132"));
		Definitions.put(43, new OptionDefinition(43,
				"Vendor specific information", "RFC 1533, RFC 2132"));
		Definitions.put(44, new OptionDefinition(44,
				"NetBIOS over TCP/IP name server", "RFC 1533, RFC 2132"));
		Definitions.put(45, new OptionDefinition(45,
				"NetBIOS over TCP/IP Datagram Distribution Server",
				"RFC 1533, RFC 2132"));
		Definitions.put(46, new OptionDefinition(46,
				"NetBIOS over TCP/IP Node Type", "RFC 1533, RFC 2132"));
		Definitions.put(47, new OptionDefinition(47,
				"NetBIOS over TCP/IP Scope", "RFC 1533, RFC 2132"));
		Definitions.put(48, new OptionDefinition(48,
				"X Window System Font Server", "RFC 1533, RFC 2132"));
		Definitions.put(49, new OptionDefinition(49,
				"X Window System Display Manager", "RFC 1533, RFC 2132"));
		Definitions.put(50, new OptionDefinition(50, "Requested IP Address",
				"RFC 1533, RFC 2132"));
		Definitions.put(51, new OptionDefinition(51, "IP address lease time",
				"RFC 1533, RFC 2132"));
		Definitions.put(52, new OptionDefinition(52, "Option overload",
				"RFC 1533, RFC 2132"));
		Definitions.put(53, new OptionDefinition(53, "DHCP message type",
				"RFC 1533, RFC 2132, RFC 3203, RFC 4388"));
		Definitions.put(54, new OptionDefinition(54, "Server identifier",
				"RFC 1533, RFC 2132"));
		Definitions.put(55, new OptionDefinition(55, "Parameter request list",
				"RFC 1533, RFC 2132"));
		Definitions.put(56, new OptionDefinition(56, "Message",
				"RFC 1533, RFC 2132"));
		Definitions.put(57, new OptionDefinition(57,
				"Maximum DHCP message size", "RFC 1533, RFC 2132"));
		Definitions.put(58, new OptionDefinition(58, "Renew time value",
				"RFC 1533, RFC 2132"));
		Definitions.put(59, new OptionDefinition(59, "Rebinding time value",
				"RFC 1533, RFC 2132"));
		Definitions.put(60, new OptionDefinition(60, "Class-identifier",
				"RFC 1533, RFC 2132"));
		Definitions.put(61, new OptionDefinition(61, "Client-identifier",
				"RFC 1533, RFC 2132, RFC 4361"));
		Definitions.put(62, new OptionDefinition(62, "NetWare/IP Domain Name",
				"RFC 2242"));
		Definitions.put(63, new OptionDefinition(63, "NetWare/IP information",
				"RFC 2242"));
		Definitions.put(64, new OptionDefinition(64,
				"Network Information Service+ Domain", "RFC 2132"));
		Definitions.put(65, new OptionDefinition(65,
				"Network Information Service+ Servers", "RFC 2132"));
		Definitions.put(66, new OptionDefinition(66, "TFTP server name",
				"RFC 2132"));
		Definitions.put(67, new OptionDefinition(67, "Bootfile name",
				"RFC 2132"));
		Definitions.put(68, new OptionDefinition(68, "Mobile IP Home Agent",
				"RFC 2132"));
		Definitions.put(69, new OptionDefinition(69,
				"Simple Mail Transport Protocol Server", "RFC 2132"));
		Definitions.put(70, new OptionDefinition(70,
				"Post Office Protocol Server", "RFC 2132"));
		Definitions.put(71, new OptionDefinition(71,
				"Network News Transport Protocol Server", "RFC 2132"));
		Definitions.put(72, new OptionDefinition(72,
				"Default World Wide Web Server", "RFC 2132"));
		Definitions.put(73, new OptionDefinition(73, "Default Finger Server",
				"RFC 2132"));
		Definitions.put(74, new OptionDefinition(74,
				"Default Internet Relay Chat Server", "RFC 2132"));
		Definitions.put(75, new OptionDefinition(75, "StreetTalk Server",
				"RFC 2132"));
		Definitions.put(76, new OptionDefinition(76,
				"StreetTalk Directory Assistance Server", "RFC 2132"));
		Definitions.put(77, new OptionDefinition(77, "User Class Information",
				"RFC 3004"));
		Definitions.put(78, new OptionDefinition(78, "SLP Directory Agent",
				"RFC 2610"));
		Definitions.put(79, new OptionDefinition(79, "SLP Service Scope",
				"RFC 2610"));
		Definitions.put(80,
				new OptionDefinition(80, "Rapid Commit", "RFC 4039"));
		Definitions.put(81, new OptionDefinition(81,
				"FQDN, Fully Qualified Domain Name", "RFC 4702"));
		Definitions.put(82, new OptionDefinition(82, "Relay Agent Information",
				"RFC 3046, RFC 5010"));
		Definitions.put(83, new OptionDefinition(83,
				"Internet Storage Name Service", "RFC 4174"));
		Definitions
				.put(85, new OptionDefinition(85, "NDS servers", "RFC 2241"));
		Definitions.put(86, new OptionDefinition(86, "NDS tree name",
				"RFC 2241"));
		Definitions
				.put(87, new OptionDefinition(87, "NDS context", "RFC 2241"));
		Definitions.put(88, new OptionDefinition(88,
				"BCMCS Controller Domain Name list", "RFC 4280"));
		Definitions.put(89, new OptionDefinition(89,
				"BCMCS Controller IPv4 address list", "RFC 4280"));
		Definitions.put(90, new OptionDefinition(90, "Authentication",
				"RFC 3118"));
		Definitions.put(91, new OptionDefinition(91,
				"client-last-transaction-time", "RFC 4388"));
		Definitions.put(92, new OptionDefinition(92, "associated-ip",
				"RFC 4388"));
		Definitions.put(93, new OptionDefinition(93,
				"Client System Architecture Type", "RFC 4578"));
		Definitions.put(94, new OptionDefinition(94,
				"Client Network Interface Identifier", "RFC 4578"));
		Definitions.put(95, new OptionDefinition(95,
				"LDAP, Lightweight Directory Access Protocol", "RFC 3679"));
		Definitions.put(97, new OptionDefinition(97,
				"Client Machine Identifier", "RFC 4578"));
		Definitions.put(98, new OptionDefinition(98,
				"Open Group's User Authentication", "RFC 2485"));
		Definitions.put(99, new OptionDefinition(99, "GEOCONF_CIVIC",
				"RFC 4776"));
		Definitions.put(100, new OptionDefinition(100, "IEEE 1003.1 TZ String",
				"RFC 4833"));
		Definitions.put(101, new OptionDefinition(101,
				"Reference to the TZ Database", "RFC 4833"));
		Definitions.put(112, new OptionDefinition(112,
				"NetInfo Parent Server Address", "RFC 3679"));
		Definitions.put(113, new OptionDefinition(113,
				"NetInfo Parent Server Tag", "RFC 3679"));
		Definitions.put(114, new OptionDefinition(114, "URL", "RFC 3679"));
		Definitions.put(115, new OptionDefinition(115, "", "RFC 3679"));
		Definitions.put(116, new OptionDefinition(116, "Auto-Configure",
				"RFC 2563"));
		Definitions.put(117, new OptionDefinition(117, "Name Service Search",
				"RFC 2937"));
		Definitions.put(118, new OptionDefinition(118, "Subnet Selection",
				"RFC 3011"));
		Definitions.put(119, new OptionDefinition(119,
				"DNS domain search list", "RFC 3397"));
		Definitions.put(120, new OptionDefinition(120,
				"SIP Servers DHCP Option", "RFC 3361"));
		Definitions.put(121, new OptionDefinition(121,
				"Classless Static Route Option", "RFC 3442"));
		Definitions.put(122, new OptionDefinition(122,
				"CCC, CableLabs Client Configuration",
				"RFC 3495, RFC 3594, RFC 3634"));
		Definitions.put(123, new OptionDefinition(123, "GeoConf", "RFC 3825"));
		Definitions.put(124, new OptionDefinition(124,
				"Vendor-Identifying Vendor Class", "RFC 3925"));
		Definitions.put(125, new OptionDefinition(125,
				"Vendor-Identifying Vendor-Specific", "RFC 3925"));
		Definitions.put(128, new OptionDefinition(128,
				"TFPT Server IP address", "RFC 4578"));
		Definitions.put(129, new OptionDefinition(129,
				"Call Server IP address", "RFC 4578"));
		Definitions.put(130, new OptionDefinition(130, "Discrimination string",
				"RFC 4578"));
		Definitions.put(131, new OptionDefinition(131,
				"Remote statistics server IP address", "RFC 4578"));
		Definitions.put(132, new OptionDefinition(132, "802.1P VLAN ID",
				"RFC 4578"));
		Definitions.put(133, new OptionDefinition(133, "802.1Q L2 Priority",
				"RFC 4578"));
		Definitions.put(134, new OptionDefinition(134, "Diffserv Code Point",
				"RFC 4578"));
		Definitions.put(135, new OptionDefinition(135,
				"HTTP Proxy for phone-specific applications", "RFC 4578"));
		Definitions.put(136, new OptionDefinition(136,
				"PANA Authentication Agent", "RFC 5192"));
		Definitions.put(137, new OptionDefinition(137, "LoST Server",
				"RFC 5223"));
		Definitions.put(138, new OptionDefinition(138,
				"CAPWAP Access Controller addresses", "RFC 5417"));
		Definitions.put(139, new OptionDefinition(139,
				"OPTION-IPv4_Address-MoS", "RFC 5678"));
		Definitions.put(140, new OptionDefinition(140, "OPTION-IPv4_FQDN-MoS",
				"RFC 5678"));
		Definitions.put(141, new OptionDefinition(141,
				"SIP UA Configuration Service Domains", "RFC 6011"));
		Definitions.put(142, new OptionDefinition(142,
				"OPTION-IPv4_Address-ANDSF", ""));
		Definitions.put(143, new OptionDefinition(143,
				"OPTION-IPv6_Address-ANDSF", ""));
		Definitions.put(150, new OptionDefinition(150, "TFTP server address",
				"RFC 5859"));
		Definitions.put(150, new OptionDefinition(150,
				"Etherboot / GRUB configuration path name", ""));
		Definitions.put(175, new OptionDefinition(175, "Etherboot", ""));
		Definitions.put(176, new OptionDefinition(176, "IP Telephone", ""));
		Definitions.put(177, new OptionDefinition(177,
				"Etherboot / PacketCable and CableHome", ""));
		Definitions.put(208, new OptionDefinition(208,
				"pxelinux.magic (string) = F1:00:74:7E (241.0.116.126)",
				"RFC 5071"));
		Definitions.put(209, new OptionDefinition(209,
				"pxelinux.configfile (text)", "RFC 5071"));
		Definitions.put(210, new OptionDefinition(210,
				"pxelinux.pathprefix (text)", "RFC 5071"));
		Definitions.put(211, new OptionDefinition(211,
				"pxelinux.reboottime (unsigned integer 32 bits)", "RFC 5071"));
		Definitions.put(212,
				new OptionDefinition(212, "OPTION_6RD", "RFC 5969"));
		Definitions.put(213, new OptionDefinition(213,
				"OPTION_V4_ACCESS_DOMAIN", "RFC 5986"));
		Definitions
				.put(220, new OptionDefinition(220, "Subnet Allocation", ""));
		Definitions.put(221, new OptionDefinition(221,
				"Virtual Subnet Selection", "RFC 6607"));
		Definitions.put(255, new OptionDefinition(255, "End", "RFC 2132"));
	}
}
