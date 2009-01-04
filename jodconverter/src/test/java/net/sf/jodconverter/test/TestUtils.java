//
// JODConverter - Java OpenDocument Converter
// Copyright (C) 2004-2009 - Mirko Nasato and Contributors
//
// JODConverter is free software: you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation, either version 3 of
// the License, or (at your option) any later version.
//
// JODConverter is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General
// Public License along with JODConverter.  If not, see
// <http://www.gnu.org/licenses/>.
//
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

	public static File getOfficeProfile() {
        String profile = System.getProperty("office.profile");
        if (profile == null) {
            throw new RuntimeException("please set the 'office.profile' system property before running tests");
        }
        return new File(profile);
	}
	
}
