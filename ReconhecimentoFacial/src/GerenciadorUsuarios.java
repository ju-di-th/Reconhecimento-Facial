import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GerenciadorUsuarios {
    private Map<Integer, Usuario> usuarios = new HashMap<>();

    public GerenciadorUsuarios(String caminhoArquivo) {
        carregarUsuarios(caminhoArquivo);
    }

    private void carregarUsuarios(String caminhoArquivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(",");
                if (dados.length == 3) {
                    try {
                        int id = Integer.parseInt(dados[0].trim());
                        String nome = dados[1].trim();
                        int nivelAcesso = Integer.parseInt(dados[2].trim());
                        usuarios.put(id, new Usuario(id, nome, nivelAcesso));
                    } catch (NumberFormatException e) {
                        System.err.println("Erro ao ler linha (ID ou Nível não é número): " + linha);
                    }
                }
            }
            System.out.println("Usuários carregados: " + usuarios.size());
        } catch (IOException e) {
            System.err.println("Erro ao carregar arquivo de usuários: " + caminhoArquivo);
            System.err.println("Certifique-se de criar o arquivo 'usuarios.txt' na raiz do projeto.");
        }
    }

    public Usuario getUsuarioPorId(int id) {
        return usuarios.get(id);
    }
}
