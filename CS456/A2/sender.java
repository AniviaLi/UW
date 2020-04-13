import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.Math;

class sender{

	static String emulatorAddr;
	static int emulatorRcvPort;
	static int senderRcvPort;
	static String fileName;
	static DatagramSocket sndSocket;
	static DatagramSocket rcvSocket;
	static InetAddress emulatorIP;
	static final int maxContentSize = 500;
	static ArrayList<packet> packetList = new ArrayList<packet>();
	static volatile int base;
	static final int windowSize = 10;
	static volatile int nextseqnum;
	static PrintWriter seqnumLog;
	static PrintWriter ackLog;
	static volatile int ackReceived;
	static Timer timer;
	static TimerTask timerTask;
	static final long timeout = 100;
	static final int pktSize = 512;


	private static void readInFile() throws Exception {

		File file = new File(fileName);
		BufferedReader br = new BufferedReader (new FileReader(file));
		int fileSize = (int)file.length();
		int numOfPkts = (fileSize / maxContentSize) + 1;
		int lastContentLen = (fileSize % maxContentSize);

		/* read in File and create packet for every 500 byte content */
		for (int i = 1; i < numOfPkts; ++i) {
			char[] content = new char[maxContentSize];
			br.read(content, 0, maxContentSize);
			packet pkt = packet.createPacket(i-1, new String(content));
			packetList.add(pkt);
		}
		if (lastContentLen > 0) {
			char[] content = new char[lastContentLen];
			br.read(content, 0, lastContentLen);
			packet pkt = packet.createPacket(numOfPkts-1, new String(content));
			packetList.add(pkt);
		}

		/* add EOT packet */
		packet pkt = packet.createEOT(numOfPkts);
		packetList.add(pkt);

	}
	

	private static void GBN_sender() throws Exception{
		base = 0;
		nextseqnum = 0;
		byte[] sndData = new byte[pktSize];

		/* send all packets except EOT */
		while(nextseqnum < packetList.size() - 1) {
			/* if the window is not full */
			if (nextseqnum < base + windowSize) {
				/* send packet #nextseqnum and print to seqnum.log*/
				sndData = packetList.get(nextseqnum).getUDPdata();
				DatagramPacket sndPkt = new DatagramPacket(sndData, sndData.length, emulatorIP, emulatorRcvPort);
				sndSocket.send(sndPkt);
				seqnumLog.println(packetList.get(nextseqnum).getSeqNum());

				System.out.println("packet" + nextseqnum + " sent");

				/* Start timer for the first sent packet */
				if (nextseqnum == base) {
					System.out.println("Set Timer");
					timer = new Timer();
					TimerTask timerTask = new TimerTask() {	
						@Override
						public void run() {
							try {	
								resend();	
							} catch	(Exception e) {
								e.printStackTrace();
							}
						}
					};

					timer.schedule(timerTask, timeout);
				}
				/* packet #nextseqnum finished sending, increment nextseqnum */
				nextseqnum ++;
			}
		}
	}


	private static void resend() throws Exception {
		byte[] sndData = new byte[pktSize];
		/* Time out, cancel the old timer and restart a new timer */
		timer.cancel();
		timer = new Timer();
		timerTask = new TimerTask() {	
			@Override
			public void run() {
				try {	
					resend();	
				} catch	(Exception e) {
					e.printStackTrace();
				}
			}
		};

		timer.schedule(timerTask, timeout);

		System.out.println("ACK" + base + " TimeOut, " + " resend packet " + base + " to " + (nextseqnum-1));

		/* resend sended-but-unACK packets: packet[base] -> packet[nextseqnum-1] */
		for (int i = base; i < nextseqnum; i++) {
			sndData = packetList.get(i).getUDPdata();
			DatagramPacket sndPkt = new DatagramPacket(sndData, sndData.length, emulatorIP, emulatorRcvPort);
			sndSocket.send(sndPkt);
			seqnumLog.println(packetList.get(i).getSeqNum());
			System.out.println("resend packet " + i);
		}
	}


