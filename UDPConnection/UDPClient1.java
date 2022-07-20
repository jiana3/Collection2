import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient1 {

	private final static String myAddress = "Server1";//"127.0.0.1";
	private final static String destAddress = "Server1";
	private final static int destPort = 1234;
	private final static int myPort = 1235;
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
        try {
        	DatagramSocket ds = new DatagramSocket();  // for sending
        	DatagramSocket dsr = new DatagramSocket(myPort);  //for receiving
		
  
        InetAddress ip = InetAddress.getByName(destAddress); 
        byte buf[] = null;
        byte[] receive = new byte[65535];
        DatagramPacket DpReceive = null;

        while (true) 
        {
            String message = "Client on [" + myAddress + "] is listening on [" + myPort + "]";

            buf = message.getBytes();
            
            DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, destPort); 

            ds.send(DpSend);

            System.out.println("Sent: " + message);
            
            DpReceive = new DatagramPacket(receive, receive.length); 

            dsr.receive(DpReceive);
            String serverMessage = new String(DpReceive.getData(), 0, DpReceive.getLength());

            System.out.println("Server: " + serverMessage);
            receive = new byte[65535];
            break;
        }
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	} 

}
