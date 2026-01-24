-- Database initialization script for Quora Application
-- This script runs when MySQL container starts for the first time

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS quora_db;
USE quora_db;

-- Create Users table
CREATE TABLE IF NOT EXISTS user (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    username VARCHAR(255) NOT NULL UNIQUE,
                                    password VARCHAR(255) NOT NULL,
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Tags table
CREATE TABLE IF NOT EXISTS tag (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   name VARCHAR(255) NOT NULL UNIQUE,
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Questions table
CREATE TABLE IF NOT EXISTS question (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        title VARCHAR(500) NOT NULL,
                                        content LONGTEXT NOT NULL,
                                        user_id BIGINT NOT NULL,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                        FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
                                        INDEX idx_user_id (user_id),
                                        INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Answers table
CREATE TABLE IF NOT EXISTS answer (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      content LONGTEXT NOT NULL,
                                      question_id BIGINT NOT NULL,
                                      user_id BIGINT NOT NULL,
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                      FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE,
                                      FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
                                      INDEX idx_question_id (question_id),
                                      INDEX idx_user_id (user_id),
                                      INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Comments table
CREATE TABLE IF NOT EXISTS comment (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       content LONGTEXT NOT NULL,
                                       answer_id BIGINT NOT NULL,
                                       parent_comment_id BIGINT,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       FOREIGN KEY (answer_id) REFERENCES answer(id) ON DELETE CASCADE,
                                       FOREIGN KEY (parent_comment_id) REFERENCES comment(id) ON DELETE CASCADE,
                                       INDEX idx_answer_id (answer_id),
                                       INDEX idx_parent_comment_id (parent_comment_id),
                                       INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create User-Tags junction table (following)
CREATE TABLE IF NOT EXISTS user_tags (
                                         user_id BIGINT NOT NULL,
                                         tag_id BIGINT NOT NULL,
                                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         PRIMARY KEY (user_id, tag_id),
                                         FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
                                         FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE,
                                         INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Question-Tags junction table
CREATE TABLE IF NOT EXISTS question_tags (
                                             question_id BIGINT NOT NULL,
                                             tag_id BIGINT NOT NULL,
                                             PRIMARY KEY (question_id, tag_id),
                                             FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE,
                                             FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE,
                                             INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Answer-Likes junction table
CREATE TABLE IF NOT EXISTS answer_likes (
                                            answer_id BIGINT NOT NULL,
                                            user_id BIGINT NOT NULL,
                                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                            PRIMARY KEY (answer_id, user_id),
                                            FOREIGN KEY (answer_id) REFERENCES answer(id) ON DELETE CASCADE,
                                            FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
                                            INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Comment-Likes junction table
CREATE TABLE IF NOT EXISTS comment_likes (
                                             comment_id BIGINT NOT NULL,
                                             user_id BIGINT NOT NULL,
                                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             PRIMARY KEY (comment_id, user_id),
                                             FOREIGN KEY (comment_id) REFERENCES comment(id) ON DELETE CASCADE,
                                             FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
                                             INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for better query performance
CREATE INDEX idx_question_user_created ON question(user_id, created_at);
CREATE INDEX idx_answer_question_created ON answer(question_id, created_at);
CREATE INDEX idx_comment_answer_created ON comment(answer_id, created_at);

-- Insert sample data for testing (optional)
INSERT IGNORE INTO user (username, password) VALUES
    ('john_doe', 'hashed_password_1'),
    ('jane_smith', 'hashed_password_2'),
    ('admin', 'hashed_password_3');

INSERT IGNORE INTO tag (name) VALUES
    ('java'),
    ('spring-boot'),
    ('mysql'),
    ('docker'),
    ('kubernetes');

-- Grant privileges to application user
GRANT ALL PRIVILEGES ON quora_db.* TO 'quora_user'@'%';
FLUSH PRIVILEGES;