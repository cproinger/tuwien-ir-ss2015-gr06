package at.ac.tuwien.ir2015;

import java.util.TreeMap;

public class CountingMap extends TreeMap<String, Integer> {

	private static final long serialVersionUID = 1L;

	public Integer add(String t) {
		return add(t, 1);
	}

	private synchronized Integer add(String t, int inc) {
		Integer i = get(t);
		i = i == null ? inc : (i+inc);
		return put(t, i);
	}

	public void addAll(CountingMap process) {
		process.entrySet().forEach(e -> add(e.getKey(), e.getValue()));
	}
}
