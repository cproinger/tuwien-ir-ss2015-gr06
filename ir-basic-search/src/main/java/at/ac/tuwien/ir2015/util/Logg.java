package at.ac.tuwien.ir2015.util;

public class Logg {

	public static final String IR_LOG = "ir.log";

	public static void info(String s) {
		if(isInfoEnabled())
			System.out.println(s);
	}

	private static volatile Boolean infoEnabled = null;
	
	private static boolean isInfoEnabled() {
		if(infoEnabled == null) {
			synchronized (Logg.class) {
				if(infoEnabled == null) {
					infoEnabled = Boolean.valueOf(System.getProperty(IR_LOG, "true"));
				}
			}
		}
		return infoEnabled;
	}
}
