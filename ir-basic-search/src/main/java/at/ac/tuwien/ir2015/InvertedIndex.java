package at.ac.tuwien.ir2015;

import java.util.HashMap;
import java.util.Map;

public class InvertedIndex extends HashMap<String, IndexValue> {

	private static final long serialVersionUID = 1L;

	public void add(AbstractIRDoc doc) {
		for(Map.Entry<String, Integer> entry : doc.getCounts().entrySet()) {
			addEntry(doc, entry);
		}
	}

	private synchronized void addEntry(AbstractIRDoc doc, Map.Entry<String, Integer> entry) {
		IndexValue value = get(entry.getKey());
		if(value == null) {
			value = new IndexValue(doc, entry.getValue());
			put(entry.getKey(), value);
		} else {
			value.add(doc, entry.getValue());
		}
	}
	
}
