package at.ac.tuwien.ir2015.ex2.similarity;

import java.io.IOException;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.SmallFloat;

/**
 * BM25L Similarity. sifaka.cs.uiuc.edu/~ylv2/pub/sigir11-bm25l.pdf
 * 
 * @lucene.experimental
 * 
 *                      As of now just a copy of BM25
 *                      Modifications:
 *                      	explainScore
 *                      		adjusted value returned in tfNormExpl, which is provided as a detail of 
 *                      		the result explanation type, takes delta into account (also provides 
 *                      		information on delta in details)
 */
public class BM25LSimilarity extends Similarity {
	private final float k1;
	private final float b;
	//NEW
	private float delta = 0.5f;

	// TODO: should we add a delta like
	// sifaka.cs.uiuc.edu/~ylv2/pub/sigir11-bm25l.pdf ?
	// KS: example states that delta should be a command line param, thus should be passed through

	public BM25LSimilarity(float delta) {
		this();
		this.delta = delta;
	}
	
	/**
	 * BM25 with the supplied parameter values.
	 * 
	 * @param k1
	 *            Controls non-linear term frequency normalization (saturation).
	 * @param b
	 *            Controls to what degree document length normalizes tf values.
	 */
	public BM25LSimilarity(float k1, float b) {
		this.k1 = k1;
		this.b = b;
	}

	/**
	 * BM25 with these default values:
	 * <ul>
	 * <li>{@code k1 = 1.2},
	 * <li>{@code b = 0.75}.</li>
	 * </ul>
	 */
	public BM25LSimilarity() {
		this.k1 = 1.2f;
		this.b = 0.75f;
	}

	/**
	 * Implemented as
	 * <code>log(1 + (numDocs - docFreq + 0.5)/(docFreq + 0.5))</code>.
	 */
	protected float idf(long docFreq, long numDocs) {
		//unterscheidet sich von der formel aus 1. motivation durch das
		//-docFreq+0.5D im Zähler + das +1 wird nicht durch den nenner dividiert. 
		/*
		 * KS
		 * Numerically identical to version in paper:
		 * log((N + 1) / (df(q) + 0.5))
		 * 
		 * Trick: add and subtract 1 to the term in the log, but, the one you subtract, you formulate as:
		 * 		(df(q) + 0.5) / (df(q) + 0.5)
		 * 		been a while since the basics! ;)
		 */
		return (float) Math.log(1 + (numDocs - docFreq + 0.5D)
				/ (docFreq + 0.5D));
	}

	
	/** The default implementation returns <code>1</code> */
	protected float scorePayload(int doc, int start, int end, BytesRef payload) {
		return 1;
	}

	/**
	 * The default implementation computes the average as
	 * <code>sumTotalTermFreq / maxDoc</code>, or returns <code>1</code> if the
	 * index does not store sumTotalTermFreq: any field that omits frequency
	 * information).
	 */
	protected float avgFieldLength(CollectionStatistics collectionStats) {
		final long sumTotalTermFreq = collectionStats.sumTotalTermFreq();
		if (sumTotalTermFreq <= 0) {
			return 1f; // field does not exist, or stat is unsupported
		} else {
			return (float) (sumTotalTermFreq / (double) collectionStats
					.maxDoc());
		}
	}

	/**
	 * The default implementation encodes <code>boost / sqrt(length)</code> with
	 * {@link SmallFloat#floatToByte315(float)}. This is compatible with
	 * Lucene's default implementation. If you change this, then you should
	 * change {@link #decodeNormValue(byte)} to match.
	 */
	protected byte encodeNormValue(float boost, int fieldLength) {
		return SmallFloat
				.floatToByte315(boost / (float) Math.sqrt(fieldLength));
	}

	/**
	 * The default implementation returns <code>1 / f<sup>2</sup></code> where
	 * <code>f</code> is {@link SmallFloat#byte315ToFloat(byte)}.
	 */
	protected float decodeNormValue(byte b) {
		return NORM_TABLE[b & 0xFF];
	}

	/**
	 * True if overlap tokens (tokens with a position of increment of zero) are
	 * discounted from the document's length.
	 */
	protected boolean discountOverlaps = true;

	/**
	 * Sets whether overlap tokens (Tokens with 0 position increment) are
	 * ignored when computing norm. By default this is true, meaning overlap
	 * tokens do not count when computing norms.
	 */
	public void setDiscountOverlaps(boolean v) {
		discountOverlaps = v;
	}

	/**
	 * Returns true if overlap tokens are discounted from the document's length.
	 * 
	 * @see #setDiscountOverlaps
	 */
	public boolean getDiscountOverlaps() {
		return discountOverlaps;
	}

	/** Cache of decoded bytes. */
	private static final float[] NORM_TABLE = new float[256];

	static {
		for (int i = 0; i < 256; i++) {
			float f = SmallFloat.byte315ToFloat((byte) i);
			NORM_TABLE[i] = 1.0f / (f * f);
		}
	}

