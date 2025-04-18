DROP DATABASE IF EXISTS codeify;
CREATE DATABASE IF NOT EXISTS codeify;

USE codeify;

CREATE TABLE users (
   user_id INT PRIMARY KEY AUTO_INCREMENT,
   username VARCHAR(50) UNIQUE NOT NULL,
   email VARCHAR(100) UNIQUE NOT NULL,
   role ENUM('admin', 'user') DEFAULT 'user' NOT NULL,
   registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   password VARCHAR(255) NULL
);

ALTER TABLE users ADD COLUMN provider VARCHAR(50) DEFAULT 'local';


CREATE TABLE programming_languages (
                                       id INT AUTO_INCREMENT PRIMARY KEY,
                                       name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE questions (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           title VARCHAR(255) NOT NULL,
                           description TEXT NOT NULL,
                           programming_language_id INT,
                           question_type ENUM('CODING', 'LOGIC') NOT NULL,
                           difficulty ENUM('EASY', 'MEDIUM', 'HARD') NOT NULL,
                           starter_code TEXT NULL,
                           ai_solution_required BOOLEAN DEFAULT FALSE,
                           correct_answer TEXT NULL,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           FOREIGN KEY (programming_language_id) REFERENCES programming_languages(id) ON DELETE SET NULL
);

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

CREATE TABLE blog_posts (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            user_id INT NOT NULL,
                            title VARCHAR(255) NOT NULL,
                            content TEXT NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE comments (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          user_id INT NOT NULL,
                          blog_post_id INT NOT NULL,
                          comment_text TEXT NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                          FOREIGN KEY (blog_post_id) REFERENCES blog_posts(id) ON DELETE CASCADE
);

CREATE TABLE leaderboard (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    language_id INT NOT NULL,
    total_score INT DEFAULT 0,
    user_rank INT NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (language_id) REFERENCES programming_languages(id) ON DELETE CASCADE,
    UNIQUE (user_id, language_id)
);

-- Flyway Schema History Table
CREATE TABLE flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200),
    type VARCHAR(20),
    script VARCHAR(1000),
    checksum INT,
    installed_by VARCHAR(100),
    installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    execution_time INT,
    success BOOLEAN,
    PRIMARY KEY (installed_rank)
);

USE codeify;

INSERT INTO programming_languages (name)
VALUES
    ('Java'),
    ('JavaScript'),
    ('MySQL');

INSERT INTO questions (
    title,
    description,
    programming_language_id,
    question_type,
    difficulty,
    starter_code,
    ai_solution_required,
    correct_answer
)
VALUES
    (
        'Even or Odd',
        'Write a function that determines if a given number is even or odd. Return true if the number is even, and false if it is odd.',
        1, -- Java
        'CODING',
        'EASY',
        'public class Solution {
    public boolean isEven(int number) {
        // Your code here
        return false;
    }
}',
        true,
        NULL
    ),
    (
        'Reverse Array',
        'Write a function that reverses an array of integers. For example, input [1,2,3,4] should return [4,3,2,1].',
        1,
        'CODING',
        'MEDIUM',
        'public class Solution {
    public int[] reverseArray(int[] arr) {
        // Your code here
        return arr;
    }
}',
        true,
        NULL
    ),
    (
        'Find Missing Number',
        'Given an array containing n distinct numbers taken from 0 to n, find the missing number. For example, given [3,0,1] return 2.',
        1,
        'CODING',
        'HARD',
        'public class Solution {
    public int findMissing(int[] nums) {
        // Your code here
        return 0;
    }
}',
        true,
        NULL
    );

INSERT INTO questions (
    title,
    description,
    programming_language_id,
    question_type,
    difficulty,
    starter_code,
    ai_solution_required,
    correct_answer
)
VALUES
    (
        'Boolean Data Type',
        'In Java, which primitive data type is used to hold the values true or false?',
        1,
        'LOGIC',
        'EASY',
        NULL,
        false,
        'boolean'
    ),
    (
        'Default Boolean Value',
        'What is the default value of a boolean instance variable in Java when it is not explicitly initialized?',
        1,
        'LOGIC',
        'EASY',
        NULL,
        false,
        'false'
    );

INSERT INTO questions (
    title,
    description,
    programming_language_id,
    question_type,
    difficulty,
    starter_code,
    ai_solution_required,
    correct_answer
)
VALUES
    (
        'Count Vowels',
        'Write a function that counts the number of vowels (a, e, i, o, u) in a given string. The string will only contain lowercase letters.',
        2,
        'CODING',
        'EASY',
        'function countVowels(str) {
    // Your code here
    return 0;
}',
        true,
        NULL
    ),
    (
        'Find Duplicates',
        'Write a function that finds all duplicate numbers in an array. For example, given [1,2,3,2,4,3], return [2,3].',
        2,
        'CODING',
        'MEDIUM',
        'function findDuplicates(arr) {
    // Your code here
    return [];
}',
        true,
        NULL
    ),
    (
        'Flatten Array',
        'Write a function that flattens a nested array. For example, given [1,[2,[3,4]],5], return [1,2,3,4,5].',
        2,
        'CODING',
        'HARD',
        'function flattenArray(arr) {
    // Your code here
    return [];
}',
        true,
        NULL
    );

