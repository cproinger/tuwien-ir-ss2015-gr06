package at.ac.tuwien.ir2015.ex2;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.FSDirectory;

import at.ac.tuwien.ir2015.ex2.MyIndexSearcher.SearchType;

public class TopicQueries {

	public static void main(String[] args) throws IOException, ParseException {

		try (IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get("target/index")));) {
			
			for(SearchType st : SearchType.values()) {
				
				
				try(PrintStream printStream = new PrintStream("target/" + st + "-trec.txt")) { 
					MyIndexSearcher searcher = new MyIndexSearcher(reader, st, printStream);
					
					for(File f : new File("topics").listFiles()) {					
						searcher.search(f, "exp-" + st);
					}
				}
			}
			
			
		}
	}

}
