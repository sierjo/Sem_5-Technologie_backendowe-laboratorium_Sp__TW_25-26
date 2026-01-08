-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: jobportal_prod
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `job_company`
--

DROP TABLE IF EXISTS `job_company`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_company` (
  `id` int NOT NULL AUTO_INCREMENT,
  `logo` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `job_company`
--

LOCK TABLES `job_company` WRITE;
/*!40000 ALTER TABLE `job_company` DISABLE KEYS */;
INSERT INTO `job_company` VALUES (1,NULL,'WSPiA'),(2,NULL,'MyCompanyIT'),(3,NULL,'WSPiA');
/*!40000 ALTER TABLE `job_company` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `job_location`
--

DROP TABLE IF EXISTS `job_location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_location` (
  `id` int NOT NULL AUTO_INCREMENT,
  `city` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `job_location`
--

LOCK TABLES `job_location` WRITE;
/*!40000 ALTER TABLE `job_location` DISABLE KEYS */;
INSERT INTO `job_location` VALUES (1,'Warszaw','Spain','Lublin'),(2,'Vitebsk','Belarus','Vitebsk'),(3,'Warszaw','Poland','Warszaw');
/*!40000 ALTER TABLE `job_location` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `job_post_activity`
--

DROP TABLE IF EXISTS `job_post_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_post_activity` (
  `job_post_id` int NOT NULL AUTO_INCREMENT,
  `description_of_job` varchar(10000) DEFAULT NULL,
  `job_title` varchar(255) DEFAULT NULL,
  `job_type` varchar(255) DEFAULT NULL,
  `posted_date` datetime(6) DEFAULT NULL,
  `remote` varchar(255) DEFAULT NULL,
  `salary` varchar(255) DEFAULT NULL,
  `job_company_id` int DEFAULT NULL,
  `job_location_id` int DEFAULT NULL,
  `posted_by_id` int DEFAULT NULL,
  PRIMARY KEY (`job_post_id`),
  KEY `FKpjpv059hollr4tk92ms09s6is` (`job_company_id`),
  KEY `FK44003mnvj29aiijhsc6ftsgxe` (`job_location_id`),
  KEY `FK62yqqbypsq2ik34ngtlw4m9k3` (`posted_by_id`),
  CONSTRAINT `FK44003mnvj29aiijhsc6ftsgxe` FOREIGN KEY (`job_location_id`) REFERENCES `job_location` (`id`),
  CONSTRAINT `FK62yqqbypsq2ik34ngtlw4m9k3` FOREIGN KEY (`posted_by_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FKpjpv059hollr4tk92ms09s6is` FOREIGN KEY (`job_company_id`) REFERENCES `job_company` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `job_post_activity`
--

LOCK TABLES `job_post_activity` WRITE;
/*!40000 ALTER TABLE `job_post_activity` DISABLE KEYS */;
INSERT INTO `job_post_activity` VALUES (1,'vsdffds112',' Erasmus+ Internship Program','Part-time','2025-10-15 19:46:57.714000','Remote-Only','123',1,1,6),(2,'<p>Little bit work with <span style=\"font-family: &quot;Arial Black&quot;; background-color: rgb(255, 0, 0);\">Java 17</span></p>','Java developer','Full-time','2025-10-20 17:30:03.785000','Partial-Remote','1000',2,2,6),(3,'<p>xdgdgdc<span style=\"background-color: rgb(255, 255, 0);\">cxvbv</span></p>','Sergey','Part-time','2025-10-23 14:51:37.417000','Remote-Only','100',3,3,6);
/*!40000 ALTER TABLE `job_post_activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `job_seeker_apply`
--

