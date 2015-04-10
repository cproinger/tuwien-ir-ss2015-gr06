package at.ac.tuwien.ir2015;

import java.io.InputStream;
import java.util.Map;

import at.ac.tuwien.ir2015.util.CountingMap;

public abstract class AbstractIRDoc {
	protected final String name;
	protected final InputStream is;
	
	protected CountingMap counts;
	protected CountingMap bicounts = new CountingMap();

	public AbstractIRDoc(String name, InputStream is) {
		this.name = name;
		this.is = is;
	}

	@Override
	public String toString() {
		return "AbstractIRDoc [name=" + name + "]";
	}

	public String getName() {
		return name;
	}

	public Map<String, Integer> getCounts() {
		if(counts == null) {
			//sicherheitshalber implizit verarbeiten falls das noch nicht passiert ist. 
			process();
		}
		return counts;
	}
	
	public abstract void process();
}