	private static void ACK_Receiver() throws Exception {
		byte[] rcvData = new byte[pktSize];
		DatagramPacket rcvPkt = new DatagramPacket(rcvData, rcvData.length);

		while (true) {

			/* Receive ACK and print to ack.log */
			rcvSocket.receive(rcvPkt);
			packet myRcvPkt = packet.parseUDPdata(rcvPkt.getData());
			ackLog.println(myRcvPkt.getSeqNum());

			/* Received EOT */
			if (myRcvPkt.getType() == 2) {
				System.out.println("EOT" + myRcvPkt.getSeqNum() + " received, exit");
				sndSocket.close();
				rcvSocket.close();
				seqnumLog.close();
				ackLog.close();
				break;
			}

			/* Received ACK with correct sequence number */
			if ((myRcvPkt.getSeqNum() >= base % 32 && Math.abs(myRcvPkt.getSeqNum() - base % 32) < 31) || 
				(myRcvPkt.getSeqNum() < base % 32 && Math.abs(myRcvPkt.getSeqNum() - base % 32) > 1)) {
				
				/* algorithm to calculate correct base value */
				if (myRcvPkt.getSeqNum() >= base % 32 && Math.abs(myRcvPkt.getSeqNum() - base % 32) < 31) {
					base = base + (myRcvPkt.getSeqNum() - base % 32) + 1;
				} else if (myRcvPkt.getSeqNum() < base % 32 && Math.abs(myRcvPkt.getSeqNum() - base % 32) > 1){
					base = base + (32 - base%32) + myRcvPkt.getSeqNum() + 1;
				}

				System.out.println("ACK" + (base - 1) + " received, base now is " + base);
				
				/* Received last ACK, need to send EOT file */
				if (base == (packetList.size() - 1)) {
					byte[] sndData = new byte[pktSize];
					sndData = packetList.get(base).getUDPdata();
					DatagramPacket sndPkt = new DatagramPacket(sndData, sndData.length, emulatorIP, emulatorRcvPort);
					sndSocket.send(sndPkt);
					seqnumLog.println(packetList.get(base).getSeqNum());
					System.out.println("EOT" + base%32 + " sent");
				}

				if (base == nextseqnum) {
					/* No more sent-but-notACK packets, cancel the timer */
					System.out.println("Timer canceled");
					timer.cancel();
				} else {
					/* still have sent-but-notACK packets, restart timer */
					timer.cancel();
					timer = new Timer();
					timerTask = new TimerTask() {	
						@Override
						public void run() {
							try {	
								resend();	
							} catch	(Exception e) {
								e.printStackTrace();
							}
						}
					};

					timer.schedule(timerTask,timeout);
				}
			}
		}
	}


	private static class rcvAck implements Runnable {
		/* thread for receiving ACK */
		public void run() {
			try {
				ACK_Receiver();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}


	public static void main(String[] args) throws Exception {
		
		if (args.length != 4) {
			System.out.println("ERROR: INVALID ARGUMENT NUMBER, PLEASE TRY AGAIN");
			System.exit(1);
		}

		emulatorAddr = args[0];
		emulatorRcvPort = Integer.parseInt(args[1]);
		senderRcvPort = Integer.parseInt(args[2]);
		fileName = args[3];
		emulatorIP = InetAddress.getByName(emulatorAddr);

		seqnumLog = new PrintWriter(new FileWriter("seqnum.log"));
		ackLog = new PrintWriter(new FileWriter("ack.log"));
		
		sndSocket = new DatagramSocket();
		emulatorIP = InetAddress.getByName(emulatorAddr);
		rcvSocket = new DatagramSocket(senderRcvPort);

		readInFile();

		Thread thread = new Thread(new rcvAck());
		thread.start();

		GBN_sender();

	}
}
