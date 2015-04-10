package at.ac.tuwien.ir2015;

import java.util.Map;
import java.util.TreeMap;

public class BagOfWords extends TreeMap<String, IndexValue> {

	private static final long serialVersionUID = 1L;

	public void add(LuceneIRDoc doc) {
		for(Map.Entry<String, Integer> entry : doc.getCounts().entrySet()) {
			IndexValue value = get(entry.getKey());
			if(value == null) {
				value = new IndexValue(doc, entry.getValue());
				put(entry.getKey(), value);
			} else {
				value.add(doc, entry.getValue());
			}
		}
	}
	
}
