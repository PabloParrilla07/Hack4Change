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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int getIdGrupo() {
		return IdGrupo;
	}

	public void setIdGrupo(int idGrupo) {
		IdGrupo = idGrupo;
	}
	
	
	
}
