package net.sf.jodconverter.office;

/**
 * Encapsulates the UNO Interprocess Connection type and parameters.
 * <p>
 * OpenOffice.org supports two connection types: TCP sockets and named pipes.
 * Named pipes are marginally faster and do not take up a TCP port, but they
 * require native libraries, which means setting <em>java.library.path</em>
 * when starting Java. E.g. on Linux
 * <pre>
 * java -Djava.library.path=/opt/openoffice.org/ure/lib ...
 * </pre>
 * <p>
 * See <a href="http://wiki.services.openoffice.org/wiki/Documentation/DevGuide/ProUNO/Opening_a_Connection">Opening a Connection</a>
 * in the OpenOffice.org Developer's Guide for more details.
 */
public class OfficeConnectionMode {

    private final String acceptString;
    private final String connectString;

    private OfficeConnectionMode(String acceptString, String connectString) {
        this.acceptString = acceptString;
        this.connectString = connectString;
    }

    public static OfficeConnectionMode socket(int port) {
        String socketString = "socket,host=127.0.0.1,port=" + port;
        return new OfficeConnectionMode(socketString, socketString + ",tcpNoDelay=1");
    }

    public static OfficeConnectionMode pipe(String pipeName) {
        String pipeString = "pipe,name=" + pipeName;
        return new OfficeConnectionMode(pipeString, pipeString);
    }

    public String getAcceptString() {
        return acceptString;
    }

    public String getConnectString() {
        return connectString;
    }

    @Override
    public String toString() {
        return connectString;
    }

}
