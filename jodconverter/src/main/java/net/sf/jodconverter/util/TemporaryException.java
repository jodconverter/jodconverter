//
// JODConverter - Java OpenDocument Converter
// Copyright (C) 2004-2008 - Mirko Nasato <mirko@artofsolving.com>
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, you can find it online
// at http://www.gnu.org/licenses/lgpl-2.1.html.
//
package net.sf.jodconverter.util;

/**
 * Represents an error condition that can be temporary, i.e. that could go
 * away by simply retrying the same operation after an interval.  
 */
public class TemporaryException extends Exception {

    private static final long serialVersionUID = 7237380113208327295L;

    public TemporaryException(Throwable cause) {
        super(cause);
    }

}
