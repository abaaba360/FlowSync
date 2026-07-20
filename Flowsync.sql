/*
SQLyog Community v13.1.6 (64 bit)
MySQL - 8.0.19 : Database - flowsync_simple
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`flowsync_simple` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_vi_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `flowsync_simple`;

/*Table structure for table `project_info` */

DROP TABLE IF EXISTS `project_info`;

CREATE TABLE `project_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) NOT NULL COMMENT '项目名称',
  `description` varchar(500) DEFAULT NULL COMMENT '项目说明',
  `status` varchar(20) NOT NULL COMMENT '项目状态（未开始/进行中/已完成）',
  `priority` varchar(20) NOT NULL COMMENT '优先级（低/中/高）',
  `owner_id` bigint DEFAULT NULL COMMENT '项目负责人 ID',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `fk_project_owner` (`owner_id`),
  CONSTRAINT `fk_project_owner` FOREIGN KEY (`owner_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目基本信息表';

/*Data for the table `project_info` */

insert  into `project_info`(`id`,`name`,`description`,`status`,`priority`,`owner_id`,`start_date`,`end_date`,`create_time`) values 
(1,'第一天实践','进行任务分配和初步开发','进行中','高',2,'2026-07-20','2026-07-20','2026-07-20 15:50:18');

/*Table structure for table `sys_user` */

DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) NOT NULL COMMENT '用户名，登录账号',
  `password` varchar(100) NOT NULL COMMENT '密码（明文存储，教学简化）',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `role` varchar(30) NOT NULL COMMENT '角色（管理员/负责人/成员）',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '电子邮箱',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

/*Data for the table `sys_user` */

insert  into `sys_user`(`id`,`username`,`password`,`real_name`,`role`,`phone`,`email`,`create_time`) values 
(1,'leader','123456','项目负责人','负责人',NULL,NULL,'2026-07-20 15:12:18'),
(2,'member1','123456','张三','成员',NULL,NULL,'2026-07-20 15:12:18'),
(3,'member2','123456','李四','成员',NULL,NULL,'2026-07-20 15:12:18');

/*Table structure for table `task_info` */

DROP TABLE IF EXISTS `task_info`;

CREATE TABLE `task_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `project_id` bigint NOT NULL COMMENT '所属项目 ID',
  `parent_id` bigint DEFAULT NULL COMMENT '父任务 ID（支持任务层级）',
  `title` varchar(100) NOT NULL COMMENT '任务标题',
  `description` varchar(1000) DEFAULT NULL COMMENT '任务说明',
  `assignee_id` bigint DEFAULT NULL COMMENT '任务负责人 ID',
  `creator_id` bigint DEFAULT NULL COMMENT '任务创建人 ID',
  `status` varchar(20) NOT NULL COMMENT '任务状态（未开始/进行中/已完成）',
  `priority` varchar(20) NOT NULL COMMENT '优先级（低/中/高）',
  `due_date` date DEFAULT NULL COMMENT '截止日期',
  `ai_suggestion` text COMMENT '千问 AI 建议内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `fk_task_project` (`project_id`),
  KEY `fk_task_parent` (`parent_id`),
  KEY `fk_task_assignee` (`assignee_id`),
  KEY `fk_task_creator` (`creator_id`),
  CONSTRAINT `fk_task_assignee` FOREIGN KEY (`assignee_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_task_creator` FOREIGN KEY (`creator_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_task_parent` FOREIGN KEY (`parent_id`) REFERENCES `task_info` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_task_project` FOREIGN KEY (`project_id`) REFERENCES `project_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务信息表';

/*Data for the table `task_info` */

insert  into `task_info`(`id`,`project_id`,`parent_id`,`title`,`description`,`assignee_id`,`creator_id`,`status`,`priority`,`due_date`,`ai_suggestion`,`create_time`) values 
(1,1,NULL,'数据库创建','创建各模块信息存储数据库',3,2,'进行中','高','2026-07-20',NULL,'2026-07-20 15:53:47');

/*Table structure for table `task_log` */

DROP TABLE IF EXISTS `task_log`;

CREATE TABLE `task_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id` bigint NOT NULL COMMENT '关联任务 ID',
  `progress_percent` int NOT NULL COMMENT '进度百分比（0-100）',
  `content` varchar(1000) NOT NULL COMMENT '进度说明文字',
  `operator_id` bigint DEFAULT NULL COMMENT '记录人 ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `fk_log_task` (`task_id`),
  KEY `fk_log_operator` (`operator_id`),
  CONSTRAINT `fk_log_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_log_task` FOREIGN KEY (`task_id`) REFERENCES `task_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务进度记录表';

/*Data for the table `task_log` */

insert  into `task_log`(`id`,`task_id`,`progress_percent`,`content`,`operator_id`,`create_time`) values 
(1,1,80,'已创建各表，正在测试数据写入',3,'2026-07-20 15:55:06');

/*Table structure for table `task_summary` */

DROP TABLE IF EXISTS `task_summary`;

CREATE TABLE `task_summary` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `project_id` bigint NOT NULL COMMENT '所属项目 ID',
  `task_id` bigint DEFAULT NULL COMMENT '关联任务 ID（可选）',
  `summary_type` varchar(30) NOT NULL COMMENT '总结类型（阶段总结/最终总结）',
  `content` varchar(2000) NOT NULL COMMENT '总结内容',
  `created_by` bigint DEFAULT NULL COMMENT '创建人 ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `fk_summary_project` (`project_id`),
  KEY `fk_summary_task` (`task_id`),
  KEY `fk_summary_creator` (`created_by`),
  CONSTRAINT `fk_summary_creator` FOREIGN KEY (`created_by`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_summary_project` FOREIGN KEY (`project_id`) REFERENCES `project_info` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_summary_task` FOREIGN KEY (`task_id`) REFERENCES `task_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目/任务总结表';

/*Data for the table `task_summary` */

insert  into `task_summary`(`id`,`project_id`,`task_id`,`summary_type`,`content`,`created_by`,`create_time`) values 
(1,1,1,'阶段总结','继续努力',2,'2026-07-20 15:56:20');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
