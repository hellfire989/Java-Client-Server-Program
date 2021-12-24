/*
	Name: Joshua Kuiper

	Project Description:
	This is a multithreaded client/server program.

	Many clients can connect to one server, and contents of their chat in a chat room
	is stored with in a chat file which is created if a user joins an empty chat room,
	displays to all users when they join, and deletes if they're the last one to leave.

	Programmer: COSC 439/522, F '21
	Multi-threaded Client program
	File name: TCPClientMT.java
	When you run this program, you must give both the host name and
	the service port number as command line arguments. For example,
	
	java TCPClientMT -u User -p 20600 -h Localhost
*/

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
public class TCPClientMT{
	private static InetAddress host;
	static long startTime = System.currentTimeMillis();

	public static void main(String[] args){
		String portNumber = "";
		String hostName = "";
		String user = "";
		//User arguments
		try {
			for(int i = 0; i < args.length; i++) {
				if(args[i] != null) {
					if(args[i].equals("-p")) {
						portNumber = args[i+1];
					}
					else if(args[i].equals("-u")) {
						user = args[i+1];
					}
					else if(args[i].equals("-h")) {
						hostName = args[i+1];
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
			//If user doesn't enter username
			if(user == "") {
				try{
					BufferedReader userEntry = new BufferedReader(new InputStreamReader(System.in));
					String message;
					System.out.print("Username required: ");
                    message = userEntry.readLine();
					user = message;
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			//Defaults if no host or port is entered
			if(hostName == "") {
				hostName = "localhost";
			}
			if(portNumber == "") {
				portNumber = "20600";
			}
			
			// Get server IP-address
			host = InetAddress.getByName(hostName);
			
			//Catches unknown host
       }catch(UnknownHostException e){
           System.out.println("Host ID not found!");
           System.exit(1);
       }
       run(Integer.parseInt(portNumber), user);
    }
	//Runs the client
	private static void run(int port, String user){
		Socket link = null;
		try{
        // Establish a connection to the server
		link = new Socket(host,port); 

        // Set up input and output streams for the connection
		BufferedReader in = new BufferedReader(
		new InputStreamReader(link.getInputStream()));
		PrintWriter out = new PrintWriter(
		link.getOutputStream(),true); 
		
		// Create a sender thread. This thread reads messages typed at the keyboard
		// and sends them to the server
		Sender sender = new Sender(out, user);
		
		// Start the sender thread
		sender.start();
      
		// The main thread reads messages sent by the server, and displays them on the screen
		String message;
		
		// Get data from the server and display it on the screen
		int numMessages = 0;
		int privateKey = (int)((Math.random() * (201 - 100)) + 100);
		String gee = "1";
		String nee = "1";
		//Gets the generated key
		long genKey = 1;
		//Secret key for Server
		long secretKey = 1;

		try{
			while (!(message = in.readLine()).equals("Done")){
				if(numMessages == 0){
					gee = message.substring(0, 4);
					nee = message.substring(5, 9);
					genKey = power(Long.parseLong(gee), privateKey, Long.parseLong(nee));
					secretKey = power(genKey, privateKey, Long.parseLong(nee));
					System.out.println("Handshake complete. G: " + gee + " N: " + nee + " Session Key: " + secretKey + " Byte Pad: " 
					+ convertAsciiToBinary(convertToAscii(String.valueOf(privateKey))).substring(0, 8));
				}
				if(!(message.contains("established"))){
					if(message.contains("10")){
						System.out.println(decrypt(message,(int)secretKey));
					}
					else{
						System.out.println(message);
					}
				}
					//If the message is "Done", print the total run time.
					//Computes the system time that the user was in the server.
					if((message = in.readLine()).equals("Done")){
						long endTime = System.currentTimeMillis();
						long mils = endTime - startTime;
						
						int leftOver = (int) mils % 1000;
						int seconds = (int) (mils / 1000) % 60;
						int minutes = (int) ((mils / (1000*60)) % 60);
						int hours = (int) ((mils / (1000*60*60)) % 24);
						System.out.println("Total run time: " + hours + "h::" + minutes + "m::" + seconds + "s::" + leftOver + "ms");
					}
				numMessages++;
			}
			//Catches errors
		}catch(NullPointerException e){
        }
        }catch(IOException e){
        }
        finally{
			
			try{
				System.out.println("\n!!!!! Closing connection... !!!!!");
				link.close(); 
            }catch(IOException e){
                  System.out.println("Unable to disconnect!");
                  System.exit(1);
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

// The sender class reads messages typed at the keyboard, and sends them to the server
class Sender extends Thread{
	private PrintWriter out;
	String username = "";
    public Sender (PrintWriter out, String user){
		this.out = out;
		username = user;
    }
	// overwrite the method 'run' of the Runnable interface

	// this method is called automatically when a sender thread starts.
	
 	public void run(){
		//Set up stream for keyboard entry
		BufferedReader userEntry = new BufferedReader(new InputStreamReader(System.in));
		String message;
  
		// Get data from the user and send it to the server
		try {
			int count = 0;
			if(count <= 0)
				out.println(username);
			count = 1;
			do{
				System.out.print("Enter message: "); 
				message = userEntry.readLine();
				out.println(message); 
				}while (!message.equals("DONE"));
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}