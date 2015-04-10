package at.ac.tuwien.ir2015;

import static org.junit.Assert.*;

import org.junit.Test;


public class LuceneIRDocTest {

	@Test
	public void test() {
		LuceneIRDoc irDoc = new LuceneIRDoc(OpenNLPIRDocTest.ALT_ATHEISM_54254_TXT, 
				getClass().getResourceAsStream(OpenNLPIRDocTest.ALT_ATHEISM_54254_TXT));
		
		LuceneIRDoc irDoc2 = new LuceneIRDoc(OpenNLPIRDocTest.ALT_ATHEISM_54254_TXT, 
				getClass().getResourceAsStream(OpenNLPIRDocTest.ALT_ATHEISM_54254_TXT));
		irDoc.process();
		irDoc2.process();
		
		InvertedIndex bow = new InvertedIndex();
		bow.add(irDoc);
		bow.add(irDoc2);
		
		IndexValue value = bow.get("self");
		assertNotNull(value);
		System.out.println(value);
		
	}
}
