START TRANSACTION;

-- Users Table
CREATE TABLE IF NOT EXISTS users (
                                     user_id            INT               PRIMARY KEY AUTO_INCREMENT,
                                     username           VARCHAR(50)       UNIQUE NOT NULL,
    email              VARCHAR(100)      UNIQUE,
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

-- Questions Table (add created_at [+ updated_at])
CREATE TABLE IF NOT EXISTS questions (
                                         id                        INT AUTO_INCREMENT PRIMARY KEY,
                                         title                     VARCHAR(200) NOT NULL,
    description               TEXT,
    programming_language_id   INT       NOT NULL,
    question_type             VARCHAR(50) NOT NULL,
    difficulty                VARCHAR(20),
    starter_code              TEXT,
    ai_solution_required      BOOLEAN DEFAULT FALSE,
    correct_answer            TEXT,
    created_at                TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (programming_language_id)
    REFERENCES programming_languages(id)
    );

-- User Progress Table
CREATE TABLE IF NOT EXISTS user_progress (
                                             id                        INT AUTO_INCREMENT PRIMARY KEY,
                                             user_id                   INT NOT NULL,
                                             question_id               INT NOT NULL,
                                             programming_language_id   INT NOT NULL,
                                             status                    ENUM('COMPLETED','IN_PROGRESS','NOTSTARTED') NOT NULL DEFAULT 'IN_PROGRESS',
    score                     INT DEFAULT 0,
    last_attempt              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)     REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
    );

-- Leaderboard Table
CREATE TABLE IF NOT EXISTS leaderboard (
                                           user_id                   INT NOT NULL,
                                           language_id               INT NOT NULL,
                                           total_score               INT NOT NULL DEFAULT 0,
                                           user_rank                 INT NOT NULL DEFAULT 0,
                                           last_updated              TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                           PRIMARY KEY (user_id, language_id)
    );

