package at.ac.tuwien.ir2015;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class SearchResult {

	private class Result {
		private AbstractIRDoc doc;
		private double score;
		
//		@Override
//		public int hashCode() {
//			return doc.hashCode();
//		}
//		
//		@Override
//		public boolean equals(Object obj) {
//			if(!(obj instanceof Result)) {
//				return false;
//			}
//			Result otherR = (Result) obj;
//			
//			return doc.equals(otherR.doc);
//		}
	}

	private final static Comparator<Result> scoreComparator = new Comparator<Result>() {

		@Override
		public int compare(Result o1, Result o2) {
			double diff = o1.score - o2.score;
			if(diff < 0.0)
				return 1;
			if(diff > 0.0)
				return -1;
			return 0;
		}
	};
	
	private HashMap<AbstractIRDoc, Result> results = new HashMap<AbstractIRDoc, Result>();
	private String runName = "test"; //TODO
	private int nr = 0; //TODO
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		TreeSet<Result> sortedSet = new TreeSet<Result>(scoreComparator);
		sortedSet.addAll(results.values());
		
		int i = 1;
		for(Result r : sortedSet) {
			sb.append(format(i++, r)).append("\n");
			//up to ir.maxResults documents
			if(i > App.getMaxResults())
				break;
		}
		return sb.toString();
	}

	private String format(int i, Result r) {
		return String.format("topic%d Q0 %s %d %f %s", nr, r.doc.getName(), i, r.score, runName);
	}

	public void add(IndexValue b) {
		for(Map.Entry<AbstractIRDoc, Integer> entry : b.getMapCounts().entrySet()) {
			Result r = results.get(entry.getKey());
			if(r == null) {
				r = new Result();
				r.doc = entry.getKey();
				r.score = entry.getValue(); //TODO score
				results.put(entry.getKey(), r);
			} else {
				r.score += entry.getValue();
			}
		}
	}
}
