package net.sf.jodconverter.test;

import java.io.File;

public abstract class TestUtils {

	public static File getOfficeHome() {
		String home = System.getProperty("office.home");
		if (home == null) {
			throw new RuntimeException("please set the 'office.home' system property before running tests");
		}
		return new File(home);
	}

}