-- Forgotten Password Tokens Table
CREATE TABLE IF NOT EXISTS forgotten_password_tokens (
                                                         id          INT AUTO_INCREMENT PRIMARY KEY,
                                                         token       VARCHAR(255) NOT NULL UNIQUE,
    user_id     INT          NOT NULL,
    expiry_date DATETIME     NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS last_attempts (
                                             id INT AUTO_INCREMENT PRIMARY KEY,
                                             user_id INT NOT NULL,
                                             question_id INT NOT NULL,
                                             code TEXT NOT NULL,
                                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                             CONSTRAINT last_attempts_unique UNIQUE (user_id, question_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (question_id) REFERENCES questions(id)
    );

COMMIT;

-- Trigger: Update Leaderboard after Progress
DELIMITER $$

CREATE TRIGGER trg_upd_leaderboard
    AFTER INSERT ON user_progress
    FOR EACH ROW
BEGIN
    DECLARE lang_id INT;
    DECLARE total   INT;

    -- find the language for this question
    SELECT programming_language_id
    INTO lang_id
    FROM questions
    WHERE id = NEW.question_id;

    -- sum up all COMPLETED scores for this user & language
    SELECT COALESCE(SUM(up.score),0)
    INTO total
    FROM user_progress AS up
             JOIN questions       AS q  ON up.question_id = q.id
    WHERE up.user_id = NEW.user_id
      AND q.programming_language_id = lang_id
      AND up.status = 'COMPLETED';

    -- upsert into leaderboard
    INSERT INTO leaderboard (user_id, language_id, total_score, user_rank)
    VALUES (NEW.user_id, lang_id, total, 0)
        ON DUPLICATE KEY UPDATE
                             total_score = VALUES(total_score),
                             last_updated = CURRENT_TIMESTAMP;
    END$$

-- restore the normal delimiter
    DELIMITER ;



-- if you also need to redefine your cleanup procedure, do the same:
DELIMITER $$

    CREATE PROCEDURE cleanup_expired_tokens()
    BEGIN
    DELETE FROM forgotten_password_tokens
    WHERE expiry_date < NOW();
    END$$

    DELIMITER ;

-- Seed Admin User
    INSERT INTO users (username, email, role, registration_date, password, provider)
    VALUES ('Admin User',NULL,'admin',NOW(),'psjc5uxuRS/ouOgQt3...=','local')
        ON DUPLICATE KEY UPDATE username = VALUES(username);

-- Seed Programming Languages
    INSERT INTO programming_languages(name)
    VALUES('Java'),('JavaScript'),('MySQL');

-- Seed Questions (Starter Code)
    INSERT INTO questions(title,description,programming_language_id,question_type,difficulty,starter_code,ai_solution_required)
    VALUES
        ('Even or Odd','Determine if number is even',1,'CODING','EASY','class Main {
    public boolean isEven(int n) {
        return false;
    }

    public static void main(String[] args) {
        Main sol = new Main();
        System.out.println(sol.isEven(4));
    }
}',TRUE),
        ('Reverse Array','Reverse int[]',1,'CODING','MEDIUM','class Main {
    public int[] rev(int[] a) {
        return a;
    }

    public static void main(String[] args) {
        Main sol = new Main();
        int[] arr = {1, 2, 3, 4, 5};
        int[] result = sol.rev(arr);
        for(int i : result) {
            System.out.print(i + " ");
        }
    }
}',TRUE),
        ('Find Missing Number','Find missing 0..n',1,'CODING','HARD','class Main {
    public int find(int[] a) {
        return 0;
    }

    public static void main(String[] args) {
        Main sol = new Main();
        int[] arr = {0, 1, 3, 4, 5};
        System.out.println(sol.find(arr));
    }
}',TRUE);

-- Seed Logic Questions (Java)
    INSERT INTO questions(title,description,programming_language_id,question_type,difficulty,correct_answer)
    VALUES
        ('Boolean Data Type','Which type holds true/false?',1,'LOGIC','EASY','boolean'),
        ('Default Boolean Value','Default boolean field value?',1,'LOGIC','EASY','false');

-- Seed Questions (JavaScript)
    INSERT INTO questions(title,description,programming_language_id,question_type,difficulty,starter_code,ai_solution_required)
    VALUES
        ('Count Vowels','Count vowels in string',2,'CODING','EASY','function countV(str){return 0;}',TRUE),
        ('Find Duplicates','Return duplicates',2,'CODING','MEDIUM','function dup(a){return [];}',TRUE),
        ('Flatten Array','Flatten nested arr',2,'CODING','HARD','function flat(a){return [];}',TRUE);

-- Seed Logic Questions (JavaScript)
    INSERT INTO questions(title,description,programming_language_id,question_type,difficulty,correct_answer)
    VALUES
        ('Single-line Comment','Symbol for single comment',2,'LOGIC','EASY','//'),
        ('JS Boolean Type','Type for true/false',2,'LOGIC','EASY','boolean');

-- Seed Questions (MySQL)
    INSERT INTO questions(title,description,programming_language_id,question_type,difficulty,starter_code,ai_solution_required)
    VALUES
        ('Select All','Order employees by salary',3,'CODING','EASY','SELECT name,salary FROM employees ORDER BY salary DESC;',TRUE),
        ('Dept Averages','Avg salary per dept',3,'CODING','MEDIUM','SELECT AVG(e.salary) FROM employees e JOIN departments d ON e.dept_id=d.id GROUP BY d.name;',TRUE),
        ('Active Users','3-day login streak',3,'CODING','HARD','SELECT u.user_id, COUNT(*) AS streak FROM (SELECT user_id, DATE(login_time) AS dt FROM logins) g GROUP BY user_id,dt HAVING COUNT(*)>=3;',TRUE);

-- Seed Logic Questions (MySQL)
    INSERT INTO questions(title,description,programming_language_id,question_type,difficulty,correct_answer)
    VALUES
        ('DISTINCT Keyword','Remove duplicate rows?',3,'LOGIC','EASY','DISTINCT'),
        ('SQL Wildcard','Wildcard in LIKE?',3,'LOGIC','EASY','%');

-- Cleanup old tokens
    CALL cleanup_expired_tokens();