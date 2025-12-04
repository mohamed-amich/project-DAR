#!/bin/bash

echo "Starting Library Client Application..."
mvn -q exec:java -Dexec.mainClass="client.LibraryClient"
