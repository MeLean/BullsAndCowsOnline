import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;
import java.io.*;
import java.net.*;

public class BullsAndCowsOnline {
	static String serverAddress = "cowsbullsonline.cnapsys.com";
	static String serverAddress2 = "cowsbullsonline2.cnapsys.com";// "89.253.133.54 ";
	static int serverPort = 9790;
	static Socket clientSocket;
	static InputStream remoteStream;
	static BufferedReader inFromServer;
	static DataOutputStream outToServer;
	static Scanner input;
	static String userName;
	static String[] connectedClients;
	static String oponent;
	static boolean isFirst = false;
	static boolean wasFirst = false;
	static boolean isDice = false;
	static boolean hasDice = false;
	static boolean hasEnded = false;
	static int guesses = 0;

	public static void main(String[] args) {
		System.out
				.println("Bulls and Cows (also known as Cows and Bulls or Pigs and Bulls or Bulls and Cleots)\n"
						+ "is an old code-breaking mind or paper and pencil game for two or more players, \n"
						+ "predating the similar commercially marketed board game Mastermind.On a sheet of paper,\n"
						+ "the players each write a 4-digit secret number. The digits must be all different. \n"
						+ "Then, in turn, the players try to guess their opponent's number"
						+ " who gives the \n"
						+ "number of matches. If the matching digits are on their right positions, "
						+ "they are \n"
						+ "\"bulls\", if on different positions, they are \"cows\". Example:\nSecret number: 4271 \n"
						+ "Opponent's try: 1234.\n"
						+ "Answer: 1 bull and 2 cows. (The bull is \"2\", the cows are \"4\" and \"1\".)\n"
						+ "The first one to reveal the other's secret number wins the game.\n"
						+ "As the \"first one to try\" has a logical advantage, on every game the \"first\" player changes.\n"
						+ "In some places, the winner of the previous game will play \"second\". Sometimes, if the \"first\" player\n"
						+ "finds the number, the \"second\" has one more move to make and if he also succeeds, the result is even.\n");
		System.out.println("Press any key to start the game...");
		input = new Scanner(System.in);
		input.nextLine();
		System.out
				.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

		while (true) {
			input = new Scanner(System.in);
		
			try {
				inititializeGame();
			} catch (Exception e) {
				input.reset();
			}

			disposeConnection();
		}
	}

	static void inititializeGame() throws Exception {

		int userDecigion = 0;
	
		System.out.println("Make your choice:");
		System.out.println("1: SinglePlayer");
		System.out.println("2: MultiPlayer");
		System.out.println("3: Exit");		
		userDecigion = input.nextInt();
		
		if (userDecigion == 1) {
			SinglePlayer.singlePlayer();
			return;
		} else if (userDecigion != 2) {			
			System.exit(0);
		} else {
			System.out.println("Checking connection. Please wait...!");
			
			boolean isConnected = checkConnection();
			
			if (!isConnected) {
				System.out
						.println("Make your choice (You seem to be offline. Choose 2 to check the connection again):");
				System.out.println("1: SinglePlayer");
				System.out.println("2: Try again");
				System.out.println("3: Exit");
				userDecigion = input.nextInt();
				if (userDecigion == 1) {
					SinglePlayer.singlePlayer();
					return;
				} else if (userDecigion != 2) {
					System.exit(0);
				}
				
			}	
		}
				while (true) {
					if (remoteStream.available() != 0) {
						readClient();
					} else {
						Thread.sleep(1);
					}
				}

	}

	static boolean hasDupes(int num) {
		boolean[] digs = new boolean[10];
		while (num > 0) {
			if (digs[num % 10])
				return true;
			digs[num % 10] = true;
			num /= 10;
		}
		return false;
	}

