package at.ac.tuwien.ir2015.cli;

import java.io.IOException;
import java.util.zip.ZipException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.analysis.Analyzer;

import at.ac.tuwien.ir2015.AbstractIRDoc;
import at.ac.tuwien.ir2015.App;
import at.ac.tuwien.ir2015.IndexType;
import at.ac.tuwien.ir2015.LuceneIRDoc;
import at.ac.tuwien.ir2015.Persistence;
import at.ac.tuwien.ir2015.ScoringMethod;
import at.ac.tuwien.ir2015.StorageType;
import at.ac.tuwien.ir2015.util.Logg;

public class CLI {
	
	private static Options options = new Options();
	static {		
		options.addOption("s", "search", true, "perform search with a topic file (zip)");
		options.addOption("m", "scoringMethod", true, "which scoring method to use, one of {TF = term frequency, TF_IDF = inverse document frequency} are allowed values, default is TF");
		options.addOption("q", "queryIndex", true, "perform the search on the 'bagOfWords'- or the 'bigram'-index. Default is 'bagOfWords'");
		options.addOption("?", "help", false, "display the help text");
		options.addOption("i", "index", true, "index a zip-file containing documents");
		options.addOption("n", "runName", true, "name of the search run, default is empty string");
		options.addOption("d", "databaseFile", true, "database file location default is './ir_db'");
		options.addOption("t", "storageType", true, "storage type, one of {INMEMORY, DATABASE} default is DATABASE. \n"
				+ "Only indexing in combination with INMEMORY does not allow "
				+ " for subsequent search calls");
		options.addOption("l", "logging", false, "turns on logging for the indexing part");
		
		OptionGroup group = new OptionGroup();
		
		options.addOptionGroup(group);
		
		group.addOption(new Option("e", "disableStemming", false, "if provided disables Stemming (indexing and search need to use the same options)"));
		group.addOption(new Option("c", "disableCaseFolding", false, "if provided disables case folding (indexing and search need to use the same options)"));
		group.addOption(new Option("w", "disableStopwords", false, "if provided disables stop words (indexing and search need to use the same options)"));
		
	}
	
	public static void main(String[] args) throws ZipException, IOException {
		CommandLineParser parser = new BasicParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			
			if(cmd.hasOption("?")) {
				printHelp();
				return;
			}
			if(cmd.hasOption("d")) {
				System.setProperty(Persistence.IR_DB_FILE, cmd.getOptionValue("d"));
			}
			System.setProperty(Logg.IR_LOG, cmd.hasOption("l") ? "true" : "false");
			System.setProperty(AbstractIRDoc.IR_ANALYZER_STEM, cmd.hasOption("e") ? "false" : "true");
			System.setProperty(AbstractIRDoc.IR_ANALYZER_CASE_FOLDING, cmd.hasOption("c") ? "false" : "true");
			System.setProperty(AbstractIRDoc.IR_ANALYZER_STOPWORDS, cmd.hasOption("w") ? "false" : "true");
			
			StorageType st = StorageType.DATABASE;
			if(cmd.hasOption("t")) {
				st = StorageType.valueOf(cmd.getOptionValue("t"));
			}
			try(App app = new App(st);) {
				if(cmd.hasOption("i")) {
					String documentCollectionFile = cmd.getOptionValue("i");
					app.index(documentCollectionFile);
				}
				
				if(cmd.hasOption("s")) {
					String runName = "";
					if(cmd.hasOption("n")) {
						runName = cmd.getOptionValue("n");
					}
					String topicFile = cmd.getOptionValue("s");
					
					ScoringMethod sm = ScoringMethod.TF;
					if(cmd.hasOption("m")) {
						sm = ScoringMethod.valueOf(cmd.getOptionValue("m"));
					}
					IndexType it = IndexType.BAGOFWORDS;
					if(cmd.hasOption("q")) {
						it = IndexType.valueOf(cmd.getOptionValue("q"));
					}
					app.search(topicFile, it, sm, runName);
				}
			}
			
			
		} catch (ParseException e) {
			e.printStackTrace();
			
			printHelp();
		}
	}

	private static void printHelp() {
		HelpFormatter hf = new HelpFormatter();
		hf.printHelp("ir", options);
	}
}
