# 📚 BookBridge — Distributed Multi-Branch Library Management System

BookBridge is a modern, high-concurrency distributed Library Management System in Java. It unites physical library branches (e.g., *Guindy*, *Adyar*, *Velachery*) under a single catalog with real-time book borrowing, inventory tracking, inter-branch transfer workflows, and new book acquisition requests.

---

## ✨ Key Features

- **🌐 Modern Glassmorphic Web Portal (Port 8081)**:
  - Responsive web interface with dark aesthetics, glowing accents, and typography.
  - Member Dashboard: Browse books, real-time search & category filters, 1-click Borrowing, Return modal, and Inter-Branch Transfer requests.
  - Librarian & Admin Console: Manage catalog inventory (Add/Delete/Stock), Approve/Reject Transfer requests, and Review/Order Purchase suggestions.
  - Real-time toast notifications and status badges.
- **🖥️ Interactive Terminal TUI**:
  - Full ANSI box drawing and cursor control with raw POSIX `stty` terminal navigation.
- **⚡ Hardened Concurrency & High Performance**:
  - Multi-threaded TCP Socket Server (Port 8080) with fixed `ExecutorService` thread pool.
  - Thread-safe transaction handling for atomic book borrows and returns.
  - Graceful shutdown hooks with clean resource recycling.
- **🛡️ Resilient Persistence & Zero-Friction Setup**:
  - Connects to MySQL with configurable settings via `config/db.properties` or environment variables (`DB_HOST`, `DB_USER`, `DB_PASSWORD`).
  - **Auto-Fallback In-Memory Store**: If MySQL is offline, the system seamlessly initializes a thread-safe in-memory database with pre-seeded branches and books.

---

## 🏗️ Architecture

```mermaid
graph TD
    Browser["🌐 Web Browser (http://localhost:8081)"] -->|HTTP / HTML5| WebUI["WebUI (HttpServer)"]
    Terminal["🖥️ Terminal Console"] -->|ANSI / stty| Menu["Menu & TerminalUI"]

    WebUI -->|Java Service API| NetClient["NetworkClient (TCP Client)"]
    Menu -->|Java Service API| NetClient

    NetClient -->|TCP Socket / NetworkMessage (Port 8080)| LibServer["LibraryServer (ExecutorService)"]
    LibServer --> ClientHandler["ClientHandler Worker"]
    ClientHandler --> DBManager["DatabaseConnection Manager"]

    DBManager -->|Primary JDBC| MySQL[("MySQL Database Engine")]
    DBManager -.->|Zero-Config Fallback| MemoryStore[("In-Memory Data Store")]
```

---

## 🚀 Quick Start

### 1. Build the Project
```bash
bash build.sh
```

### 2. Start the Backend Server
```bash
bash run-server.sh
```
*The server will start on TCP port `8080`.*

### 3. Start the Web Portal & Client
In a new terminal window:
```bash
bash run-client.sh
```
*Open your browser and navigate to [http://localhost:8081](http://localhost:8081).*

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
*You can also set environment variables:* `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.

---

## 📁 Project Structure

```
.
├── config/
│   └── db.properties               # Database connection settings
├── src/
│   └── com/bookbridge/
│       ├── bookbridge.sql          # MySQL Schema & Initial Data
│       ├── client/                 # Client Layer
│       │   ├── Main.java           # Client Entry Point
│       │   ├── NetworkClient.java  # Thread-safe TCP Socket Client
│       │   ├── service/            # Business Logic Services
│       │   │   ├── LibraryService.java
│       │   │   └── RequestService.java
│       │   └── ui/                 # UI Engines
│       │       ├── Input.java      # Raw Terminal Key Reader
│       │       ├── Menu.java       # CLI Navigation
│       │       ├── TerminalUI.java # ANSI Box Renderer
│       │       └── WebUI.java      # Modern Glassmorphic Web Server
│       ├── model/                  # Domain Entities
│       │   ├── Book.java
│       │   ├── Branch.java
│       │   ├── PurchaseRequest.java
│       │   └── TransferRequest.java
│       ├── network/
│       │   └── NetworkMessage.java # Serialized IPC Protocol
│       └── server/                 # Backend Server Layer
│           ├── DatabaseConnection.java # Connection Pool & Data Layer
│           ├── ClientHandler.java  # Request Dispatcher
│           └── LibraryServer.java  # ServerSocket & Thread Pool
├── build.sh                        # Compilation Script
├── run-server.sh                   # Launch Backend Server
├── run-client.sh                   # Launch Web Portal (Port 8081)
├── run-cli.sh                      # Launch Terminal TUI
└── pom.xml                         # Maven Configuration
```
