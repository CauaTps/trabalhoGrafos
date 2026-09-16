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
import javafx.scene.control.ScrollPane;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    private Button colorir;
    private Button proximaEtapa;
    private Button reiniciarEtapas;
    private Label explicacaoEtapa;
    private Label contadorEtapas;
    private ArrayList<EtapaAlgoritmo> etapas = new ArrayList<>();
    private int indiceEtapa = -1;
    private EtapaAlgoritmo etapaAtual;
    private String resumoFinal = "";
    private boolean mostrarArticulacao;
    private Map<String, Point2D> posicoes = new HashMap<>();
    private Map<String, Integer> coresVertices = new HashMap<>();
    private Color[] paletaCores = {
            Color.web("#bfdbfe"),
            Color.web("#bbf7d0"),
            Color.web("#fde68a"),
            Color.web("#e9d5ff"),
            Color.web("#fecaca"),
            Color.web("#a7f3d0"),
            Color.web("#fbcfe8"),
            Color.web("#c7d2fe")
    };

    @Override
    public void start(Stage stage) {

        BorderPane raiz = new BorderPane();

        raiz.setTop(criarCabecalho());
        //em uma janela menor permite rolar sem esconder os controles
        ScrollPane conteudo = new ScrollPane(criarConteudo(stage));
        conteudo.setFitToWidth(true);
        conteudo.setFitToHeight(true);
        conteudo.setStyle("-fx-background-color: transparent; -fx-background: #f4f6f8;");
        raiz.setCenter(conteudo);
        raiz.setBottom(criarRodape());
        raiz.setStyle("-fx-background-color: #f4f6f8;");

        Scene scene = new Scene(raiz, 900, 600);

        stage.setTitle("Trabalho de Grafos");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(740);
        stage.setMaximized(true);
        stage.show();
    }

    private VBox criarCabecalho() {

        Label titulo = new Label("Analise de Grafos");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 25));
        titulo.setTextFill(Color.WHITE);

        Label subtitulo = new Label("Pontos de articulacao e coloracao de vertices");
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
                "o primeiro rotulo (numero ou letra) é o vertice e os seguintes sao seus vizinhos."
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
        analisar.setStyle(
                "-fx-background-color: #1e3a5f;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 7;" +
                "-fx-padding: 10 14;"
        );
        analisar.setOnAction(event -> executarArticulacao());

        colorir = new Button("Coloracao");
        colorir.setMaxWidth(Double.MAX_VALUE);
        colorir.setDisable(true);
        colorir.setStyle(
                "-fx-background-color: #15803d;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 7;" +
                "-fx-padding: 10 14;"
        );
        colorir.setOnAction(event -> executarColoracao());

        resultado = new TextArea();
        resultado.setEditable(false);
        resultado.setWrapText(true);
        resultado.setPrefRowCount(15);
        resultado.setStyle("-fx-font-family: Consolas; -fx-font-size: 13px;");
        resultado.setPromptText("O teste de mesa e o resultado do algoritmo aparecem aqui.");

        VBox painel = new VBox(
                9,
                titulo,
                explicacao,
                abrirArquivo,
                rotuloNome,
                nomeArquivo,
                quantidadeVertices,
                analisar,
                colorir,
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

        Label explicacao = new Label("Escolha um algoritmo e avance em Proxima etapa. Arraste os vertices para organizar.\n"
                + "Anel laranja = atual; anel roxo = vizinho; linha laranja = ligacao analisada.\n"
                + "Articulacao: azul claro = visitado; borda vermelha = articulacao confirmada.\n"
                + "Coloracao: branco = ainda sem cor; preenchimento e numero = cor atribuida.");
        explicacao.setWrapText(true);
        explicacao.setTextFill(Color.web("#64748b"));

        proximaEtapa = new Button("Proxima etapa >");
        proximaEtapa.setDisable(true);
        proximaEtapa.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 9 14;");
        proximaEtapa.setOnAction(event -> avancarEtapa());
        reiniciarEtapas = new Button("Reiniciar passos");
        reiniciarEtapas.setDisable(true);
        reiniciarEtapas.setOnAction(event -> { indiceEtapa = -1; avancarEtapa(); });
        contadorEtapas = new Label("Nenhuma execucao iniciada");
        HBox controles = new HBox(10, proximaEtapa, reiniciarEtapas, contadorEtapas);
        controles.setAlignment(Pos.CENTER_LEFT);
        explicacaoEtapa = new Label("Escolha Pontos de articulacao ou Coloracao para comecar.");
        explicacaoEtapa.setWrapText(true);
        explicacaoEtapa.setMinHeight(78);
        explicacaoEtapa.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e3a5f;");

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
        listaAdjacencia.setPrefHeight(110);
        listaAdjacencia.setMinHeight(60);

        VBox.setVgrow(areaGrafo, Priority.ALWAYS);

        VBox painel = new VBox(7, titulo, explicacao, controles, explicacaoEtapa, areaGrafo, tituloLista, listaAdjacencia);
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

        limparVisualizacao();
        mostrarArticulacao = false;
        coresVertices.clear();
        try {
            lista = new ListaA(arquivo);
            resultado.clear();
            analisar.setDisable(false);
            colorir.setDisable(false);

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
            colorir.setDisable(true);
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
            String linha = vertice.getRotulo();
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

    private void limparVisualizacao() {
        //mostra so o algoritmo escolhido, sem misturar os resultados no desenho
        mostrarArticulacao = false;
        coresVertices.clear();
        resultado.clear();
        etapas.clear();
        indiceEtapa = -1;
        etapaAtual = null;
        resumoFinal = "";
        proximaEtapa.setDisable(true);
        reiniciarEtapas.setDisable(true);
        contadorEtapas.setText("Nenhuma execucao iniciada");
        explicacaoEtapa.setText("Escolha um algoritmo para comecar.");
        desenharGrafo();
    }

    private void iniciarEtapas(ArrayList<EtapaAlgoritmo> novasEtapas, String resumo) {
        etapas = novasEtapas;
        resumoFinal = resumo;
        indiceEtapa = -1;
        reiniciarEtapas.setDisable(etapas.isEmpty());
        avancarEtapa();
    }

    private void avancarEtapa() {
        if (indiceEtapa + 1 >= etapas.size()) return;
        indiceEtapa++;
        etapaAtual = etapas.get(indiceEtapa);
        //mostra a copia daquele momento, nunca os valores de uma etapa futura
        etapaAtual.aplicar(lista, coresVertices);
        mostrarArticulacao = etapaAtual.isArticulacao();
        boolean terminou = indiceEtapa == etapas.size() - 1;
        String titulo = (mostrarArticulacao ? "ARTICULACAO" : "COLORACAO")
                + " - Etapa " + (indiceEtapa + 1) + " de " + etapas.size();
        contadorEtapas.setText((indiceEtapa + 1) + " / " + etapas.size());
        explicacaoEtapa.setText(etapaAtual.getExplicacao());
        StringBuilder texto = new StringBuilder(titulo + "\n\n" + etapaAtual.getExplicacao()
                + "\n\n" + etapaAtual.getTabela());
        if (terminou) texto.append("\nRESULTADO FINAL\n").append(resumoFinal);
        texto.append("\nHISTORICO ATE AQUI\n");
        for (int i = 0; i <= indiceEtapa; i++) {
            texto.append(i + 1).append(") ").append(etapas.get(i).getExplicacao()).append("\n\n");
        }
        resultado.setText(texto.toString());
        resultado.positionCaret(0);
        resultado.setScrollTop(0);
        proximaEtapa.setDisable(terminou);
        mensagem.setText(terminou ? "Execucao concluida. Reinicie os passos ou escolha outro algoritmo."
                : "Execucao pausada. Clique em Proxima etapa para continuar.");
        desenharGrafo();
    }

    private void executarArticulacao() {
        limparVisualizacao();
        try {
            PontoArticulacao algoritmo = new PontoArticulacao(lista);
            algoritmo.visitaVertices();
            mostrarArticulacao = true;

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
                    pontos = pontos + v.getRotulo();
                }
            }
            if (pontos.isEmpty()) pontos = "Nenhum";
            iniciarEtapas(algoritmo.getEtapas(), "Pontos: " + pontos
                    + "\n\nOrdem da DFS:\n" + ordem + "\n\n" + tabela);
        } catch (IllegalArgumentException erro) {
            resultado.setText(erro.getMessage());
            mensagem.setText("Confira o arquivo antes de executar a analise.");
        }
    }

    private void executarColoracao() {
        limparVisualizacao();
        try {
            Coloracao algoritmo = new Coloracao(lista);
            coresVertices.clear();
            coresVertices.putAll(algoritmo.colorir());

            Map<Integer, List<String>> grupos = algoritmo.agruparPorCor(coresVertices);
            String texto = "Coloracao dos vertices\n\n" +
                    "Vertices da mesma cor nao possuem aresta entre si.\n" +
                    "O algoritmo guloso nao garante o menor numero de cores.\n\n";

            for (Integer grupo : grupos.keySet()) {
                texto = texto + "Cor " + grupo + ": ";
                List<String> vertices = grupos.get(grupo);

                for (int i = 0; i < vertices.size(); i++) {
                    if (i > 0) {
                        texto = texto + ", ";
                    }
                    texto = texto + vertices.get(i);
                }

                texto = texto + "\n";
            }

            texto = texto + "\nTotal de cores utilizadas: " + grupos.size();


            iniciarEtapas(algoritmo.getEtapas(), texto);

        } catch (IllegalArgumentException erro) {
            resultado.setText(erro.getMessage());
            mensagem.setText("Confira o arquivo antes de executar a coloracao.");
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
        double raioX = largura * 0.35;
        double raioY = altura * 0.30;
        int quantidade = lista.getVertices().size();

        for (int i = 0; i < quantidade; i++) {

            Vertice vertice = lista.getVertices().get(i);

            if (!posicoes.containsKey(vertice.getRotulo())) {
                double angulo = (2 * Math.PI * i / quantidade) - Math.PI / 2;
                double x = centroX + Math.cos(angulo) * raioX;
                double y = centroY + Math.sin(angulo) * raioY;
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
                Vertice destino = lista.buscarVertice(aresta.getDestino());
                Point2D pontoDestino = destino == null ? null : posicoes.get(destino.getRotulo());

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
                    if (etapaAtual != null && etapaAtual.getAtual() != null && etapaAtual.getVizinho() != null
                            && ((origem.getRotulo().equalsIgnoreCase(etapaAtual.getAtual())
                            && aresta.getDestino().equalsIgnoreCase(etapaAtual.getVizinho()))
                            || (origem.getRotulo().equalsIgnoreCase(etapaAtual.getVizinho())
                            && aresta.getDestino().equalsIgnoreCase(etapaAtual.getAtual())))) {
                        linha.setStroke(Color.web("#ea580c"));
                        linha.setStrokeWidth(5);
                    }
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
        circulo.setFill(corPreenchimento(vertice));
        circulo.setStroke(Color.web("#2563eb"));

        if (mostrarArticulacao && vertice.isArticulacao()) circulo.setStroke(Color.web("#b91c1c"));
        circulo.setStrokeWidth(3);

        Text rotulo = new Text(vertice.getRotulo());
        rotulo.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        rotulo.setFill(Color.web("#172033"));
        rotulo.setMouseTransparent(true);
        rotulo.setX(-rotulo.getLayoutBounds().getWidth() / 2);
        rotulo.setY(5);

        Group grupo = new Group(circulo, rotulo);

        if (etapaAtual != null && (vertice.getRotulo().equals(etapaAtual.getAtual())
                || vertice.getRotulo().equals(etapaAtual.getVizinho()))) {
            boolean atual = vertice.getRotulo().equals(etapaAtual.getAtual());
            Circle destaque = new Circle(28, Color.TRANSPARENT);
            destaque.setStroke(Color.web(atual ? "#ea580c" : "#9333ea"));
            destaque.setStrokeWidth(3);
            destaque.setMouseTransparent(true);
            grupo.getChildren().add(destaque);
            Text papel = new Text(atual ? "ATUAL" : "VIZINHO");
            papel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            papel.setFill(destaque.getStroke());
            papel.setX(-papel.getLayoutBounds().getWidth() / 2);
            papel.setY(-34);
            papel.setMouseTransparent(true);
            grupo.getChildren().add(papel);
        }

        if (mostrarArticulacao && vertice.isVisitado()) {
            Text dados = new Text("pre: " + vertice.getPrenum() + " | menor: " + vertice.getMenor()
                    + "\npai: " + (vertice.getPai() == null ? "-" : vertice.getPai().getRotulo()));
            dados.setFont(Font.font("Arial", 11));
            dados.setX(-dados.getLayoutBounds().getWidth() / 2);
            dados.setY(38);
            dados.setMouseTransparent(true);
            grupo.getChildren().add(dados);
        }
        if (coresVertices.containsKey(vertice.getRotulo())) {
            Text grupoCor = new Text("cor " + coresVertices.get(vertice.getRotulo()));
            grupoCor.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            grupoCor.setX(-grupoCor.getLayoutBounds().getWidth() / 2);
            grupoCor.setY(38);
            grupoCor.setMouseTransparent(true);
            grupo.getChildren().add(grupoCor);
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

    private Color corPreenchimento(Vertice vertice) {
        Integer cor = coresVertices.get(vertice.getRotulo());

        if (cor != null) {
            return paletaCores[(cor - 1) % paletaCores.length];
        }

        if (mostrarArticulacao && vertice.isArticulacao()) {
            return Color.web("#fed7aa");
        }

        if (mostrarArticulacao && vertice.isVisitado()) return Color.web("#dbeafe");

        return Color.WHITE;
    }

}
