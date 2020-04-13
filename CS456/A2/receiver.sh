#!/bin/bash

#Run script for client distributed as part of 
#Assignment 2
#Computer Networks (CS 456)
#Number of parameters: 4
#Parameter:
#    $1: <emulator's network address>
#    $2: <emulator's receiving UDP port number in
#		 the backward (receiver) direction>
#    $3: <receiver's receiving UDP port number>
#    $4: <name of file into which the recieved data is written>

#For Java implementation
java receiver $1 $2 $3 "$4"
