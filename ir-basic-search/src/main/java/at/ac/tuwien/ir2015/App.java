package at.ac.tuwien.ir2015;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.input.ReaderInputStream;

/**
 * Main application class, is instantiated by the CLI
 * for indexing and searching. 
 * 
 * @author cproinger
 *
 */
public class App {
	
	private class Reader implements Consumer<ZipEntry> {

		private final ZipFile zip;

		public Reader(ZipFile zip) {
			this.zip = zip;
		}

		@Override
		public void accept(ZipEntry t) {
			String name = t.getName();
			try (InputStream is = zip.getInputStream(t);) {
				
				
				if(skip != null) {
					String lastChar = name.substring(name.length()-1, 
							name.length() - 0);
					if(skip.compareTo(lastChar) < 0)
						return;
				}
				
				System.out.println("reading: " + name);
				LuceneIRDoc doc = new LuceneIRDoc(name, is);
				doc.process();
				try {
					docQueue.put(doc);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				throw new RuntimeException("error reading " + name, e);
			}
		}		
	}

	private class Searcher implements Consumer<ZipEntry> {

		private final ZipFile zip;
		private final String runName;

		public Searcher(ZipFile zip, String runName) {
			this.zip = zip;
			this.runName = runName;
		}
		
		@Override
		public void accept(ZipEntry t) {
			try (InputStream is = zip.getInputStream(t)) {
				search(new LuceneIRDoc(t.getName(), is), runName);
			} catch (IOException e) {
				throw new RuntimeException("error searching " + t.getName(), e);
			}
		}
	}
	
	/**
	 * mein laptop (cproinger) is schon alt und einmal hat er sich abgeschaltet vermutlich
	 * weil er zu heiß wurde wegen der hohen cpu-last. Könnte so ein
	 * System-property angeben welches z.B. alle x files eine pause macht. 
	 * 
	 * aber momentan gehts eh, mal schaun. 
	 */
	private static final String IR_PAUSE = "ir.pause";
	private static final String IR_RUN_NAME = "ir.runName";

	private static final String skip = System.getProperty("ir.skip");

	static int getMaxResults() {
		return Integer.parseInt(System.getProperty("ir.maxResults", "100"));
	}
	
	public static void main(String[] args) throws ZipException, IOException, ParseException {
		long start = System.currentTimeMillis();
		App app = new App();
		app.index("e:/tu/information retrieval/20_newsgroups_subset.zip");
		long took = System.currentTimeMillis() - start;
		System.out.println("took " + (took / 1000) + " seconds");
		
		StringReader sr = new StringReader("screen");
		
		LuceneIRDoc doc = new LuceneIRDoc("test", new ReaderInputStream(sr));
		doc.process();
		app.search(doc, "test");
	}

	private InvertedIndex bagOfWords = new InvertedIndex();

	private BlockingQueue<LuceneIRDoc> docQueue = new LinkedBlockingQueue<LuceneIRDoc>(20);

	public void index(String documentCollectionFile) throws ZipException, IOException {
		Thread t = new Thread() {
			@Override
			public void run() {
				AbstractIRDoc doc = null;
				try {
					while((doc = docQueue.take()) != null) {
						System.out.println("adding to index: " + doc.getName()); 
						bagOfWords.add(doc);
					}
				} catch (InterruptedException e) {
					System.out.println("interrupted");
				}
				System.out.println("index done");
			}
		};
		t.start();
		
		
		try (ZipFile zip = new ZipFile(new File(documentCollectionFile));) {
			Reader reader = new Reader(zip);
			zip.stream()
				.parallel()
				.forEach(reader);
		}

		t.interrupt();
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void search(AbstractIRDoc doc, String runName) {
		SearchResult sr = new SearchResult(doc.getName(), runName);
		for(String s : doc.getCounts().keySet()) {
			IndexValue b = bagOfWords.get(s);
			if(b != null) {
				//hit
				sr.add(b);
			}
		}
		System.out.println("search result: \n\n" 
				 + sr.toString());
	}

	public void search(String topicFile, String runName) throws ZipException, IOException {
		
		try (ZipFile zip = new ZipFile(new File(topicFile));) {
			Searcher searcher = new Searcher(zip, runName);
			zip.stream().sequential().forEach(searcher);
		}
	}
}
