# MultiThreaded Java-Client-Server-Program
Only username is required, and will be prompted for one if
one is not entered.

Default port for both programs is set to 20600.
Default G set to 1019
Deafult N set to 1823

## Project description
This is a multithreaded client/server program.

Many clients can connect to one server, and contents of their chat in a chat room
is stored with in a chat file which is created if a user joins an empty chat room,
displays to all users when they join, and deletes if they're the last one to leave.

This project runs a client and server program and connets the two given a 
port name, host name, and username.
This version is multi threaded, so the server can accept multiple clients.

## How to compile and run the programs
	
To Compile:
	javac TCPServerMT.java
	javac TCPClientMT.java

To Run:
Basic run with defualt everything:	
	java TCPServerMT
	java TCPClientMT

To Run with set variables
	java TCPServerMT -p (Port Number) -g (Varible used in encryption) -n (Varible used in encryption)
	java TCPClient -p (Port Number) -h (Host Name) -u (Username)

## Credit to borrowed code
Used to help convert milliseconds into hours, minutes, and seconds:

https://stackoverflow.com/questions/10874048/from-milliseconds-to-hour-minutes-seconds-and-milliseconds

Basis of my Diffie-Hellman:

https://www.geeksforgeeks.org/implementation-diffie-hellman-algorithm/

## Notes & current bugs: To run with set variables on client, not all 3 are required.
G & N are not properly computed

All clients use the same key

Extremely large numbers have trouble processing
