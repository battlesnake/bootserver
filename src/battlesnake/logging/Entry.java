package battlesnake.logging;

import java.util.Date;

/* A log entry */
public class Entry {
	public final String AppSource;
	public final Date When;
	public final byte Type;
	public final String Message;
	public final int Indent;

	public Entry(String AppSource, byte Type, String Message, int Indent) {
		this.AppSource = AppSource;
		this.When = new Date();
		this.Type = Type;
		this.Message = Message;
		this.Indent = Indent;
	}

	@Override
	public String toString() {
		String AppSrc = AppSource + ((AppSource.length() < 8) ? '\t' : "");
		String indents = "";
		for (int i = 0; i < Indent; i++)
			indents += "\t";
		return String.format("%1$TF %1$TT\t%2$s\t%3$s\t%4$s%5$s", When, AppSrc,
				Log.TypeStr(Type), indents, Message);
	}

}
