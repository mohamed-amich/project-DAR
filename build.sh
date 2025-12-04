#!/bin/bash

echo "=============================================="
echo "    Building Distributed Library System"
echo "=============================================="

echo ""
echo "[1/2] Downloading dependencies and compiling..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "Error: Build failed"
    exit 1
fi

echo "[2/2] Build complete!"
echo "=============================================="
echo ""
echo "To run the system:"
echo "  1. Start RMI Server:    ./run_rmi_server.sh"
echo "  2. Start CORBA Server:  ./run_corba_server.sh"
echo "  3. Start Client:        ./run_client.sh"
echo "  Or run all:             ./run_all.sh"
echo "=============================================="
