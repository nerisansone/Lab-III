import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;


public class Mess_Receiver implements Runnable {

    int port;
    String  host;
    ArrayList<String> repository;
    boolean exit;
    
    public Mess_Receiver(int port, String host){
        this.port = port;
        this.host = host;
        //the repository is used to store all the messages received from the server 
        this.repository = new ArrayList<String>();
        //Variabile boolleana per uscire dal while 
        this.exit = false;
    }

    public void run(){
        byte[] buf = new byte[128];
        try { // open the multicast, connect to the multicast 
            InetAddress ia = InetAddress.getByName(host);
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            MulticastSocket ms = new MulticastSocket(port);
            //it uses old version of the method but we've seen it in class 
            ms.joinGroup(ia);
            //timeout for leaving the group (otherwise it would be stuck in the while)
            ms.setSoTimeout(200);
            //receive messages from the server and add them to the repository
            //the while is used to keep receiving messages until the user wants to exit 
            while(exit==false){
                try{
                    ms.receive(dp);
                    //converto messaggio da bytes in stringa 
                    String mess = new String(dp.getData(), 0, dp.getLength(), "US-ASCII");
                    //aggiungo messaggio a repository 
                    repository.add(mess);
                }
                catch(SocketTimeoutException e){
                    continue;
                }
            }
            //leaves the group and closes the socket
            ms.leaveGroup(ia);
            ms.close();
        } 
        
        catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void close_connection(){ 
        this.exit = true;
    }

    public synchronized void print_mess(){
        Iterator<String> iter = repository.iterator();
        while (iter.hasNext()) {
            System.out.println(iter.next()+"\n");
        }
    }
      
}
