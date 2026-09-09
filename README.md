# 📚 BookBridge — Distributed Multi-Branch Library Management System

**BookBridge** is a high-performance, distributed multi-branch Library Management System in Java. It connects physical library branches (e.g., *Guindy*, *Adyar*, *Velachery*) under a single interactive catalog, featuring **role-based authentication**, **real-time borrowing/returns**, **inter-branch transfer logistics**, **user administration**, and **book acquisition suggestions**.

---

## 🔑 Default Login Credentials

| Role | Username | Password | Assigned Branch | Permissions |
|---|---|---|---|---|
| **🛡️ System Admin** | `admin` | `admin123` | Guindy Library (Branch #1) | Full Inventory Control, User Creation & Deletion, Request Approvals |
| **👤 Member** | `purushothaman` | `user123` | Guindy Library (Branch #1) | Borrow, Return, Request Inter-Branch Transfers, Suggest Purchases |
| **👤 Member** | `alice` | `user123` | Adyar Library (Branch #2) | Borrow, Return, Request Inter-Branch Transfers, Suggest Purchases |

> [!TIP]
> Administrators can create new Member or Admin accounts directly from the **User Management** tab in the Admin Console.

---

## 🏗️ Architecture & Separation of Concerns

BookBridge follows a clean multi-tier client-server architecture:

```mermaid
graph TD
    subgraph Client Layer
        WebBrowser["🌐 Web Browser (http://localhost:8081)"]
        Terminal["🖥️ Terminal TUI (ANSI / stty)"]
        WebUI["WebUI (HTTP Server & Views)"]
        CLI["Menu & Input Engine"]
        Services["AuthService / LibraryService / RequestService"]
        NetClient["NetworkClient (TCP Socket Client)"]
    end

    subgraph Server Layer (Port 8080)
        LibServer["LibraryServer (ServerSocket & ExecutorService)"]
        ClientHandler["ClientHandler Worker Threads"]
        DBManager["DatabaseConnection (JDBC Pool / Memory Fallback)"]
    end

    subgraph Persistence Layer
        MySQL[("MySQL Database (bookbridge)")]
        MemoryStore[("In-Memory Datastore (Auto-Fallback)")]
    end

    WebBrowser -->|HTTP| WebUI
    Terminal -->|Keyboard| CLI
    WebUI --> Services
    CLI --> Services
    Services --> NetClient
    NetClient -->|TCP Socket / NetworkMessage| LibServer
    LibServer --> ClientHandler
    ClientHandler --> DBManager
    DBManager -->|Primary| MySQL
    DBManager -.->|Zero-Config Fallback| MemoryStore
```

---

## ✨ Features

### 1. 🌐 Modern Glassmorphic Web Portal (`http://localhost:8081`)
- **Member Dashboard**:
  - Browse books with real-time multi-criteria filtering (Title, Author, Category, ID).
  - 1-Click **Borrow** for books available in the user's home branch.
  - Interactive **Inter-Branch Transfer Modal** when a title is located at another branch.
  - **Return Book Modal** with branch validation.
  - **Purchase Suggestion Modal** to recommend new book acquisitions.
- **Librarian & Admin Console**:
  - **User Management Tab**: View all registered users, create new Member/Admin accounts with branch assignments, and remove users.
  - **Catalog Inventory Tab**: Add new books, delete books, and adjust copies.
  - **Transfer Requests Tab**: 1-click Approve or Reject inter-branch requests.
  - **Purchase Requests Tab**: 1-click Approve or Order requested books.

### 2. 🖥️ Interactive Terminal TUI
- Full ANSI box drawing, color themes, and raw POSIX `stty` keyboard navigation.
- Username & Password authentication prompt before entering member or admin menus.

### 3. ⚡ High Concurrency & Resilient Storage
- **Thread Pool Execution**: Server runs on an `ExecutorService` (50 workers) with graceful JVM shutdown hooks.
- **Dual Persistence Mode**: Connects to MySQL (`config/db.properties` or ENV variables). If MySQL is offline, it automatically initializes a thread-safe in-memory store pre-seeded with accounts and books for zero-friction testing.

---

## 🚀 Quick Start Guide

### 1. Build the Project
```bash
bash build.sh
```

### 2. Start the Backend Server (Port 8080)
```bash
bash run-server.sh
```

### 3. Start the Web Portal & Client (Port 8081)
In a second terminal window:
```bash
bash run-client.sh
```
*Open your web browser and go to **[http://localhost:8081](http://localhost:8081)**.*

### 4. Optional: Run Interactive Terminal UI
```bash
bash run-cli.sh
```

---

## ⚙️ Configuration (`config/db.properties`)

```properties
db.host=localhost
db.port=3306
db.name=bookbridge
db.user=root
db.password=your_password
```
*Environment variable overrides supported:* `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.

---

## 📁 Repository Structure

```
.
├── config/
│   └── db.properties               # Database connection settings
├── src/
│   └── com/bookbridge/
│       ├── bookbridge.sql          # MySQL Schema with Users & Initial Data
│       ├── client/                 # Client Layer
│       │   ├── Main.java           # Client Entry Point
│       │   ├── NetworkClient.java  # Thread-safe TCP Socket Client
│       │   ├── service/            # Business Logic Services
│       │   │   ├── AuthService.java    # Login, Registration & User Admin
│       │   │   ├── LibraryService.java # Catalog & Book Actions
│       │   │   └── RequestService.java # Transfers & Purchase Requests
│       │   └── ui/                 # UI Engines
│       │       ├── Input.java      # Raw Terminal Key Reader
│       │       ├── Menu.java       # CLI Navigation & Auth Prompt
│       │       ├── TerminalUI.java # ANSI Box Renderer
│       │       └── WebUI.java      # Modern Glassmorphic Web Server
│       ├── model/                  # Domain Entities
│       │   ├── Book.java
│       │   ├── Branch.java
│       │   ├── PurchaseRequest.java
│       │   ├── TransferRequest.java
│       │   └── User.java           # User Model with Roles & Branches
│       ├── network/
│       │   └── NetworkMessage.java # Serialized IPC Protocol
│       └── server/                 # Backend Server Layer
│           ├── DatabaseConnection.java # Data Access, Pool & In-Memory Fallback
│           ├── ClientHandler.java  # Request Dispatcher
│           └── LibraryServer.java  # ServerSocket & Thread Pool
├── build.sh                        # Compilation Script
├── run-server.sh                   # Start Backend Server (8080)
├── run-client.sh                   # Start Web Portal (8081)
├── run-cli.sh                      # Start Interactive Terminal TUI
└── pom.xml                         # Maven Project Descriptor
```
