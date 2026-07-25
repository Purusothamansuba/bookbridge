package com.bookbridge;

public class LibraryService {

    public static void viewAllBooks() {

        System.out.println("\n========== AVAILABLE BOOKS ==========\n");

        for (Book book : SampleData.books) {

            System.out.println(book);
            System.out.println("-------------------------------");
        }
    }
 public static int searchBook(String bookName, int userBranch) {

    boolean found = false;

    for (Book book : SampleData.books) {

        if (book.getTitle().equalsIgnoreCase(bookName)) {

            found = true;

            if (book.getBranchId() == userBranch) {

                System.out.println("\nBook Available in Your Branch!");
                System.out.println(book);

                return 1;

            } else {

                String branchName = "";

                for (Branch branch : SampleData.branches) {
                    if (branch.getBranchId() == book.getBranchId()) {
                        branchName = branch.getBranchName();
                        break;
                    }
                }

                System.out.println("\nBook is NOT available in your branch.");
                System.out.println("Available at : " + branchName);
                System.out.println("Branch ID : " + book.getBranchId());
                System.out.println("\nYou can request a transfer.");

                return 2;
            }
        }
    }

    System.out.println("\nBook not available in any branch.");
    System.out.println("You can request the library to purchase this book.");

    return 3;
}
public static void borrowBook(int bookId, int userBranch) {

    for (Book book : SampleData.books) {

        if (book.getBookId() == bookId) {

            if (book.getBranchId() != userBranch) {
                System.out.println("\nThis book is not available in your selected branch.");
                return;
            }

            if (book.getAvailableCopies() > 0) {

                book.setAvailableCopies(book.getAvailableCopies() - 1);

                System.out.println("\nBook Borrowed Successfully!");
                System.out.println("Remaining Copies : " + book.getAvailableCopies());

            } else {

                System.out.println("\nSorry! No copies available.");

            }

            return;
        }
    }

    System.out.println("\nInvalid Book ID!");
}
public static void returnBook(int bookId, int userBranch) {

    for (Book book : SampleData.books) {

        if (book.getBookId() == bookId && book.getBranchId() == userBranch) {

            book.setAvailableCopies(book.getAvailableCopies() + 1);

            System.out.println("\nBook Returned Successfully!");
            return;
        }
    }

    System.out.println("\nInvalid Book ID!");
}
public static void addBook(Book book) {

    SampleData.books.add(book);

    System.out.println("\nBook Added Successfully!");
}
public static String getBranchName(int branchId) {

    for (Branch branch : SampleData.branches) {
        if (branch.getBranchId() == branchId) {
            return branch.getBranchName();
        }
    }

    return "";
}
}