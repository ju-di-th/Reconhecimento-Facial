import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

public class TreinadorFacial {

    // Método auxiliar para converter Mat para BufferedImage (o mesmo do DetectorFacial)
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

        // Configurações Iniciais
        String nomeUsuario = JOptionPane.showInputDialog("Digite o NOME do usuário a ser treinado:");
        
        // O ID será usado para nomear a pasta do usuário
        int idUsuario = 0;
        try {
            idUsuario = Integer.parseInt(JOptionPane.showInputDialog("Digite o ID numérico (ex: 1, 2, 3...) para " + nomeUsuario + ":"));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID inválido. Usando ID 0.");
            idUsuario = 0;
        }
        
        int numeroDeAmostras = 25;
        int amostraAtual = 1;

        // Acessando a Webcam
        VideoCapture camera = new VideoCapture(0);
        if (!camera.isOpened()) {
            System.out.println("Erro: A câmera não pôde ser acessada.");
            return;
        }

        // Preparando o Detector de Faces
        String caminhoClassificador = "C:/Faculdade/opencvantigo/build/etc/haarcascades/haarcascade_frontalface_alt.xml";
        CascadeClassifier detectorFace = new CascadeClassifier(caminhoClassificador);
        if (detectorFace.empty()) {
            System.out.println("Erro: Não foi possível carregar o classificador de faces. Verifique o caminho: " + caminhoClassificador);
            return;
        }
        
        // Cria a pasta para o usuário
        File pastaUsuario = new File("fotos/" + idUsuario + "_" + nomeUsuario.replaceAll("\\s+", ""));
        if (!pastaUsuario.exists()) {
            pastaUsuario.mkdirs();
        }

        // Criando a Janela para Exibir o Vídeo
        JFrame janela = new JFrame("Treinamento Facial - Capturando Amostras para " + nomeUsuario);
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel labelVideo = new JLabel();
        janela.setContentPane(labelVideo);
        janela.setVisible(true);

        Mat frame = new Mat();

        // Loop para Capturar as Amostras
        System.out.println("Iniciando captura de " + numeroDeAmostras + " amostras para o usuário " + nomeUsuario + " (ID: " + idUsuario + ").");
        while (true) { 
            if (camera.read(frame)) {
                Mat frameCinza = new Mat();
                Imgproc.cvtColor(frame, frameCinza, Imgproc.COLOR_BGR2GRAY);
                MatOfRect facesDetectadas = new MatOfRect();
                detectorFace.detectMultiScale(frameCinza, facesDetectadas, 1.3, 5, 0, new Size(150, 150), new Size(500, 500));

                for (Rect rect : facesDetectadas.toArray()) {
                    // Desenha um retângulo na imagem colorida
                    Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0), 2);

                    // Só salva a imagem se ainda não atingimos o limite
                    if (amostraAtual <= numeroDeAmostras) {
                        // Extrai a face, converte para cinza e redimensiona
                        Mat faceRecortada = new Mat(frameCinza, rect);
                        Imgproc.resize(faceRecortada, faceRecortada, new Size(160, 160));

                        // Salva a imagem na pasta do usuário
                        String nomeArquivo = pastaUsuario.getAbsolutePath() + "/" + amostraAtual + ".jpg";
                        Imgcodecs.imwrite(nomeArquivo, faceRecortada);

                        System.out.println("Foto " + amostraAtual + " capturada e salva em " + nomeArquivo);
                        amostraAtual++;
                        
                        // NOVO CÓDIGO: Pausa a thread para dar tempo de mudar a pose
                        try {
                            Thread.sleep(500); // Pausa de 500 milissegundos (meio segundo)
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                
                // Se atingimos o limite, saímos do loop
                if (amostraAtual > numeroDeAmostras) {
                    break; 
                }
            }

                // Mostra a imagem na tela
                BufferedImage imagem = matParaBufferedImage(frame);
                labelVideo.setIcon(new ImageIcon(imagem));
                janela.pack();
            }
   

        System.out.println("Captura de amostras concluída para " + nomeUsuario + "!");
        janela.dispose(); // Fecha a janela da câmera
        camera.release();
        
        // Neste novo plano, não há treinamento aqui. O "treinamento" é a coleta das imagens.
        // O reconhecimento será feito em tempo real, comparando a imagem da webcam com as imagens salvas.
    }
  
}

