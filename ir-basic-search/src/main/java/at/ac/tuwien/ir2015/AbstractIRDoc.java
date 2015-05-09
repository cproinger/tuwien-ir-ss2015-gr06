package at.ac.tuwien.ir2015;

import java.util.Map;

import at.ac.tuwien.ir2015.util.CountingMap;

public abstract class AbstractIRDoc {
	protected final String name;
	
	protected CountingMap counts;
	protected CountingMap bicounts;

	public static final String IR_ANALYZER_CASE_FOLDING = "ir.analyzer.caseFolding";

	public static final String IR_ANALYZER_STOPWORDS = "ir.analyzer.stopwords";

	public static final String IR_ANALYZER_STEM = "ir.analyzer.stem";

	public AbstractIRDoc(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "AbstractIRDoc [name=" + name + "]";
	}

	public String getName() {
		return name;
	}
	
	public String getSimpleName() {
		return name.substring(name.lastIndexOf("/") + 1);
	}

	public Map<String, Integer> getCounts() {
		return this.counts;
	}

	public Map<String, Integer> getBiCounts() {
		return this.bicounts;
	}
}