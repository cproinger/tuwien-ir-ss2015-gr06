package at.ac.tuwien.ir2015;

public enum StorageType {

	INMEMORY {
		@Override
		public InvertedIndex newInvertedIndex(IndexType it) {
			return new InMemoryInvertedIndex(it);
		}
	},
	DATABASE {
		@Override
		public InvertedIndex newInvertedIndex(IndexType it) {
			return new RDBMSInvertedIndex(it);
		}
	};
	
	public abstract InvertedIndex newInvertedIndex(IndexType it);
}
