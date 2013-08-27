package battlesnake.utils;

import java.io.Console;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/* Console input utils, currently just implements a menu */
public class Input {

	public static Console console = System.console();

	/*
	 * Show a list of choices below a title, then follow it all with a
	 * prompt asking the user to choose an option
	 */
	public static <T> T Choice(String title, String prompt, Map<String, T> choices, String def) throws IOException {
		do {
			System.out.print(title + ":\n");
			char idx = '1';
			char defc = 0;
			Map<Character, Map.Entry<String, T>> choicesidx = new HashMap<Character, Map.Entry<String, T>>();
			for (Map.Entry<String, T> e : choices.entrySet()) {
				choicesidx.put(idx, e);
				System.out.print("  " + idx + ".\t" + e.getKey() + "\n");
				if (e.getKey().equalsIgnoreCase(def))
					defc = idx;
				if (idx == '9')
					idx = 'a';
				else if (idx == 'z')
					idx = 'A';
				else
					idx++;
			}
			if (defc != 0)
				System.out.print("Default value: " + choicesidx.get(defc).getKey());
			System.out.print(prompt + ": ");
			int numraw;
			do
				numraw = System.in.read();
			while (numraw == -1 || numraw == 0 || numraw == 13 || numraw == 10);
			char num = (char) (byte) numraw;
			if (choicesidx.containsKey(num)) {
				Map.Entry<String, T> e = choicesidx.get(num);
				System.out.print("Selected: " + e.getKey() + "\n\n");
				return choicesidx.get(num).getValue();
			}
			System.out.print("\n");
		} while (true);
	}

}
