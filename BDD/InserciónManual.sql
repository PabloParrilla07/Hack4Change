USE BDD_DAD;
INSERT INTO Grupo(idGrupo, canalMQTT, nombre)
VALUES
	(0, 'esp32/sensor/#', 'sensor'),
    (1, 'esp32/actuador/#', 'actuador');
INSERT INTO Dispositivo(idDispositivo, nombre, idGrupo)
VALUES 
	(0, 'sensores', 0),
    (1, 'actuadores', 1);
INSERT INTO sensor(idSensor, nombre, tipo, idDispositivo)
VALUES
	(0,"CH4","MQ-9",0),
    (1,"CO","MQ-9",0),
    (2,"LPG","MQ-9",0),
    (3,"MAX30105","MAX30105",0);
INSERT INTO actuador(idActuador, nombre, tipo, idDispositivoAct)
VALUES
	(0,"OLED","OLED",1),
    (1,"BOCINA","BOCINA",1);

