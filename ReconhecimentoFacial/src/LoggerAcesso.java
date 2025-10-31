import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerAcesso {
    private static final String NOME_ARQUIVO = "log_acessos.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // Este método registra a tentativa de acesso no arquivo de log
    public static void registrarAcesso(String nomeUsuario, int nivelAcesso, String status) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(NOME_ARQUIVO, true))) {
            String timestamp = LocalDateTime.now().format(FORMATTER);
            String logEntry = String.format("[%s] USUARIO: %s (Nivel %d) | STATUS: %s", 
                                            timestamp, nomeUsuario, nivelAcesso, status);
            pw.println(logEntry);
            System.out.println("LOG: " + status);
        } catch (IOException e) {
            System.err.println("Erro ao escrever no arquivo de log.");
        }
    }
}
