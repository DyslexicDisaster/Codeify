-- Insert programming languages
INSERT INTO programming_languages (name)
VALUES
    ('Java'),
    ('JavaScript'),
    ('MySQL');

--------------------------------------------------
-- sample coding questions for Java
--------------------------------------------------
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
        1,
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

--------------------------------------------------
-- logic questions for Java
--------------------------------------------------
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

--------------------------------------------------
-- sample coding questions for JavaScript
--------------------------------------------------
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

--------------------------------------------------
-- questions for JavaScript
--------------------------------------------------
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

--------------------------------------------------
-- sample coding questions for MySQL
--------------------------------------------------
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
SELECT ',
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
SELECT ',
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
SELECT ',
        true,
        NULL
    );

--------------------------------------------------
-- logic questions for MySQL
--------------------------------------------------
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
