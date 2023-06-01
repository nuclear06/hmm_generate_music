-- MySQL dump 10.13  Distrib 8.0.32, for Linux (x86_64)
--
-- Host: localhost    Database: homework
-- ------------------------------------------------------
-- Server version	8.0.32-0ubuntu0.20.04.2

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `homework`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `homework` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `homework`;

--
-- Table structure for table `tbl_user`
--

DROP TABLE IF EXISTS `tbl_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` char(32) NOT NULL COMMENT 'md5,加盐计算3次',
  `salt` char(32) NOT NULL COMMENT '盐',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_unique_index` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbl_user`
--

LOCK TABLES `tbl_user` WRITE;
/*!40000 ALTER TABLE `tbl_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `tbl_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tbl_user_history`
--

DROP TABLE IF EXISTS `tbl_user_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_user_history` (
  `history_id` int NOT NULL AUTO_INCREMENT COMMENT '历史记录id',
  `applicant_id` int NOT NULL COMMENT '服务请求者id',
  `date` datetime NOT NULL COMMENT '服务请求时间',
  `rhythm` int NOT NULL COMMENT '音乐节奏',
  `key` char(2) NOT NULL COMMENT 'key',
  `speed` int NOT NULL COMMENT '速度',
  `mention` int NOT NULL COMMENT '情绪,0-100',
  `piano` bit(1) NOT NULL DEFAULT b'0' COMMENT '钢琴分解',
  `chord_piano` bit(1) NOT NULL DEFAULT b'0' COMMENT '钢琴柱式',
  `guitar` bit(1) NOT NULL DEFAULT b'0' COMMENT '吉他分解',
  `chord_guitar` bit(1) NOT NULL DEFAULT b'0' COMMENT '吉他柱式',
  `drum` bit(1) NOT NULL DEFAULT b'0' COMMENT '鼓',
  `bass` bit(1) NOT NULL DEFAULT b'0' COMMENT '贝斯',
  `file_name` varchar(100) NOT NULL COMMENT '输入文件缓存路径',
  `file_name_md5` varchar(32) NOT NULL,
  PRIMARY KEY (`history_id`),
  KEY `fk_id` (`applicant_id`),
  KEY `index_file_name_md5` (`file_name_md5`),
  CONSTRAINT `fk_id` FOREIGN KEY (`applicant_id`) REFERENCES `tbl_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;