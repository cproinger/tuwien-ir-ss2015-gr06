package at.ac.tuwien.ir2015;

import static org.junit.Assert.*;

import org.junit.Test;


public class LuceneIRDocTest {


	private static final String ALT_ATHEISM_54254_TXT = "/alt.atheism/54254.txt";
	private static final String ALT_ATHEISM_51137_TXT = "/alt.atheism/51137.txt";

	
	@Test
	public void test() {
		LuceneIRDoc irDoc = new LuceneIRDoc(ALT_ATHEISM_54254_TXT, 
				getClass().getResourceAsStream(ALT_ATHEISM_54254_TXT));
		
		LuceneIRDoc irDoc2 = new LuceneIRDoc(ALT_ATHEISM_51137_TXT, 
				getClass().getResourceAsStream(ALT_ATHEISM_51137_TXT));
		irDoc.process();
		irDoc2.process();
		
		InvertedIndex bow = new InMemoryInvertedIndex();
		bow.add(irDoc);
		bow.add(irDoc2);
		
		IndexValue value = bow.get("self");
		assertNotNull(value);
		System.out.println(value);
		
	}
}
