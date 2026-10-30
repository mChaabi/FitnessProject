
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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `body_measurements` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `arm_cm` double DEFAULT NULL,
  `body_fat_percent` double DEFAULT NULL,
  `chest_cm` double DEFAULT NULL,
  `measured_on` date NOT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `recorded_by` varchar(100) DEFAULT NULL,
  `waist_cm` double DEFAULT NULL,
  `weight_kg` double DEFAULT NULL,
  `member_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6j4wx7icwp7ngpeqhr279jwot` (`member_id`,`measured_on`),
  CONSTRAINT `FKki3dwgn9iqssg2sov53hc4ti4` FOREIGN KEY (`member_id`) REFERENCES `members` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `check_ins` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `checked_at` datetime(6) NOT NULL,
  `outcome` enum('ALLOWED','DENIED','DUPLICATE') NOT NULL,
  `performed_by` varchar(100) DEFAULT NULL,
  `reason` enum('EXPIRED','FROZEN','INACTIVE','NOT_FOUND','NO_SUBSCRIPTION') DEFAULT NULL,
  `member_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXka7cpyt3p5af8lmxxx6hkl3xg` (`member_id`,`checked_at`),
  CONSTRAINT `FKejp1an6sdpmragkv1ptxn6qxi` FOREIGN KEY (`member_id`) REFERENCES `members` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `class_bookings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `queued_at` datetime(6) NOT NULL,
  `status` enum('CANCELLED','CONFIRMED','WAITLIST') NOT NULL,
  `member_id` bigint NOT NULL,
  `session_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKb2lbu4norlo39hbw4mvlarj6c` (`session_id`,`member_id`),
  KEY `FKm71d2h42w3kgeftdx4205ntrk` (`member_id`),
  CONSTRAINT `FKm71d2h42w3kgeftdx4205ntrk` FOREIGN KEY (`member_id`) REFERENCES `members` (`id`),
  CONSTRAINT `FKn0daqmkewvspex9s4bo68mdg4` FOREIGN KEY (`session_id`) REFERENCES `class_sessions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `class_sessions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `capacity` int NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `end_time` time NOT NULL,
  `name` varchar(100) NOT NULL,
  `room` varchar(50) DEFAULT NULL,
  `session_date` date NOT NULL,
  `start_time` time NOT NULL,
  `status` enum('CANCELLED','SCHEDULED') NOT NULL,
  `coach_id` bigint NOT NULL,
  `template_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK8j48223des2o7l7061ehivpi9` (`template_id`,`session_date`),
  KEY `IDX64xksd2lbdcj7p1bgax19wu4a` (`session_date`),
  KEY `FKjmg91l25s36tvb9vq58949ft4` (`coach_id`),
  CONSTRAINT `FK1a1nm8c09nk3fveks7tqe84gk` FOREIGN KEY (`template_id`) REFERENCES `class_templates` (`id`),
  CONSTRAINT `FKjmg91l25s36tvb9vq58949ft4` FOREIGN KEY (`coach_id`) REFERENCES `coaches` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `class_templates` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `capacity` int NOT NULL,
  `day_of_week` int NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `duration_minutes` int NOT NULL,
  `name` varchar(100) NOT NULL,
  `room` varchar(50) DEFAULT NULL,
  `start_time` time NOT NULL,
  `coach_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK81cxie62qun84sanpcjhq7coc` (`coach_id`),
  CONSTRAINT `FK81cxie62qun84sanpcjhq7coc` FOREIGN KEY (`coach_id`) REFERENCES `coaches` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coaches` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `bio` varchar(1000) DEFAULT NULL,
  `specialties` varchar(150) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKcmp5j50stxr45fxhwhn0bgs3s` (`user_id`),
  CONSTRAINT `FKo2s6iuqyundkjflwh6ps8d0f0` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contacts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `message` varchar(1000) NOT NULL,
  `nom` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `email_outbox` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ai_generated` bit(1) DEFAULT NULL,
  `attempts` int NOT NULL,
  `claimed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `dedup_key` varchar(120) DEFAULT NULL,
  `facts_text` text,
  `last_error` varchar(500) DEFAULT NULL,
  `member_id` bigint NOT NULL,
  `next_attempt_at` datetime(6) NOT NULL,
  `recipient` varchar(150) NOT NULL,
  `recipient_name` varchar(100) DEFAULT NULL,
  `sent_at` datetime(6) DEFAULT NULL,
  `status` enum('FAILED','PENDING','SENDING','SENT','SKIPPED') NOT NULL,
  `type` enum('WELCOME','PAYMENT_RECEIPT','REMINDER','OTHER_TYPE') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_outbox_dedup` (`dedup_key`),
  KEY `IDX43aifa5e9wq7xdsqr2x2dmgb` (`status`,`next_attempt_at`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exercices` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `categorie` varchar(50) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `duree` int DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL,
  `nom` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `members` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `birth_date` date DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `goal` varchar(255) DEFAULT NULL,
  `joined_at` date NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `qr_code` varchar(36) NOT NULL,
  `sex` enum('FEMALE','MALE','OTHER') DEFAULT NULL,
  `status` enum('ACTIVE','EXPIRED','FROZEN','INACTIVE') NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `coach_id` bigint DEFAULT NULL,
  `email_opt_out` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK9d30a9u1qpg8eou0otgkwrp5d` (`email`),
  UNIQUE KEY `UKc8s74d8c8ackb7knckvxffs95` (`qr_code`),
  UNIQUE KEY `UKda61ga2jecphdliwvkqyt6sw2` (`user_id`),
  KEY `IDX9d30a9u1qpg8eou0otgkwrp5d` (`email`),
  KEY `IDXfbb3ytxu91bffuht3l42rbs2` (`last_name`),
  KEY `FKhnbvnv3lwrt1jc39tfvn57uqo` (`coach_id`),
  CONSTRAINT `FKhnbvnv3lwrt1jc39tfvn57uqo` FOREIGN KEY (`coach_id`) REFERENCES `coaches` (`id`),
  CONSTRAINT `FKpj3n6wh5muoeakc485whgs3x5` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `membership_plans` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `duration_days` int NOT NULL,
  `name` varchar(80) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKnconflq2x1uvxvs7sc0mnmh4` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(10,2) NOT NULL,
  `method` enum('CARD','CASH','CHECK','TRANSFER') NOT NULL,
  `paid_at` datetime(6) NOT NULL,
  `receipt_number` varchar(30) DEFAULT NULL,
  `received_by` varchar(100) DEFAULT NULL,
  `subscription_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7dlkg5hxn3mdha0gqt9mw8nxy` (`subscription_id`),
  UNIQUE KEY `UKu6rnuxne864s4rh7qgeql1vx` (`receipt_number`),
  CONSTRAINT `FKa3xnf2o6mt8cqbewvq2ouq3rq` FOREIGN KEY (`subscription_id`) REFERENCES `subscriptions` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscriptions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `end_date` date NOT NULL,
  `start_date` date NOT NULL,
  `status` enum('ACTIVE','CANCELLED','EXPIRED') NOT NULL,
  `member_id` bigint NOT NULL,
  `plan_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXacbcvyomyd22o7cps0p363d53` (`member_id`,`status`),
  KEY `FKslal8rolpqvkasj3ma2qg2854` (`plan_id`),
  CONSTRAINT `FKf53uswcymu3hg9mgr3sdp99n` FOREIGN KEY (`member_id`) REFERENCES `members` (`id`),
  CONSTRAINT `FKslal8rolpqvkasj3ma2qg2854` FOREIGN KEY (`plan_id`) REFERENCES `membership_plans` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `mot_de_passe` varchar(255) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `prenom` varchar(255) NOT NULL,
  `role` enum('ADMIN','COACH','MEMBER','RECEPTION') NOT NULL,
  `enabled` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `workout_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `day_of_week` int NOT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `position` int NOT NULL,
  `reps` int NOT NULL,
  `rest_seconds` int NOT NULL,
  `sets` int NOT NULL,
  `exercice_id` bigint NOT NULL,
  `program_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKa6yoie8qh1i8xqje58c3xylfl` (`exercice_id`),
  KEY `FKifoqfcuonvred9vtcsbcn8gun` (`program_id`),
  CONSTRAINT `FKa6yoie8qh1i8xqje58c3xylfl` FOREIGN KEY (`exercice_id`) REFERENCES `exercices` (`id`),
  CONSTRAINT `FKifoqfcuonvred9vtcsbcn8gun` FOREIGN KEY (`program_id`) REFERENCES `workout_programs` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `workout_programs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `goal` varchar(255) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `start_date` date NOT NULL,
  `coach_id` bigint NOT NULL,
  `member_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXi81cpncoljhwqtfmbe2ceue9w` (`member_id`),
  KEY `IDXg355n6nbm6mlrav6f717keb5o` (`coach_id`),
  CONSTRAINT `FKpqc1667dlvru0tq6e8gnb7yqy` FOREIGN KEY (`coach_id`) REFERENCES `coaches` (`id`),
  CONSTRAINT `FKtkbj30dvxp9u8tl2r0xsr0qnv` FOREIGN KEY (`member_id`) REFERENCES `members` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