DROP TABLE IF EXISTS `job_seeker_apply`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_seeker_apply` (
  `id` int NOT NULL AUTO_INCREMENT,
  `apply_date` datetime(6) DEFAULT NULL,
  `cover_letter` varchar(255) DEFAULT NULL,
  `job` int DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK8v6qok40anljlhpkc486nsdmu` (`user_id`,`job`),
  KEY `FKmfhx9q4uclbb74vm49lv9dmf4` (`job`),
  CONSTRAINT `FKmfhx9q4uclbb74vm49lv9dmf4` FOREIGN KEY (`job`) REFERENCES `job_post_activity` (`job_post_id`),
  CONSTRAINT `FKs9fftlyxws2ak05q053vi57qv` FOREIGN KEY (`user_id`) REFERENCES `job_seeker_profile` (`user_account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `job_seeker_apply`
--

LOCK TABLES `job_seeker_apply` WRITE;
/*!40000 ALTER TABLE `job_seeker_apply` DISABLE KEYS */;
INSERT INTO `job_seeker_apply` VALUES (4,'2025-10-23 16:27:44.647000',NULL,1,14),(5,'2025-10-23 16:40:24.945000',NULL,1,15),(6,'2025-10-23 20:17:39.607000',NULL,3,14),(7,'2025-12-18 00:43:35.992000',NULL,3,16);
/*!40000 ALTER TABLE `job_seeker_apply` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `job_seeker_profile`
--

DROP TABLE IF EXISTS `job_seeker_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_seeker_profile` (
  `user_account_id` int NOT NULL,
  `city` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `employment_type` varchar(255) DEFAULT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `profile_photo` varchar(255) DEFAULT NULL,
  `resume` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `work_authorization` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_account_id`),
  CONSTRAINT `FKohp1poe14xlw56yxbwu2tpdm7` FOREIGN KEY (`user_account_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `job_seeker_profile`
--

LOCK TABLES `job_seeker_profile` WRITE;
/*!40000 ALTER TABLE `job_seeker_profile` DISABLE KEYS */;
INSERT INTO `job_seeker_profile` VALUES (4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(14,'Vitebsk','Spain','Full-Time','Valiantsin','Ushk','avatar_render.png','ushkevich_valiantsin.pdf','Vitebsk','US Citizen'),(15,'','','Full-Time','Siarhei','Ushkevich','','CV_Ushkevich_Siarhei.pdf','','H1 Visa'),(16,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `job_seeker_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `job_seeker_save`
--

DROP TABLE IF EXISTS `job_seeker_save`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_seeker_save` (
  `id` int NOT NULL AUTO_INCREMENT,
  `job` int DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK1vn1w4dxfiavb5q2gu1n0whxo` (`user_id`,`job`),
  KEY `FKpb44x040gkdltxqy9m7jmvvf3` (`job`),
  CONSTRAINT `FK96dyvgd8hmdohqsfdpvyl89mg` FOREIGN KEY (`user_id`) REFERENCES `job_seeker_profile` (`user_account_id`),
  CONSTRAINT `FKpb44x040gkdltxqy9m7jmvvf3` FOREIGN KEY (`job`) REFERENCES `job_post_activity` (`job_post_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `job_seeker_save`
--

LOCK TABLES `job_seeker_save` WRITE;
/*!40000 ALTER TABLE `job_seeker_save` DISABLE KEYS */;
INSERT INTO `job_seeker_save` VALUES (9,2,14),(10,2,15);
/*!40000 ALTER TABLE `job_seeker_save` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `recruiter_profile`
--

DROP TABLE IF EXISTS `recruiter_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recruiter_profile` (
  `user_account_id` int NOT NULL,
  `city` varchar(255) DEFAULT NULL,
  `company` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `profile_photo` varchar(64) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_account_id`),
  CONSTRAINT `FK42q4eb7jw1bvw3oy83vc05ft6` FOREIGN KEY (`user_account_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recruiter_profile`
--

LOCK TABLES `recruiter_profile` WRITE;
/*!40000 ALTER TABLE `recruiter_profile` DISABLE KEYS */;
INSERT INTO `recruiter_profile` VALUES (3,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(5,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(6,'WITEBSK','online','Belarus','val','ush','profile.jpg','WITEBSKIY'),(10,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `recruiter_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `skills`
--

DROP TABLE IF EXISTS `skills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `skills` (
  `id` int NOT NULL AUTO_INCREMENT,
  `experience_level` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `years_of_experience` varchar(255) DEFAULT NULL,
  `job_seeker_profile` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsjdksau8sat30c00aqh5xf2wh` (`job_seeker_profile`),
  CONSTRAINT `FKsjdksau8sat30c00aqh5xf2wh` FOREIGN KEY (`job_seeker_profile`) REFERENCES `job_seeker_profile` (`user_account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `skills`
--

LOCK TABLES `skills` WRITE;
/*!40000 ALTER TABLE `skills` DISABLE KEYS */;
INSERT INTO `skills` VALUES (1,'Intermediate','Java','1',14),(3,'Intermediate','HTML','1',14),(4,'Intermediate','Java','3',15);
/*!40000 ALTER TABLE `skills` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `registration_date` datetime(6) DEFAULT NULL,
  `user_type_id` int DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UK_6dotkott2kjsp8vw4d0m25fb7` (`email`),
  KEY `FK5snet2ikvi03wd4rabd40ckdl` (`user_type_id`),
  CONSTRAINT `FK5snet2ikvi03wd4rabd40ckdl` FOREIGN KEY (`user_type_id`) REFERENCES `users_type` (`user_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (3,'pifa.87@bk.ru',_binary '','$2a$12$9FWw4YWX2K8hTkp5aKog/eYeHdOAmp7/eYWd5B6PBzFqwj9PozJNG','2025-10-12 21:25:06.122000',1),(4,'epifan.v@mail.ru',_binary '','$2a$12$9FWw4YWX2K8hTkp5aKog/eYeHdOAmp7/eYWd5B6PBzFqwj9PozJNG','2025-10-12 21:25:49.347000',2),(5,'siarheiushkevich@gmail.com',_binary '','$2a$12$9FWw4YWX2K8hTkp5aKog/eYeHdOAmp7/eYWd5B6PBzFqwj9PozJNG','2025-10-13 00:38:42.853000',1),(6,'admin@gmail.com',_binary '','$2a$12$9FWw4YWX2K8hTkp5aKog/eYeHdOAmp7/eYWd5B6PBzFqwj9PozJNG','2025-10-14 18:27:19.608000',1),(10,'admin2@gmail.com',_binary '','$2a$12$9FWw4YWX2K8hTkp5aKog/eYeHdOAmp7/eYWd5B6PBzFqwj9PozJNG','2025-10-14 18:49:56.072000',1),(14,'jobseeker@gmail.com',_binary '','$2a$12$9FWw4YWX2K8hTkp5aKog/eYeHdOAmp7/eYWd5B6PBzFqwj9PozJNG','2025-10-17 20:43:22.381000',2),(15,'jobseeker2@gmail.com',_binary '','$2a$12$9FWw4YWX2K8hTkp5aKog/eYeHdOAmp7/eYWd5B6PBzFqwj9PozJNG','2025-10-20 21:51:40.785000',2),(16,'prodUser@gmail.com',_binary '','$2a$10$Z3oDE6EYeOmml9iJO7iBp.L3Y1fzKssRC7ibVuW2dG4KPGep5yNfq','2025-12-18 00:42:40.242000',2);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users_type`
--

DROP TABLE IF EXISTS `users_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users_type` (
  `user_type_id` int NOT NULL AUTO_INCREMENT,
  `user_type_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users_type`
--

LOCK TABLES `users_type` WRITE;
/*!40000 ALTER TABLE `users_type` DISABLE KEYS */;
INSERT INTO `users_type` VALUES (1,'Recruiter'),(2,'Job Seeker');
/*!40000 ALTER TABLE `users_type` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-18  0:59:27
