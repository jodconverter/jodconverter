//
// JODConverter - Java OpenDocument Converter
// Copyright 2009 Art of Solving Ltd
// Copyright 2004-2009 Mirko Nasato
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
package org.artofsolving.jodconverter.office;

public class ExternalOfficeManagerConfiguration {

    private OfficeConnectionProtocol connectionProtocol = OfficeConnectionProtocol.SOCKET;
    private int portNumber = 2002;
    private String pipeName = "office";
    private boolean connectOnStart = true;

    public ExternalOfficeManagerConfiguration setConnectionProtocol(OfficeConnectionProtocol connectionProtocol) {
        this.connectionProtocol = connectionProtocol;
        return this;
    }

    public ExternalOfficeManagerConfiguration setPortNumber(int portNumber) {
        this.portNumber = portNumber;
        return this;
    }

    public ExternalOfficeManagerConfiguration setPipeName(String pipeName) {
        this.pipeName = pipeName;
        return this;
    }

    public ExternalOfficeManagerConfiguration setConnectOnStart(boolean connectOnStart) {
        this.connectOnStart = connectOnStart;
        return this;
    }

    public OfficeManager buildOfficeManager() {
        UnoUrl unoUrl = connectionProtocol == OfficeConnectionProtocol.SOCKET ? UnoUrl.socket(portNumber) : UnoUrl.pipe(pipeName);
        return new ExternalOfficeManager(unoUrl, connectOnStart);
    }

}
