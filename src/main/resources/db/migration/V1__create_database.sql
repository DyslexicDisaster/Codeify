START TRANSACTION;

-- Users Table
CREATE TABLE IF NOT EXISTS users (
   user_id            INT               PRIMARY KEY AUTO_INCREMENT,
   username           VARCHAR(50)       UNIQUE NOT NULL,
   email              VARCHAR(100)      UNIQUE NOT NULL,
   role               ENUM('admin','user') NOT NULL DEFAULT 'user',
   registration_date  TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP,
   password           VARCHAR(255),
   provider           VARCHAR(50)       NOT NULL DEFAULT 'local'
);

-- Programming Languages Table
CREATE TABLE IF NOT EXISTS programming_languages (
   id    INT AUTO_INCREMENT PRIMARY KEY,
   name  VARCHAR(50) NOT NULL UNIQUE
);

-- Questions Table
CREATE TABLE IF NOT EXISTS questions (
   id                        INT AUTO_INCREMENT PRIMARY KEY,
   title                     VARCHAR(255) NOT NULL,
   description               TEXT         NOT NULL,
   programming_language_id   INT,
   question_type             ENUM('CODING','LOGIC') NOT NULL,
   difficulty                ENUM('EASY','MEDIUM','HARD') NOT NULL,
   starter_code              TEXT         NULL,
   ai_solution_required      BOOLEAN      NOT NULL DEFAULT FALSE,
   correct_answer            TEXT         NULL,
   created_at                TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
   FOREIGN KEY (programming_language_id)
     REFERENCES programming_languages(id) ON DELETE SET NULL
);

-- User Progress Table
CREATE TABLE IF NOT EXISTS user_progress (
   id           INT AUTO_INCREMENT PRIMARY KEY,
   user_id      INT NOT NULL,
   question_id  INT NOT NULL,
   status       ENUM('NOT_STARTED','IN_PROGRESS','COMPLETED') NOT NULL DEFAULT 'NOT_STARTED',
   score        INT NOT NULL DEFAULT 0,
   last_attempt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   FOREIGN KEY (user_id)     REFERENCES users(user_id)     ON DELETE CASCADE,
   FOREIGN KEY (question_id) REFERENCES questions(id)       ON DELETE CASCADE
);

-- Leaderboard Table
CREATE TABLE IF NOT EXISTS leaderboard (
   id           INT AUTO_INCREMENT PRIMARY KEY,
   user_id      INT NOT NULL,
   language_id  INT NOT NULL,
   total_score  INT NOT NULL DEFAULT 0,
   user_rank    INT NOT NULL,
   last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   UNIQUE(user_id, language_id),
   FOREIGN KEY (user_id)     REFERENCES users(user_id)             ON DELETE CASCADE,
   FOREIGN KEY (language_id) REFERENCES programming_languages(id) ON DELETE CASCADE
);

