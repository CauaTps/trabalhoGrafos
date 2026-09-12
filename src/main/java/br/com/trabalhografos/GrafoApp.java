package br.com.trabalhografos;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GrafoApp extends Application {

    private ListaA lista;
    private Label nomeArquivo;
    private Label quantidadeVertices;
    private Label mensagem;
    private ListView<String> listaAdjacencia;
    private Pane areaGrafo;
    private TextArea resultado;
    private Button analisar;
    private boolean exemploLoja;
    private String[] nomes = {"Tela da loja", "Carrinho", "Catalogo", "Pedidos",
            "Estoque", "Pagamentos", "Cartao", "Antifraude"};
    private Map<String, Point2D> posicoes = new HashMap<>();

    @Override
    public void start(Stage stage) {

        BorderPane raiz = new BorderPane();

        raiz.setTop(criarCabecalho());
        raiz.setCenter(criarConteudo(stage));
        raiz.setBottom(criarRodape());
        raiz.setStyle("-fx-background-color: #f4f6f8;");

        Scene scene = new Scene(raiz, 900, 600);

        stage.setTitle("Trabalho de Grafos");
        stage.setScene(scene);
        stage.setMinWidth(720);
        stage.setMinHeight(500);
        stage.setMaximized(true);
        stage.show();
    }

    private VBox criarCabecalho() {

        Label titulo = new Label("Analise de Estruturas de Software");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 25));
        titulo.setTextFill(Color.WHITE);

        Label subtitulo = new Label("Identificacao de modulos criticos por pontos de articulacao");
        subtitulo.setFont(Font.font("Arial", 14));
        subtitulo.setTextFill(Color.web("#dbeafe"));

        VBox cabecalho = new VBox(5, titulo, subtitulo);
        cabecalho.setPadding(new Insets(22, 28, 22, 28));
        cabecalho.setStyle("-fx-background-color: #1e3a5f;");

        return cabecalho;
    }

    private HBox criarConteudo(Stage stage) {

        VBox painelArquivo = criarPainelArquivo(stage);
        VBox painelLista = criarPainelGrafo();

        HBox.setHgrow(painelLista, Priority.ALWAYS);

        HBox conteudo = new HBox(20, painelArquivo, painelLista);
        conteudo.setPadding(new Insets(24));

        return conteudo;
    }

    private VBox criarPainelArquivo(Stage stage) {

        Label titulo = new Label("Arquivo do grafo");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 17));

        Label explicacao = new Label(
                "A primeira linha lista todos os vertices. Nas demais linhas, " +
                "o primeiro numero é o vertice e os seguintes sao seus vizinhos."
        );
        explicacao.setWrapText(true);
        explicacao.setTextFill(Color.web("#475569"));

        Button abrirArquivo = new Button("Abrir arquivo .txt");
        abrirArquivo.setMaxWidth(Double.MAX_VALUE);
        abrirArquivo.setStyle(
                "-fx-background-color: #2563eb;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;" +
                "-fx-background-radius: 7;" +
                "-fx-padding: 11 16;"
        );
        abrirArquivo.setOnAction(event -> abrirArquivo(stage));

        Label rotuloNome = new Label("Arquivo selecionado:");
        rotuloNome.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        nomeArquivo = new Label("Nenhum arquivo selecionado");
        nomeArquivo.setWrapText(true);
        nomeArquivo.setTextFill(Color.web("#64748b"));

        quantidadeVertices = new Label("Vertices: 0");
        quantidadeVertices.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        quantidadeVertices.setTextFill(Color.web("#1e3a5f"));

        analisar = new Button("Pontos de articulacao");
        analisar.setMaxWidth(Double.MAX_VALUE);
        analisar.setDisable(true);
        analisar.setOnAction(event -> executarArticulacao());

        resultado = new TextArea();
        resultado.setEditable(false);
        resultado.setWrapText(true);
        resultado.setPrefRowCount(15);
        resultado.setStyle("-fx-font-family: Consolas; -fx-font-size: 12px;");
        resultado.setPromptText("O resultado da DFS aparece aqui.");

        VBox painel = new VBox(
                14,
                titulo,
                explicacao,
                abrirArquivo,
                rotuloNome,
                nomeArquivo,
                quantidadeVertices,
                analisar,
                resultado
        );
        painel.setPrefWidth(390);
        painel.setMinWidth(340);
        VBox.setVgrow(resultado, Priority.ALWAYS);
        painel.setPadding(new Insets(20));
        painel.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #dbe2ea;" +
                "-fx-border-radius: 10;"
        );

        return painel;
    }

    private VBox criarPainelGrafo() {

        Label titulo = new Label("Representacao do grafo");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 17));

        Label explicacao = new Label("Ligacao = integracao entre modulos (sem direcao). Borda vermelha = modulo critico.\n" +
                "Arraste os circulos para organizar.");
        explicacao.setWrapText(true);
        explicacao.setTextFill(Color.web("#64748b"));

        areaGrafo = new Pane();
        areaGrafo.setMinHeight(260);
        areaGrafo.setStyle(
                "-fx-background-color: #f8fafc;" +
                "-fx-border-color: #dbe2ea;" +
                "-fx-border-radius: 7;" +
                "-fx-background-radius: 7;"
        );
        areaGrafo.widthProperty().addListener((observavel, valorAntigo, valorNovo) -> desenharGrafo());
        areaGrafo.heightProperty().addListener((observavel, valorAntigo, valorNovo) -> desenharGrafo());

        Label tituloLista = new Label("Lista de adjacencia");
        tituloLista.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        listaAdjacencia = new ListView<>();
        listaAdjacencia.setPlaceholder(new Label("Abra um arquivo TXT para exibir o grafo."));
        listaAdjacencia.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 15px;");
        listaAdjacencia.setPrefHeight(170);

        VBox.setVgrow(areaGrafo, Priority.ALWAYS);

        VBox painel = new VBox(7, titulo, explicacao, areaGrafo, tituloLista, listaAdjacencia);
        painel.setPadding(new Insets(20));
        painel.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #dbe2ea;" +
                "-fx-border-radius: 10;"
        );

        return painel;
    }

    private HBox criarRodape() {

        mensagem = new Label("Aguardando a selecao de um arquivo.");
        mensagem.setTextFill(Color.web("#475569"));

        HBox rodape = new HBox(mensagem);
        rodape.setAlignment(Pos.CENTER_LEFT);
        rodape.setPadding(new Insets(10, 24, 10, 24));
        rodape.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #dbe2ea;" +
                "-fx-border-width: 1 0 0 0;"
        );

        return rodape;
    }

    private void abrirArquivo(Stage stage) {

        FileChooser seletor = new FileChooser();
        seletor.setTitle("Selecione o arquivo do grafo");
        seletor.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivos de texto (*.txt)", "*.txt")
        );

        File arquivo = seletor.showOpenDialog(stage);

        if (arquivo != null) {
            carregarArquivo(arquivo);
        }
    }

    private void carregarArquivo(File arquivo) {

        exemploLoja = false;
        try {
            lista = new ListaA(arquivo);
            exemploLoja = conferirExemplo();
            resultado.clear();
            analisar.setDisable(false);
            if (exemploLoja) resultado.setText(
                    "EXEMPLO FICTICIO: SISTEMA DE LOJA\n\n" +
                    "Cada vertice é um modulo. As ligacoes representam integracoes.\n\n" +
                    "Articulacao: quais modulos separam partes do grafo ao serem removidos?\n\n" +
                    "A analise representa este modelo, nao todos os efeitos de uma falha real.");

            nomeArquivo.setText(arquivo.getName());
            quantidadeVertices.setText("Vertices: " + lista.getVertices().size());
            mensagem.setText("Arquivo lido com sucesso.");

            exibirLista();
            posicoes.clear();
            desenharGrafo();

        } catch (Exception erro) {
            lista = null;
            resultado.clear();
            analisar.setDisable(true);
            nomeArquivo.setText("Nenhum arquivo selecionado");
            listaAdjacencia.getItems().clear();
            areaGrafo.getChildren().clear();
            posicoes.clear();
            quantidadeVertices.setText("Vertices: 0");
            mensagem.setText("Nao foi possivel ler o arquivo.");

            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro ao abrir arquivo");
            alerta.setHeaderText("O arquivo nao pode ser lido");
            alerta.setContentText(erro.getMessage());
            alerta.showAndWait();
        }
    }

    private void exibirLista() {

        listaAdjacencia.getItems().clear();

        for (int i = 0; i < lista.getVertices().size(); i++) {

            Vertice vertice = lista.getVertices().get(i);
            String linha = nomeModulo(vertice);
            Aresta aresta = vertice.getInicio();

            while (aresta != null) {
                linha = linha + " -> " + aresta.getDestino();

                if (aresta.getPeso() != null) {
                    linha = linha + "," + aresta.getPeso();
                }

                aresta = aresta.getProx();
            }

            listaAdjacencia.getItems().add(linha);
        }
    }

    private void executarArticulacao() {
        try {
            PontoArticulacao algoritmo = new PontoArticulacao(lista);
            algoritmo.visitaVertices();

            String pontos = "";
            String ordem = "";
            String tabela = "Vertice  Pre  Menor  Pai\n";
            for (int pre = 1; pre <= lista.getVertices().size(); pre++) {
                for (int i = 0; i < lista.getVertices().size(); i++) {
                    Vertice v = lista.getVertices().get(i);
                    if (v.getPrenum() == pre) {
                        if (!ordem.isEmpty()) ordem = ordem + " -> ";
                        ordem = ordem + v.getRotulo();
                    }
                }
            }
            for (int i = 0; i < lista.getVertices().size(); i++) {
                Vertice v = lista.getVertices().get(i);
                String pai = "-";
                if (v.getPai() != null) pai = v.getPai().getRotulo();
                tabela = tabela + String.format("%-8s %-4d %-6d %s%n",
                        v.getRotulo(), v.getPrenum(), v.getMenor(), pai);
                if (v.isArticulacao()) {
                    if (!pontos.isEmpty()) pontos = pontos + ", ";
                    pontos = pontos + nomeModulo(v);
                }
            }
            if (pontos.isEmpty()) pontos = "Nenhum";
            resultado.setText("Pontos: " + pontos + "\n\nOrdem da DFS:\n" + ordem + "\n\n" + tabela);
            if (exemploLoja) resultado.appendText("\nINTERPRETACAO\n" +
                    "Sem 4 - Pedidos: {Tela da loja, Carrinho, Catalogo, Estoque} ficam separados de " +
                    "{Pagamentos, Cartao, Antifraude}.\n\n" +
                    "Sem 6 - Pagamentos: {Cartao, Antifraude} ficam separados do restante.\n\n" +
                    "Esses modulos merecem atencao nos testes, na manutencao e nas alternativas de integracao.");
            mensagem.setText("Analise concluida. Borda vermelha = ponto de articulacao.");
            desenharGrafo();
        } catch (IllegalArgumentException erro) {
            resultado.setText(erro.getMessage());
            mensagem.setText("Confira o arquivo antes de executar a analise.");
        }
    }

    private void desenharGrafo() {

        areaGrafo.getChildren().clear();

        if (lista == null || lista.getVertices().isEmpty()) {
            return;
        }

        double largura = Math.max(areaGrafo.getWidth(), 500);
        double altura = Math.max(areaGrafo.getHeight(), 260);
        double centroX = largura / 2;
        double centroY = altura / 2;
        double raio = Math.min(largura, altura) * 0.35;
        int quantidade = lista.getVertices().size();

        for (int i = 0; i < quantidade; i++) {

            Vertice vertice = lista.getVertices().get(i);

            if (!posicoes.containsKey(vertice.getRotulo())) {
                double angulo = (2 * Math.PI * i / quantidade) - Math.PI / 2;
                double x = centroX + Math.cos(angulo) * raio;
                double y = centroY + Math.sin(angulo) * raio;
                posicoes.put(vertice.getRotulo(), new Point2D(x, y));
            }
        }

        desenharArestas();

        for (int i = 0; i < quantidade; i++) {
            Vertice vertice = lista.getVertices().get(i);
            desenharVertice(vertice);
        }
    }

    private void desenharArestas() {

        Set<String> arestasDesenhadas = new HashSet<>();

        for (int i = 0; i < lista.getVertices().size(); i++) {

            Vertice origem = lista.getVertices().get(i);
            Aresta aresta = origem.getInicio();

            while (aresta != null) {

                Point2D pontoOrigem = posicoes.get(origem.getRotulo());
                Point2D pontoDestino = posicoes.get(aresta.getDestino());

                String primeiro = origem.getRotulo().toLowerCase();
                String segundo = aresta.getDestino().toLowerCase();
                String chave;

                if (primeiro.compareTo(segundo) < 0) {
                    chave = primeiro + "-" + segundo;
                } else {
                    chave = segundo + "-" + primeiro;
                }

                if (pontoOrigem != null && pontoDestino != null && !arestasDesenhadas.contains(chave)) {
                    Line linha = new Line(
                            pontoOrigem.getX(),
                            pontoOrigem.getY(),
                            pontoDestino.getX(),
                            pontoDestino.getY()
                    );
                    linha.setStroke(Color.web("#64748b"));
                    linha.setStrokeWidth(2.2);
                    areaGrafo.getChildren().add(linha);
                    arestasDesenhadas.add(chave);
                }

                aresta = aresta.getProx();
            }
        }
    }

    private void desenharVertice(Vertice vertice) {

        Point2D ponto = posicoes.get(vertice.getRotulo());

        Circle circulo = new Circle(22);
        circulo.setFill(Color.WHITE);
        if (vertice.isArticulacao()) circulo.setFill(Color.web("#fed7aa"));
        circulo.setStroke(Color.web("#2563eb"));

        if (vertice.isArticulacao()) circulo.setStroke(Color.web("#b91c1c"));
        circulo.setStrokeWidth(3);

        Text rotulo = new Text(vertice.getRotulo());
        rotulo.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        rotulo.setFill(Color.web("#172033"));
        rotulo.setMouseTransparent(true);
        rotulo.setX(-rotulo.getLayoutBounds().getWidth() / 2);
        rotulo.setY(5);

        Group grupo = new Group(circulo, rotulo);

        if (exemploLoja) {
            Text nome = new Text(nomeModulo(vertice));
            nome.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            nome.setX(-nome.getLayoutBounds().getWidth() / 2);
            nome.setY(-32);
            nome.setMouseTransparent(true);
            grupo.getChildren().add(nome);
        }
        if (vertice.isVisitado()) {
            Text dados = new Text("pre: " + vertice.getPrenum() + " | menor: " + vertice.getMenor());
            dados.setFont(Font.font("Arial", 11));
            dados.setX(-dados.getLayoutBounds().getWidth() / 2);
            dados.setY(38);
            dados.setMouseTransparent(true);
            grupo.getChildren().add(dados);
        }
        grupo.setLayoutX(ponto.getX());
        grupo.setLayoutY(ponto.getY());

        grupo.setOnMouseDragged(event -> {
            Point2D inicioArea = areaGrafo.localToScene(0, 0);
            double x = event.getSceneX() - inicioArea.getX();
            double y = event.getSceneY() - inicioArea.getY();

            x = Math.max(25, Math.min(areaGrafo.getWidth() - 25, x));
            y = Math.max(25, Math.min(areaGrafo.getHeight() - 25, y));

            posicoes.put(vertice.getRotulo(), new Point2D(x, y));
            desenharGrafo();
        });

        areaGrafo.getChildren().add(grupo);
    }

    public static void main(String[] args) {
        launch(args);
    }

    private String nomeModulo(Vertice v) {
        if (exemploLoja) return v.getRotulo() + " - " + nomes[Integer.parseInt(v.getRotulo()) - 1];
        return v.getRotulo();
    }

    private boolean conferirExemplo() {
        //so associa os nomes da loja quando o arquivo representa esse mesmo exemplo
        String[] vizinhos = {"2 3", "1 4 5", "1 4", "2 3 5 6", "2 4", "4 7 8", "6 8", "6 7"};
        if (lista.getVertices().size() != 8) return false;
        for (int i = 1; i <= 8; i++) {
            Vertice v = lista.buscarVertice("" + i);
            if (v == null) return false;
            Set<String> encontrados = new HashSet<>();
            Aresta a = v.getInicio();
            while (a != null) {
                if (!encontrados.add(a.getDestino())) return false;
                a = a.getProx();
            }
            Set<String> esperados = new HashSet<>(java.util.Arrays.asList(vizinhos[i - 1].split(" ")));
            if (!encontrados.equals(esperados)) return false;
        }
        return true;
    }

}
