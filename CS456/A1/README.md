# How To Run the Program

	- Run the server program firstly. On the server machine, navigate to the project directory. 
	Then, use following command to run the server: bash ./server.sh <req_code>

	<req_code> is an integer and will be checked when client asks for connection

	Server program will print out a SERVER_PORT number for client program to use as parameter <n_port>

	- Then run the client program. On the client machine, navigate to the project directory. 
	Then, use following command to run the client: bash ./client.sh <server_address> <n_port> <req_code> <msg>

	<server_address> is the IP address of the server machine
		you can get the public IP address by command (for Linux): curl ifconfig.me
	<n_port> is the port number for negotiation
	<req_code> is the request code for negotiation
	<msg> is the message sending to the server for reversing


# Where the Program Built and Tested on
	
	The program is built and tested on student.cs machines ubtuntu1804-004 and ubtuntu1804-004 


# What Version of Make and Compilers using

	Python 2.7.17
