-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema BDD_DAD
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema BDD_DAD
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `BDD_DAD` DEFAULT CHARACTER SET utf8 ;
USE `BDD_DAD` ;

-- -----------------------------------------------------
-- Table `BDD_DAD`.`Grupo`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `BDD_DAD`.`Grupo` (
  `idGrupo` INT NOT NULL AUTO_INCREMENT,
  `canalMQTT` VARCHAR(45) NULL,
  `nombre` VARCHAR(45) NULL,
  PRIMARY KEY (`idGrupo`),
  UNIQUE INDEX `idGrupo_UNIQUE` (`idGrupo` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `BDD_DAD`.`Dispositivo`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `BDD_DAD`.`Dispositivo` (
  `idDispositivo` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45) NULL,
  `idGrupo` INT NULL,
  PRIMARY KEY (`idDispositivo`),
  UNIQUE INDEX `idDispositivo_UNIQUE` (`idDispositivo` ASC) VISIBLE,
  INDEX `fk_idGrupo_idx` (`idGrupo` ASC) VISIBLE,
  CONSTRAINT `fk_idGrupo`
    FOREIGN KEY (`idGrupo`)
    REFERENCES `BDD_DAD`.`Grupo` (`idGrupo`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `BDD_DAD`.`Sensor`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `BDD_DAD`.`Sensor` (
  `idSensor` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45) NULL,
  `tipo` VARCHAR(45) NULL,
  `idDispositivo` INT NULL,
  PRIMARY KEY (`idSensor`),
  UNIQUE INDEX `idSensor_UNIQUE` (`idSensor` ASC) VISIBLE,
  INDEX `fk_idDispositivo_idx` (`idDispositivo` ASC) VISIBLE,
  CONSTRAINT `fk_idDispositivo`
    FOREIGN KEY (`idDispositivo`)
    REFERENCES `BDD_DAD`.`Dispositivo` (`idDispositivo`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `BDD_DAD`.`Actuador`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `BDD_DAD`.`Actuador` (
  `idActuador` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45) NULL,
  `tipo` VARCHAR(45) NULL,
  `idDispositivo` INT NULL,
  PRIMARY KEY (`idActuador`),
  UNIQUE INDEX `idActuador_UNIQUE` (`idActuador` ASC) VISIBLE,
  INDEX `fk_idDispositivo_idx` (`idDispositivo` ASC) VISIBLE,
  CONSTRAINT `fk_idDispositivo`
    FOREIGN KEY (`idDispositivo`)
    REFERENCES `BDD_DAD`.`Dispositivo` (`idDispositivo`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `BDD_DAD`.`ActuadorState`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `BDD_DAD`.`ActuadorState` (
  `idActuadorState` INT NOT NULL AUTO_INCREMENT,
  `idActuador` INT NULL,
  `estado` TINYINT NOT NULL,
  `timeStamp` VARCHAR(45) NULL,
  PRIMARY KEY (`idActuadorState`),
  UNIQUE INDEX `idActuadorState_UNIQUE` (`idActuadorState` ASC) VISIBLE,
  INDEX `fk_idActuador_idx` (`idActuador` ASC) VISIBLE,
  CONSTRAINT `fk_idActuador`
    FOREIGN KEY (`idActuador`)
    REFERENCES `BDD_DAD`.`Actuador` (`idActuador`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `BDD_DAD`.`SensorPMS`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `BDD_DAD`.`SensorPMS` (
  `idSensorPMS` INT NOT NULL AUTO_INCREMENT,
  `idSensor` INT NULL,
  `valuePM10` FLOAT NULL,
  `valuePM1` FLOAT NULL,
  `vamluePM25` FLOAT NULL,
  PRIMARY KEY (`idSensorPMS`),
  UNIQUE INDEX `idSensorPMS_UNIQUE` (`idSensorPMS` ASC) VISIBLE,
  INDEX `fk_idSensor_idx` (`idSensor` ASC) VISIBLE,
  CONSTRAINT `fk_idSensor`
    FOREIGN KEY (`idSensor`)
    REFERENCES `BDD_DAD`.`Sensor` (`idSensor`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `BDD_DAD`.`SensorValue`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `BDD_DAD`.`SensorValue` (
  `idSensorValue` INT NOT NULL AUTO_INCREMENT,
  `idSensor` INT NULL,
  `value` FLOAT NULL,
  `timeStamp` VARCHAR(45) NULL,
  PRIMARY KEY (`idSensorValue`),
  UNIQUE INDEX `idSensorValue_UNIQUE` (`idSensorValue` ASC) VISIBLE,
  INDEX `fk_idSensor_idx` (`idSensor` ASC) VISIBLE,
  CONSTRAINT `fk_idSensor`
    FOREIGN KEY (`idSensor`)
    REFERENCES `BDD_DAD`.`Sensor` (`idSensor`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
