package com.bookbridge;

import java.util.Scanner;

public class Menu {

    static Scanner sc = new Scanner(System.in);

    public static void start() {

        while (true) {

            System.out.println("\n====================================");
            System.out.println("          BOOKBRIDGE");
            System.out.println(" Multi-Branch Library Management");
            System.out.println("====================================");

            System.out.println("1. Admin");
            System.out.println("2. User");
            System.out.println("3. Exit");

            System.out.print("\nEnter your choice : ");

            int choice = sc.nextInt();

            switch (choice) {

                case 1:
                    adminMenu();
                    break;

                case 2:
                    userMenu();
                    break;

                case 3:
                    System.out.println("\nThank you for using BookBridge!");
                    System.exit(0);

                default:
                    System.out.println("Invalid Choice!");
            }
        }
    }

    public static void adminMenu() {

        while (true) {

            System.out.println("\n========== ADMIN ==========");
          System.out.println("1. View Transfer Requests");
System.out.println("2. View Purchase Requests");
System.out.println("3. Add Book");
System.out.println("4. Back");

            System.out.print("Enter choice : ");

            int choice = sc.nextInt();

          switch (choice) {

    case 1:
        RequestService.viewTransferRequests();
        break;

    case 2:
        RequestService.viewPurchaseRequests();
        break;

    case 3:

        sc.nextLine();

        System.out.print("Book ID : ");
        int id = sc.nextInt();
        sc.nextLine();

        System.out.print("Book Title : ");
        String title = sc.nextLine();

        System.out.print("Author : ");
        String author = sc.nextLine();

        System.out.print("Available Copies : ");
        int copies = sc.nextInt();

        System.out.print("Branch ID (1-3): ");
        int branchId = sc.nextInt();

        LibraryService.addBook(
            new Book(id, title, author, copies, branchId)
        );

        break;

    case 4:
        return;

    default:
        System.out.println("Invalid Choice!");
}
        }
    }

    public static void userMenu() {

        sc.nextLine();

        System.out.print("\nEnter your Name : ");
        String name = sc.nextLine();

        System.out.println("\nSelect Branch");
        System.out.println("1. Guindy Library");
        System.out.println("2. Adyar Library");
        System.out.println("3. Velachery Library");

        System.out.print("Enter choice : ");

        int branch = sc.nextInt();

        while (true) {

            System.out.println("\n========== USER MENU ==========");
            System.out.println("1. View All Books");
            System.out.println("2. Search Book");
            System.out.println("3. Borrow Book");
            System.out.println("4. Return Book");
            System.out.println("5. Logout");

            System.out.print("Enter choice : ");

            int choice = sc.nextInt();

            switch (choice) {

                case 1:
                    LibraryService.viewAllBooks();
                    break;

             case 2:

    sc.nextLine();

    System.out.print("Enter Book Name : ");
    String book = sc.nextLine();

    int result = LibraryService.searchBook(book, branch);

    if (result == 2) {

        System.out.print("Do you want to request transfer? (Y/N): ");
        String ch = sc.nextLine();

        if (ch.equalsIgnoreCase("Y")) {
            RequestService.addTransferRequest(
                book,
                LibraryService.getBranchName(3),   // temporary
                LibraryService.getBranchName(branch)
            );
        }

    } else if (result == 3) {

        System.out.print("Do you want to request purchase? (Y/N): ");
        String ch = sc.nextLine();

        if (ch.equalsIgnoreCase("Y")) {
            RequestService.addPurchaseRequest(book);
        }
    }

    break;


              case 3:

    System.out.print("\nEnter Book ID : ");
    int bookId = sc.nextInt();

    LibraryService.borrowBook(bookId, branch);

    break;

               case 4:

    System.out.print("Enter Book ID : ");
    int returnId = sc.nextInt();

    LibraryService.returnBook(returnId, branch);

    break;

                case 5:
                    System.out.println("\nGoodbye " + name + "!");
                    return;

                default:
                    System.out.println("Invalid Choice!");
            }

        }
    }
}