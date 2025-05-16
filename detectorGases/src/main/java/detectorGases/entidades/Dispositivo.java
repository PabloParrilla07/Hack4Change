package detectorGases.entidades;

public class Dispositivo {
	
	protected int dispositivoId;
	protected String name;
	protected int grupoId;//(FK) Id del grupo
	
	public Dispositivo(int id, String nombre, int grupoid) {
		super();
		this.dispositivoId = id;
		this.name = nombre;
		grupoId = grupoid;
	}

	public int getId() {
		return dispositivoId;
	}

	public void setId(int id) {
		this.dispositivoId = id;
	}

	public String getNombre() {
		return name;
	}

	public void setNombre(String nombre) {
		this.name = nombre;
	}

	public int getIdGrupo() {
		return grupoId;
	}

	public void setIdGrupo(int idGrupo) {
		grupoId = idGrupo;
	}
	
	
	
}
