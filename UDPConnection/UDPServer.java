import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {

	private final static int myPort = 1234;
	private final static String myAddress = "server1";// "127.0.0.1";
	private static String[] clientAddress = new String[2];
	private static int[] clientPort = new int[2];
	private static int i = 0;  //total count
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		byte[] receive = new byte[65535];
		DatagramSocket ds = new DatagramSocket(myPort); //for receiving
		  DatagramPacket DpReceive = null;
		  byte buf[] = null;
		  System.out.println("The server is running...");
	        
		  while (true) {

		  DpReceive = new DatagramPacket(receive, receive.length);
		  ds.receive(DpReceive);

		  Handler handler = new Handler(DpReceive);
		  handler.run();

		  System.out.println(handler.message);
		  i++;
		  if(i == 2) {
			  String message = "Server is about to shut down";
			  System.out.println("Server counts down 5s, ready to send shut down message");
			  buf = message.getBytes();
			  for (int j = 0; j<2; j++) {
			  InetAddress ip = InetAddress.getByName(clientAddress[j]);
			  DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, clientPort[j]);
			  Thread.sleep(5000); 
			  ds.send(DpSend); 
			  }
			  break;
		  }
		  }
		  ds.close();
		 
		
		
        
	}
	
	//ClientHandler
	    private static class Handler implements Runnable {
        private DatagramPacket packet;
        public String message;

        public Handler(DatagramPacket dpReceive) {
            this.packet = dpReceive;
        }

        public void run() {
            try {
                //String str = new String(dp.getData(), 0, dp.getLength());
                message = new String(packet.getData(), 0, packet.getLength());
                extractInfo(message);
            } catch (Exception e) {
                System.out.println(e);
			}
        }
        
        public void extractInfo(String receive) {
        	//receive = "Client on address [address] is listening on [port]
        	String[] parts = receive.split("\\["); //[Client on address , address] is listening on , port]]
        	
        	String[] addressPart = parts[1].split("\\]");
        	clientAddress[i] = addressPart[0];
        	
        	String[] portPart = parts[2].split("\\]");
        	clientPort[i] = Integer.parseInt(portPart[0]);
        }
    }
	

}
