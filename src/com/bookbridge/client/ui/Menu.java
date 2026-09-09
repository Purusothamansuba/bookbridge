package com.bookbridge.client.ui;

import com.bookbridge.client.service.LibraryService;
import com.bookbridge.client.service.RequestService;
import com.bookbridge.model.Book;
import com.bookbridge.model.Branch;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Menu {

    private static final String[] MAIN = { "Member Portal", "Admin Portal", "View System Stats", "Exit" };

    private static final String[] ADMIN = {
        "View All Books",
        "Add New Book",
        "View Transfer Requests",
        "View Purchase Requests",
        "Back"
    };

    private static final String[] USER = {
        "View All Books",
        "Search Book",
        "Borrow Book",
        "Return Book",
        "Request Purchase",
        "Logout"
    };

    private static final Scanner sc = new Scanner(System.in);

    public static void start() throws Exception {
        while (true) {
            int choice = showMenu("📚 BOOKBRIDGE DISTRIBUTED LIBRARY", MAIN);
            switch (choice) {
                case 0:
                    userMenu();
                    break;
                case 1:
                    adminMenu();
                    break;
                case 2:
                    viewStatsScreen();
                    break;
                case 3:
                    gracefulExit();
                    return;
            }
        }
    }

    private static void adminMenu() throws Exception {
        while (true) {
            int choice = showMenu("🛡️ ADMIN & LIBRARIAN CONSOLE", ADMIN);
            switch (choice) {
                case 0:
                    runScreenAction(LibraryService::viewAllBooks);
                    break;
                case 1:
                    addBookScreen();
                    break;
                case 2:
                    runScreenAction(RequestService::viewTransferRequests);
                    break;
                case 3:
                    runScreenAction(RequestService::viewPurchaseRequests);
                    break;
                case 4:
                    return;
            }
        }
    }

    private static void userMenu() throws Exception {
        Input.disableRawMode();
        TerminalUI.clear();
        TerminalUI.showCursor();

        System.out.print("\nEnter your Name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) name = "Reader";

        List<Branch> branches = LibraryService.getBranches();
        System.out.println("\nSelect Your Current Active Branch:");
        if (branches.isEmpty()) {
            System.out.println("1. Guindy Library\n2. Adyar Library\n3. Velachery Library");
        } else {
            for (Branch b : branches) {
                System.out.println(b.getBranchId() + ". " + b.getBranchName() + " (" + b.getLocation() + ")");
            }
        }
        System.out.print("Enter branch ID: ");

        int branch = readValidInt();

        while (true) {
            int choice = showMenu("👤 MEMBER: " + name.toUpperCase(), USER);
            switch (choice) {
                case 0:
                    runScreenAction(LibraryService::viewAllBooks);
                    break;
                case 1:
                    searchBookScreen(branch, name);
                    break;
                case 2:
                    borrowBookScreen(branch);
                    break;
                case 3:
                    returnBookScreen(branch);
                    break;
                case 4:
                    purchaseRequestScreen(name);
                    break;
                case 5:
                    return;
            }
        }
    }

    private static void addBookScreen() throws Exception {
        Input.disableRawMode();
        TerminalUI.clear();
        TerminalUI.showCursor();

        System.out.println("========== ➕ ADD NEW BOOK ==========");
        System.out.print("Book ID: ");
        int id = readValidInt();

        System.out.print("Book Title: ");
        String title = sc.nextLine().trim();

        System.out.print("Author: ");
        String author = sc.nextLine().trim();

        System.out.print("Category / Genre: ");
        String category = sc.nextLine().trim();

        System.out.print("Available Copies: ");
        int copies = readValidInt();

        System.out.print("Branch ID: ");
        int branchId = readValidInt();

        try {
            LibraryService.addBook(new Book(id, title, author, copies, branchId, category));
            System.out.println("\n✅ Book added successfully to catalog!");
        } catch (Exception e) {
            System.out.println("\n❌ Error: " + e.getMessage());
        }

        System.out.println("\nPress Enter to return to menu...");
        sc.nextLine();
    }

    private static void searchBookScreen(int branch, String userName) throws Exception {
        Input.disableRawMode();
        TerminalUI.clear();
        TerminalUI.showCursor();

        System.out.println("========== 🔍 SEARCH BOOK ==========");
        System.out.print("Enter Book Title / Author / Keyword: ");
        String bookName = sc.nextLine().trim();

        int result = LibraryService.searchBookCLI(bookName, branch);

        if (result == 2) {
            System.out.print("\nDo you want to request an Inter-Branch Transfer? (Y/N): ");
            if (sc.nextLine().trim().equalsIgnoreCase("Y")) {
                try {
                    RequestService.addTransferRequest(bookName, "Other Branch", LibraryService.getBranchName(branch), userName);
                    System.out.println("✅ Transfer request submitted!");
                } catch (Exception e) {
                    System.out.println("❌ Error: " + e.getMessage());
                }
            }
        } else if (result == 3) {
            System.out.print("\nDo you want to submit a Purchase Suggestion? (Y/N): ");
            if (sc.nextLine().trim().equalsIgnoreCase("Y")) {
                try {
                    RequestService.addPurchaseRequest(bookName, "Unknown", userName);
                    System.out.println("✅ Purchase request submitted!");
                } catch (Exception e) {
                    System.out.println("❌ Error: " + e.getMessage());
                }
            }
        }

        System.out.println("\nPress Enter to return to menu...");
        sc.nextLine();
    }

    private static void borrowBookScreen(int branch) throws Exception {
        Input.disableRawMode();
        TerminalUI.clear();
        TerminalUI.showCursor();

        System.out.println("========== 📖 BORROW BOOK ==========");
        System.out.print("Enter Book ID to borrow: ");
        int bookId = readValidInt();

        try {
            String res = LibraryService.borrowBook(bookId, branch);
            System.out.println("\n✅ Book borrowed successfully! " + res);
        } catch (Exception e) {
            System.out.println("\n❌ Failed to borrow book: " + e.getMessage());
        }

        System.out.println("\nPress Enter to return to menu...");
        sc.nextLine();
    }

    private static void returnBookScreen(int branch) throws Exception {
        Input.disableRawMode();
        TerminalUI.clear();
        TerminalUI.showCursor();

        System.out.println("========== 🔄 RETURN BOOK ==========");
        System.out.print("Enter Book ID to return: ");
        int bookId = readValidInt();

        try {
            LibraryService.returnBook(bookId, branch);
            System.out.println("\n✅ Book returned successfully to this branch!");
        } catch (Exception e) {
            System.out.println("\n❌ Failed to return book: " + e.getMessage());
        }

        System.out.println("\nPress Enter to return to menu...");
        sc.nextLine();
    }

    private static void purchaseRequestScreen(String userName) throws Exception {
        Input.disableRawMode();
        TerminalUI.clear();
        TerminalUI.showCursor();

        System.out.println("========== ✨ SUGGEST PURCHASE ==========");
        System.out.print("Book Title: ");
        String title = sc.nextLine().trim();

        System.out.print("Author (optional): ");
        String author = sc.nextLine().trim();

        try {
            RequestService.addPurchaseRequest(title, author.isEmpty() ? "Unknown" : author, userName);
            System.out.println("\n✅ Purchase request submitted successfully!");
        } catch (Exception e) {
            System.out.println("\n❌ Error: " + e.getMessage());
        }

        System.out.println("\nPress Enter to return to menu...");
        sc.nextLine();
    }

    private static void viewStatsScreen() throws Exception {
        runScreenAction(() -> {
            Map<String, Object> stats = LibraryService.getSystemStatistics();
            System.out.println("========== 📊 SYSTEM METRICS & STATS ==========");
            System.out.println("Total Book Titles   : " + stats.getOrDefault("totalTitles", 0));
            System.out.println("Total Book Copies   : " + stats.getOrDefault("totalCopies", 0));
            System.out.println("Connected Branches  : " + stats.getOrDefault("totalBranches", 0));
            System.out.println("Pending Transfers   : " + stats.getOrDefault("pendingTransfers", 0));
            System.out.println("Pending Purchases   : " + stats.getOrDefault("pendingPurchases", 0));
            System.out.println("Storage Engine      : " + (Boolean.TRUE.equals(stats.get("isInMemory")) ? "High-Speed In-Memory Store" : "MySQL Database Engine"));
        });
    }

    private static void runScreenAction(Runnable action) throws Exception {
        Input.disableRawMode();
        TerminalUI.clear();
        TerminalUI.showCursor();

        action.run();

        System.out.println("\nPress Enter to return to menu...");
        sc.nextLine();
    }

    private static int readValidInt() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a valid number: ");
            }
        }
    }

    private static int showMenu(String title, String[] items) throws Exception {
        Input.enableRawMode();
        TerminalUI.reset();
        TerminalUI.init(title, "↑↓ Move   Enter Select   q Quit");

        int selected = 0;
        TerminalUI.drawMenu(items, selected);

        while (true) {
            int old = selected;
            int key = Input.readKey();

            switch (key) {
                case Input.UP:
                    if (selected > 0) selected--;
                    break;
                case Input.DOWN:
                    if (selected < items.length - 1) selected++;
                    break;
                case Input.ENTER:
                    return selected;
                case Input.Q:
                    gracefulExit();
            }

            if (old != selected) {
                TerminalUI.updateSelection(items, old, selected);
            }
        }
    }

    private static void gracefulExit() {
        Input.disableRawMode();
        TerminalUI.showCursor();
        TerminalUI.clear();
        System.out.println("Thank you for using BookBridge!\n");
        System.exit(0);
    }
}
