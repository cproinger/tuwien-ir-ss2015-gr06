package at.ac.tuwien.ir2015;

import java.util.Map;

import at.ac.tuwien.ir2015.util.CountingMap;

public abstract class AbstractIRDoc {
	protected final String name;
	
	protected CountingMap counts;
	protected CountingMap bicounts = new CountingMap();

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

	public Map<String, Integer> getCounts() {
		return this.counts;
	}
}