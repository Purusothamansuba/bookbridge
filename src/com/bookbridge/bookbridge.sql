-- =====================================
-- BookBridge Database
-- =====================================

DROP DATABASE IF EXISTS bookbridge;

CREATE DATABASE bookbridge;

USE bookbridge;

-- =========================
-- Branch Table
-- =========================

CREATE TABLE branches (
    branch_id INT PRIMARY KEY,
    branch_name VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL
);

-- =========================
-- Book Table
-- =========================

CREATE TABLE books (
    book_id INT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    available_copies INT NOT NULL,
    branch_id INT NOT NULL,
    FOREIGN KEY (branch_id)
        REFERENCES branches(branch_id)
);

-- =========================
-- Purchase Requests
-- =========================

CREATE TABLE purchase_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    book_name VARCHAR(200) NOT NULL,
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- Transfer Requests
-- =========================

CREATE TABLE transfer_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    book_name VARCHAR(200) NOT NULL,
    from_branch INT NOT NULL,
    to_branch INT NOT NULL,
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (from_branch)
        REFERENCES branches(branch_id),
    FOREIGN KEY (to_branch)
        REFERENCES branches(branch_id)
);

-- =========================
-- Users
-- =========================

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(100),
    branch_id INT,
    FOREIGN KEY (branch_id)
        REFERENCES branches(branch_id)
);

-- =========================
-- Sample Branches
-- =========================

INSERT INTO branches VALUES
(1,'Guindy Library','Guindy'),
(2,'Adyar Library','Adyar'),
(3,'Velachery Library','Velachery');

-- =========================
-- Sample Books
-- =========================

INSERT INTO books VALUES
(101,'Clean Code','Robert C. Martin',5,1),
(102,'Java The Complete Reference','Herbert Schildt',3,1),
(103,'Data Structures','Mark Allen Weiss',4,2),
(104,'Operating System Concepts','Galvin',2,2),
(105,'Computer Networks','Forouzan',6,3),
(106,'Database System Concepts','Korth',1,3);

-- =========================
-- Sample Users
-- =========================

INSERT INTO users(user_name,branch_id) VALUES
('Rahul',1),
('Priya',2),
('Karthik',3);

-- =========================
-- Sample Purchase Requests
-- =========================

INSERT INTO purchase_requests(book_name) VALUES
('Artificial Intelligence'),
('Modern Operating Systems');

-- =========================
-- Sample Transfer Requests
-- =========================

INSERT INTO transfer_requests(book_name,from_branch,to_branch) VALUES
('Computer Networks',3,1),
('Operating System Concepts',2,3);
