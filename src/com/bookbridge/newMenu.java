package com.bookbridge;

public class newMenu {

    private static final String[] MAIN = { "Admin", "User", "Exit" };

    private static final String[] ADMIN = {
        "View Transfer Requests",
        "View Purchase Requests",
        "Add Book",
        "Back",
    };

    private static final String[] USER = {
        "View All Books",
        "Search Book",
        "Borrow Book",
        "Return Book",
        "Logout",
    };

    public static void start() throws Exception {
        while (true) {
            int choice = showMenu("BOOKBRIDGE", MAIN);

            switch (choice) {
                case 0:
                    adminMenu();
                    break;
                case 1:
                    userMenu();
                    break;
                case 2:
                    Input.disableRawMode();
                    TerminalUI.showCursor();
                    TerminalUI.clear();
                    System.exit(0);
            }
        }
    }

    private static void adminMenu() throws Exception {
        while (true) {
            int choice = showMenu("ADMIN", ADMIN);

            switch (choice) {
                case 0:
                    pause("Transfer Requests");
                    // RequestService.viewTransferRequests();
                    break;
                case 1:
                    pause("Purchase Requests");
                    // RequestService.viewPurchaseRequests();
                    break;
                case 2:
                    pause("Add Book");
                    // addBookScreen();
                    break;
                case 3:
                    return;
            }
        }
    }

    private static void userMenu() throws Exception {
        while (true) {
            int choice = showMenu("USER", USER);

            switch (choice) {
                case 0:
                    pause("View Books");
                    // LibraryService.viewAllBooks();
                    break;
                case 1:
                    pause("Search Book");
                    break;
                case 2:
                    pause("Borrow Book");
                    break;
                case 3:
                    pause("Return Book");
                    break;
                case 4:
                    return;
            }
        }
    }

    private static int showMenu(String title, String[] items) throws Exception {
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
                    Input.disableRawMode();
                    TerminalUI.showCursor();
                    TerminalUI.clear();
                    System.exit(0);
            }

            if (old != selected) {
                TerminalUI.updateSelection(items, old, selected);
            }
        }
    }

    private static void pause(String message) throws Exception {
        TerminalUI.reset();

        TerminalUI.init(message, "Press Enter to continue");

        TerminalUI.move(8, 5);
        System.out.print("This screen will be implemented next.");

        while (Input.readKey() != Input.ENTER) {}
    }
}
