import java.net.InetAddress;
import java.net.DatagramSocket;

public class IPScanner{
    public static void main(String[] args) throws Exception{
        //TODO: initalization ip address
        String ip_addr = null;
        try(final DatagramSocket socket = new DatagramSocket()){
  			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
  			ip_addr = socket.getLocalAddress().getHostAddress();
		}
        String ip_col[] = ip_addr.split("\\.");
        String subnet = ip_col[0] + "." + ip_col[1] + "." + ip_col[2];
        System.out.println("Scanning: " + subnet);
        checkHosts(subnet);
    }

    public static void checkHosts(String subnet){
        int timeout = 1500; // 1.5 sec
        try{
            for(int i=0;i<255;i++){
                /* Local network for netAddress/24 */
                String host = subnet + "." + i;
                if(InetAddress.getByName(host).isReachable(timeout)){
                    //TODO: Connect socket server

                    //TODO: Check InetAddress do scan for port number
                    System.out.println(host + " is reachable");
                    Thread client = new Thread(new ClientThread(host,54321));
                    client.start();
                }
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
