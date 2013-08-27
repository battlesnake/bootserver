package battlesnake.utils;

import java.util.HashMap;

/* A case-insensitive hashmap, achieved by storing all keys in lowercase */
public class CaseInsensitiveHashMap<V> extends HashMap<String, V> {

	public static final long serialVersionUID = 0;

	@Override
	public V put(String key, V value) {
		return super.put(key.toLowerCase(), value);
	}

	public V get(String key) {
		return super.get(key.toLowerCase());
	}
}
