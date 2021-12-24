/*	
	Name: Joshua Kuiper

	Project Description:
	This is a multithreaded client/server program.

	Many clients can connect to one server, and contents of their chat in a chat room
	is stored with in a chat file which is created if a user joins an empty chat room,
	displays to all users when they join, and deletes if they're the last one to leave.
	Programmer: COSC 439/522, F '21
	Multi-threaded Server program
	File name: TCPServerMT.java
	When you run this program, you must give the service port
	number as a command line argument. For example,
	
	java TCPServerMT -p 20600 -g 1019 -n 1823
*/


import java.net.*;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TCPServerMT {
	private ServerSocket servSock;	
	//ArrayList of handlers to be used in file and output to server / client screens
	private ArrayList<ClientHandler> myHandlers;
	File myFile;
	PrintWriter writer;
	int userCount = 0;
	int privateKey = (int)((Math.random() * (201 - 100)) + 100);
	static String gee = "";
	static String nee = "";
	
	//Creates ability to create handlers and server socket to be used by everyone
	public jku_TCPServerMT(ServerSocket servSock) {
		this.servSock = servSock;
		myHandlers = new ArrayList<ClientHandler>();
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Opening port...\n");
		//Default declaration if portNumber isn't entered
		String portNumber = "20600";
		String g = "1019";
		String n = "1823";
		gee = g;
		nee = n;

		try{
			//Accepts command line arguments to change portNumber
			for(int i = 0; i < args.length; i++) {
				if(args[i] != null) {
					if(args[i].equals("-p")) {
						portNumber = args[i+1];
					}
					else if(args[i].equals("-g")) {
						g = args[i+1];
						gee = g;
					}
					else if(args[i].equals("-n")) {
						n = args[i+1];
						nee = n;
					}
					/*
					I know you said we need to do this but if you uncomment this,
					The only way for it to work is by not giving any arguments.
					It always complains and I couldn't figure it out...
					else{
						System.out.println("Invalid Input!");
						System.exit(0);
					}
					*/
				}
			}
        }catch(Exception e){
			System.out.println("Unable to attach to port!");
            System.exit(1);
        }
		//Creates a new instance with server socket then adds the new handler
		//Removes the need for the void run() method which would do this before
		//It does the same thing as the old run() in less lines and it's cleaner
		new jku_TCPServerMT(new ServerSocket(Integer.parseInt(portNumber))).getConnections();
	}
	//Creates the new handler, starts it, and adds the handler to the ArrayList of handlers
	public void getConnections() throws IOException {
		while(true){
			ClientHandler newHandler = new ClientHandler(servSock.accept(), this);
			newHandler.start();
			addNewClient(newHandler);
		}
	}
	//This is the method that sends the message out to all clients, except itself
	public synchronized void threadedOut(ClientHandler sender, String message){
		//Gets the generated key
		long genKey = power(Long.parseLong(gee), privateKey, Long.parseLong(nee));
		//Secret key for Server
		long secretKey = power(genKey, privateKey, Long.parseLong(nee)); 
		for (ClientHandler handlerList : myHandlers) {
			if (handlerList != sender) {
				message = encrypt(message, (int)secretKey);
				handlerList.out.println(message);
			}
		}
	}
	//Adds a new client handler to ArrayList
	public synchronized void addNewClient(ClientHandler newHandler){
		myHandlers.add(newHandler);
	}
	//Removes handler from the ArrayList
	public synchronized void removeOldClient(ClientHandler newHandler){
		myHandlers.remove(newHandler);
	}
	//Synchronized write to file.
	//Sure it's not efficient, but it works!
	public synchronized void writeToFile(String message){
		//The extra println is because it was bugging when I was reading it. It works this way idk.
		writer.println(message + "\n");
		writer.flush();
	}
	public synchronized void readFromFile(PrintWriter out){
		try{
			BufferedReader fileReader = new BufferedReader(new FileReader(myFile));
			String currLine;
			while ((currLine = fileReader.readLine()) != null){
				out.println(currLine);
			}
		}catch(Exception e){}
	}
	//Creates new file when needed
	public void createFile(){
		if(userCount == 0){
			myFile = new File("jku_chat.txt");
			try{
				writer = new PrintWriter(myFile);
			}catch(Exception e){};
		}
	}
	//Deletes file when needed
	public void deleteFile(){
		myFile.delete();
	}
	
	//Client Handler Class
	public class ClientHandler extends Thread {
		//Socket and Server object to send output to all clients
		private Socket client;					
		private jku_TCPServerMT server;	
		//Input & Output to console
		private BufferedReader in;
		private PrintWriter out;
		//Makes sure it's only using THIS handler
		public ClientHandler(Socket client, jku_TCPServerMT server) {
			this.client = client;
			this.server = server;
		}

		public void run() {
			int numMessages = -1;
			server.createFile();
			
			userCount = userCount + 1;
			
			try {
				//Set up input and output streams for socket
				in = new BufferedReader(new InputStreamReader(client.getInputStream())); 
				out = new PrintWriter(client.getOutputStream(),true); 
				
				//Gets username based on first line of input.
				String username = "";
				if(numMessages <= -1){
					username = in.readLine();
					numMessages = 0;
				}
				long genKey = power(Long.parseLong(gee), privateKey, Long.parseLong(nee));
				long secretKey = power(genKey, privateKey, Long.parseLong(nee)); 
				//Prints the username + host you're at
				String host = InetAddress.getLocalHost().getHostName();
				System.out.println(username + " has established a connection to " + host);
				server.writeToFile(gee + " " + nee + " " + username + " has established a connection to " + host);
				System.out.println("Handshake complete. G: " + gee + " N: " + nee + " Session Key: " + secretKey + " Byte Pad: " 
					+ convertAsciiToBinary(convertToAscii(String.valueOf(privateKey))).substring(8, 16));
				//Reads from file
				server.readFromFile(out);
				//Reads in the message, outputs to screen, increments number of messages, 
				//then ouputs to all other clients.
				String message;
				while ((message = in.readLine()) != null) {
					if(message.equals("DONE")){
						break;
					}
					//Prints all the messages and writes the messages to file.
					String newMessage = username + ": " + message;
					System.out.println(newMessage);
					numMessages++;
					server.writeToFile(newMessage);
					server.threadedOut(this, newMessage);
				}
				//Sends message if client leaves the chat room
				server.threadedOut(this, username + " Left the chat room.");
				server.writeToFile(username + " Left the chat room.");
				userCount = userCount - 1;
				if(userCount == 0){
					server.deleteFile();
					writer.close();
				}
				//Send a report back and close the connection
				out.println("Server received " + numMessages + " messages");
				out.println("Done");
				
				server.removeOldClient(this);
				out.close();
				in.close();
				client.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			finally{
				try{
					System.out.println("!!!!! Closing connection... !!!!!");
					client.close(); 
				}
				catch(IOException e){
					System.out.println("Unable to disconnect!");
					System.exit(1);
				}
			}
		}
	}
	// Power function to return value of a ^ b mod N
	private static long power(long a, long b, long N){
	    if (b == 1)
	        return a;
	    else
	        return (((long)Math.pow(a, b)) % N);
	}
	/*
	 * The following methods are all for the encryption and
	 * decryption of all messages sent / received.
	 */
	//Encrypts the string
	public static String encrypt(String message, int key) {
		String encrypted = "";
		
		//Convert key to binary number
		String binaryKey = Integer.toBinaryString(key);
		binaryKey = binaryKey.substring(binaryKey.length() - 8);
		
		//Converts the string to ascii, then to binary
		String newMessage = convertToAscii(message);
		newMessage = convertAsciiToBinary(newMessage);
		
		//Creates array of encrypted words
		String[] binaryArray = newMessage.split(" ");
		String[] encryptedWords = new String[binaryArray.length];
		for(int i = 0; i < binaryArray.length; i++) {
			encryptedWords[i] = binaryAdd(binaryArray[i], binaryKey);
		}
		//Adds encrypted words all into one string
		for(int q = 0; q < encryptedWords.length; q++) {
			encrypted += encryptedWords[q] + " ";
		}
		
		return encrypted;
	}
	//Decrypts the encrypted string
	public static String decrypt(String message, int key) {
		String decrypted = "";
		
		//Convert key to binary number
		String binaryKey = Integer.toBinaryString(key);
		binaryKey = binaryKey.substring(binaryKey.length() - 8);
		
		//Converts encrypted binary to decrypted binary
		String[] decryptedWords = message.split(" ");
		
		for(int i = 0; i < decryptedWords.length; i++) {
			decryptedWords[i] = binaryAdd(decryptedWords[i], binaryKey);
		}
		//Converts binary to ascii
		String[] asciiWords = new String[decryptedWords.length];
		
		for(int j = 0; j < decryptedWords.length; j++) {
			int temp = Integer.parseInt(decryptedWords[j],2);
			asciiWords[j] = String.valueOf(temp);
		}
		//Converts array to string to be decrypted
		for(int q = 0; q < decryptedWords.length; q++) {
			decrypted += asciiWords[q] + " ";
		}
		
		//Converts the ascii to binary
		decrypted = convertBack(decrypted);
		return decrypted;
	}
	//Does binary addition for encryption
	public static String binaryAdd(String word, String key){
		String newString = "";
		for (int i = 0; i < 8; i++){
			if (word.charAt(i) == key.charAt(i))
				newString += "0";
			else
				newString += "1";
	        }
	       return newString;
	 }
	
	//Converts words to ascii characters
	public static String convertToAscii(String message) {
		byte[] ascii = message.getBytes(StandardCharsets.US_ASCII);
		String asciiString = Arrays.toString(ascii);
		asciiString = removeJunk(asciiString);
		return asciiString;
	}

	//Converts Ascii characters to binary
	public static String convertAsciiToBinary(String ascii) {
		String binary = "";
		String[] arr = ascii.split(" ");
		String[] converted = new String[arr.length];
		//Converts all ascii values to binary
		for(int i = 0; i < converted.length; i++) {
			int num = Integer.valueOf(arr[i]);
			converted[i] = Integer.toBinaryString(num);
		}
		//Adds 0's to front of binary numbers if they're shorter 8
		for(int j = 0; j < converted.length; j++) {
			while(converted[j].length() < 8) {
				converted[j] = "0" + converted[j];
			}
		}
		//Converts string array to string
		for(int q = 0; q < converted.length; q++) {
			binary += converted[q] + " ";
		}
		
		return binary;
	}

	//Makes string pretty
	public static String removeJunk(String message) {
		//Removes junk from string
		String newMessage = message.replaceAll(",", "");
		newMessage = newMessage.replaceAll("\\[", "");
		newMessage = newMessage.replaceAll("\\]", "");
		return newMessage;
	}
	//Converts ascii to real characters
	public static String convertBack(String message) {
		String convertedMessage = "";
		
		//Split string on spaces and convert from ascii values to letters
		String[] arr = message.split(" ");
		for(String str: arr) {
			int num = (Integer.valueOf(str));
			char a = (char)num;
			convertedMessage += "" + a;
		}
		return convertedMessage;
	}
}