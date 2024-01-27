CREATE DATABASE mybatis CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
DROP TABLE IF EXISTS USER;
CREATE TABLE USER (
                      id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                      username VARCHAR ( 64 ),
                      `password` VARCHAR ( 52 ),
                      avatar VARCHAR ( 100 ) COMMENT '用户头像',
                      PRIMARY KEY ( id )
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
INSERT INTO USER ( id, username, `password`, avatar )
VALUES( 1 , "hexin" , "123456" , "HeXin 的头像" );