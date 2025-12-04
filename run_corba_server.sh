#!/bin/bash

echo "Starting CORBA User Service Server..."
mvn -q exec:java -Dexec.mainClass="corba.CORBAServer"
