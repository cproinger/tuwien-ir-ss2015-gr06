package at.ac.tuwien.ir2015.ex2;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;


public class MyIndexSearcherTest {

	@Test
	public void test() throws IOException, ParseException {
		new MyIndexSearcher().search();
	}
}