	static String readClient() throws Exception {
		String result = "";
		try {
			result = inFromServer.readLine();
			// check if the message is a system message
			if (result.equals(Messages.REQUEST_USER_NAME)) {
				System.out.println("Choose a unique name:");
				userName = input.nextLine();
				while (userName.equals("")) {
					userName = input.nextLine();
				}
				sendClient(userName);
				return readClient();
			} else if (result.equals(Messages.AWAITIN_GCOMMAND)) {
				sendClient(Messages.REPORT_CLIENTS);
				System.out
						.println("Choose a oponent from the list, or wait for invitation.");
				printSeparator();
				return readClient();
			} else if (result.equals(Messages.REPORT_CLIENTS)) {
				int connectedPlayersCount = Integer.parseInt(readClient());
				connectedClients = new String[connectedPlayersCount];
				if (connectedPlayersCount == 0) {
					System.out
							.println("There are no players online at this moment.");
				} else {
					for (int i = 1; i < connectedPlayersCount + 1; i++) {
						String playerName = readClient();
						System.out.println(i + ": " + playerName);
						connectedClients[i - 1] = playerName;
					}
				}
				printSeparator();
				System.out.println((connectedPlayersCount + 1)
						+ ": Check online players again.");
				System.out.println((connectedPlayersCount + 2)
						+ ": Change your name (" + userName + ")");
				System.out.println((connectedPlayersCount + 3)
						+ ": Quit multiplayer.");
				while (System.in.available() == 0
						&& remoteStream.available() == 0) {
					Thread.sleep(1000);
				}
				if (remoteStream.available() != 0) {
					return readClient();
				} else {
					int decigion = input.nextInt();
					while (decigion < 1 || decigion > connectedPlayersCount + 3) {
						System.out
								.println("Please choose an option from the list:");
						printSeparator();
						decigion = input.nextInt();
						// fix here as it will prevent receiving invitations
					}
					if (decigion == connectedPlayersCount + 1) {
						sendClient(Messages.REPORT_CLIENTS);
						System.out
								.println("Choose a oponent from the list, or wait for invitation.");
						printSeparator();
						return readClient();
					} else if (decigion == connectedPlayersCount + 2) {
						sendClient(Messages.REQUEST_USER_NAME);
					} else if (decigion == connectedPlayersCount + 3) {
						sendClient(Messages.QUIT_MULTIPLAYER);
						throw new Exception();
					} else {
						sendClient(Messages.REQUEST_TO_PLAY);

						sendClient(connectedClients[decigion - 1]);
						oponent = connectedClients[decigion - 1];
						System.out.println("Request to "
								+ connectedClients[decigion - 1]
								+ " sent. Awaiting responce.");
						printSeparator();
						System.out.println("Please wait...");
						isFirst = true;
						wasFirst = true;
						return readClient();
					}
				}
			} else if (result.equals(Messages.OPONENT_NOT_AVAILABLE)) {
				sendClient(Messages.REPORT_CLIENTS);
				System.out
						.println("It looks like the player is already playing with someone else, or has been disconnected!");
				System.out.println("Choose another player from the list:");
				return readClient();
			} else if (result.equals(Messages.ACCEPT_TEAM_PLAY)) {
				System.out
						.println("Your invitation to play has been accepted.");
				return readClient();
			} else if (result.equals(Messages.DECLINE_TEAM_PLAY)) {
				sendClient(Messages.REPORT_CLIENTS);
				System.out.println("Your invitation has been declined.");
				System.out
						.println("Choose another oponent from the list, or wait for invitation.");
				printSeparator();
				return readClient();
			} else if (result.equals(Messages.START_TEAM_PLAY)) {
				System.out
						.println("Invitation accepted. The game is starting. Type 'quit' to leave.");
				multiplayer(false);
			} else if (result.equals(Messages.UNKNOWN_COMMAND)) {
				System.out
						.println("Something went wrong and the game will restart.");
				printSeparator();
				sendClient(Messages.QUIT_MULTIPLAYER);
				throw new Exception();
			} else if (result.equals(Messages.RECCONNECT_REQUEST)) {
				System.out
						.println("Something went wrong and the game will restart.");
				printSeparator();
				throw new Exception();
			} else if (result.equals(Messages.REQUEST_TO_PLAY)) {
				oponent = readClient();
				System.out
						.println(oponent + " sent you an invitation to play.");
				printSeparator();
				System.out.println("1: Accept");
				System.out.println("2: Decilne");
				int decigion = input.nextInt();
				if (decigion == 1) {
					sendClient(Messages.ACCEPT_TEAM_PLAY);
					sendClient(oponent);
					isFirst = false;
					wasFirst = false;
				} else {
					sendClient(Messages.DECLINE_TEAM_PLAY);

					sendClient(oponent);

					// do not confuse yourself, oponent must be returned before
					// requesting clients again :)
					sendClient(Messages.REPORT_CLIENTS);
					System.out
							.println("Choose oponent from the list, or wait for invitation.");
					printSeparator();
				}
				return readClient();
			}
		} catch (Exception ex) {
			input.reset();
			throw new Exception();
		}
		return result;
	}

	static void sendClient(String msg) throws Exception {
		outToServer.writeBytes(msg + '\n');
	}

	static boolean checkConnection() {
		try {
			initializeConnection(false);
			return true;
		} catch (Exception ex) {
		}
		try {
			initializeConnection(true);
			return true;
		} catch (Exception ex) {
		}
		return false;
	}

	static void initializeConnection(boolean second) throws Exception {
		if (!second) {
			clientSocket = new Socket(serverAddress, serverPort);
		} else {
			clientSocket = new Socket(serverAddress2, serverPort);
		}
		outToServer = new DataOutputStream(clientSocket.getOutputStream());
		remoteStream = clientSocket.getInputStream();
		inFromServer = new BufferedReader(new InputStreamReader(remoteStream));
	}

