from socket import *
import sys

def server_tcp_negotiation(welcomingSocket, req_code):

	while 1:
		# create TCP connection between clientSocket and
		# connectionSocket
		connectionSocket, addr = welcomingSocket.accept()
		# print("TCP CONNECTION REQUEST ACCEPTED")

		# receive <req_code> from client
		recv_code = connectionSocket.recv(1024)
		# print("CLIENT REQUEST CODE RECEIVED: " + recv_code)

		# verify <req_code>
		if recv_code == str(req_code):
			# print("CLIENT REQUEST CODE VERIFIED")

			# create server socket (UDP) for receiving <msg>
			recvSocket = socket(AF_INET, SOCK_DGRAM)

			# assign port number <r_port> to recvSocket 
			recvSocket.bind(('',0))
			r_port = recvSocket.getsockname()[1]

			# send <r_port> to client using TCP connection
			connectionSocket.send(str(r_port))
			# print("TRANSACTION PORT NUMBER SENT: " + str(r_port))
			connectionSocket.close()
			return recvSocket
			# client will close the TCP connection after receving
			# <r_port>

		else:
			# client fails to send the intended <req_code>,
			# inform client and close the TCP connection
			# print("INVALID CLIENT REQUEST CODE") 
			connectionSocket.send("-1")
			connectionSocket.close()
			# print("TCP CONNECTION CLOSED")


def server_udp_transaction(recvSocket):

	#receive <msg> from client
	msg, clientAddress = recvSocket.recvfrom(2048)
	# print("CLIENT MESSAGE RECEIVED: " + msg)

	#reverse <msg>
	reversedMsg = msg[::-1]

	#send reversed <msg> to client
	recvSocket.sendto(reversedMsg, clientAddress)
	# print("REVERSED MESSAGE SENT")

	#close the UDP connection"
	recvSocket.close()


def main():

	# check command line argument
	try:
		req_code = int(sys.argv[1])
	except IndexError:
		print("ERROR: MISSING REQUEST CODE")
		sys.exit(1)
	except ValueError:
		print("ERROR: REQUEST CODE MUST BE INTEGER")
		sys.exit(1)

	# create welcoming socket (TCP)
	welcomingSocket = socket(AF_INET, SOCK_STREAM)

	# assign port number <n_port> to welcoming socket
	welcomingSocket.bind(('',0))
	n_port = welcomingSocket.getsockname()[1]

	# print out <n_port> to screen and into "port.txt"
	print("SERVER_PORT=" + str(n_port))
	f = open("port.txt","w")
	f.write("SERVER_PORT=" + str(n_port))
	f.close()

	# listen for TCP connection requests from the client
	welcomingSocket.listen(1)
	# print("WAITING FOR TCP CONNECTION REQUEST")

	while 1:
		recvSocket = server_tcp_negotiation(welcomingSocket, req_code)
		server_udp_transaction(recvSocket)


if __name__ == "__main__":
	main()
