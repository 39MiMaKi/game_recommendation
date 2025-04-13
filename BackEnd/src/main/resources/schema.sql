/*
 Navicat Premium Dump SQL

 Source Server         : go_project
 Source Server Type    : MySQL
 Source Server Version : 80400 (8.4.0)
 Source Host           : localhost:3306
 Source Schema         : steam

 Target Server Type    : MySQL
 Target Server Version : 80400 (8.4.0)
 File Encoding         : 65001

 Date: 08/04/2025 17:13:43
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for app
-- ----------------------------
DROP TABLE IF EXISTS `app`;
CREATE TABLE `app` (
  `app_id` bigint NOT NULL,
  `average_rating` decimal(3,2) DEFAULT '0.00',
  `cover` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `description` varchar(4095) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `developer` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `final_price` double DEFAULT NULL,
  `header` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `images` varchar(4095) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `linux` tinyint(1) DEFAULT NULL,
  `mac` tinyint(1) DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `popularity` int DEFAULT NULL,
  `price` double DEFAULT NULL,
  `publisher` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `win` tinyint(1) DEFAULT NULL,
  `positive_rate` decimal(5,2) DEFAULT '0.00',
  `review_count` int DEFAULT '0',
  PRIMARY KEY (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for app_tags
-- ----------------------------
DROP TABLE IF EXISTS `app_tags`;
CREATE TABLE `app_tags` (
  `app_id` bigint NOT NULL,
  `tag` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  KEY `FKoml600fijiupv7n2vvjh5y3ij` (`app_id`),
  CONSTRAINT `FKoml600fijiupv7n2vvjh5y3ij` FOREIGN KEY (`app_id`) REFERENCES `app` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(4095) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` bigint DEFAULT NULL,
  `from_id` bigint DEFAULT NULL,
  `is_read` bit(1) NOT NULL,
  `to_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for friend
-- ----------------------------
DROP TABLE IF EXISTS `friend`;
CREATE TABLE `friend` (
  `friend_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `create_time` bigint DEFAULT NULL,
  PRIMARY KEY (`friend_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for id_generator
-- ----------------------------
DROP TABLE IF EXISTS `id_generator`;
CREATE TABLE `id_generator` (
  `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `value` bigint DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for invitation
-- ----------------------------
DROP TABLE IF EXISTS `invitation`;
CREATE TABLE `invitation` (
  `friend_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `create_time` bigint DEFAULT NULL,
  PRIMARY KEY (`friend_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;



-- ----------------------------
-- Table structure for recommendation_config
-- ----------------------------
DROP TABLE IF EXISTS `recommendation_config`;
CREATE TABLE `recommendation_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `algorithm_version` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `collaborative_filtering_weight` double NOT NULL DEFAULT '0.5',
  `collaborative_weight` double NOT NULL,
  `content_based_weight` double NOT NULL DEFAULT '0.5',
  `content_weight` double NOT NULL,
  `create_time` bigint NOT NULL,
  `hybrid_weight` double NOT NULL DEFAULT '0.6',
  `similarity_threshold` double NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for user_preference
-- ----------------------------
DROP TABLE IF EXISTS `user_preference`;
CREATE TABLE `user_preference` (
  `user_id` bigint NOT NULL,
  `preference_value` double DEFAULT NULL,
  `preference_key` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`user_id`,`preference_key`),
  CONSTRAINT `FK7mgxw6j2m7uvuk3svr9vsar8p` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for user_rating
-- ----------------------------
DROP TABLE IF EXISTS `user_rating`;
CREATE TABLE `user_rating` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `app_id` bigint NOT NULL,
  `rating` double NOT NULL,
  `timestamp` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `comment` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `recommended` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `user_id` bigint NOT NULL,
  `avatar` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime(6) NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  `nickname` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `password` varchar(64) COLLATE utf8mb4_general_ci NOT NULL,
  `role` int NOT NULL DEFAULT '0',
  `tags` varchar(512) COLLATE utf8mb4_general_ci NOT NULL,
  `username` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UK_6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UK_r43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for wishlist_item
-- ----------------------------
DROP TABLE IF EXISTS `wishlist_item`;
CREATE TABLE `wishlist_item` (
  `app_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `create_time` bigint DEFAULT NULL,
  `sort` int DEFAULT NULL,
  PRIMARY KEY (`app_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;
