package at.ac.tuwien.ir2015;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class SearchResult implements ISearchResult  {

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
	private String runName;
	private String topic;
	
	public SearchResult(String topic, String runName) {
		this.topic = topic;
		this.runName = runName;
	}

	/* (non-Javadoc)
	 * @see at.ac.tuwien.ir2015.ISearchResult#toString()
	 */
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
		return String.format(RESULT_FORMAT, topic, r.doc.getName(), i, r.score, runName);
	}

	public void add(IndexValue iv) {
		for(Map.Entry<AbstractIRDoc, Integer> entry : iv.getMapCounts().entrySet()) {
			Result r = results.get(entry.getKey());
			if(r == null) {
				r = new Result();
				r.doc = entry.getKey();
				r.score = calcScore(entry); 
				results.put(entry.getKey(), r);
			} else {
				r.score += calcScore(entry);//tf = sum(1 + log(tf));
			}
		}
	}
//	
//	public void add(AbstractIRDoc doc, double score) {
//		
//		Result r = results.get(doc);
//		if(r == null) {
//			r = new Result();
//			r.doc = doc;
//			r.score = score;
//			results.put(doc, r);
//		} else {
//			r.score += score;//tf = sum(1 + log(tf));
//		}
//	}

	/**
	 * entry-score = 1 + log(tf)
	 */
	private double calcScore(Map.Entry<AbstractIRDoc, Integer> entry) {
		return 1 + Math.log10(entry.getValue());
	}
}
