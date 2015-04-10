package at.ac.tuwien.ir2015;

import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;

public class SearchResult {

	private class Result {
		private int nr;
		private AbstractIRDoc doc;
		private double score;
		private String runName;
		
		public String toString(int rank) {
			return String.format("topic%i Q0 %s %i %d %s", nr, doc.getName(), rank, score, runName);
		}
	}

	private final static Comparator<Result> scoreComparator = new Comparator<Result>() {

		@Override
		public int compare(Result o1, Result o2) {
			double diff = o1.score - o2.score;
			if(diff < 0.0)
				return -1;
			if(diff > 0.0)
				return 1;
			return 0;
		}
	};
	
	private TreeSet<Result> results = new TreeSet<SearchResult.Result>(scoreComparator);
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int i = 1;
		for(Result r : results) {
			sb.append(r.toString(i++)).append("\n");
			//up to ir.maxResults documents
			if(i > App.getMaxResults())
				break;
		}
		return sb.toString();
	}
}
