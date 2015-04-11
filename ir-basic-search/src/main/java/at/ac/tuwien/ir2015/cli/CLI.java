package at.ac.tuwien.ir2015.cli;

import java.io.IOException;
import java.util.zip.ZipException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import at.ac.tuwien.ir2015.App;

public class CLI {
	
	private static Options options = new Options();
	static {		
		options.addOption("s", "search", true, "perform search with a topic file (zip)");
		options.addOption("?", "help", false, "display the help text");
		options.addOption("i", "index", true, "index a zip-file containing documents");
		options.addOption("n", "runName", true, "name of the search run, default is empty string");
		options.addOption("d", "databaseFile", true, "database file location default is './ir_db'");
	}
	
	public static void main(String[] args) throws ZipException, IOException {
		//Arrays.asList(args).forEach(s -> System.out.println(s));
		CommandLineParser parser = new BasicParser();
		
		try {
			CommandLine cmd = parser.parse(options, args);
			
			if(cmd.hasOption("?")) {
				printHelp();
				return;
			}
			try(App app = new App();) {
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
					app.search(topicFile, runName);
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
