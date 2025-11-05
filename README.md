# Reconhecimento-Facial
Projeto Acadêmico de Visão Computacional em Java. Desenvolve um sistema de identificação facial via webcam (OpenCV) para autenticação. Simula o acesso a dados confidenciais do Ministério do Meio Ambiente, aplicando regras de segurança e tratamento de falhas (bloqueio temporário).

# Sistema de Identificação e Autenticação Biométrica (Webcam)

Este projeto foi desenvolvido como Atividade Prática Supervisionada (APS) com o objetivo de criar uma **ferramenta de segurança biométrica** robusta, utilizando a webcam para autenticação facial e implementando um sistema de controle de acesso hierárquico.

O sistema simula a restrição de acesso a dados estratégicos do Ministério do Meio Ambiente, garantindo que apenas usuários autorizados (Analistas, Diretores e Ministro) possam visualizar informações confidenciais, de acordo com seus respectivos níveis de permissão.

## Funcionalidades Principais

*   **Autenticação Biométrica Facial:** Utiliza a webcam para capturar o rosto do usuário em tempo real.
*   **Reconhecimento de Features:** Implementa um algoritmo de comparação de histogramas (baseado em `Imgproc.compareHist` do OpenCV) para identificar o usuário com base em imagens de referência previamente treinadas.
*   **Controle de Acesso Hierárquico (Níveis 1, 2 e 3):**
    *   **Nível 1 (Público):** Acesso a dados gerais.
    *   **Nível 2 (Restrito):** Acesso a relatórios de não conformidade.
    *   **Nível 3 (Confidencial):** Acesso a dados estratégicos e mapas georeferenciados.
*   **Lógica de Acesso Cumulativa:** Usuários de níveis superiores (ex: Nível 3) têm acesso automático a todos os dados de níveis inferiores (Nível 1 e 2).
*   **Segurança e Tratamento de Erros:** Implementação de um contador de falhas com **bloqueio temporário** do sistema após tentativas consecutivas de acesso não autorizado.
*   **Auditoria e Relatórios:** Geração de um **Log de Acesso** (`log_acessos.txt`) para rastreabilidade de todas as tentativas de autenticação (sucesso e falha).
*   **Experiência de Usuário Refinada:** Fechamento automático da janela de vídeo após o sucesso da autenticação e manutenção da janela de dados aberta para consulta.

## Tecnologias Utilizadas

| Tecnologia | Função no Projeto |
| :--- | :--- |
| **Java (JDK 8+)** | Linguagem de programação principal, garantindo portabilidade e robustez. |
| **OpenCV (Open Source Computer Vision Library)** | Biblioteca de Visão Computacional para detecção facial (`Haar Cascades`) e comparação de features. |
| **Eclipse IDE** | Ambiente de Desenvolvimento Integrado utilizado para a codificação e execução. |
| **Swing/AWT** | Utilizado para a criação da interface gráfica (janela de vídeo e janela de dados). |

## Como Executar o Projeto

### Pré-requisitos

1.  **Java Development Kit (JDK 8 ou superior)** instalado.
2.  **Eclipse IDE** configurado.
3.  **OpenCV** configurado no Build Path do projeto (com a DLL nativa vinculada).

### Passos

1.  **Clonar o Repositório:**
    ```bash
    git clone https://github.com/ju-di-th/Reconhecimento-Facial.git
    ```
2.  **Configurar Dados:**
    *   Crie o arquivo `usuarios.txt` na raiz do projeto para definir os IDs e Níveis de Acesso.
    *   Crie os arquivos `dados_nivel1.txt`, `dados_nivel2.txt` e `dados_nivel3.txt` com o conteúdo de simulação.
3.  **Treinamento Facial:**
    *   Execute a classe `TreinadorFacial.java`.
    *   Siga as instruções para capturar 25 amostras de cada usuário autorizado (associando o ID correto).
4.  **Autenticação:**
    *   Execute a classe principal `SistemaAutenticacao.java`.
    *   Posicione-se em frente à webcam para iniciar o processo de autenticação e controle de acesso.

---

*Desenvolvido para fins acadêmicos.*
