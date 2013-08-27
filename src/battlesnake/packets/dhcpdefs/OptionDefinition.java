package battlesnake.packets.dhcpdefs;

/* Represents the definition of a DHCP option */
public class OptionDefinition {
	public final byte Code;
	public final String Name;
	public final String RFCs;

	public OptionDefinition(int Code, String Name, String RFCs) {
		this.Code = (byte) Code;
		this.Name = Name;
		this.RFCs = RFCs;
	}
}
