package at.ac.tuwien.ir2015.ex2;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.ac.tuwien.ir2015.ex2.MyIndexSearcher.SearchType;


public class MyIndexSearcherTest {

	private IndexReader reader;

	@Before
	public void setup() throws IOException {
		reader = DirectoryReader.open(FSDirectory.open(Paths
				.get("target/index")));
	}
	
	@After
	public void tearDown() throws IOException {
		reader.close();
	}
	
	@Test
	public void test() throws IOException, ParseException {
		System.out.println("Before Normal Search");
		System.out.println("____________________________________________________________________________");
		// queries "jail cat fish" "search for atheism"
		new MyIndexSearcher(reader, SearchType.BM25).search("jail cat fish");
		System.out.println("Before BM25L Search");
		System.out.println("____________________________________________________________________________");
		new MyIndexSearcher(reader, SearchType.BM25L).search("jail cat fish");
	}
}


