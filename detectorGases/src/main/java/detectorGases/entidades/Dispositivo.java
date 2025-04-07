package detectorGases.entidades;

public class Dispositivo {
	
	protected int id;
	protected String nombre;
	protected int IdGrupo;//(FK) Id del grupo
	
	public Dispositivo(int id, String nombre, int idGrupo) {
		super();
		this.id = id;
		this.nombre = nombre;
		IdGrupo = idGrupo;
	}
	
}
