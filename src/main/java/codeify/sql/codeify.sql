DROP DATABASE IF EXISTS codeify;
CREATE DATABASE IF NOT EXISTS codeify;

USE codeify;

-- Users Table
DROP TABLE IF EXISTS users;
CREATE TABLE users (
   user_id INT PRIMARY KEY AUTO_INCREMENT,
   username VARCHAR(50) UNIQUE NOT NULL,
   email VARCHAR(100) UNIQUE NOT NULL,
   password VARCHAR(255) NOT NULL,
   role ENUM('admin', 'user') DEFAULT 'user',
   registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   salt VARCHAR(255) NOT NULL
);

-- Programming Languages Table
CREATE TABLE programming_languages (
                                       id INT AUTO_INCREMENT PRIMARY KEY,
                                       name VARCHAR(50) NOT NULL UNIQUE -- Example: Java, MySQL, JavaScript
);

-- Questions Table (Supports Both Logic & Coding Questions)
CREATE TABLE questions (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           title VARCHAR(255) NOT NULL,
                           description TEXT NOT NULL,
                           programming_language_id INT, -- Nullable for logic questions
                           question_type ENUM('CODING', 'LOGIC') NOT NULL,
                           difficulty ENUM('EASY', 'MEDIUM', 'HARD') NOT NULL,

    -- Fields for Coding Questions
                           starter_code TEXT NULL, -- Pre-filled code snippet
                           ai_solution_required BOOLEAN DEFAULT FALSE, -- If TRUE, use AI API for validation

    -- Fields for Logic Questions
                           correct_answer TEXT NULL, -- Expected answer for logic questions

                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           FOREIGN KEY (programming_language_id) REFERENCES programming_languages(id) ON DELETE SET NULL
);

-- User Progress Table (Tracks Completion & Scores)
CREATE TABLE user_progress (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               user_id INT NOT NULL,
                               question_id INT NOT NULL,
                               status ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED') DEFAULT 'NOT_STARTED',
                               score INT DEFAULT 0,
                               last_attempt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                               FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

-- Leaderboard Table (Tracks Rankings)
CREATE TABLE leaderboard (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             user_id INT NOT NULL UNIQUE,
                             total_score INT DEFAULT 0,
                             last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Blog Posts Table (Community Discussion)
CREATE TABLE blog_posts (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            user_id INT NOT NULL,
                            title VARCHAR(255) NOT NULL,
                            content TEXT NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Comments Table (User Engagement)
CREATE TABLE comments (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          user_id INT NOT NULL,
                          blog_post_id INT NOT NULL,
                          comment_text TEXT NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                          FOREIGN KEY (blog_post_id) REFERENCES blog_posts(id) ON DELETE CASCADE
);