	static void disposeConnection() {
		try {
			clientSocket.close();
		} catch (Exception ex) {
		}
	}

	static void multiplayer(boolean wasDice) throws Exception {
		hasDice = false;
		isDice = false;
		hasEnded = false;
		String targetStr = "";
		if (wasDice) {
			System.out
					.print("Enter a 4-digit number with no duplicate digits: ");
		} else {
			guesses = 0;
		}
		while (targetStr.equals("")) {
			try {
				String temp = input.nextLine();
				int myNumnber = Integer.parseInt(temp);
				if (hasDupes(myNumnber) || myNumnber < 1000 || myNumnber > 9999) {
					throw new Exception();
				}
				targetStr = temp;
				break;
			} catch (Exception ex) {
			}
			System.out
					.print("Enter a 4-digit number with no duplicate digits: ");
		}

		System.out.println("Please wait... " + oponent
				+ " is picking a number.");
		sendClient("I am ready :) . Guess my number");

		boolean guessed = false;
		boolean quited = false;

		do {
			int bulls = 0;
			int cows = 0;
			int guess;
			String oponentMessage = "";
			try {
				while (System.in.available() == 0
						&& remoteStream.available() == 0) {
					Thread.sleep(1);
				}
				if (remoteStream.available() != 0) {
					oponentMessage = readClient();
					if (oponentMessage.equals(Messages.GAME_OVER)
							|| oponentMessage
									.equals(Messages.OPONENT_DISCONNECTED)) {
						System.out.println("Game ended.");
						throw new Exception("2");
					}
					guess = Integer.parseInt(oponentMessage);
					if (hasDupes(guess) || guess < 1000 || guess > 9999) {
						sendClient("Please enter a valid number.");
						continue;
					}

					if (isFirst) {
						sendClient("Please wait for your turn.");
						continue;
					} else {
						isFirst = true;
					}
				} else {
					String userDecigion = input.nextLine();
					if (userDecigion.equalsIgnoreCase("quit")) {
						throw new Exception("1");
					}
					sendClient(userDecigion);
					try {
						Integer.parseInt(userDecigion);
						isFirst = false;
					} catch (Exception ex) {

					}
					continue;
				}

			} catch (Exception e) {
				if (e.getMessage().equals("1")) {
					sendClient("I am giving up! Moooooooo...");
					quited = true;
					break;
				}
				if (e.getMessage().equals("2")) {
					System.out.println("You win after " + guesses
							+ " guesses :)");
					quited = true;
					hasEnded = true;
					break;
				} else if (oponentMessage.equals(Messages.DICE)) {
					if (!isDice) {
						hasDice = true;
						continue;
					} else {
						System.out.println("You found " + oponent
								+ "'s number too.");
						break;
					}
				} else {
					if (!oponentMessage.equals("")) {
						System.out
								.println(oponent + " says: " + oponentMessage);
					}
					continue;
				}
			}
			guesses++;
			String guessStr = guess + "";
			for (int i = 0; i < 4; i++) {
				if (guessStr.charAt(i) == targetStr.charAt(i)) {
					bulls++;
				} else if (targetStr.contains(guessStr.charAt(i) + "")) {
					cows++;
				}
			}
			if (bulls == 4) {
				guessed = true;
				if (!wasFirst) {
					sendClient("You got my number, but I have one more try.");
					System.out.println(oponent
							+ " found your number, you have one last try.");
					sendClient(Messages.DICE);
					hasDice = true;
					isDice = true;
				} else {
					if (hasDice) {
						System.out.println(oponent + " found your number too.");
						sendClient(Messages.DICE);
						isDice = true;
						break;
					}
				}
			} else if (!hasDice) {
				sendClient(cows + " Cows and " + bulls + " Bulls.");
				System.out.println(oponent + " tryed " + oponentMessage + " ("
						+ cows + " Cows and " + bulls + " Bulls)");
			} else {
				sendClient(cows + " Cows and " + bulls + " Bulls.");
				System.out.println(oponent + " tryed " + oponentMessage + " ("
						+ cows + " Cows and " + bulls + " Bulls)");
				break;
			}
		} while (!guessed || hasDice);
		if (guessed && !isDice) {
			System.out.println(oponent + " won after " + guesses + " guesses!");
		} else if (isDice && !quited) {
			System.out.println("Playing dice.");
			if (remoteStream.available() != 0) {
				System.out.println(readClient());
			}
			multiplayer(true);
		}
		if (!wasDice) {
			printSeparator();
			Thread.sleep(2000);
			if (!hasEnded) {
				sendClient(Messages.GAME_OVER);
			}
		} else {
			hasDice = false;
			isDice = false;
		}
	}

	static void printSeparator() {
		System.out.println("---------------------");
	}
}