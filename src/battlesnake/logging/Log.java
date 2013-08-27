package battlesnake.logging;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/* An enhanced logging class */
public class Log {

	public static final byte TYPE_INFO = 0, TYPE_WARN = 1, TYPE_ERR = 2, TYPE_FATAL = 3;

	/* Log entry type number to string */
	public static String TypeStr(byte type) {
		if (type == TYPE_INFO)
			return "info";
		else if (type == TYPE_WARN)
			return "warn";
		else if (type == TYPE_ERR)
			return "error";
		else if (type == TYPE_FATAL)
			return "fatal";
		else
			return "";
	}

	/* Maximum number of log entries to keep */
	public static int MaxLength = 30;

	/* The log entries */
	private static final List<Entry> list = new ArrayList<Entry>();
	/* Maintains a list of observers listening for log entries */
	private static final List<Listener> onmsg = new LinkedList<Listener>();
	/* Log indentation level */
	private static int indent = 0;

	/* Increase indentation level */
	public static synchronized void Indent() {
		indent++;
	}

	/* Decrease indentation level */
	public static synchronized void Unindent() {
		if (indent > 0)
			indent--;
		else
			Log.Add("Log", TYPE_WARN, "Too many unindent requests");
	}

	/* Add an entry to the log */
	public static synchronized void Add(String AppSource, byte Type, String Message) {
		Entry entry = new Entry(AppSource, Type, Message, indent);
		list.add(entry);
		while (Length() > MaxLength)
			list.remove(0);
		for (Listener listener : onmsg)
			listener.OnMessage(entry);
	}

	/* Add an entry to the log */
	public static synchronized void Add(Object Source, byte Type, String Message) {
		Add(Source.getClass().getSimpleName(), Type, Message);
	}

	/* Register an observer */
	public static synchronized void Subscribe(Listener listener) {
		onmsg.add(listener);
	}

	/* Unregister an observer */
	public static synchronized void Unsubscribe(Listener listener) {
		onmsg.remove(listener);
	}

	/* Length of the log */
	public static synchronized int Length() {
		return list.size();

	}

	/* Get a log entry */
	public static synchronized Entry get(int index) {
		return list.get(index);
	}

}
