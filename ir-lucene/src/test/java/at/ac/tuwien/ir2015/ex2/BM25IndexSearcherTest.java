package at.ac.tuwien.ir2015.ex2;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;


public class BM25IndexSearcherTest {

	@Test
	public void test() throws IOException, ParseException {
		new BM25IndexSearcher().search();
	}
}
