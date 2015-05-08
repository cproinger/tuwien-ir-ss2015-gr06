package at.ac.tuwien.ir2015.ex2;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;

import at.ac.tuwien.ir2015.ex2.MyIndexSearcher.SearchType;


public class MyIndexSearcherTest {

	@Test
	public void test() throws IOException, ParseException {
		System.out.println("Before Normal Search");
		System.out.println("____________________________________________________________________________");
		// queries "jail cat fish" "search for atheism"
		new MyIndexSearcher("target/index", SearchType.BM25).search("jail cat fish");
		System.out.println("Before BM25L Search");
		System.out.println("____________________________________________________________________________");
		new MyIndexSearcher("target/index", SearchType.BM25L).search("jail cat fish");
	}
}


