package at.ac.tuwien.ir2015;

import java.util.Map;

public enum IndexType {

	BAGOFWORDS {

		@Override
		public Map<String, Integer> getCounts(AbstractIRDoc doc) {
			return doc.getCounts();
		}
		
	},
	BIGRAM {

		@Override
		public Map<String, Integer> getCounts(AbstractIRDoc doc) {
			return doc.getBiCounts();
		}
		
	};

	public abstract Map<String, Integer> getCounts(AbstractIRDoc doc);
}
