import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Server {

	private static final String address = "127.0.0.1";
	private static boolean inGame = false;
	private static int answer;
	private static List<String> scoreList = new ArrayList<String>();  //store number of guess of each client in each game
	private static List<SocketThread> threadList = new ArrayList<SocketThread>();  //list of all clients in thread
	private static int playerNumber = 0;  //record number of current players


	public static void main(String[] args) throws IOException {
		scoketServer();
	}

	public static void scoketServer() throws IOException {
		ServerSocket server = new ServerSocket();
		server.bind(new InetSocketAddress(address, 8888));  //start connection
		//Socket s = ss.accept();
		//ServerSocket server = new ServerSocket(4444);
		System.out.println("The server is running...");
		while (true) {
			if(threadList.size() < 6) {  //maximum 6 connections
				Socket socket = server.accept();
				System.out.println("Client on address " + socket.getInetAddress().getHostAddress() + " is connected");
				SocketThread st = new SocketThread(socket); // create a new thread for each client
				st.start();
				threadList.add(st);
			}
		}
	}

	private static class SocketThread extends Thread {

		private BufferedReader bufferedReader;
		private PrintWriter printWriter;
		private Socket socket;
		private int count = 0;
		private User user;
		private String name;

		public SocketThread(Socket socket) throws IOException {
			this.socket = socket;
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));  //bind bufferedreader and printwriter
			this.printWriter = new PrintWriter(socket.getOutputStream(), true);
		}

		private void send(String msg) throws IOException {  //send message from server to the specific client
			printWriter.println(msg);
			System.out.println("Send to [" + name + "] : " + msg);
		}

		private String[] getMessage() throws IOException {  //handle message from clients with the format "command-content"
			String[] result = new String[2];
			String msg = bufferedReader.readLine();
			if (msg != null && msg.split("-").length == 2) {
				result = msg.split("-");
			}
			return result;
		}

		private boolean guessing(String number) throws IOException {  //print hint or congratulates to the client
			boolean result = false;
			int guess = Integer.parseInt(number);
			count++;

			if (guess > answer) {
				send(guess + " is greater than the answer! ");
			} else if (guess < answer) {
				send(guess + " is smaller than the answer! ");
			} else if (guess == answer) {
				send("Congratulations! You got the correct answer! ");
				result = true;
			}
			return result;
		}

		private void broadcast(String msg) {  //broadcast message from server to all the client
			for (SocketThread st : threadList) {
				st.printWriter.println("Server: " + msg);
			}
		}
		
		private void checkFinished() {  //check whether all the user has finished the game
			if (scoreList.size() == playerNumber) {
				System.out.println("Game end");
				broadcast("Game end! Answer is " + answer + ", and the number of guesses for each player is " + scoreList.toString());
				inGame = false;
			}
		}

		@Override
		public void run() {  //start thread
			String[] msg;
			
			while (true) {
				try {
					msg = getMessage();  //receive message from clients
					String command = msg[0];
					String content = msg[1];

					if (command.equals("AddUser")) {  //if client send the command to add new user
						name = content;
						user = new User(name);

						System.out.println("User [" + name + "] added");
						broadcast("User [" + name + "] added");
						if(playerNumber != 0) {
							send("Please wait int the queue");
						}
					} else if (command.equals("StartGame")) {  //if a client select to start the game
						
						if (inGame) {  //if a user send "start game" command when the game has started 
							send("Game has started, please wait");
						} else if (threadList.size() >= 1) {  //if there is more than one client
							playerNumber = 0;
							scoreList.clear();
							answer = ThreadLocalRandom.current().nextInt(0, 13);  //generate a random number as the answer in range [0-12]
							System.out.println("Game start. The answer is " + answer);
							broadcast("[" + name + "] starts the game");
						    inGame = true;
							int starterIndex = 0;
							String currentPlayer = "";
							for(SocketThread st : threadList) {  //reset the number of guess for all clients in game
								st.count = 0;
							}

							for(SocketThread st : threadList) {
								if (playerNumber < 3) {  //select the first 3 client in the queue if available
									// no more than 3 players in game at once
									currentPlayer = currentPlayer + " " + st.user.getUsername();
									playerNumber++;
									if (st.user.getUsername().equals(name)) {
										starterIndex = threadList.indexOf(st);  //find who start the game
									}
								}
							}
							broadcast(playerNumber + " user(s) in game:" + currentPlayer);  //server announces players' name
							for (int i=0;i<playerNumber;i++) {
								threadList.get(i).printWriter.println("Game start");  //server announces game start
								if(i!=starterIndex) {  //print the menu for clients who did not selecting to start game but in game
									threadList.get(i).printWriter.println("Enter a number or e (Exit Game)");
								}
							}
							if(threadList.size() > 3) {  //for other clients after the first 3
								for(int i=playerNumber;i<threadList.size();i++) {
									threadList.get(i).printWriter.println("Please wait int the queue");  //ask to wait
								}
							}
						}
					} else if (command.equals("Guess")) {  //if clients send their guess number
						System.out.println(name + " guesses " + content);
						boolean isWin = guessing(content);
						if (isWin) {  //if guess right
							scoreList.add(name + " : " + count);  //record the number of guess
						} else if ((count == 4) && !isWin) {  //if guess wrong or it's the 4th guess
							System.out.println(name + " has guessed 4 times, must exit the game. ");  //ask to exit the game
							send("You has run out of chance. Exit Game. ");
						}	
					} else if (command.equals("Exit")) {  //if clients ask to exit
						if(!scoreList.contains(name + " : " + count)) {
							scoreList.add(name + " : " + count + " false");  //recored the number of guess if has not recorded yet
						}
						checkFinished();
						send("exit");
					} else if (command.equals("PlayAgain")) {  //if clients want to play again
						for(SocketThread st : threadList) {  //remove from current position and add to the queue as the last one
							if(st.user.getUsername().equals(name)) {
								threadList.remove(st);
								threadList.add(st);
								break;
							}
						}
						System.out.println("[" + name + "] wants to play again. Added into queue");
						send("Added you into queue");
					} else if (command.equals("Quit")) {  //if clients want to quit
						broadcast(name + " quit");
						send("disconnected");
						for(SocketThread st : threadList) {  //remove from list
							if(st.user.getUsername().equals(name)) {
								threadList.remove(st);
								break;
							}
						}
						System.out.println(threadList.size() + " user(s) are conected. ");
						break;
					}
				} catch (SocketException e) {
					e.printStackTrace();
					System.out.println(e.getMessage());
					break;
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.getMessage();
				}
			}
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}

		}
	}
}
