-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: bdd_dad
-- ------------------------------------------------------
-- Server version	8.0.42

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
-- Table structure for table `actuador`
--

DROP TABLE IF EXISTS `actuador`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `actuador` (
  `idActuador` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(45) DEFAULT NULL,
  `tipo` varchar(45) DEFAULT NULL,
  `idDispositivoAct` int DEFAULT NULL,
  PRIMARY KEY (`idActuador`),
  UNIQUE KEY `idActuador_UNIQUE` (`idActuador`),
  KEY `fk_idDispositivo_idx` (`idDispositivoAct`),
  CONSTRAINT `fk_idDispositivoAct` FOREIGN KEY (`idDispositivoAct`) REFERENCES `dispositivo` (`idDispositivo`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `actuador`
--

LOCK TABLES `actuador` WRITE;
/*!40000 ALTER TABLE `actuador` DISABLE KEYS */;
/*!40000 ALTER TABLE `actuador` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `actuadorstate`
--

DROP TABLE IF EXISTS `actuadorstate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `actuadorstate` (
  `idActuadorState` int NOT NULL AUTO_INCREMENT,
  `idActuador` int DEFAULT NULL,
  `estado` tinyint NOT NULL,
  `timeStamp` varchar(45) DEFAULT NULL,
  `valor` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`idActuadorState`),
  UNIQUE KEY `idActuadorState_UNIQUE` (`idActuadorState`),
  KEY `fk_idActuador_idx` (`idActuador`),
  CONSTRAINT `fk_idActuador` FOREIGN KEY (`idActuador`) REFERENCES `actuador` (`idActuador`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `actuadorstate`
--

LOCK TABLES `actuadorstate` WRITE;
/*!40000 ALTER TABLE `actuadorstate` DISABLE KEYS */;
/*!40000 ALTER TABLE `actuadorstate` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dispositivo`
--

DROP TABLE IF EXISTS `dispositivo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dispositivo` (
  `idDispositivo` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(45) DEFAULT NULL,
  `idGrupo` int DEFAULT NULL,
  PRIMARY KEY (`idDispositivo`),
  UNIQUE KEY `idDispositivo_UNIQUE` (`idDispositivo`),
  KEY `fk_idGrupo_idx` (`idGrupo`),
  CONSTRAINT `fk_idGrupo` FOREIGN KEY (`idGrupo`) REFERENCES `grupo` (`idGrupo`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dispositivo`
--

LOCK TABLES `dispositivo` WRITE;
/*!40000 ALTER TABLE `dispositivo` DISABLE KEYS */;
/*!40000 ALTER TABLE `dispositivo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `grupo`
--

DROP TABLE IF EXISTS `grupo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `grupo` (
  `idGrupo` int NOT NULL AUTO_INCREMENT,
  `canalMQTT` varchar(45) DEFAULT NULL,
  `nombre` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`idGrupo`),
  UNIQUE KEY `idGrupo_UNIQUE` (`idGrupo`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `grupo`
--

LOCK TABLES `grupo` WRITE;
/*!40000 ALTER TABLE `grupo` DISABLE KEYS */;
/*!40000 ALTER TABLE `grupo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sensor`
--

DROP TABLE IF EXISTS `sensor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sensor` (
  `idSensor` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(45) DEFAULT NULL,
  `tipo` varchar(45) DEFAULT NULL,
  `idDispositivo` int DEFAULT NULL,
  PRIMARY KEY (`idSensor`),
  UNIQUE KEY `idSensor_UNIQUE` (`idSensor`),
  KEY `fk_idDispositivo_idx` (`idDispositivo`),
  CONSTRAINT `fk_idDispositivo` FOREIGN KEY (`idDispositivo`) REFERENCES `dispositivo` (`idDispositivo`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sensor`
--

LOCK TABLES `sensor` WRITE;
/*!40000 ALTER TABLE `sensor` DISABLE KEYS */;
/*!40000 ALTER TABLE `sensor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sensorvalue`
--

DROP TABLE IF EXISTS `sensorvalue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sensorvalue` (
  `idSensorValue` int NOT NULL AUTO_INCREMENT,
  `idSensor` int DEFAULT NULL,
  `value` float DEFAULT NULL,
  `timeStamp` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`idSensorValue`),
  UNIQUE KEY `idSensorValue_UNIQUE` (`idSensorValue`),
  KEY `fk_idSensor_idx` (`idSensor`),
  CONSTRAINT `fk_idSensor` FOREIGN KEY (`idSensor`) REFERENCES `sensor` (`idSensor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sensorvalue`
--

LOCK TABLES `sensorvalue` WRITE;
/*!40000 ALTER TABLE `sensorvalue` DISABLE KEYS */;
/*!40000 ALTER TABLE `sensorvalue` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-06-09 14:18:53
