CREATE DATABASE IF NOT EXISTS bookbridge;
USE bookbridge;

CREATE TABLE branches (
    branch_id INT PRIMARY KEY,
    branch_name VARCHAR(100),
    location VARCHAR(100)
);

CREATE TABLE books (
    book_id INT PRIMARY KEY,
    title VARCHAR(100),
    author VARCHAR(100),
    available_copies INT,
    branch_id INT,
    FOREIGN KEY (branch_id) REFERENCES branches(branch_id)
);

CREATE TABLE transfer_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    book_name VARCHAR(100),
    from_branch VARCHAR(100),
    to_branch VARCHAR(100)
);

CREATE TABLE purchase_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    book_name VARCHAR(100)
);

-- Insert Sample Data
INSERT INTO branches VALUES
(1, 'Guindy Library', 'Guindy'),
(2, 'Adyar Library', 'Adyar'),
(3, 'Velachery Library', 'Velachery');

INSERT INTO books VALUES
(101, 'Clean Code', 'Robert C. Martin', 5, 1),
(102, 'Java The Complete Reference', 'Herbert Schildt', 3, 1),
(103, 'Data Structures', 'Mark Allen Weiss', 4, 2),
(104, 'Operating System Concepts', 'Galvin', 2, 2),
(105, 'Computer Networks', 'Forouzan', 6, 3),
(106, 'Database System Concepts', 'Korth', 1, 3);
