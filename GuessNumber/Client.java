import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	private final static String address = "127.0.0.1";
	private final static int port = 8888;
	private static String name;
	static Scanner scanner;
	private static int gameState = 0; // 0-wait, 1-exit, 2-in game

	public static void main(String[] args) {

		scanner = new Scanner(System.in);  //ask to input a name
		System.out.println("Enter your name please");
		name = scanner.nextLine();
		socketClient();
	}

	public static void socketClient() {
		try {
			//Socket socket = new Socket("127.0.0.1", 8888);
			Socket socket = new Socket();  //start connection
			socket.connect(new InetSocketAddress(address, port), 300000); //time out in 5 minutes (300,000 ms)
			if (socket.isConnected()) {  //when server has 6 connections already, can show menu but no further response
				new Writer(socket, name).start();
				new Reader(socket).start();
			} else {
				System.out.println("Server is not running");
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	private static class Writer extends Thread {
		private Socket socket;
		private PrintWriter printWriter;
		private Scanner scanner = new Scanner(System.in);
		private String name;

		public Writer(Socket socket, String name) throws IOException {
			this.socket = socket;
			this.printWriter = new PrintWriter(socket.getOutputStream(), true);  //bind printwriter
			this.name = name;
		}

		private void mainMenu() {  //main menu
			System.out.println("------ MENU -------");
			System.out.println("Select by entering a letter");
			System.out.println("s (Start Game)");
			System.out.println("q (Exit Connection)");

		}

		private void endMenu() {  //after-play menu
			System.out.println("What's Next?");
			System.out.println("p (Play Again)");
			System.out.println("q (Quit Connection)");
		}

		private void guessMenu() {  //guessing menu
			System.out.println("Enter a number or e (Exit Game)");
		}

		private void waitServer(long millis) {  //wait for server's reply
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.getMessage();
			}
		}

		private void addUser() {  //command to add user
			printWriter.println("AddUser-" + name);
			// printWriter.flush();
		}

		private void startGame() {  //command to start game
			printWriter.println("StartGame-" + name);
		}

		private void quit(Socket socket) {  //command to quit
			printWriter.println("Quit-" + name);
		}

		private void exitGame() {  //command to exit game
			printWriter.println("Exit-" + name);
		}

		private void playAgain() {  //command to play again
			printWriter.println("PlayAgain-" + name);
		}

		private void guess(int n) {  //command to guess
			printWriter.println("Guess-" + n);
		}

		private int validNumberString(String msg) {  //check whether the guess number is valid
			int number;
			try {
				number = Integer.parseInt(msg);
				if (number > 12 || number < 0) {
					number = -1;
				}
			} catch (NumberFormatException e) {
				if(msg.equals("e")) {
					number = -2;
				}else {
					number = -1;
				}
				
			}
			return number;
		}

		public void run() {  //thread starts
			// scanner.useDelimiter("\r\n");

			addUser();  //ask to add user
			String str;

			while (true) {
				waitServer(200);
				if (gameState == 0) {  //if user is waiting
					mainMenu();
				} else if (gameState == 1) {  //if game is started
					endMenu();
				}

				str = scanner.nextLine();  //get user input

				if (str.equals("e") && gameState == 2) {  //if the client select to exit and game state is in game
					exitGame();
					endMenu();
				} else if (str.equals("s") && gameState == 0) {  //if the client select to start game and game state is wait
					startGame();
					waitServer(200);
					guessMenu();
				} else if (validNumberString(str) != -1 && gameState == 2) {  //if the client is guessing and game state is in game
					while (true) {
						int count = 0;
						int number = validNumberString(str);
						if(number == -2) {
							exitGame();
							break;
						} else if(number == -1) {  //if invalid input
							System.out.println("Invalid input");
							continue;
						} else {  //if valid input
							count++;
							guess(number);
							waitServer(1000);
							if (count == 4 || gameState == 1) {  //if the client has guessed 4th or guessed correct
								exitGame();
								break;
							}
						}
						
						str = scanner.nextLine();  //get user input
					}
				} else if (str.equals("q") && gameState != 2) {  //if the client select to quit and is not in game
					quit(socket);
					waitServer(1000);
					break;
				} else if (str.equals("p") && gameState == 1) {  //if the client select to play again and is wait
					playAgain();
				} else {  //if the client's input is not follow the instruction of menu
					System.out.println("Invalid input");
					continue;
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

	private static class Reader extends Thread {
		private Socket socket;
		private BufferedReader bufferedReader;
		private String msg = null;

		public Reader(Socket socket) throws IOException {
			this.socket = socket;
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));  //bind bufferedreader
		}

		@Override
		public void run() {
			while (true) {
				try {
					msg = bufferedReader.readLine();  //read message from server
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.getMessage();
					break;
				}
				if (msg != null) {
					System.out.println(msg);  //show message to client
					if (msg.equals("disconnected")) {  //if server send to disconnect
						break;
					} else if (msg.equals("You has run out of chance. Exit Game. ")
							|| msg.equals("Congratulations! You got the correct answer! ") || msg.equals("exit")) {  //if server ask to exit game
						gameState = 1;  //set game state as exit
					} else if (msg.equals("Please wait int the queue")) {  //if server ask the client to wait in the queue
						gameState = 0;  //set game state as wait
					} else if (msg.equals("Game start")) {  //if server announces game start
						gameState = 2;  //set game state as in game
					} else if (msg.equals("Added you into queue")) {  //if server announces the client has been added in the queue
						gameState = 0;  //set game state as wait
					}
				}
			}
		}
	}
}