package at.ac.tuwien.ir2015;

import java.util.HashMap;
import java.util.Map;

public class InvertedIndex {

	private HashMap<String, IndexValue> map = new HashMap<String, IndexValue>();

	public void add(AbstractIRDoc doc) {
		for(Map.Entry<String, Integer> entry : doc.getCounts().entrySet()) {
			addEntry(doc, entry);
		}
	}

	private synchronized void addEntry(AbstractIRDoc doc, Map.Entry<String, Integer> entry) {
		IndexValue value = map.get(entry.getKey());
		if(value == null) {
			value = new IndexValue(doc, entry.getValue());
			map.put(entry.getKey(), value);
		} else {
			value.add(doc, entry.getValue());
		}
	}

	public IndexValue get(String s) {
		return map.get(s);
	}
	
}
