CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(255) NOT NULL,
  tokens INT NOT NULL,
  banned BOOLEAN NOT NULL,
  email VARCHAR(255) NOT NULL
);

INSERT INTO users (username, password, role, tokens, banned, email) VALUES
('user1', '$2a$12$g3MpPVpTlhFMYuduRmeoDeEpDKVzOCF4xWIbyEWZRctpAT.SpnY1G', 'admin', 10, FALSE, 'user1@example.com'),
('user2', '$2a$12$E8EYMDKhHsmzH36IlgUxd.jJh2GIl1sR5Fpx3b/Noq6/aINSxuGJu', 'user', 20, FALSE, 'user2@example.com'),
('user3', '$2a$12$zbdoppMBtgbV/WQNKWiww.VZkOOPmU9.per52YuP03SCpdB5.encC', 'user', 30, TRUE, 'user3@example.com');

--pass 1234