INSERT INTO questions (
    title,
    description,
    programming_language_id,
    question_type,
    difficulty,
    starter_code,
    ai_solution_required,
    correct_answer
)
VALUES
    (
        'Single-line Comment Syntax',
        'In JavaScript, what symbol is used to indicate a single-line comment?',
        2,
        'LOGIC',
        'EASY',
        NULL,
        false,
        '//'
    ),
    (
        'Boolean Data Type in JavaScript',
        'What is the data type used to represent true or false values in JavaScript?',
        2,
        'LOGIC',
        'EASY',
        NULL,
        false,
        'boolean'
    );

INSERT INTO questions (
    title,
    description,
    programming_language_id,
    question_type,
    difficulty,
    starter_code,
    ai_solution_required,
    correct_answer
)
VALUES
    (
        'Select All Employees',
        'Write a query to select all employees ordered by their salary in descending order. Return the employee name and salary.',
        3,
        'CODING',
        'EASY',
        '/* Write your MySQL query statement below */
-- Given table:
-- employees (id, name, salary)
SELECT name, salary FROM employees ORDER BY salary DESC;',
        true,
        NULL
    ),
    (
        'Department Averages',
        'Write a query to find the average salary for each department. Return the department name and the average salary, ordered by average salary descending.',
        3,
        'CODING',
        'MEDIUM',
        '/* Write your MySQL query statement below */
-- Given tables:
-- employees (id, name, salary, department_id)
-- departments (id, name)
SELECT d.name AS department_name, AVG(e.salary) AS average_salary
FROM employees e
JOIN departments d ON e.department_id = d.id
GROUP BY d.name
ORDER BY average_salary DESC;',
        true,
        NULL
    ),
    (
        'Active Users',
        'Write a query to find users who logged in for 3 or more consecutive days. Return the user_id and the start date of their streak.',
        3,
        'CODING',
        'HARD',
        '/* Write your MySQL query statement below */
-- Given table:
-- logins (user_id, login_date)
SELECT user_id, MIN(login_date) AS streak_start
FROM (
    SELECT user_id, login_date,
           DATE_SUB(login_date, INTERVAL ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY login_date) DAY) AS grp
    FROM logins
) AS grouped
GROUP BY user_id, grp
HAVING COUNT(*) >= 3;',
        true,
        NULL
    );

INSERT INTO questions (
    title,
    description,
    programming_language_id,
    question_type,
    difficulty,
    starter_code,
    ai_solution_required,
    correct_answer
)
VALUES
    (
        'Remove Duplicate Rows',
        'Which SQL keyword is used to remove duplicate rows from the results of a SELECT query?',
        3,
        'LOGIC',
        'EASY',
        NULL,
        false,
        'DISTINCT'
    ),
    (
        'Wildcard in SQL LIKE Clause',
        'In SQL, what symbol is used as a wildcard in the LIKE clause to match any sequence of characters?',
        3,
        'LOGIC',
        'EASY',
        NULL,
        false,
        '%'
    );

DROP DATABASE IF EXISTS codeify_test;
CREATE DATABASE IF NOT EXISTS codeify_test;

USE codeify_test;

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

CREATE TABLE programming_languages (
                                       id INT AUTO_INCREMENT PRIMARY KEY,
                                       name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE questions (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           title VARCHAR(255) NOT NULL,
                           description TEXT NOT NULL,
                           programming_language_id INT,
                           question_type ENUM('CODING', 'LOGIC') NOT NULL,
                           difficulty ENUM('EASY', 'MEDIUM', 'HARD') NOT NULL,

                           starter_code TEXT NULL,
                           ai_solution_required BOOLEAN DEFAULT FALSE,
                           correct_answer TEXT NULL,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           FOREIGN KEY (programming_language_id) REFERENCES programming_languages(id) ON DELETE SET NULL
);

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

CREATE TABLE leaderboard (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             user_id INT NOT NULL UNIQUE,
                             total_score INT DEFAULT 0,
                             last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE blog_posts (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            user_id INT NOT NULL,
                            title VARCHAR(255) NOT NULL,
                            content TEXT NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE comments (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          user_id INT NOT NULL,
                          blog_post_id INT NOT NULL,
                          comment_text TEXT NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                          FOREIGN KEY (blog_post_id) REFERENCES blog_posts(id) ON DELETE CASCADE
);

CREATE TABLE flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200),
    type VARCHAR(20),
    script VARCHAR(1000),
    checksum INT,
    installed_by VARCHAR(100),
    installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    execution_time INT,
    success BOOLEAN,
    PRIMARY KEY (installed_rank)
);