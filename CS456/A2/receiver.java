import java.io.*;
import java.net.*;
import java.util.*;

class receiver {

	static String emulatorAddr;
	static int emulatorRcvPort;
	static int receiverRcvPort;
	static String fileName;
	static DatagramSocket sndSocket;
	static DatagramSocket rcvSocket;
	static InetAddress emulatorIP;
	static PrintWriter arrivalLog;
	static PrintWriter writeFile;
	static final int pktSize = 512;


	private static void rcvPkt() throws Exception {
		int pktReceived = 0;
		byte[] rcvData = new byte[pktSize];
		byte[] sndData = new byte[pktSize];
		DatagramPacket rcvPkt = new DatagramPacket(rcvData, rcvData.length);
		
		/* begin to receive packet */
		while (true) {
			/* receive packet and write sequence number to arrival.log */
			rcvSocket.receive(rcvPkt);
			packet myRcvPkt = packet.parseUDPdata(rcvPkt.getData());
			arrivalLog.println(myRcvPkt.getSeqNum());
			
			System.out.println("packet"+myRcvPkt.getSeqNum()+" received");

			/* received packet with correct sequence number */
			if (myRcvPkt.getSeqNum() == pktReceived%32) {
				pktReceived ++;

				/* received packet needs to be Ack */
				if (myRcvPkt.getType() == 1) {
					String content = new String (myRcvPkt.getData());
					writeFile.print(content);

					/* create and send Ack packet */
					packet ackPkt = packet.createACK(myRcvPkt.getSeqNum());
					sndData = ackPkt.getUDPdata();
					DatagramPacket sndPkt = new DatagramPacket(sndData, sndData.length, emulatorIP, emulatorRcvPort);
					sndSocket.send(sndPkt);
					System.out.print("Sent ACK");
					System.out.println(ackPkt.getSeqNum());

				/* received EOT */ 
				} else if (myRcvPkt.getType() == 2) {
					/* create and send EOT packet */
					packet eotPkt = packet.createEOT(myRcvPkt.getSeqNum());
					sndData = eotPkt.getUDPdata();
					DatagramPacket sndPkt = new DatagramPacket(sndData, sndData.length, emulatorIP, emulatorRcvPort);
					sndSocket.send(sndPkt);
					System.out.print("Sent EOT");
					System.out.println(eotPkt.getSeqNum());

					/* close the sockets, file, and exit */
					sndSocket.close();
					rcvSocket.close();
					arrivalLog.close();
					writeFile.close();
					break;
				}
			/* received packet with incorrect sequence number */
			} else {

				/* send back most recent correct ACK */
				if (pktReceived != 0) {
					packet ackPkt = packet.createACK(pktReceived-1);
					sndData = ackPkt.getUDPdata();
					DatagramPacket sndPkt = new DatagramPacket(sndData, sndData.length, emulatorIP, emulatorRcvPort);
					sndSocket.send(sndPkt);
					System.out.print("Sent ACK");
					System.out.println(ackPkt.getSeqNum());
				}

				/* ignore if waiting for the first packet */
			}
			
		}

	}


	public static void main(String[] args) throws Exception {
		
		/* read in arguments */
		if (args.length != 4) {
			System.out.println("ERROR: INVALID ARGUMENT NUMBER, PLEASE TRY AGAIN");
			System.exit(1);
		}
		emulatorAddr = args[0];
		emulatorRcvPort = Integer.parseInt(args[1]);
		receiverRcvPort = Integer.parseInt(args[2]);
		fileName = args[3];

		/* create UDP sockets and get emulator IP address*/
		sndSocket = new DatagramSocket();
		emulatorIP = InetAddress.getByName(emulatorAddr);
		rcvSocket = new DatagramSocket(receiverRcvPort);
		
		/* create arrival.log and write file*/
		arrivalLog = new PrintWriter(new FileWriter("arrival.log"));
		writeFile = new PrintWriter(new FileWriter(fileName));

		rcvPkt();
	}
}