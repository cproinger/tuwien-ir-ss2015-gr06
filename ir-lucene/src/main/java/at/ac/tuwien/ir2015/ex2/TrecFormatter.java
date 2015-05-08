package at.ac.tuwien.ir2015.ex2;

import java.io.IOException;
import java.io.PrintStream;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

public class TrecFormatter {

	private IndexReader reader;
	private PrintStream printStream;

	public TrecFormatter(IndexReader reader, PrintStream printStream) {
		this.reader = reader;
		this.printStream = printStream;
	}

	public void format(String queryName, TopDocs result, String experimentName) throws IOException {
		for(int i = 0; i < result.scoreDocs.length; i++) {
			ScoreDoc sd = result.scoreDocs[i];
			StringBuilder docName = getOutputDocumentPath(sd);
			printStream.format("%s Q0 %s %d %f %s\n", queryName, docName, i+1, sd.score, experimentName);
		}
		
	}

	private StringBuilder getOutputDocumentPath(ScoreDoc sd) throws IOException {
		Document document = reader.document(sd.doc);
		IndexableField field = document.getField("path");
		//sonst passt das mit den qrels wo das mit einem unix-/ drinsteht nicht zusammen und liefert 0 resultate.
		String p = field.stringValue().replace("\\", "/"); 
		StringBuilder reverse = new StringBuilder(p).reverse();
		String a = reverse.substring(0, reverse.indexOf("/", reverse.indexOf("/")+1));
		StringBuilder docName = new StringBuilder(a).reverse();
		return docName;
	}
}
