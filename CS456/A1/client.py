from socket import *
import sys

def client_tcp_negotiation(server_address, n_port, req_code):
	
	# create client socket (TCP)
	clientSocket = socket(AF_INET, SOCK_STREAM)

	# perfrom three-way handshake and establish TCP connection
	clientSocket.connect((server_address,n_port))
	# print("TCP CONNECTION ESTABLISHED")

	# send request code to server
	clientSocket.send(str(req_code))
	# print("REQUEST CODE SENDED: " + str(req_code))

	# recieve r_port for UDP socket
	r_port = int(clientSocket.recv(1024))
	if r_port == -1:
		# print("INVALID REQUEST CODE")
		clientSocket.close()
		sys.exit(1)
	else:
		# close the TCP connection
		# print("TRANSACTION PORT NUMBER RECEIVED: " + str(r_port))
		clientSocket.close()
		# print("TCP CONNECTION CLOSED")
		return r_port


def client_udp_transaction(server_address, r_port, msg):

	# create the sending socket (UDP)
	sendingSocket = socket(AF_INET, SOCK_DGRAM)

	# send <msg> to server
	sendingSocket.sendto(msg, (server_address, r_port))
	# print("MESSAGE SENT")

	# receive the reversed <msg> from server
	reversedMsg, server_address = sendingSocket.recvfrom(2048)
	# print("REVERSED MESSAGE RECEIVED: ")

	#print out the reversed <msg>
	print reversedMsg

	# close the UDP connection 
	sendingSocket.close()
	# print("UDP CONNECTION CLOSED")


def main():

	# check command line arguments
	try:
		server_address = str(sys.argv[1])
		n_port = int(sys.argv[2])
		req_code = int(sys.argv[3])
		msg = str(sys.argv[4])
	except IndexError:
		print("ERROR: MISSING PARAMETER(S), PLEASE TRY AGAIN")
		sys.exit(1)
	except ValueError:
		print("ERROR: WRONG PARAMETER TYPE, PLEASE TRY AGAIN")
		sys.exit(1)

	r_port = client_tcp_negotiation(server_address, n_port, req_code)
	client_udp_transaction(server_address, r_port, msg)


if __name__ == "__main__":
	main()