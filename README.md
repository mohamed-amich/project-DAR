# Distributed Library Management System
This project is presented by Mohamed Amich && Mohamed Ali Zarrouk 

A Java-based distributed application demonstrating both CORBA-style and RMI technologies for remote method invocation.

## Project Overview

This project implements a distributed library management system using two distributed computing technologies:
- **RMI (Remote Method Invocation)**: Handles book inventory management using Java's built-in RMI
- **CORBA-style ORB (SimpleORB)**: Handles user account management using a custom CORBA-like implementation that works with modern Java

## Project Structure

```
├── pom.xml                          # Maven configuration
├── src/
│   ├── common/
│   │   └── Book.java                # Book data model (serializable for RMI)
│   ├── rmi/
│   │   ├── BookService.java         # RMI remote interface
│   │   ├── BookServiceImpl.java     # RMI service implementation
│   │   └── RMIServer.java           # RMI server startup
│   ├── corba/
│   │   ├── SimpleORB.java           # Custom ORB implementation (CORBA-like)
│   │   ├── UserData.java            # User data structure (serializable)
│   │   ├── UserServiceServant.java  # User service implementation
│   │   └── CORBAServer.java         # CORBA-style server startup
│   └── client/
│       └── LibraryClient.java       # Client application (uses both RMI and CORBA)
├── build.sh                         # Build script
├── run_rmi_server.sh                # Run RMI server only
├── run_corba_server.sh              # Run CORBA server only
├── run_client.sh                    # Run client only
└── run_all.sh                       # Run complete system
```

## Technologies Used

- **Java 19** (GraalVM)
- **Maven** for build management
- **Java RMI** (built-in) for book service
- **SimpleORB** (custom implementation) for user service - demonstrates CORBA concepts

## How to Run

### Option 1: Run everything together
```bash
./run_all.sh
```
This will:
1. Build the project
2. Start the RMI server (Book Service) on port 1099
3. Start the CORBA server (User Service) on port 1100
4. Launch the interactive client application

### Option 2: Run components separately
In separate terminals:
```bash
# Terminal 1: Start RMI Server
mvn exec:java -Dexec.mainClass="rmi.RMIServer"

# Terminal 2: Start CORBA Server  
mvn exec:java -Dexec.mainClass="corba.CORBAServer"

# Terminal 3: Run Client
mvn javafx:run
```

## Features

### Book Management (RMI)
- Add new books to inventory
- Search books by title or author
- List all available books
- Borrow and return books

### User Management (CORBA-style)
- User registration and authentication
- View user information
- List all users
- Admin role support

## Sample Data

### Pre-configured Users
| User ID   | Password | Role      |
|-----------|----------|-----------|
| admin     | admin123 | admin     |
| user1     | pass123  | user      |
| user2     | pass456  | user      |
| librarian | lib123   | librarian |

### Pre-configured Books
- Distributed Systems: Concepts and Design
- Computer Networking: A Top-Down Approach
- Head First Design Patterns
- Java: The Complete Reference
- Building Microservices

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   Library Client                         │
│         (Console Application)                            │
└────────────────────┬────────────────────┬───────────────┘
                     │                    │
         ┌───────────▼──────────┐ ┌──────▼─────────────┐
         │   RMI Connection     │ │  CORBA Connection  │
         │   (Port 1099)        │ │  (Port 1100)       │
         └───────────┬──────────┘ └──────┬─────────────┘
                     │                    │
┌────────────────────▼────────────────────▼───────────────┐
│                                                          │
│  ┌────────────────────┐      ┌─────────────────────┐   │
│  │    RMI Server      │      │   SimpleORB Server  │   │
│  │  (Book Service)    │      │   (User Service)    │   │
│  │                    │      │                     │   │
│  │  - Add Book        │      │  - Register User    │   │
│  │  - Search Books    │      │  - Authenticate     │   │
│  │  - Borrow/Return   │      │  - Get User Info    │   │
│  └────────────────────┘      └─────────────────────┘   │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

## Technical Notes

### Why SimpleORB instead of standard CORBA?
- CORBA was removed from Java 11+ (JEP 320)
- External CORBA libraries (GlassFish CORBA, JacORB) have compatibility issues with Java 19
- SimpleORB provides the same distributed computing concepts:
  - Object Request Broker (ORB) for message routing
  - Servant registration and lookup
  - Remote method invocation with serialization
  - Stub-based client access

### RMI vs CORBA Comparison (demonstrated in this project)
| Aspect | RMI | CORBA (SimpleORB) |
|--------|-----|-------------------|
| Protocol | JRMP | Custom TCP/Object Streams |
| Registry | RMI Registry (port 1099) | Reference file |
| Interface | Java Remote Interface | Service operations |
| Serialization | Java Serialization | Java Serialization |
| Language | Java only | Language-independent design |

## Recent Changes

- **2024-12-03**: Initial project setup
  - Implemented RMI-based Book Service
  - Created SimpleORB as CORBA-like implementation for modern Java
  - Implemented User Service using SimpleORB
  - Created unified client application connecting to both services
  - Added sample data for books and users

## Development Notes

- The project compiles and runs with Java 19 (GraalVM)
- Maven handles the build process
- No external dependencies required (pure Java implementation)
