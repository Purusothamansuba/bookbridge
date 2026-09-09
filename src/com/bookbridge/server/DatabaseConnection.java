package com.bookbridge.server;

import com.bookbridge.model.Book;
import com.bookbridge.model.Branch;
import com.bookbridge.model.PurchaseRequest;
import com.bookbridge.model.TransferRequest;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseConnection {

    private static String dbHost = "localhost";
    private static int dbPort = 3306;
    private static String dbName = "bookbridge";
    private static String dbUser = "root";
    private static String dbPassword = "your_password";

    private static boolean useFallbackInMemory = false;
    private static boolean initialized = false;

    // --- IN-MEMORY FALLBACK DATA STORE (Thread-Safe) ---
    private static final Map<Integer, Branch> memoryBranches = new ConcurrentHashMap<>();
    private static final Map<Integer, Book> memoryBooks = new ConcurrentHashMap<>();
    private static final List<TransferRequest> memoryTransferRequests = new CopyOnWriteArrayList<>();
    private static final List<PurchaseRequest> memoryPurchaseRequests = new CopyOnWriteArrayList<>();
    private static final AtomicInteger transferIdGen = new AtomicInteger(100);
    private static final AtomicInteger purchaseIdGen = new AtomicInteger(100);

    static {
        loadConfig();
        initDataSource();
    }

    private static void loadConfig() {
        Properties props = new Properties();
        File configFile = new File("config/db.properties");
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
                dbHost = props.getProperty("db.host", dbHost);
                dbPort = Integer.parseInt(props.getProperty("db.port", String.valueOf(dbPort)));
                dbName = props.getProperty("db.name", dbName);
                dbUser = props.getProperty("db.user", dbUser);
                dbPassword = props.getProperty("db.password", dbPassword);
            } catch (Exception e) {
                System.err.println("[Database] Notice: Could not read config/db.properties, using defaults.");
            }
        }

        // Environment variable overrides
        if (System.getenv("DB_HOST") != null) dbHost = System.getenv("DB_HOST");
        if (System.getenv("DB_PORT") != null) {
            try { dbPort = Integer.parseInt(System.getenv("DB_PORT")); } catch (Exception ignored) {}
        }
        if (System.getenv("DB_NAME") != null) dbName = System.getenv("DB_NAME");
        if (System.getenv("DB_USER") != null) dbUser = System.getenv("DB_USER");
        if (System.getenv("DB_PASSWORD") != null) dbPassword = System.getenv("DB_PASSWORD");
    }

    private static synchronized void initDataSource() {
        if (initialized) return;

        String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", dbHost, dbPort, dbName);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection testConn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
                useFallbackInMemory = false;
                System.out.println("✅ [Database] Successfully connected to MySQL at " + jdbcUrl);
                initMySqlSchema(testConn);
            }
        } catch (Exception e) {
            useFallbackInMemory = true;
            System.out.println("⚠️  [Database] MySQL unavailable (" + e.getMessage() + ").");
            System.out.println("🚀 [Database] Seamlessly initialized High-Speed In-Memory Data Store with seeded catalog.");
            seedInMemoryData();
        }
        initialized = true;
    }

    private static void initMySqlSchema(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS branches (branch_id INT PRIMARY KEY, branch_name VARCHAR(100), location VARCHAR(100))");
            stmt.execute("CREATE TABLE IF NOT EXISTS books (book_id INT PRIMARY KEY, title VARCHAR(100), author VARCHAR(100), available_copies INT, branch_id INT, category VARCHAR(100) DEFAULT 'Computer Science')");
            stmt.execute("CREATE TABLE IF NOT EXISTS transfer_requests (id INT AUTO_INCREMENT PRIMARY KEY, book_name VARCHAR(100), from_branch VARCHAR(100), to_branch VARCHAR(100), requester_name VARCHAR(100) DEFAULT 'Member', status VARCHAR(50) DEFAULT 'PENDING', request_date VARCHAR(50))");
            stmt.execute("CREATE TABLE IF NOT EXISTS purchase_requests (id INT AUTO_INCREMENT PRIMARY KEY, book_name VARCHAR(100), author VARCHAR(100) DEFAULT 'Unknown', requester_name VARCHAR(100) DEFAULT 'Member', status VARCHAR(50) DEFAULT 'PENDING', request_date VARCHAR(50))");

            // Seed branches if empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM branches");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate("INSERT INTO branches VALUES (1, 'Guindy Library', 'Guindy'), (2, 'Adyar Library', 'Adyar'), (3, 'Velachery Library', 'Velachery')");
                stmt.executeUpdate("INSERT INTO books (book_id, title, author, available_copies, branch_id, category) VALUES " +
                        "(101, 'Clean Code', 'Robert C. Martin', 5, 1, 'Software Engineering'), " +
                        "(102, 'Java: The Complete Reference', 'Herbert Schildt', 3, 1, 'Programming'), " +
                        "(103, 'Data Structures & Algorithms', 'Mark Allen Weiss', 4, 2, 'Computer Science'), " +
                        "(104, 'Operating System Concepts', 'Silberschatz & Galvin', 2, 2, 'Systems'), " +
                        "(105, 'Computer Networks', 'Andrew S. Tanenbaum', 6, 3, 'Networking'), " +
                        "(106, 'Database System Concepts', 'Korth & Sudarshan', 1, 3, 'Databases'), " +
                        "(107, 'Design Patterns: Elements of Reusable Object-Oriented Software', 'Gang of Four', 4, 1, 'Architecture'), " +
                        "(108, 'Designing Data-Intensive Applications', 'Martin Kleppmann', 3, 2, 'Distributed Systems')");
            }
        } catch (SQLException e) {
            System.err.println("[Database] Schema init notice: " + e.getMessage());
        }
    }

    private static void seedInMemoryData() {
        memoryBranches.put(1, new Branch(1, "Guindy Library", "Guindy"));
        memoryBranches.put(2, new Branch(2, "Adyar Library", "Adyar"));
        memoryBranches.put(3, new Branch(3, "Velachery Library", "Velachery"));

        Book[] seedBooks = new Book[] {
            new Book(101, "Clean Code", "Robert C. Martin", 5, 1, "Software Engineering"),
            new Book(102, "Java: The Complete Reference", "Herbert Schildt", 3, 1, "Programming"),
            new Book(103, "Data Structures & Algorithms", "Mark Allen Weiss", 4, 2, "Computer Science"),
            new Book(104, "Operating System Concepts", "Silberschatz & Galvin", 2, 2, "Systems"),
            new Book(105, "Computer Networks", "Andrew S. Tanenbaum", 6, 3, "Networking"),
            new Book(106, "Database System Concepts", "Korth & Sudarshan", 1, 3, "Databases"),
            new Book(107, "Design Patterns (GoF)", "Erich Gamma et al.", 4, 1, "Architecture"),
            new Book(108, "Designing Data-Intensive Applications", "Martin Kleppmann", 3, 2, "Distributed Systems")
        };

        for (Book b : seedBooks) {
            Branch br = memoryBranches.get(b.getBranchId());
            if (br != null) b.setBranchName(br.getBranchName());
            memoryBooks.put(b.getBookId(), b);
        }

        memoryTransferRequests.add(new TransferRequest(transferIdGen.incrementAndGet(), "Clean Code", "Guindy Library", "Adyar Library", "Alice", "PENDING", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())));
        memoryPurchaseRequests.add(new PurchaseRequest(purchaseIdGen.incrementAndGet(), "Refactoring: Improving the Design of Existing Code", "Martin Fowler", "Bob", "PENDING", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())));
    }

    public static Connection getConnection() throws SQLException {
        if (useFallbackInMemory) return null;
        String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", dbHost, dbPort, dbName);
        return DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
    }

    // =========================================================================
    // HIGH-LEVEL THREAD-SAFE DATA ACCESS METHODS (Support MySQL & In-Memory)
    // =========================================================================

    public static List<Book> getAllBooks() {
        List<Book> list = new ArrayList<>();
        if (useFallbackInMemory) {
            for (Book b : memoryBooks.values()) {
                Branch br = memoryBranches.get(b.getBranchId());
                b.setBranchName(br != null ? br.getBranchName() : "Branch " + b.getBranchId());
                list.add(b);
            }
            list.sort(Comparator.comparingInt(Book::getBookId));
            return list;
        }

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT b.*, br.branch_name FROM books b LEFT JOIN branches br ON b.branch_id = br.branch_id ORDER BY b.book_id ASC")) {
            while (rs.next()) {
                Book book = new Book(
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getInt("available_copies"),
                    rs.getInt("branch_id"),
                    rs.getString("category")
                );
                book.setBranchName(rs.getString("branch_name"));
                list.add(book);
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] getAllBooks: " + e.getMessage());
        }
        return list;
    }

    public static List<Book> searchBooks(String query, Integer branchId) {
        List<Book> all = getAllBooks();
        List<Book> filtered = new ArrayList<>();
        String q = (query != null) ? query.trim().toLowerCase() : "";

        for (Book b : all) {
            boolean matchesQuery = q.isEmpty() ||
                    b.getTitle().toLowerCase().contains(q) ||
                    b.getAuthor().toLowerCase().contains(q) ||
                    (b.getCategory() != null && b.getCategory().toLowerCase().contains(q)) ||
                    String.valueOf(b.getBookId()).equals(q);

            boolean matchesBranch = (branchId == null || branchId <= 0 || b.getBranchId() == branchId);

            if (matchesQuery && matchesBranch) {
                filtered.add(b);
            }
        }
        return filtered;
    }

    public static Book getBookById(int bookId) {
        if (useFallbackInMemory) {
            return memoryBooks.get(bookId);
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT b.*, br.branch_name FROM books b LEFT JOIN branches br ON b.branch_id = br.branch_id WHERE b.book_id = ?")) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Book b = new Book(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("available_copies"),
                        rs.getInt("branch_id"),
                        rs.getString("category")
                    );
                    b.setBranchName(rs.getString("branch_name"));
                    return b;
                }
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] getBookById: " + e.getMessage());
        }
        return null;
    }

    public static synchronized String borrowBook(int bookId, int userBranchId) throws Exception {
        if (useFallbackInMemory) {
            Book book = memoryBooks.get(bookId);
            if (book == null) throw new Exception("Book ID #" + bookId + " does not exist.");
            if (book.getBranchId() != userBranchId) {
                Branch targetBranch = memoryBranches.get(book.getBranchId());
                String targetName = targetBranch != null ? targetBranch.getBranchName() : "Branch " + book.getBranchId();
                throw new Exception("Book is located at '" + targetName + "', not your current branch.");
            }
            if (book.getAvailableCopies() <= 0) {
                throw new Exception("All copies of '" + book.getTitle() + "' are currently borrowed.");
            }
            book.setAvailableCopies(book.getAvailableCopies() - 1);
            return "Remaining Copies: " + book.getAvailableCopies();
        }

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement checkStmt = conn.prepareStatement("SELECT available_copies, branch_id, title FROM books WHERE book_id = ? FOR UPDATE");
                checkStmt.setInt(1, bookId);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    throw new Exception("Book ID #" + bookId + " not found.");
                }

                int copies = rs.getInt("available_copies");
                int branch = rs.getInt("branch_id");
                String title = rs.getString("title");

                if (branch != userBranchId) {
                    conn.rollback();
                    throw new Exception("Book is available at Branch #" + branch + ", not your branch.");
                }
                if (copies <= 0) {
                    conn.rollback();
                    throw new Exception("No copies of '" + title + "' available.");
                }

                PreparedStatement updateStmt = conn.prepareStatement("UPDATE books SET available_copies = available_copies - 1 WHERE book_id = ?");
                updateStmt.setInt(1, bookId);
                updateStmt.executeUpdate();
                conn.commit();
                return "Remaining Copies: " + (copies - 1);
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    public static synchronized void returnBook(int bookId, int userBranchId) throws Exception {
        if (useFallbackInMemory) {
            Book book = memoryBooks.get(bookId);
            if (book == null) throw new Exception("Book ID #" + bookId + " does not exist.");
            if (book.getBranchId() != userBranchId) {
                throw new Exception("This book belongs to Branch #" + book.getBranchId() + ", cannot return to Branch #" + userBranchId + ".");
            }
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            return;
        }

        try (Connection conn = getConnection()) {
            PreparedStatement retStmt = conn.prepareStatement("UPDATE books SET available_copies = available_copies + 1 WHERE book_id = ? AND branch_id = ?");
            retStmt.setInt(1, bookId);
            retStmt.setInt(2, userBranchId);
            int rows = retStmt.executeUpdate();
            if (rows == 0) {
                throw new Exception("Invalid Book ID or Book does not belong to this branch.");
            }
        }
    }

    public static synchronized void addBook(Book book) throws Exception {
        if (useFallbackInMemory) {
            if (memoryBooks.containsKey(book.getBookId())) {
                throw new Exception("Book with ID #" + book.getBookId() + " already exists.");
            }
            Branch br = memoryBranches.get(book.getBranchId());
            if (br != null) book.setBranchName(br.getBranchName());
            memoryBooks.put(book.getBookId(), book);
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO books (book_id, title, author, available_copies, branch_id, category) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, book.getBookId());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setInt(4, book.getAvailableCopies());
            ps.setInt(5, book.getBranchId());
            ps.setString(6, book.getCategory());
            ps.executeUpdate();
        }
    }

    public static synchronized void deleteBook(int bookId) throws Exception {
        if (useFallbackInMemory) {
            if (memoryBooks.remove(bookId) == null) {
                throw new Exception("Book ID #" + bookId + " not found.");
            }
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM books WHERE book_id = ?")) {
            ps.setInt(1, bookId);
            int count = ps.executeUpdate();
            if (count == 0) throw new Exception("Book ID #" + bookId + " not found.");
        }
    }

    public static synchronized void updateBook(Book book) throws Exception {
        if (useFallbackInMemory) {
            if (!memoryBooks.containsKey(book.getBookId())) {
                throw new Exception("Book ID #" + book.getBookId() + " not found.");
            }
            Branch br = memoryBranches.get(book.getBranchId());
            if (br != null) book.setBranchName(br.getBranchName());
            memoryBooks.put(book.getBookId(), book);
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE books SET title = ?, author = ?, available_copies = ?, branch_id = ?, category = ? WHERE book_id = ?")) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setInt(3, book.getAvailableCopies());
            ps.setInt(4, book.getBranchId());
            ps.setString(5, book.getCategory());
            ps.setInt(6, book.getBookId());
            ps.executeUpdate();
        }
    }

    public static List<Branch> getBranches() {
        List<Branch> list = new ArrayList<>();
        if (useFallbackInMemory) {
            list.addAll(memoryBranches.values());
            list.sort(Comparator.comparingInt(Branch::getBranchId));
            return list;
        }

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM branches ORDER BY branch_id ASC")) {
            while (rs.next()) {
                list.add(new Branch(rs.getInt("branch_id"), rs.getString("branch_name"), rs.getString("location")));
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] getBranches: " + e.getMessage());
        }
        return list;
    }

    public static String getBranchName(int branchId) {
        if (useFallbackInMemory) {
            Branch b = memoryBranches.get(branchId);
            return b != null ? b.getBranchName() : "Branch " + branchId;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT branch_name FROM branches WHERE branch_id = ?")) {
            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("branch_name");
            }
        } catch (SQLException ignored) {}
        return "Branch " + branchId;
    }

    public static synchronized void addTransferRequest(TransferRequest tr) {
        if (useFallbackInMemory) {
            tr.setId(transferIdGen.incrementAndGet());
            memoryTransferRequests.add(0, tr);
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO transfer_requests (book_name, from_branch, to_branch, requester_name, status, request_date) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, tr.getBookName());
            ps.setString(2, tr.getFromBranch());
            ps.setString(3, tr.getToBranch());
            ps.setString(4, tr.getRequesterName());
            ps.setString(5, tr.getStatus());
            ps.setString(6, tr.getRequestDate());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Database Error] addTransferRequest: " + e.getMessage());
        }
    }

    public static List<TransferRequest> getTransferRequests() {
        if (useFallbackInMemory) {
            return new ArrayList<>(memoryTransferRequests);
        }

        List<TransferRequest> list = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM transfer_requests ORDER BY id DESC")) {
            while (rs.next()) {
                list.add(new TransferRequest(
                    rs.getInt("id"),
                    rs.getString("book_name"),
                    rs.getString("from_branch"),
                    rs.getString("to_branch"),
                    rs.getString("requester_name"),
                    rs.getString("status"),
                    rs.getString("request_date")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] getTransferRequests: " + e.getMessage());
        }
        return list;
    }

    public static synchronized void updateTransferStatus(int id, String status) {
        if (useFallbackInMemory) {
            for (TransferRequest tr : memoryTransferRequests) {
                if (tr.getId() == id) {
                    tr.setStatus(status);
                    break;
                }
            }
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE transfer_requests SET status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Database Error] updateTransferStatus: " + e.getMessage());
        }
    }

    public static synchronized void addPurchaseRequest(PurchaseRequest pr) {
        if (useFallbackInMemory) {
            pr.setId(purchaseIdGen.incrementAndGet());
            memoryPurchaseRequests.add(0, pr);
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO purchase_requests (book_name, author, requester_name, status, request_date) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, pr.getBookName());
            ps.setString(2, pr.getAuthor());
            ps.setString(3, pr.getRequesterName());
            ps.setString(4, pr.getStatus());
            ps.setString(5, pr.getRequestDate());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Database Error] addPurchaseRequest: " + e.getMessage());
        }
    }

    public static List<PurchaseRequest> getPurchaseRequests() {
        if (useFallbackInMemory) {
            return new ArrayList<>(memoryPurchaseRequests);
        }

        List<PurchaseRequest> list = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM purchase_requests ORDER BY id DESC")) {
            while (rs.next()) {
                list.add(new PurchaseRequest(
                    rs.getInt("id"),
                    rs.getString("book_name"),
                    rs.getString("author"),
                    rs.getString("requester_name"),
                    rs.getString("status"),
                    rs.getString("request_date")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] getPurchaseRequests: " + e.getMessage());
        }
        return list;
    }

    public static synchronized void updatePurchaseStatus(int id, String status) {
        if (useFallbackInMemory) {
            for (PurchaseRequest pr : memoryPurchaseRequests) {
                if (pr.getId() == id) {
                    pr.setStatus(status);
                    break;
                }
            }
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE purchase_requests SET status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Database Error] updatePurchaseStatus: " + e.getMessage());
        }
    }

    public static Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<Book> books = getAllBooks();
        int totalTitles = books.size();
        int totalCopies = 0;
        for (Book b : books) totalCopies += b.getAvailableCopies();

        int totalBranches = getBranches().size();
        int pendingTransfers = 0;
        for (TransferRequest tr : getTransferRequests()) {
            if ("PENDING".equalsIgnoreCase(tr.getStatus())) pendingTransfers++;
        }
        int pendingPurchases = 0;
        for (PurchaseRequest pr : getPurchaseRequests()) {
            if ("PENDING".equalsIgnoreCase(pr.getStatus())) pendingPurchases++;
        }

        stats.put("totalTitles", totalTitles);
        stats.put("totalCopies", totalCopies);
        stats.put("totalBranches", totalBranches);
        stats.put("pendingTransfers", pendingTransfers);
        stats.put("pendingPurchases", pendingPurchases);
        stats.put("isInMemory", useFallbackInMemory);
        return stats;
    }

    public static void closeConnection() {
        // Cleanup resources
    }
}