	@Override
	public final long computeNorm(FieldInvertState state) {
		final int numTerms = discountOverlaps ? state.getLength()
				- state.getNumOverlap() : state.getLength();
		return encodeNormValue(state.getBoost(), numTerms);
	}

	/**
	 * Computes a score factor for a simple term and returns an explanation for
	 * that score factor.
	 * 
	 * <p>
	 * The default implementation uses:
	 * 
	 * <pre class="prettyprint">
	 * idf(docFreq, searcher.maxDoc());
	 * </pre>
	 * 
	 * Note that {@link CollectionStatistics#maxDoc()} is used instead of
	 * {@link org.apache.lucene.index.IndexReader#numDocs()
	 * IndexReader#numDocs()} because also {@link TermStatistics#docFreq()} is
	 * used, and when the latter is inaccurate, so is
	 * {@link CollectionStatistics#maxDoc()}, and in the same direction. In
	 * addition, {@link CollectionStatistics#maxDoc()} is more efficient to
	 * compute
	 * 
	 * @param collectionStats
	 *            collection-level statistics
	 * @param termStats
	 *            term-level statistics for the term
	 * @return an Explain object that includes both an idf score factor and an
	 *         explanation for the term.
	 */
	public Explanation idfExplain(CollectionStatistics collectionStats,
			TermStatistics termStats) {
		final long df = termStats.docFreq();
		final long max = collectionStats.maxDoc();
		final float idf = idf(df, max);
		return new Explanation(idf, "idf(docFreq=" + df + ", maxDocs=" + max
				+ ")");
	}

	/**
	 * Computes a score factor for a phrase.
	 * 
	 * <p>
	 * The default implementation sums the idf factor for each term in the
	 * phrase.
	 * 
	 * @param collectionStats
	 *            collection-level statistics
	 * @param termStats
	 *            term-level statistics for the terms in the phrase
	 * @return an Explain object that includes both an idf score factor for the
	 *         phrase and an explanation for each term.
	 */
	public Explanation idfExplain(CollectionStatistics collectionStats,
			TermStatistics termStats[]) {
		final long max = collectionStats.maxDoc();
		float idf = 0.0f;
		final Explanation exp = new Explanation();
		exp.setDescription("idf(), sum of:");
		for (final TermStatistics stat : termStats) {
			final long df = stat.docFreq();
			final float termIdf = idf(df, max);
			exp.addDetail(new Explanation(termIdf, "idf(docFreq=" + df
					+ ", maxDocs=" + max + ")"));
			idf += termIdf;
		}
		exp.setValue(idf);
		return exp;
	}

	@Override
	public final SimWeight computeWeight(float queryBoost,
			CollectionStatistics collectionStats, TermStatistics... termStats) {
		
		Explanation idf = termStats.length == 1 ? idfExplain(collectionStats,
				termStats[0]) : idfExplain(collectionStats, termStats);

		float avgdl = avgFieldLength(collectionStats);

		// compute freq-independent part of bm25 equation across all norm values
		float cache[] = new float[256];
		for (int i = 0; i < cache.length; i++) {
			//(2)  
			cache[i] = k1 * ((1 - b) + b * decodeNormValue((byte) i) / avgdl);
		}
		return new BM25LStats(collectionStats.field(), idf, queryBoost, avgdl,
				cache);
	}

	@Override
	public final SimScorer simScorer(SimWeight stats, LeafReaderContext context)
			throws IOException {
		BM25LStats bm25lstats = (BM25LStats) stats;
		return new BM25LDocScorer(bm25lstats, context.reader().getNormValues(
				bm25lstats.field));
	}

	private class BM25LDocScorer extends SimScorer {
		private final BM25LStats stats;
		private final float weightValue; // boost * idf * (k1 + 1)
		private final NumericDocValues norms;
		private final float[] cache;

		BM25LDocScorer(BM25LStats stats, NumericDocValues norms)
				throws IOException {
			this.stats = stats;
			//zähler von (2)?
			this.weightValue = stats.weight * (k1 + 1);
			this.cache = stats.cache;
			this.norms = norms;
		}

		@Override
		public float score(int doc, float freq) {
			// if there are no norms, we act as if b=0
			// KS: here we still have to adjust something!!!
			
			
			float norm;
			float returnVal;
			
			if (norms == null) {
				norm = k1;
			} else {
				norm = cache[(byte) norms.get(doc) & 0xFF];
			}
			returnVal = weightValue * freq / (freq + norm);
			return returnVal;
			
			
			
/*
 * expanded for debuging
 * 			float norm = norms == null ? k1
					: cache[(byte) norms.get(doc) & 0xFF];
			return weightValue * freq / (freq + norm);
			*/
		}

		@Override
		public Explanation explain(int doc, Explanation freq) {
			return explainScore(doc, freq, stats, norms);
		}

		@Override
		public float computeSlopFactor(int distance) {
			/* Implemented as <code>1 / (distance + 1)</code>. */
			return 1.0f / (distance + 1);
		}

		@Override
		public float computePayloadFactor(int doc, int start, int end,
				BytesRef payload) {
			return scorePayload(doc, start, end, payload);
		}
	}

