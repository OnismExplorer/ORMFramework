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

DROP TABLE
    IF
        EXISTS `task`;
CREATE TABLE `task` (
                        `id` BIGINT ( 35 ) NOT NULL auto_increment COMMENT 'id',
                        `task_id` BIGINT ( 35 ) NOT NULL COMMENT '任务id',
                        `task_name` VARCHAR ( 100 ) CHARACTER
                            SET utf8mb4 DEFAULT NULL COMMENT '任务名称',
                        `task_description` VARCHAR ( 255 ) CHARACTER
                            SET utf8mb4 DEFAULT NULL COMMENT '任务描述',
                        `gmt_create` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `gmt_modified` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY ( `id` ),
                        UNIQUE KEY `unique_task_id` ( `task_id` )
) ENGINE = InnoDB AUTO_INCREMENT = 4 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '任务';

insert into `task` values (1,100001,'任务一','这是第一个任务','2024-02-16 14:25:32','2024-02-16 14:25:32');
insert into `task` values (2,100011,'任务二','这是第二个任务','2024-02-18 10:38:41','2024-02-18 15:39:20');