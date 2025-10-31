import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SistemaAutenticacao {

    // Método auxiliar para converter Mat para BufferedImage
    public static BufferedImage matParaBufferedImage(Mat mat) {
        int tipo = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            tipo = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] buffer = new byte[bufferSize];
        mat.get(0, 0, buffer);
        BufferedImage imagem = new BufferedImage(mat.cols(), mat.rows(), tipo);
        final byte[] targetPixels = ((DataBufferByte) imagem.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return imagem;
    }

    public static void main(String[] args) {
        // Carrega a biblioteca nativa do OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Inicialização dos Módulos
        GerenciadorUsuarios gerenciador = new GerenciadorUsuarios("usuarios.txt");
        ReconhecedorSimples reconhecedor = new ReconhecedorSimples();

        // Variáveis de Controle
        final int MAX_FALHAS = 5;
        int falhasConsecutivas = 0;
        long tempoBloqueio = 0;
        final long DURACAO_BLOQUEIO = 10000; // 10 segundos de bloqueio

        // Variável para controlar se o acesso já foi dado (para que a janela de dados abra apenas uma vez)
        Map<Integer, Boolean> acessoConcedido = new HashMap<>();

        // Configuração do Detector de Faces e Webcam 
        String caminhoClassificador = "C:/faculdade/opencvantigo/build/etc/haarcascades/haarcascade_frontalface_alt.xml"; // VERIFIQUE O CAMINHO
        CascadeClassifier detectorFace = new CascadeClassifier(caminhoClassificador);
        if (detectorFace.empty()) {
            System.err.println("Erro: Não foi possível carregar o classificador de faces.");
            return;
        }

        VideoCapture camera = new VideoCapture(0);
        if (!camera.isOpened()) {
            System.err.println("Erro: A câmera não pôde ser acessada.");
            return;
        }

        // Criação da Janela e Loop Principal
        JFrame janela = new JFrame("Sistema de Autenticação Biométrica");
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel labelVideo = new JLabel();
        janela.setContentPane(labelVideo);
        janela.setVisible(true);

        Mat frame = new Mat();
        Mat grayFrame = new Mat();
        MatOfRect facesDetectadas = new MatOfRect();
        
        String statusAutenticacao = "Aguardando Usuário...";
        int idAutenticado = -1;

        while (true) {
            // LÓGICA DE BLOQUEIO
            if (tempoBloqueio > 0 && System.currentTimeMillis() < tempoBloqueio) {
                statusAutenticacao = "SISTEMA BLOQUEADO! Tente novamente em " + (tempoBloqueio - System.currentTimeMillis()) / 1000 + " segundos.";
                
                if (camera.read(frame)) {
                    Imgproc.putText(frame, statusAutenticacao, new Point(10, 30), Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0, 0, 255), 2);
                    BufferedImage imagem = matParaBufferedImage(frame);
                    labelVideo.setIcon(new ImageIcon(imagem));
                    janela.pack();
                }
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue; // Pula o restante do loop
            }

            if (camera.read(frame)) {
                Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
                
                // Detecta faces
                detectorFace.detectMultiScale(grayFrame, facesDetectadas, 1.3, 5, 0, new Size(150, 150), new Size(500, 500));

                if (facesDetectadas.toArray().length > 0) {
                    Rect rect = facesDetectadas.toArray()[0];
                    Mat rostoCapturado = new Mat(grayFrame, rect);
                    
                    // Lógica de Reconhecimento
                    int id = reconhecedor.reconhecer(rostoCapturado);
                    
                    if (id != -1) {
                        idAutenticado = id;
                        Usuario usuario = gerenciador.getUsuarioPorId(idAutenticado);
                        
                        if (usuario != null) {
                            // SUCESSO: Acesso Concedido
                            statusAutenticacao = "Acesso Concedido: " + usuario.getNome() + " (Nível " + usuario.getNivelAcesso() + ")";
                            Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0), 3);
                            
                            // Lógica de Abertura da Janela de Dados (Abre apenas uma vez)
                            if (!acessoConcedido.containsKey(id) || !acessoConcedido.get(id)) {
                                simularAcesso(usuario); // Abre a janela de dados
                                acessoConcedido.put(id, true); // Marca como acessado
                                
                                // LOG E FECHAMENTO DA CÂMERA
                                LoggerAcesso.registrarAcesso(usuario.getNome(), usuario.getNivelAcesso(), "SUCESSO (Janela Aberta)");
                                falhasConsecutivas = 0; // Reseta as falhas
                                break; // SAI DO while(true)
                            }
                            
                            falhasConsecutivas = 0; // Reseta as falhas
                            
                        } else {
                            // ERRO: Usuário Reconhecido, mas sem cadastro
                            statusAutenticacao = "Erro: Usuário Reconhecido, mas sem cadastro no sistema.";
                            Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 255), 3);
                            LoggerAcesso.registrarAcesso("ID " + id, 0, "FALHA - ID Reconhecido, mas sem Cadastro");
                        }
                    } else {
                        // FALHA: Rosto Detectado, mas Não Reconhecido
                        statusAutenticacao = "Acesso Negado: Rosto não reconhecido.";
                        idAutenticado = -1;
                        Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255), 3);
                        
                        // Conta a falha
                        falhasConsecutivas++;
                        LoggerAcesso.registrarAcesso("DESCONHECIDO", 0, "FALHA - Rosto Não Reconhecido (Tentativa " + falhasConsecutivas + ")");
                        
                        // Bloqueia se atingir o limite
                        if (falhasConsecutivas >= MAX_FALHAS) {
                            tempoBloqueio = System.currentTimeMillis() + DURACAO_BLOQUEIO;
                            falhasConsecutivas = 0; // Reseta o contador
                            statusAutenticacao = "SISTEMA BLOQUEADO POR FALHAS CONSECUTIVAS!";
                            LoggerAcesso.registrarAcesso("SISTEMA", 0, "BLOQUEIO DE 10 SEGUNDOS ATIVADO");
                        }
                    }
                } else {
                    statusAutenticacao = "Aguardando Usuário...";
                    idAutenticado = -1;
                }

                // Exibição do Status na Tela
                Imgproc.putText(frame, statusAutenticacao, new Point(10, 30), Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(255, 255, 255), 2);

                // Mostra o frame na janela
                BufferedImage imagem = matParaBufferedImage(frame);
                labelVideo.setIcon(new ImageIcon(imagem));
                janela.pack();

            } else {
                System.err.println("Erro: Não foi possível ler o frame da câmera.");
                break;
            }
        } // Fim do while(true)

        // CÓDIGO DE FECHAMENTO APÓS O BREAK
        janela.dispose(); // Fecha a janela da câmera
        camera.release(); // Libera a câmera ao sair
        
        // O programa continua rodando para manter a janela de dados aberta
    }
    
    // Método de Simulação de Acesso
    private static void simularAcesso(Usuario usuario) {
        StringBuilder dadosAcessados = new StringBuilder();
        
        // Aviso de Acesso Cumulativo
        if (usuario.getNivelAcesso() > 1) {
            dadosAcessados.append("================================================\n");
            dadosAcessados.append("  AVISO: ACESSO CUMULATIVO\n");
            dadosAcessados.append("  Seu nível de acesso (" + usuario.getNivelAcesso() + ") permite a visualização\n");
            dadosAcessados.append("  de informações de níveis inferiores (Nível 1 e/ou Nível 2).\n");
            dadosAcessados.append("================================================\n\n");
        }
        
        // Nível 1: Todos podem ver
        if (usuario.getNivelAcesso() >= 1) {
            dadosAcessados.append("--- DADOS NÍVEL 1 (Acesso Público) ---\n");
            dadosAcessados.append(lerArquivo("dados_nivel1.txt"));
            dadosAcessados.append("\n\n");
        }
        
        // Nível 2: Diretores e Ministro podem ver
        if (usuario.getNivelAcesso() >= 2) {
            dadosAcessados.append("--- DADOS NÍVEL 2 (Restrito a Diretores) ---\n");
            dadosAcessados.append(lerArquivo("dados_nivel2.txt"));
            dadosAcessados.append("\n\n");
        }
        
        // Nível 3: Apenas o Ministro pode ver
        if (usuario.getNivelAcesso() >= 3) {
            dadosAcessados.append("--- DADOS NÍVEL 3 (Restrito ao Ministro) ---\n");
            dadosAcessados.append(lerArquivo("dados_nivel3.txt"));
            dadosAcessados.append("\n\n");
        }
        
        // Exibir os dados em uma nova janela
        JFrame frameAcesso = new JFrame("Acesso Autorizado - Nível " + usuario.getNivelAcesso());
        JTextArea textArea = new JTextArea(dadosAcessados.toString());
        textArea.setEditable(false);
        
        // GARANTE QUE FECHAR ESTA JANELA NÃO ENCERRA O PROGRAMA INTEIRO
        frameAcesso.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        
        frameAcesso.add(new JScrollPane(textArea));
        frameAcesso.setSize(600, 400);
        frameAcesso.setVisible(true);
    }
    
    // Método auxiliar para ler o conteúdo de um arquivo
    private static String lerArquivo(String nomeArquivo) {
        StringBuilder conteudo = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                conteudo.append(linha).append("\n");
            }
        } catch (IOException e) {
            return "ERRO: Arquivo " + nomeArquivo + " não encontrado na raiz do projeto.";
        }
        return conteudo.toString();
    }
}
