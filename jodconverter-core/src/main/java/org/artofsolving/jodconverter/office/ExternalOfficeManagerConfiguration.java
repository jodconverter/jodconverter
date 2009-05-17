package org.artofsolving.jodconverter.office;

public class ExternalOfficeManagerConfiguration {

    private int portNumber = 2002;

    public ExternalOfficeManagerConfiguration setPortNumber(int portNumber) {
        this.portNumber = portNumber;
        return this;
    }

    public OfficeManager buildOfficeManager() {
        return new ExternalOfficeManager(portNumber);
    }

}
