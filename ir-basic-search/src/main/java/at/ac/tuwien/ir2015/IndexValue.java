package at.ac.tuwien.ir2015;

import java.util.HashMap;
import java.util.Map;

public class IndexValue {

	public IndexValue(AbstractIRDoc doc,
			int count) {
		this.sum = count;
		mapCounts.put(doc, count);
	}

	private Integer sum = 0;
	
	private Map<AbstractIRDoc, Integer> mapCounts = new HashMap<AbstractIRDoc, Integer>();
	
	public void add(AbstractIRDoc doc, int count) {
		sum+=count;
		mapCounts.put(doc, count);
	}

	@Override
	public String toString() {
		return "IndexValue [sum=" + sum + ", mapCounts=" + mapCounts + "]";
	}
	
	
}
