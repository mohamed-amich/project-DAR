#!/bin/bash

echo "=============================================="
echo "    Distributed Library System Launcher"
echo "=============================================="

# Build first
./build.sh

if [ $? -ne 0 ]; then
    echo "Build failed. Cannot start servers."
    exit 1
fi

echo ""
echo "Starting servers in background..."

# Start RMI Server in background
echo "Starting RMI Server..."
mvn -q exec:java -Dexec.mainClass="rmi.RMIServer" &
RMI_PID=$!
sleep 3

# Start CORBA Server in background
echo "Starting CORBA Server..."
mvn -q exec:java -Dexec.mainClass="corba.CORBAServer" &
CORBA_PID=$!
sleep 3

echo ""
echo "=============================================="
echo "Servers are running!"
echo "RMI Server PID: $RMI_PID"
echo "CORBA Server PID: $CORBA_PID"
echo "=============================================="
echo ""
echo "Starting client..."
echo ""

# Start Client
mvn -q exec:java -Dexec.mainClass="client.LibraryClient"

# Cleanup
echo ""
echo "Shutting down servers..."
kill $RMI_PID 2>/dev/null
kill $CORBA_PID 2>/dev/null
echo "Done."
