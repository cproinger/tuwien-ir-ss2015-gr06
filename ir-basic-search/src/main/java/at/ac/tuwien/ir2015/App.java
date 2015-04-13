package at.ac.tuwien.ir2015;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.input.ReaderInputStream;

import at.ac.tuwien.ir2015.util.Logg;

/**
 * Main application class, is instantiated by the CLI
 * for indexing and searching. 
 * 
 * @author cproinger
 *
 */
public class App implements Closeable {
	
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
				
				//Logg.info("reading: " + name);
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
		private IndexType it;
		private ScoringMethod sm;

		public Searcher(ZipFile zip, String runName, IndexType it, ScoringMethod sm) {
			this.zip = zip;
			this.runName = runName;
			this.it = it;
			this.sm = sm;
		}
		
		@Override
		public void accept(ZipEntry t) {
			try (InputStream is = zip.getInputStream(t)) {
				search(new LuceneIRDoc(t.getName(), is), it, sm, runName);
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
		Logg.info("took " + (took / 1000) + " seconds");
		
		StringReader sr = new StringReader("screen");
		
		LuceneIRDoc doc = new LuceneIRDoc("test", new ReaderInputStream(sr));
		doc.process();
		app.search(doc, IndexType.BAGOFWORDS, ScoringMethod.TF, "test");
	}

	private final Map<IndexType, InvertedIndex> indices = new HashMap<IndexType, InvertedIndex>();
	
	public App() {
		this(StorageType.INMEMORY);
	}
	
	public App(StorageType st) {
		for(IndexType it : IndexType.values()) {
			indices.put(it, st.newInvertedIndex(it));
		}
	}
	

	private BlockingQueue<AbstractIRDoc> docQueue = new LinkedBlockingQueue<AbstractIRDoc>(20);
	
	private class PoisonIRDoc extends AbstractIRDoc {
		public PoisonIRDoc() {
			super("poison");
		}
	}

	public void index(String documentCollectionFile) throws ZipException, IOException {
		Thread t = new Thread("Indexing-worker") {
			@Override
			public void run() {
				AbstractIRDoc doc = null;
				try {
					while((doc = docQueue.take()) != null) {
						if(doc instanceof PoisonIRDoc)
							break;
						Logg.info("adding to index: " + doc.getName());
						try {
							for(InvertedIndex ii : indices.values()) {
								ii.add(doc);
							}
						} catch (RuntimeException e) {
							e.printStackTrace();
							System.err.println("some shit happened, exiting!");
							System.exit(1);
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Logg.info("index done");
			}
		};
		t.start();
		
		
		try (ZipFile zip = new ZipFile(new File(documentCollectionFile));) {
			Reader reader = new Reader(zip);
			zip.stream()
				.filter(p -> !p.isDirectory())
				.parallel()
				.forEach(reader);
		}

		try {
			docQueue.put(new PoisonIRDoc());
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void search(AbstractIRDoc irDoc, IndexType it, ScoringMethod sm, String runName) {
		ISearchResult sr = indices.get(it).search(irDoc, sm, runName);
		System.out.print(sr.toString());
	}

	public void search(String topicFile, IndexType it, ScoringMethod sm, String runName) throws ZipException, IOException {
		
		try (ZipFile zip = new ZipFile(new File(topicFile));) {
			Searcher searcher = new Searcher(zip, runName, it, sm);
			zip.stream()
				.filter(p -> !p.isDirectory())
				.sequential()
				.forEach(searcher);
		}
	}

	@Override
	public void close() {
		Persistence.close(); 
	}
}
