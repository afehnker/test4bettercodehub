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
import java.util.List;

import server.*;
import server.Stone.Color;
import server.Stone.Shape;
import shared.Protocol;

public class Client extends Thread {

	private static final String USAGE = "usage: Client <name> <address> <port>";

	/** Start een Client-applicatie op. */
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println(USAGE);
			System.exit(0);
		}

		InetAddress host = null;
		int port = 0;

		try {
			host = InetAddress.getByName(args[1]);
		} catch (UnknownHostException e) {
			print("ERROR: no valid hostname!");
			System.exit(0);
		}

		try {
			port = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			print("ERROR: no valid portnummer!");
			System.exit(0);
		}

		try {
			Client client = new Client(args[0], host, port);
			client.start();
		} catch (IOException e) {
			print("ERROR: couldn't construct a client object!");
			System.exit(0);
		}

	}

	private String clientName;
	private Socket sock;
	private BufferedReader in;
	private BufferedWriter out;
	private String[] options;
	private Game game;
	private View view;

	public Client(String name, InetAddress host, int port) throws IOException {
		this.clientName = name;
		this.sock = new Socket(host, port);
		this.in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		this.out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
	}

	public void run() {
		sendMessage(Protocol.REGISTER + Protocol.SPLIT + getClientName());
		while (true) {
			String input = readString();
			String[] inputArray = input.split(Protocol.SPLIT);
			if (inputArray[0].equals(Protocol.ENDGAME)) {
				// implement
			} else if (inputArray.length > 1) {
				if (inputArray[0].equals(Protocol.ERROR)) {
					if (inputArray[1].equals("0")) {
						print("Fout commando: 0");
					} else if (inputArray[1].equals("1")) {
						print("Foute beurt: 1");
					} else if (inputArray[1].equals("2")) {
						print("Niet unieke naam of onjuiste naam: 2");
					} else if (inputArray[1].equals("3")) {
						print("Speler disconnected: 3");
					} else if (inputArray[1].equals("4")) {
						print("Speler heeft functie niet: 4");
					} else if (inputArray.length == 1) {
						print("Geen foutcode meegegeven foei foei foei");
					}
				} else if (inputArray[0].equals(Protocol.PLACED)) {
					List<Stone> stones = Protocol.intsToStonesAndPositions(inputArray);
					// implement
				} else if (inputArray[0].equals(Protocol.NEWSTONES)) {
					List<Stone> stones = Protocol.convertNewStones(inputArray);
					game.currentPlayer().takeStones(stones);
					// implement
				} else if (inputArray[0].equals(Protocol.TRADED)) {

				} else if (inputArray[0].equals(Protocol.TURN)) {

				} else if (inputArray[0].equals(Protocol.ACKNOWLEDGE)) {

				} else if (inputArray[0].equals(Protocol.PLAYERS)) {

				} else if (inputArray[0].equals(Protocol.JOINLOBBY)) {

				} else if (inputArray[0].equals(Protocol.START)) {

				} else if (inputArray[0].equals(Protocol.MSG)) {

				} else if (inputArray[0].equals(Protocol.MSGPM)) {

				} else if (inputArray[0].equals(Protocol.NEWCHALLENGE)) {

				} else if (inputArray[0].equals(Protocol.ACCEPT)) {

				} else if (inputArray[0].equals(Protocol.DECLINE)) {

				}
			} else if (inputArray[0] == null)
				;
		}

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
		print("Closing socket connection...");
		try {
			in.close();
			out.close();
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** returns the client name */
	public String getClientName() {
		return clientName;
	}

	private static void print(String message) {
		System.out.println(message);
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
			msg = msg + Protocol.SPLIT + s.toUsableString() + Protocol.SPLIT + s.getPosition().toUsableString();
		}
		sendMessage(msg);
	}

	public void trade(List<Stone> stones) {
		String msg = Protocol.TRADE;
		for (Stone s : stones) {
			msg = msg + Protocol.SPLIT + s.toUsableString();
		}
		sendMessage(msg);
	}

	public void register() {
		sendMessage(Protocol.REGISTER + Protocol.SPLIT + options);
	}

	public void join(int amount) {
		sendMessage(Protocol.JOINAANTAL + Protocol.SPLIT + amount);
	}

	public void chat(String msg) {
		sendMessage(Protocol.CHAT + Protocol.SPLIT + msg);
	}

	public void chatPM(String msg, Player player) {
		sendMessage(Protocol.CHATPM + Protocol.SPLIT + player.getName() + Protocol.SPLIT + msg);
	}

}
