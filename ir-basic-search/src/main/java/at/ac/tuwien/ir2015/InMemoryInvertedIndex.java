package at.ac.tuwien.ir2015;

import java.util.HashMap;
import java.util.Map;

public class InMemoryInvertedIndex implements InvertedIndex {

	private IndexType it;

	public InMemoryInvertedIndex(IndexType it) {
		this.it = it;
	}
	
	private HashMap<String, IndexValue> map = new HashMap<String, IndexValue>();
	private int docs = 0;

	/* (non-Javadoc)
	 * @see at.ac.tuwien.ir2015.InvertedIndex#add(at.ac.tuwien.ir2015.AbstractIRDoc)
	 */
	@Override
	public void add(AbstractIRDoc doc) {
		docs++;
		for(Map.Entry<String, Integer> entry : doc.getCounts().entrySet()) {
			addEntry(doc, entry);
		}
	}

	private void addEntry(AbstractIRDoc doc, Map.Entry<String, Integer> entry) {
		IndexValue value = map.get(entry.getKey());
		if(value == null) {
			value = new IndexValue(doc, entry.getValue());
			map.put(entry.getKey(), value);
		} else {
			value.add(doc, entry.getValue());
		}
	}

	/* (non-Javadoc)
	 * @see at.ac.tuwien.ir2015.InvertedIndex#get(java.lang.String)
	 */
	@Override
	public IndexValue get(String s) {
		return map.get(s);
	}

	@Override
	public ISearchResult search(AbstractIRDoc doc, ScoringMethod sm, String runName) {
		SearchResult sr = new SearchResult(doc.getName(), runName, sm);
		for(String s : it.getCounts(doc).keySet()) {
			IndexValue b = get(s);
			double df = Math.log10((double) docs / (double) b.getMapCounts().size());
			if(b != null) {
				//hit
				sr.add(b, df);
			}
		}
		return sr;
	}

}
