package battlesnake.utils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/* Path utilities */
public class Path {

	/* Replaces all backslashes and forward slashes with the File.separator */
	public static String Slashify(String path) {
		return path.replace("\\", "/").replace("/", File.separator);
	}

	/* Joins an array of path parts using the File.separator */
	public static String Join(String[] parts) {
		StringBuilder s = new StringBuilder();
		String t = "";
		for (int i = 0; i < parts.length; i++) {
			t = parts[i];
			if (i > 0 && !t.startsWith(File.separator))
				s.append(File.separator);
			s.append(t);
		}
		return s.toString();
	}

	/* Splits a path on the file separator (not entirely sure what the "replace" thing is about...) */
	public static String[] SplitPath(String path) {
		return path.split(File.separator.replace("\\", "\\\\"));
	}

	/* Parses dot entries in paths ./ ../ and forces a leading/trailing slash if specified */
	public static String ParseDots(String path, boolean LeadingSlash,
			boolean TrailingSlash) {
		List<String> parts = new LinkedList<String>();
		for (String s : SplitPath(path))
			parts.add(s);
		int i = 0;
		while (parts.size() > 0 && i < parts.size()) {
			String s = parts.get(i).trim();
			if (s.startsWith("..")) {
				parts.remove(i);
				if (i > 0)
					parts.remove(--i);
			} else if (s == "." || s.length() == 0)
				parts.remove(i);
			else
				i++;
		}
		StringBuilder s = new StringBuilder();
		for (i = 0; i < parts.size(); i++) {
			if (LeadingSlash || i > 0)
				s.append(File.separator);
			s.append(parts.get(i));
		}
		if (TrailingSlash)
			s.append(File.separator);
		return s.toString();
	}

	/* Forces a trailing slash */
	public static String IncludeTrailingSlash(String s) {
		if (s.endsWith(File.separator))
			return s;
		else
			return s + File.separator;
	}

	/* Forces no trailing slash */
	public static String ExcludeTrailingSlash(String s) {
		if (s.endsWith(File.separator))
			return s.substring(0, s.length() - 1);
		else
			return s;
	}

	/* Forces a leading slash */
	public static String IncludeLeadingSlash(String s) {
		if (s.startsWith(File.separator))
			return s;
		else
			return File.separator + s;
	}

	/* Forces no leading slash */
	public static String ExcludeLeadingSlash(String s) {
		if (s.startsWith(File.separator))
			return s.substring(1);
		else
			return s;
	}

}
