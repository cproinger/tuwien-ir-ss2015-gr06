package at.ac.tuwien.ir2015.ex2;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.FSDirectory;

import at.ac.tuwien.ir2015.ex2.MyIndexSearcher.SearchType;

public class TopicQueries {

	public static void main(String[] args) throws IOException, ParseException {

		try (IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get("target/index")));) {
			
			LinkedList<SearchType> values = new LinkedList<SearchType>(Arrays.asList(SearchType.values()));
			values.addFirst(null);
			for(SearchType st : values) {
				
				
				try(PrintStream printStream = new PrintStream("target/" + searchTypeToString(st) + "-trec.txt")) { 
					MyIndexSearcher searcher = new MyIndexSearcher(reader, st, printStream);
					
					for(File f : new File("topics").listFiles()) {					
						searcher.search(f, "group6-experiment-" + searchTypeToString(st));
					}
				}
			}
			
			
		}
	}

	private static String searchTypeToString(SearchType st) {
		return st == null ? "LuceneDefault" : st.toString();
	}

}