	/** Collection statistics for the BM25 model. */
	private static class BM25LStats extends SimWeight {
		/** BM25's idf */
		private final Explanation idf;
		/** The average document length. */
		private final float avgdl;
		/** query's inner boost */
		private final float queryBoost;
		/** query's outer boost (only for explain) */
		private float topLevelBoost;
		/** weight (idf * boost) */
		private float weight;
		/** field name, for pulling norms */
		private final String field;
		/** precomputed norm[256] with k1 * ((1 - b) + b * dl / avgdl) */
		// KS: this is what we need to adjust the document weights!!!
		// KS: the term ((1 - b) + b * dl / avgdl) is the bottom half of c'
		private final float cache[];

		BM25LStats(String field, Explanation idf, float queryBoost, float avgdl,
				float cache[]) {
			this.field = field;
			this.idf = idf;
			this.queryBoost = queryBoost;
			this.avgdl = avgdl;
			this.cache = cache;
		}

		@Override
		public float getValueForNormalization() {
			// we return a TF-IDF like normalization to be nice, but we don't
			// actually normalize ourselves.
			final float queryWeight = idf.getValue() * queryBoost;
			return queryWeight * queryWeight;
		}

		@Override
		public void normalize(float queryNorm, float topLevelBoost) {
			// we don't normalize with queryNorm at all, we just capture the
			// top-level boost
			this.topLevelBoost = topLevelBoost;
			this.weight = idf.getValue() * queryBoost * topLevelBoost;
		}
	}

	private Explanation explainScore(int doc, Explanation freq, BM25LStats stats, NumericDocValues norms) {

		Explanation result = new Explanation();
		result.setDescription("score(doc=" + doc + ", freq=" + freq + "), product of:");

		Explanation boostExpl = new Explanation(stats.queryBoost * stats.topLevelBoost, "boost");
		if (boostExpl.getValue() != 1.0f) result.addDetail(boostExpl);

		result.addDetail(stats.idf);

		Explanation tfNormExpl = new Explanation();
		tfNormExpl.setDescription("tfNorm, computed from:");
		tfNormExpl.addDetail(freq);
		tfNormExpl.addDetail(new Explanation(k1, "parameter k1"));
		if (norms == null) {
			tfNormExpl.addDetail(new Explanation(0, "parameter b (norms omitted for field)"));
			// KS: if be is omitted, then the rest of the document lenght algorithms don't run
			// thus irrelevant
			//das ist der rechte teil von (2): (k1+1) * c'(q,D) / k1 + c'(q,D). 
			tfNormExpl.setValue((freq.getValue() * (k1 + 1)) / (freq.getValue() + k1));
		} else {
			float doclen = decodeNormValue((byte) norms.get(doc));
			float normTF2;
			float normTFDelta;
			float avgdl = stats.avgdl;
			tfNormExpl.addDetail(new Explanation(b, "parameter b"));
			tfNormExpl.addDetail(new Explanation(stats.avgdl, "avgFieldLength"));
			tfNormExpl.addDetail(new Explanation(doclen, "fieldLength"));
			tfNormExpl.addDetail(new Explanation(delta, "delta"));
			// KS!!!
			// F(q,D)= 
			// but including b, so middle formula using c
			//das ist der linke teil von (2): (k1+1*c(q,D)) / c(q,D)+k1*(1-b+b*doclen/avdl). 
			// check if c' > 0
			// c(q, D) = freq.getValue()
			// c'(q,D) = freq.getValue()/(1 - b + b*doclen/avdl)
			// f'(q, D) = ((k1 + 1)*abs(c' + delta)) / (k1 + abs(c' + delta)) 
			// f'(q, D) = ((k1 + 1)*normTF2) / (k1 + normTF2) 
			// 
			// normTF2 = c'(q,D) 
			normTF2 = freq.getValue()/(1 - b + b*doclen/avgdl);
			if (normTF2 <=0) {
				tfNormExpl.setValue(0);
			} else {
				normTFDelta = Math.abs(normTF2 + delta);
				tfNormExpl.setValue(((k1 + 1)*normTFDelta) / (k1 + normTFDelta));
				
			}
			//tfNormExpl.setValue((freq.getValue() * (k1 + 1)) / (freq.getValue() + k1 * (1 - b + b * doclen / stats.avgdl)));
		}
		result.addDetail(tfNormExpl);
		result.setValue(boostExpl.getValue() * stats.idf.getValue() * tfNormExpl.getValue());
		return result;
	}

	@Override
	public String toString() {
		return "BM25L(k1=" + k1 + ", b=" + b + ", delta=" + delta + ")";
	}

	/**
	 * Returns the <code>k1</code> parameter
	 * 
	 * @see #BM25Similarity(float, float)
	 */
	public float getK1() {
		return k1;
	}

	/**
	 * Returns the <code>b</code> parameter
	 * 
	 * @see #BM25Similarity(float, float)
	 */
	public float getB() {
		return b;
	}

	/**
	 * Returns the <code>delta</code> parameter
	 * 
	 * @see #BM25Similarity(float, float)
	 */
	public float getDelta() {
		return delta;
	}
}
