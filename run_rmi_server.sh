#!/bin/bash

echo "Starting RMI Book Service Server..."
mvn -q exec:java -Dexec.mainClass="rmi.RMIServer"
