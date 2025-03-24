import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    //Initial IP
    public static String AIP = null;//PC
    public static String BIP = null;//Client
/*
* Class for Making Muti-Thread Socket Server*/
    public static void main(String[] args){

        ArrayList<Thread> list = new ArrayList<>();

        try{
            // listen for incoming connections on port 54321
            ServerSocket socket = new ServerSocket(54321);
            System.out.println("Server Listening on port 54321");

            /* Pi Version : Identify source ip between PC or Pi */

//            Scanner scan = new Scanner(System.in);
//            System.out.println("Setting IP Address for PC: ");
//            AIP = scan.nextLine();
//            System.out.println("Setting IP Address for Client Pi: ");
//            BIP = scan.nextLine();

            while (true){
                Socket client = socket.accept();// hand on connection until incoming
                // Use ServerThread to handle connection
                // and send output to client
                Thread thread = new Thread(new ServerThread(client));
                list.add(thread);
                thread.start();
                System.out.println("Thread started...");
            }
        }catch (IOException e){
            e.printStackTrace();
            System.out.println("Server Down : Error");
        }
    }
}
