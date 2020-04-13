## How To Run the Program

### Notice before running the program

Run NSE, and routers on two different machines or a single machine in CS Undergrad Environment.

Get the public IP address by command (for Linux): 
```
curl ifconfig.me
```
Navigate to this program's directory. 

Make the program by command:
```
make
```

### Run the NSE firstly

On the NSE machine, navigate to the project directory. Use following command to run the NSE:
```
./nse-linux386 (with following command line parameters in the given order:)

	<routers' IP address>

	<nse's receiving UDP port number>
```

### Then run the router program
On the router machine, navigate to the project directory. Use following command to run the router:
```
bash ./router.sh (with following command line parameters in the given order:)

	<router's id>

	<nse's IP address>

	<router receiving UDP port number>
```
Notice: Please run the router program five times in order from the 1st to 5th on the same machine with difference port number.

## Where the Program Built and Tested on
	
The program is built and tested on student.cs machines ubuntu1804-002, and ubuntu1804-008.

## What Version of Make and Compilers using

openjdk 11.0.6
