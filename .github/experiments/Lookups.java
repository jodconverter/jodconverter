import java.net.InetAddress;
import java.net.Socket;

/** Times the name lookups that the Java UNO socket connection does. */
public class Lookups {

  interface Call {
    Object run() throws Exception;
  }

  static void time(String label, Call call) {
    long start = System.nanoTime();
    Object result;
    try {
      result = call.run();
    } catch (Exception ex) {
      result = ex.toString();
    }
    System.out.printf("  %-52s %6.1f s -> %s%n", label, (System.nanoTime() - start) / 1e9, result);
  }

  public static void main(String[] args) throws Exception {
    System.out.println(args[0]);
    time("InetAddress.getLocalHost()", InetAddress::getLocalHost);
    time("getByName(127.0.0.1).getHostName()", () -> InetAddress.getByName("127.0.0.1").getHostName());
    time("getByName(127.0.0.1).getCanonicalHostName()", () -> InetAddress.getByName("127.0.0.1").getCanonicalHostName());
    try (Socket socket = new Socket("127.0.0.1", Integer.parseInt(args[1]))) {
      time("socket.getLocalAddress().getHostName()", () -> socket.getLocalAddress().getHostName());
      time("socket.getInetAddress().getHostName()", () -> socket.getInetAddress().getHostName());
    }
  }
}
