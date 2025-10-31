public class Usuario {
    private int id;
    private String nome;
    private int nivelAcesso;

    public Usuario(int id, String nome, int nivelAcesso) {
        this.id = id;
        this.nome = nome;
        this.nivelAcesso = nivelAcesso;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public int getNivelAcesso() { return nivelAcesso; }
}
