package at.ac.tuwien.ir2015.ex2;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
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
						try(Analyzer a = new EnglishAnalyzer();
								TokenStream ts = a.tokenStream("contents", new FileReader(f));) {
							
							String searchString = toString(ts);
							searcher.search(f.getName(), "exp-" + st, searchString);
						}
					}
				}
			}
			
			
		}
	}
	
	private static String toString(TokenStream stream) throws IOException {
		CharTermAttribute att = stream.addAttribute(CharTermAttribute.class);
		
		stream.reset();
		StringBuilder sb = new StringBuilder();
		while(stream.incrementToken()) {
			String curr = att.toString();
			sb.append(curr).append(" ");
			//System.out.print(".");
		}
		stream.end();
		return sb.toString();
	}

}
