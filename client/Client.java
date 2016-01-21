package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import shared.InvalidCommandException;
import shared.InvalidStoneException;
import shared.Protocol;
import shared.Stone;

public class Client extends Thread {

	private static void printStatic(String message) {
		System.out.println(message);
	}

	/** Start een Client-applicatie op. */
	public static void main(String[] args) {
        Client client = new Client();
        client.start();
        client.startGame();
	}

	// hello_from_the_other_side

	private String clientName;
	private Socket sock;
	private BufferedReader in;
	private BufferedWriter out;
	private String[] options;
	private ClientGame game;
	private View view;
	private int aantal;
	private boolean registerSuccesfull = false;
	private boolean computerPlayerBool;
	private Strategy strategy;
	private List<String> players;
	private Player you;

	public Client() {
		this.view = new View(this);
		players = new LinkedList<String>();
		init();
		logIn();
	}

	public void init() {
		Socket socket = null;
		while (socket == null) {
			InetAddress hostname = null;
			while (hostname == null) {
				try {
					hostname = view.getHostName();

				} catch (UnknownHostException e) {
					view.print("Invalid hostname");
				}
			}
			int port = view.getPort();
			try {
				socket = new Socket(hostname, port);
			} catch (IOException e) {
				view.print("Could not connect to server");
			}
		}
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void logIn() {
		while (!registerSuccesfull) {
			clientName = view.getClientName();
			sendMessage(Protocol.REGISTER + Protocol.SPLIT + getClientName());
			String input = null;
			if ((input = readString()) != null) {
				System.out.println(input);
				String[] inputArray = input.split(Protocol.SPLIT);
				if (inputArray[0].equals(Protocol.ACKNOWLEDGE)) {
					registerSuccesfull = true;
				} else if (inputArray[0].equals(Protocol.ERROR)) {
					if (inputArray.length == 1) {
						System.out.println("wrong error code");
					} else if (!inputArray[1].equals(Protocol.errorcode.INVALIDNAME)) {
						System.out.println(input);
					}
				}
			}
		}
	}

	public void startGame() {
		this.computerPlayerBool = view.askHumanOrComputerPlayer();
		if (computerPlayerBool == true) {
			this.strategy = view.getStrategyFromInput();
		}
		this.aantal = view.startGame();
		join(aantal);
	}

	public void setYou(Player player) {
		you = player;
	}

	public boolean getComputerPlayerBool() {
		return computerPlayerBool;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public void run() {
		String input = null;
		while ((input = readString()) != null) {
			String[] inputArray = input.split(Protocol.SPLIT);
			if (inputArray[0].equals(Protocol.ENDGAME)) {
				// implement
			} else if (inputArray[0].equals(Protocol.ERROR)) {
				if (inputArray.length == 1) {
					System.out.println("error");
					view.print("Geen foutcode meegegeven foei foei foei");
				} else if (inputArray[1].equals("0")) {
					view.print("Fout commando: 0");
				} else if (inputArray[1].equals("1")) {
					view.print("Foute beurt: 1");
				} else if (inputArray[1].equals("2")) {
					view.print("Niet unieke naam of onjuiste naam: 2");
				} else if (inputArray[1].equals("3")) {
					view.print("Speler disconnected: 3");
				} else if (inputArray[1].equals("4")) {
					view.print("Speler heeft functie niet: 4");
				}
			} else if (inputArray[0].equals(Protocol.PLACED)) {
				List<Stone> stones = new ArrayList<Stone>();
				try {
					stones = Protocol.StringToPlacedStonelist(inputArray);
				} catch (InvalidCommandException e) {
					serverBroken();
				}
				int[] x = Protocol.convertPlacedX(inputArray);
				int[] y = Protocol.convertPlacedY(inputArray);
				for (int i = 0; i < stones.size(); i++) {
					game.getBoard().makeMove(x[i], y[i], stones.get(i));
				}
			} else if (inputArray[0].equals(Protocol.NEWSTONES)) {
				List<Stone> stones = null;
				try {
					stones = Protocol.StringToStonelist(inputArray);
				} catch (InvalidStoneException e) {
					serverBroken();
				}
				you.takeStones(stones);
			} else if (inputArray[0].equals(Protocol.TRADED)) {
				view.print("Speler " + inputArray[1] + " " + inputArray[0] + " " + inputArray[2] + " stones.");
			} else if (inputArray[0].equals(Protocol.TURN)) {
				Player[] players = game.getPlayers();
				for (int i = 0; i < game.getPlayers().length; i++) {
					if (players[i].getName().equals(inputArray[1])) {
						game.setCurrentPlayer(players[i]);
						if (inputArray[1].equals(you.getName())) {
							you.makeMove();
						}
						break;
					}
				}
			} else if (inputArray[0].equals(Protocol.PLAYERS)) {
				if (inputArray.length >= 2) {
					players = new ArrayList<String>();
					for (int i=1; i<inputArray.length;i++) {
						players.add(inputArray[i]);
					}
				}
			} else if (inputArray[0].equals(Protocol.JOINLOBBY)) {
				if (inputArray.length >= 2) {
					String message = "Player " + inputArray[1] + " joined the room";
					view.print(message);
				}
			} else if (inputArray[0].equals(Protocol.START)) {
				if (inputArray.length >= 3 ) {
					if (input.contains(clientName)) {
						initGame(inputArray);
					}
				}
				else {
					serverBroken();
				}
			}
		}
		shutdown();
	}
	public void initGame(String[] inputArray) {
		String[] players = new String[inputArray.length - 1];
		for (int i = 1; i < inputArray.length; i++) {
			players[i - 1] = inputArray[i];
		}
		this.game = new ClientGame(players, this);
	}

	public ClientGame getGame() {
		return game;
	}

	public View getView() {
		return view;
	}

	public void sendMessage(String msg) {
		try {
			out.write(msg);
			out.newLine();
			out.flush();
		} catch (IOException e) {
			shutdown();
		}
	}

	/** close the socket connection. */
	public void shutdown() {
		view.print("Closing socket connection...");
		try {
			in.close();
			out.close();
			sock.close();
			// implement dingen enzo
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** returns the client name */
	public String getClientName() {
		return clientName;
	}

	public String readString() {
		String line = null;
		try {
			line = in.readLine();
		} catch (IOException e) {
		}
		return line;
	}

	public void place(List<Stone> stones) {
		String msg = Protocol.PLACE;
		for (Stone s : stones) {
			if (you.getStones().contains(s)) {
				you.removeStone(s);
			}
			msg = msg + Protocol.SPLIT + s.toUsableString() + Protocol.SPLIT + s.getPosition().toUsableString();
		}
		sendMessage(msg);
	}

	public void trade(List<Stone> stones) {
		String msg = Protocol.TRADE;
		for (Stone s : stones) {
			if (you.getStones().contains(s)) {
				you.removeStone(s);
			}
			msg = msg + Protocol.SPLIT + s.toUsableString();
		}
		sendMessage(msg);
	}

	public void register() {
		sendMessage(Protocol.REGISTER + Protocol.SPLIT + options);
	}

	public void join(int amount) {
		sendMessage(Protocol.JOINAANTAL + Protocol.SPLIT + amount);
		view.print("waiting for other players:");
	}

	public void chat(String msg) {
		sendMessage(Protocol.CHAT + Protocol.SPLIT + msg);
	}

	public void chatPM(String msg, Player player) {
		sendMessage(Protocol.CHATPM + Protocol.SPLIT + player.getName() + Protocol.SPLIT + msg);
	}

	public void askPlayers() {
		sendMessage(Protocol.WHICHPLAYERS);
	}
	public void serverBroken() {
		System.out.println("Server is broken, OK DOEI!");
		shutdown();
	}
}
