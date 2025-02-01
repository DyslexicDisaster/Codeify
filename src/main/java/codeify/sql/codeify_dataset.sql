INSERT INTO programming_languages (name) VALUES ('Java');

-- Logic Question 1: Easy
INSERT INTO questions (
    title, description, programming_language_id, question_type, difficulty, correct_answer
) VALUES (
             'Which of the following is a valid Java data type for storing a true/false value?',
             'int, double, flout, boolean, char',
             NULL, 'LOGIC', 'EASY', 'boolean'
         );

-- Logic Question 2: Medium
INSERT INTO questions (
    title, description, programming_language_id, question_type, difficulty, correct_answer
) VALUES (
             'Two Sum Problem',
             'Given an array of integers and a target sum, find two numbers in the array that add up to the target.',
             NULL, 'LOGIC', 'MEDIUM', 'Use a hash map to store visited numbers and check if the complement exists.'
         );

-- Coding Question 1: Easy
INSERT INTO questions (
    title, description, programming_language_id, question_type, difficulty, starter_code, ai_solution_required
) VALUES (
             'Reverse a String',
             'Write a Java function to reverse a given string.',
             1, 'CODING', 'EASY',
             'public class Main {
                 public static String reverseString(String str) {
                     // Your code here
                 }
             }',
             TRUE
         );

-- Coding Question 2: Medium
INSERT INTO questions (
    title, description, programming_language_id, question_type, difficulty, starter_code, ai_solution_required
) VALUES (
             'Check for Palindrome',
             'Write a Java function to check if a given string is a palindrome. ' ||
             'a word, phrase, or sequence that reads the same backwards as forwards, e.g. racecar',
             1, 'CODING', 'MEDIUM',
             'public class Main {
                 public static boolean isPalindrome(String str) {
                     // Your code here
                 }
             }',
             TRUE
         );