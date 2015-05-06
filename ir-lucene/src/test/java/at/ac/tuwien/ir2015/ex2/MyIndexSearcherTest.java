package at.ac.tuwien.ir2015.ex2;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;


public class MyIndexSearcherTest {

	@Test
	public void test() throws IOException, ParseException {
		System.out.println("Before Normal Search");
		System.out.println("____________________________________________________________________________");
		new MyIndexSearcher().search();
		System.out.println("Before BM25L Search");
		System.out.println("____________________________________________________________________________");
		new MyBM25LIndexSearcher().search();
	}
}
