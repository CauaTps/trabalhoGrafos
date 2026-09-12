package br.com.trabalhografos;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class GrafoApp extends Application {

    private ListaA lista;
    private Label nomeArquivo;
    private Label quantidadeVertices;
    private Label mensagem;
    private ListView<String> listaAdjacencia;

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
        stage.show();
    }

    private VBox criarCabecalho() {

        Label titulo = new Label("Lista de Adjacencia");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 25));
        titulo.setTextFill(Color.WHITE);

        Label subtitulo = new Label("Carregue um arquivo TXT para representar o grafo");
        subtitulo.setFont(Font.font("Arial", 14));
        subtitulo.setTextFill(Color.web("#dbeafe"));

        VBox cabecalho = new VBox(5, titulo, subtitulo);
        cabecalho.setPadding(new Insets(22, 28, 22, 28));
        cabecalho.setStyle("-fx-background-color: #1e3a5f;");

        return cabecalho;
    }

    private HBox criarConteudo(Stage stage) {

        VBox painelArquivo = criarPainelArquivo(stage);
        VBox painelLista = criarPainelLista();

        HBox.setHgrow(painelLista, Priority.ALWAYS);

        HBox conteudo = new HBox(20, painelArquivo, painelLista);
        conteudo.setPadding(new Insets(24));

        return conteudo;
    }

    private VBox criarPainelArquivo(Stage stage) {

        Label titulo = new Label("Arquivo do grafo");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 17));

        Label explicacao = new Label(
                "A primeira informacao de cada linha representa um vertice. " +
                "As informacoes seguintes representam seus vizinhos."
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

        VBox painel = new VBox(
                14,
                titulo,
                explicacao,
                abrirArquivo,
                rotuloNome,
                nomeArquivo,
                quantidadeVertices
        );
        painel.setPrefWidth(270);
        painel.setPadding(new Insets(20));
        painel.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #dbe2ea;" +
                "-fx-border-radius: 10;"
        );

        return painel;
    }

    private VBox criarPainelLista() {

        Label titulo = new Label("Grafo carregado");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 17));

        Label explicacao = new Label("Representacao por lista de adjacencia");
        explicacao.setTextFill(Color.web("#64748b"));

        listaAdjacencia = new ListView<>();
        listaAdjacencia.setPlaceholder(new Label("Abra um arquivo TXT para exibir o grafo."));
        listaAdjacencia.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 15px;");

        VBox.setVgrow(listaAdjacencia, Priority.ALWAYS);

        VBox painel = new VBox(7, titulo, explicacao, listaAdjacencia);
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

        try {
            lista = new ListaA(arquivo);

            nomeArquivo.setText(arquivo.getName());
            quantidadeVertices.setText("Vertices: " + lista.getVertices().size());
            mensagem.setText("Arquivo lido com sucesso.");

            exibirLista();

        } catch (Exception erro) {
            lista = null;
            listaAdjacencia.getItems().clear();
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

    public static void main(String[] args) {
        launch(args);
    }
}
