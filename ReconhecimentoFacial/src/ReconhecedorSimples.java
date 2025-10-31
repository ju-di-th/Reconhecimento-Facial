import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReconhecedorSimples {

    // Mapa para armazenar o ID do usuário e a imagem de referência (a "assinatura" do rosto)
    private Map<Integer, Mat> imagensReferencia = new HashMap<>();
    private final double LIMITE_SIMILARIDADE = 0.40; // Limite de similaridade (de 0 a 1)

    public ReconhecedorSimples() {
        carregarImagensReferencia();
    }

    // Carrega a primeira imagem de cada pasta de usuário como referência
    private void carregarImagensReferencia() {
        File pastaFotos = new File("fotos");
        File[] pastasUsuarios = pastaFotos.listFiles(File::isDirectory);

        if (pastasUsuarios == null) {
            System.err.println("Pasta 'fotos' não encontrada ou vazia. Execute o TreinadorFacial primeiro.");
            return;
        }

        for (File pasta : pastasUsuarios) {
            try {
                // O nome da pasta é "ID_Nome" (ex: 1_Ministro)
                int idUsuario = Integer.parseInt(pasta.getName().split("_")[0]);
                
                // Pega a primeira imagem de referência (a 1.jpg)
                File imagemRef = new File(pasta.getAbsolutePath() + "/1.jpg");

                if (imagemRef.exists()) {
                    Mat foto = Imgcodecs.imread(imagemRef.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
                    if (!foto.empty()) {
                        // Redimensiona para o tamanho padrão
                        Imgproc.resize(foto, foto, new Size(160, 160)); 
                        imagensReferencia.put(idUsuario, foto);
                        System.out.println("Referência carregada: ID " + idUsuario + " (" + pasta.getName() + ")");
                    }
                }
            } catch (Exception e) {
                System.err.println("Erro ao carregar referência da pasta: " + pasta.getName() + ". Verifique o nome da pasta.");
            }
        }
    }
    
    // Método para calcular o histograma da imagem (usado para comparação)
    private Mat calcularHistograma(Mat imagem) {
        Mat hist = new Mat();
        MatOfInt histSize = new MatOfInt(256);
        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        List<Mat> images = new ArrayList<>();
        images.add(imagem);
        
        // Calcula o histograma da imagem em escala de cinza
        Imgproc.calcHist(images, new MatOfInt(0), new Mat(), hist, histSize, ranges);
        Core.normalize(hist, hist, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        return hist;
    }

    // O método principal que faz o "reconhecimento"
    public int reconhecer(Mat rostoCapturado) {
        if (rostoCapturado.empty()) return -1;

        // 1. Prepara o rosto capturado (o que veio da webcam)
        Mat rostoPreparado = new Mat();
        Imgproc.resize(rostoCapturado, rostoPreparado, new Size(160, 160));
        Mat histRosto = calcularHistograma(rostoPreparado);
        
        int idReconhecido = -1;
        double melhorSimilaridade = 0.0;

        // 2. Compara com todas as referências
        for (Map.Entry<Integer, Mat> entry : imagensReferencia.entrySet()) {
            int idReferencia = entry.getKey();
            Mat imagemReferencia = entry.getValue();
            
            Mat histReferencia = calcularHistograma(imagemReferencia);
            
            // Compara os histogramas. O método 'CORREL' retorna 1.0 para similaridade perfeita.
            double similaridade = Imgproc.compareHist(histRosto, histReferencia, Imgproc.HISTCMP_CORREL);
            
            // Encontra a melhor correspondência
            if (similaridade > melhorSimilaridade) {
                melhorSimilaridade = similaridade;
                idReconhecido = idReferencia;
            }
        }

        // 3. Verifica se a melhor similaridade está acima do limite
        if (melhorSimilaridade >= LIMITE_SIMILARIDADE) {
            System.out.println("Melhor Similaridade: " + melhorSimilaridade);
            return idReconhecido; // Retorna o ID do usuário
        } else {
            System.out.println("Similaridade abaixo do limite: " + melhorSimilaridade);
            return -1; // Não reconhecido
        }
    }
}
