package at.ac.tuwien.ir2015.ex2.similarity;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import at.ac.tuwien.ir2015.ex2.similarity.BM25LSimilarity;


public class BM25LSimilarityTest {

	
	private static final float DELTA = 0.0000000000001f;
	private static final BM25LSimilarity BM25L_SIMILARITY = new BM25LSimilarity();

	@Test
	public void testIDF() {
		//um ein gefühl für diese werte zu bekommen.
		float x = BM25L_SIMILARITY.idf(5, 100);
		assertEquals(2.9103725f, x, DELTA);
	}
	
	@Test
	public void testDecodeNormValue() {
		//um ein gefühl für diese werte zu bekommen. 
		//streng monoton fallend
		assertEquals(2.95147899e18f, BM25L_SIMILARITY.decodeNormValue((byte) 1), DELTA);
		assertEquals(2.95147899e18f, BM25L_SIMILARITY.decodeNormValue((byte) 1), DELTA);
		
		assertEquals(2.04963825e18f, BM25L_SIMILARITY.decodeNormValue((byte) 2), DELTA);
		assertEquals(1.1529215e18f, BM25L_SIMILARITY.decodeNormValue((byte) 4), DELTA);
		assertEquals(7.3786975e17f, BM25L_SIMILARITY.decodeNormValue((byte) 5), DELTA);
		assertEquals(5.12409561e17f, BM25L_SIMILARITY.decodeNormValue((byte) 6), DELTA); 
		assertEquals(2.88230376e17f, BM25L_SIMILARITY.decodeNormValue((byte) 8), DELTA);
		assertEquals(1.80143985e16f, BM25L_SIMILARITY.decodeNormValue((byte) 16), DELTA);
		assertEquals(7.0368744e13f, BM25L_SIMILARITY.decodeNormValue((byte) 32), DELTA);
		assertEquals(1.07374182e9f, BM25L_SIMILARITY.decodeNormValue((byte) 64), DELTA);
		assertEquals(0.25, BM25L_SIMILARITY.decodeNormValue((byte) 128), DELTA);
		assertEquals(1.770126e-20f, BM25L_SIMILARITY.decodeNormValue((byte) 255), DELTA);
		//bis hier halt. 
		assertEquals(Float.POSITIVE_INFINITY, BM25L_SIMILARITY.decodeNormValue((byte) 256), DELTA);
	}
}
