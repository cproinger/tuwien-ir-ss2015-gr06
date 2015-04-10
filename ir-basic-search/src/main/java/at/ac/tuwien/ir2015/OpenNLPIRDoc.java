package at.ac.tuwien.ir2015;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;

import org.apache.lucene.analysis.Tokenizer;

import at.ac.tuwien.ir2015.util.CountingMap;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

@Deprecated
public class OpenNLPIRDoc extends AbstractIRDoc {
	

	private static final String INVALID_WORD_REG_EXP = "^\\W+";
	private static final String TOKEN_STRIP_REG_EXP = "^\\W+|\\W+$";
	private static final String VALID_TOPIC_REG_EXP = "^[a-zA-Z0-9]+$";
	

	
    public OpenNLPIRDoc(String name, InputStream is) {
		super(name, is);
	}

    @Override
	public void process() {
    	this.counts = new CountingMap();
		try {
			TokenizerModel model = new TokenizerModel(getClass().getResource("/en-token.bin"));
			TokenizerME tok = new TokenizerME(model);
			
			//WhitespaceTokenizer tok = new WhitespaceTokenStream()
			
		
			Scanner sc = new Scanner(is);
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				
				line = line.replaceAll("[>!:|/]", " ");
				line = line.replaceAll("--+", " ");
				line = line.replaceAll("\\.\\.+", " ");
				line = line.replaceAll("\\. ", " ");
				line = line.replaceAll("  ", " ");
				
				String[] tokenized = tok.tokenize(line);
				String lastToken = null;
				for(String t : tokenized) {
					String actToken = t;//t.intern();
					counts.add(actToken);
					
					if(lastToken != null) {
						bicounts.add(lastToken + " " + actToken);
					}
					lastToken = actToken;
				}
				//System.out.println(Arrays.toString(tokenized));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(tokens);
		
	}
	

}
