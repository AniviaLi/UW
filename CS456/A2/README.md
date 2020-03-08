## How To Run the Program

### Notice before running the program

Run nEmulator, receiver, sender of three different machines or a single machine in CS Undergrad Environment.

Get the public IP address by command (for Linux): 
```
curl ifconfig.me
```
Navigate to this program's directory. 

Make the java program by command:
```
make
```

### Run the nEmulator firstly

On the emulator machine, navigate to the project directory. Use following command to run the emulator:
```
./nEmulator-linux386 (with following command line parameters in the given order:)

	<emulator's receiving UDP port number in the forward (sender) direction>

	<receiver's network address>

	<receiver's receiving UDP port number>

	<emulator's receiving UDP port number in the backward (receiver) direction>

	<sender's network address>

	<sender's receiving UDP port number>

	<maximum delay of the link in units of millisecond>

	<packet discard probability>

	<verbose-mode>: set to 1 to output its internal processing
```

### Then run the receiver program
On the receiver machine, navigate to the project directory. Use following command to run the receiver:
```
java receiver (with following command line parameters in the given order:)

	<emulator's network address>

	<emulator's receiving UDP port number in the backward (receiver) direction>

	<receiver's receiving UDP port number>

	<name of file into which the recieved data is written>
```

### Finally run the sender program
On the sender machine, navigate to the project directory. Use following command to run the sender:
```
java sender (with following command line parameters in the given order:)

	<emulator's network address>

	<emulator's receiving UDP port number in the forward (sender) direction>

	<sender's receiving UDP port number>

	<name of file to be transferred>

```

## Where the Program Built and Tested on
	
The program is built and tested on student.cs machines ubuntu1804-002, ubuntu1804-004 and ubuntu1804-008.

## What Version of Make and Compilers using

openjdk 11.0.6




