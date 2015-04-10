package at.ac.tuwien.ir2015;

import java.io.InputStream;

public class AbstractIRDoc {

	private static final String INVALID_WORD_REG_EXP = "^\\W+";
	private static final String TOKEN_STRIP_REG_EXP = "^\\W+|\\W+$";
	private static final String VALID_TOPIC_REG_EXP = "^[a-zA-Z0-9]+$";
	
	protected final String name;
	protected final InputStream is;
	
	protected CountingMap bagOfWords = new CountingMap();
	protected CountingMap biword = new CountingMap();

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
}