package at.ac.tuwien.ir2015;

import static org.junit.Assert.*;

import org.junit.Test;

public class OpenNLPIRDocTest {

	public static final String ALT_ATHEISM_54254_TXT = "/alt.atheism/54254.txt";
	public static final String ALT_ATHEISM_51137_TXT = "/alt.atheism/51137.txt";

	@Test
	public void test() {
		OpenNLPIRDoc doc = new OpenNLPIRDoc("test", getClass().getResourceAsStream(ALT_ATHEISM_54254_TXT));
		System.out.println(doc.getCounts());
	}
}
