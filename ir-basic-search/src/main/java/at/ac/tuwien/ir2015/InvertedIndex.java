package at.ac.tuwien.ir2015;

public interface InvertedIndex {

	public abstract void add(AbstractIRDoc doc);

	public abstract IndexValue get(String s);

	public abstract ISearchResult search(AbstractIRDoc doc, String runName);

}