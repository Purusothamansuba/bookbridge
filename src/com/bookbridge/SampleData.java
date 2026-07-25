package com.bookbridge;

import java.util.ArrayList;

public class SampleData {

    public static ArrayList<Book> books = new ArrayList<>();
    public static ArrayList<Branch> branches = new ArrayList<>();

    public static void loadData() {

        // Branches
        branches.add(new Branch(1, "Guindy Library", "Guindy"));
        branches.add(new Branch(2, "Adyar Library", "Adyar"));
        branches.add(new Branch(3, "Velachery Library", "Velachery"));

        // Books
        books.add(new Book(101, "Clean Code", "Robert C. Martin", 5, 1));
        books.add(new Book(102, "Java The Complete Reference", "Herbert Schildt", 3, 1));

        books.add(new Book(103, "Data Structures", "Mark Allen Weiss", 4, 2));
        books.add(new Book(104, "Operating System Concepts", "Galvin", 2, 2));

        books.add(new Book(105, "Computer Networks", "Forouzan", 6, 3));
        books.add(new Book(106, "Database System Concepts", "Korth", 1, 3));
    }
}