-- Blog Posts and Comments Tables
CREATE TABLE IF NOT EXISTS blog_posts (
   id          INT AUTO_INCREMENT PRIMARY KEY,
   user_id     INT NOT NULL,
   title       VARCHAR(255) NOT NULL,
   content     TEXT         NOT NULL,
   created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
   FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comments (
   id           INT AUTO_INCREMENT PRIMARY KEY,
   user_id      INT NOT NULL,
   blog_post_id INT NOT NULL,
   comment_text TEXT NOT NULL,
   created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   FOREIGN KEY (user_id)       REFERENCES users(user_id)      ON DELETE CASCADE,
   FOREIGN KEY (blog_post_id)  REFERENCES blog_posts(id)      ON DELETE CASCADE
);

-- Forgotten Password Tokens Table
CREATE TABLE IF NOT EXISTS forgotten_password_tokens (
   id          INT AUTO_INCREMENT PRIMARY KEY,
   token       VARCHAR(255) NOT NULL UNIQUE,
   user_id     INT          NOT NULL,
   expiry_date DATETIME     NOT NULL,
   FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Flyway Schema History
CREATE TABLE IF NOT EXISTS flyway_schema_history (
   installed_rank INT        NOT NULL PRIMARY KEY,
   version        VARCHAR(50),
   description    VARCHAR(200),
   type           VARCHAR(20),
   script         VARCHAR(1000),
   checksum       INT,
   installed_by   VARCHAR(100),
   installed_on   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   execution_time INT,
   success        BOOLEAN
);

COMMIT;

DELIMITER $$
CREATE TRIGGER trg_upd_leaderboard
AFTER INSERT ON user_progress
FOR EACH ROW
BEGIN
  DECLARE lang_id INT;
  DECLARE total INT;

  SELECT programming_language_id
    INTO lang_id
  FROM questions
   WHERE id = NEW.question_id;

  SELECT COALESCE(SUM(up.score),0)
    INTO total
  FROM user_progress up
    JOIN questions q ON up.question_id = q.id
   WHERE up.user_id = NEW.user_id
     AND q.programming_language_id = lang_id
     AND up.status = 'COMPLETED';

  INSERT INTO leaderboard(user_id,language_id,total_score,user_rank)
    VALUES(NEW.user_id, lang_id, total, 0)
  ON DUPLICATE KEY UPDATE
    total_score = total,
    last_updated = CURRENT_TIMESTAMP;
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE cleanup_expired_tokens()
BEGIN
  DELETE FROM forgotten_password_tokens
    WHERE expiry_date < NOW();
END$$
DELIMITER ;

INSERT INTO programming_languages(name) VALUES('Java'),('JavaScript'),('MySQL');

INSERT INTO questions(title,description,programming_language_id,question_type,difficulty,starter_code,ai_solution_required)
VALUES
('Even or Odd','Determine if number is even',1,'CODING','EASY','public class S{public boolean isEven(int n){return false;}}',TRUE),
('Reverse Array','Reverse int[]',1,'CODING','MEDIUM','public class S{public int[] rev(int[] a){return a;}}',TRUE),
('Find Missing Number','Find missing 0..n',1,'CODING','HARD','public class S{public int find(int[] a){return 0;}}',TRUE);

INSERT INTO questions(title,description,programming_language_id,question_type,difficulty,correct_answer)
VALUES
('Boolean Data Type','Which type holds true/false?',1,'LOGIC','EASY','boolean'),
('Default Boolean Value','Default boolean field value?',1,'LOGIC','EASY','false');

INSERT INTO questions(title,description,programming_language_id,question_type,difficulty,starter_code,ai_solution_required)
VALUES
('Count Vowels','Count vowels in string',2,'CODING','EASY','function countV(str){return 0;}',TRUE),
('Find Duplicates','Return duplicates',2,'CODING','MEDIUM','function dup(a){return [];}',TRUE),
('Flatten Array','Flatten nested arr',2,'CODING','HARD','function flat(a){return [];}',TRUE);

INSERT INTO questions(title,description,programming_language_id,question_type,difficulty,correct_answer)
VALUES
('Single‑line Comment','Symbol for single comment',2,'LOGIC','EASY','//'),
('JS Boolean Type','Type for true/false',2,'LOGIC','EASY','boolean');

INSERT INTO questions(title,description,programming_language_id,question_type,difficulty,starter_code,ai_solution_required)
VALUES
('Select All','Order employees by salary',3,'CODING','EASY','SELECT name,salary FROM employees ORDER BY salary DESC;',TRUE),
('Dept Averages','Avg salary per dept',3,'CODING','MEDIUM','SELECT d.name,AVG(e.salary) FROM employees e JOIN departments d ON e.dept_id=d.id GROUP BY d.name;',TRUE),
('Active Users','3‑day login streak',3,'CODING','HARD','SELECT user_id,MIN(login_date) FROM (SELECT user_id,login_date,DATE_SUB(login_date,INTERVAL ROW_NUMBER() OVER(PARTITION BY user_id ORDER BY login_date) DAY) grp FROM logins) g GROUP BY user_id,grp HAVING COUNT(*)>=3;',TRUE);

INSERT INTO questions(title,description,programming_language_id,question_type,difficulty,correct_answer)
VALUES
('DISTINCT Keyword','Remove duplicate rows?',3,'LOGIC','EASY','DISTINCT'),
('SQL Wildcard','Wildcard in LIKE?',3,'LOGIC','EASY','%');

INSERT INTO users(username,email,password,role,provider)
VALUES('admin','admin@codeify.com','<initial_hashed_password>','admin','local');

CALL cleanup_expired_tokens();