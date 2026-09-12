package br.com.trabalhografos;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

public class GrafoApp extends Application {
    private static final double RAIO_VERTICE = 24;
    private static final Color COR_FUNDO = Color.web("#f7f8fb");
    private static final Color COR_PRIMARIA = Color.web("#2563eb");
    private static final Color COR_VERTICE = Color.web("#ffffff");
    private static final Color COR_ARESTA = Color.web("#475569");
    private static final Color COR_DESTAQUE = Color.web("#f59e0b");

    private final Grafo grafo = new Grafo();
    private final Map<String, Point2D> posicoes = new LinkedHashMap<>();
    private final ObservableList<String> verticesTela = FXCollections.observableArrayList();
    private final Pane areaGrafo = new Pane();
    private final ListView<String> listaAdjacencia = new ListView<>();
    private final ComboBox<String> origemAlgoritmo = new ComboBox<>(verticesTela);
    private final ComboBox<String> destinoAlgoritmo = new ComboBox<>(verticesTela);
    private final TextArea resultado = new TextArea();
    private final Label contador = new Label();
    private final Set<String> verticesDestacados = new HashSet<>();
    private final Set<ArestaChave> arestasDestacadas = new HashSet<>();

    private CheckBox grafoDirecionado;

    @Override
    public void start(Stage stage) {
        BorderPane raiz = new BorderPane();
        raiz.setTop(criarCabecalho());
        raiz.setLeft(criarPainelLateral());
        raiz.setCenter(criarAreaCentral());
        raiz.setBottom(criarRodape());
        raiz.setStyle("-fx-background-color: #f7f8fb;");

        carregarExemplo();

        Scene scene = new Scene(raiz, 1100, 720);
        stage.setTitle("Trabalho de Grafos");
        stage.setScene(scene);
        stage.setMinWidth(920);
        stage.setMinHeight(620);
        stage.show();
    }

    private HBox criarCabecalho() {
        Label titulo = new Label("Visualizador de Grafos");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 22));
        titulo.setTextFill(Color.web("#111827"));

        Label subtitulo = new Label("vertices, arestas, BFS, DFS e menor caminho");
        subtitulo.setTextFill(Color.web("#64748b"));

        VBox textos = new VBox(2, titulo, subtitulo);
        HBox cabecalho = new HBox(16, textos);
        cabecalho.setAlignment(Pos.CENTER_LEFT);
        cabecalho.setPadding(new Insets(18, 22, 14, 22));
        cabecalho.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
        return cabecalho;
    }

    private VBox criarPainelLateral() {
        TextField campoVertice = new TextField();
        campoVertice.setPromptText("Nome do vertice");

        Button adicionarVertice = botaoPrimario("Adicionar vertice");
        adicionarVertice.setMaxWidth(Double.MAX_VALUE);
        adicionarVertice.setOnAction(event -> {
            String nome = normalizarNome(campoVertice.getText());
            if (nome.isEmpty()) {
                informar("Digite o nome do vertice.");
                return;
            }
            adicionarVertice(nome);
            campoVertice.clear();
        });

        TextField campoOrigem = new TextField();
        campoOrigem.setPromptText("Origem");
        TextField campoDestino = new TextField();
        campoDestino.setPromptText("Destino");
        TextField campoPeso = new TextField("1");
        campoPeso.setPromptText("Peso");

        Button adicionarAresta = botaoPrimario("Adicionar aresta");
        adicionarAresta.setMaxWidth(Double.MAX_VALUE);
        adicionarAresta.setOnAction(event -> adicionarAresta(campoOrigem, campoDestino, campoPeso));

        grafoDirecionado = new CheckBox("Grafo direcionado");
        grafoDirecionado.selectedProperty().addListener((observable, oldValue, newValue) -> {
            grafo.setDirecionado(newValue);
            limparDestaques();
            atualizarTela();
        });

        origemAlgoritmo.setPromptText("Inicio");
        origemAlgoritmo.setMaxWidth(Double.MAX_VALUE);
        destinoAlgoritmo.setPromptText("Destino");
        destinoAlgoritmo.setMaxWidth(Double.MAX_VALUE);

        Button bfs = botaoSecundario("Rodar BFS");
        bfs.setMaxWidth(Double.MAX_VALUE);
        bfs.setOnAction(event -> executarBfs());

        Button dfs = botaoSecundario("Rodar DFS");
        dfs.setMaxWidth(Double.MAX_VALUE);
        dfs.setOnAction(event -> executarDfs());

        Button dijkstra = botaoSecundario("Menor caminho");
        dijkstra.setMaxWidth(Double.MAX_VALUE);
        dijkstra.setOnAction(event -> executarMenorCaminho());

        Button exemplo = botaoSecundario("Carregar exemplo");
        exemplo.setMaxWidth(Double.MAX_VALUE);
        exemplo.setOnAction(event -> carregarExemplo());

        Button limpar = botaoPerigo("Limpar grafo");
        limpar.setMaxWidth(Double.MAX_VALUE);
        limpar.setOnAction(event -> {
            grafo.limpar();
            posicoes.clear();
            limparDestaques();
            atualizarTela();
            informar("Grafo limpo.");
        });

        VBox painel = new VBox(
                10,
                secao("Vertices"),
                campoVertice,
                adicionarVertice,
                separador(),
                secao("Arestas"),
                grafoDirecionado,
                campoOrigem,
                campoDestino,
                campoPeso,
                adicionarAresta,
                separador(),
                secao("Algoritmos"),
                origemAlgoritmo,
                destinoAlgoritmo,
                bfs,
                dfs,
                dijkstra,
                separador(),
                exemplo,
                limpar
        );
        painel.setPadding(new Insets(18));
        painel.setPrefWidth(260);
        painel.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-width: 0 1 0 0;");
        return painel;
    }

    private BorderPane criarAreaCentral() {
        areaGrafo.setMinSize(400, 380);
        areaGrafo.setStyle("-fx-background-color: #f7f8fb;");
        areaGrafo.widthProperty().addListener((ChangeListener<Number>) (observable, oldValue, newValue) -> renderizarGrafo());
        areaGrafo.heightProperty().addListener((ChangeListener<Number>) (observable, oldValue, newValue) -> renderizarGrafo());

        listaAdjacencia.setPrefHeight(170);
        listaAdjacencia.setFocusTraversable(false);

        resultado.setEditable(false);
        resultado.setWrapText(true);
        resultado.setPrefRowCount(4);
        resultado.setStyle("-fx-control-inner-background: #ffffff;");

        Label tituloLista = secao("Lista de adjacencia");
        Label tituloResultado = secao("Resultado");

        VBox painelInferior = new VBox(8, tituloLista, listaAdjacencia, tituloResultado, resultado);
        painelInferior.setPadding(new Insets(12, 18, 18, 18));
        painelInferior.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-width: 1 0 0 0;");

        BorderPane centro = new BorderPane();
        centro.setCenter(areaGrafo);
        centro.setBottom(painelInferior);
        return centro;
    }

    private HBox criarRodape() {
        contador.setTextFill(Color.web("#475569"));
        Label dica = new Label("Dica: arraste os vertices para reorganizar o desenho.");
        dica.setTextFill(Color.web("#64748b"));
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        HBox rodape = new HBox(12, contador, espaco, dica);
        rodape.setAlignment(Pos.CENTER_LEFT);
        rodape.setPadding(new Insets(8, 16, 8, 16));
        rodape.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-width: 1 0 0 0;");
        return rodape;
    }

    private void adicionarAresta(TextField campoOrigem, TextField campoDestino, TextField campoPeso) {
        String origem = normalizarNome(campoOrigem.getText());
        String destino = normalizarNome(campoDestino.getText());
        if (origem.isEmpty() || destino.isEmpty()) {
            informar("Preencha origem e destino.");
            return;
        }

        double peso;
        try {
            peso = Double.parseDouble(campoPeso.getText().trim().replace(',', '.'));
            if (peso < 0) {
                informar("O peso precisa ser maior ou igual a zero.");
                return;
            }
        } catch (NumberFormatException ex) {
            informar("Peso invalido. Use um numero, por exemplo: 1 ou 2.5");
            return;
        }

        adicionarVertice(origem);
        adicionarVertice(destino);
        grafo.adicionarAresta(origem, destino, peso);
        limparDestaques();
        atualizarTela();
        informar("Aresta adicionada: " + origem + " -> " + destino + " (peso " + formatarPeso(peso) + ").");
    }

    private void adicionarVertice(String nome) {
        if (grafo.adicionarVertice(nome)) {
            posicoes.put(nome, proximaPosicao());
        }
        limparDestaques();
        atualizarTela();
    }

    private void executarBfs() {
        Optional<String> inicio = verticeSelecionado(origemAlgoritmo, "Escolha o vertice inicial para BFS.");
        if (inicio.isEmpty()) {
            return;
        }
        List<String> ordem = grafo.bfs(inicio.get());
        destacarVertices(ordem);
        resultado.setText("BFS a partir de " + inicio.get() + ":\n" + String.join(" -> ", ordem));
    }

    private void executarDfs() {
        Optional<String> inicio = verticeSelecionado(origemAlgoritmo, "Escolha o vertice inicial para DFS.");
        if (inicio.isEmpty()) {
            return;
        }
        List<String> ordem = grafo.dfs(inicio.get());
        destacarVertices(ordem);
        resultado.setText("DFS a partir de " + inicio.get() + ":\n" + String.join(" -> ", ordem));
    }

    private void executarMenorCaminho() {
        Optional<String> inicio = verticeSelecionado(origemAlgoritmo, "Escolha o vertice inicial.");
        Optional<String> destino = verticeSelecionado(destinoAlgoritmo, "Escolha o vertice de destino.");
        if (inicio.isEmpty() || destino.isEmpty()) {
            return;
        }

        Caminho caminho = grafo.menorCaminho(inicio.get(), destino.get());
        if (caminho.vertices().isEmpty()) {
            limparDestaques();
            resultado.setText("Nao existe caminho de " + inicio.get() + " ate " + destino.get() + ".");
            return;
        }

        destacarCaminho(caminho.vertices());
        resultado.setText(
                "Menor caminho de " + inicio.get() + " ate " + destino.get() + ":\n"
                        + String.join(" -> ", caminho.vertices())
                        + "\nCusto total: " + formatarPeso(caminho.distancia())
        );
    }

    private void carregarExemplo() {
        grafo.limpar();
        posicoes.clear();
        limparDestaques();
        grafo.setDirecionado(false);
        if (grafoDirecionado != null) {
            grafoDirecionado.setSelected(false);
        }

        List<String> nomes = List.of("A", "B", "C", "D", "E", "F");
        for (String nome : nomes) {
            grafo.adicionarVertice(nome);
        }

        grafo.adicionarAresta("A", "B", 2);
        grafo.adicionarAresta("A", "C", 4);
        grafo.adicionarAresta("B", "D", 1);
        grafo.adicionarAresta("C", "D", 3);
        grafo.adicionarAresta("C", "E", 2);
        grafo.adicionarAresta("D", "F", 5);
        grafo.adicionarAresta("E", "F", 1);

        double largura = Math.max(areaGrafo.getWidth(), 620);
        double altura = Math.max(areaGrafo.getHeight(), 360);
        double centroX = largura / 2;
        double centroY = altura / 2;
        double raio = Math.min(largura, altura) * 0.33;
        for (int i = 0; i < nomes.size(); i++) {
            double angulo = (2 * Math.PI * i / nomes.size()) - Math.PI / 2;
            posicoes.put(nomes.get(i), new Point2D(centroX + Math.cos(angulo) * raio, centroY + Math.sin(angulo) * raio));
        }

        atualizarTela();
        origemAlgoritmo.setValue("A");
        destinoAlgoritmo.setValue("F");
        informar("Exemplo carregado. Use os botoes de algoritmos para testar.");
    }

    private void atualizarTela() {
        verticesTela.setAll(grafo.vertices());
        listaAdjacencia.getItems().setAll(grafo.descreverAdjacencia());
        contador.setText(grafo.vertices().size() + " vertices | " + grafo.arestas().size() + " arestas");
        renderizarGrafo();
    }

    private void renderizarGrafo() {
        areaGrafo.getChildren().clear();
        double largura = Math.max(areaGrafo.getWidth(), 420);
        double altura = Math.max(areaGrafo.getHeight(), 320);

        for (String vertice : grafo.vertices()) {
            posicoes.putIfAbsent(vertice, proximaPosicao());
        }

        for (Aresta aresta : grafo.arestas()) {
            Point2D origem = posicoes.get(aresta.origem());
            Point2D destino = posicoes.get(aresta.destino());
            if (origem == null || destino == null) {
                continue;
            }
            desenharAresta(aresta, origem, destino);
        }

        for (String vertice : grafo.vertices()) {
            Point2D ponto = limitar(posicoes.getOrDefault(vertice, new Point2D(largura / 2, altura / 2)));
            posicoes.put(vertice, ponto);
            desenharVertice(vertice, ponto);
        }
    }

    private void desenharAresta(Aresta aresta, Point2D origem, Point2D destino) {
        Point2D direcao = destino.subtract(origem);
        if (direcao.magnitude() == 0) {
            return;
        }
        Point2D unidade = direcao.normalize();
        Point2D inicio = origem.add(unidade.multiply(RAIO_VERTICE));
        Point2D fim = destino.subtract(unidade.multiply(RAIO_VERTICE));

        boolean destaque = arestasDestacadas.contains(new ArestaChave(aresta.origem(), aresta.destino()))
                || (!grafo.isDirecionado() && arestasDestacadas.contains(new ArestaChave(aresta.destino(), aresta.origem())));

        Line linha = new Line(inicio.getX(), inicio.getY(), fim.getX(), fim.getY());
        linha.setStroke(destaque ? COR_DESTAQUE : COR_ARESTA);
        linha.setStrokeWidth(destaque ? 4 : 2);
        areaGrafo.getChildren().add(linha);

        if (grafo.isDirecionado()) {
            areaGrafo.getChildren().add(criarSeta(fim, unidade, destaque));
        }

        Text peso = new Text(formatarPeso(aresta.peso()));
        peso.setFont(Font.font("System", FontWeight.BOLD, 12));
        peso.setFill(Color.web("#334155"));
        peso.setX((inicio.getX() + fim.getX()) / 2 + 6);
        peso.setY((inicio.getY() + fim.getY()) / 2 - 6);
        areaGrafo.getChildren().add(peso);
    }

    private Polygon criarSeta(Point2D ponta, Point2D direcao, boolean destaque) {
        Point2D normal = new Point2D(-direcao.getY(), direcao.getX());
        double tamanho = 12;
        Point2D base = ponta.subtract(direcao.multiply(tamanho));
        Point2D p1 = base.add(normal.multiply(6));
        Point2D p2 = base.subtract(normal.multiply(6));

        Polygon seta = new Polygon(
                ponta.getX(), ponta.getY(),
                p1.getX(), p1.getY(),
                p2.getX(), p2.getY()
        );
        seta.setFill(destaque ? COR_DESTAQUE : COR_ARESTA);
        return seta;
    }

    private void desenharVertice(String nome, Point2D ponto) {
        boolean destaque = verticesDestacados.contains(nome);
        Circle circulo = new Circle(RAIO_VERTICE);
        circulo.setFill(destaque ? Color.web("#fff7ed") : COR_VERTICE);
        circulo.setStroke(destaque ? COR_DESTAQUE : COR_PRIMARIA);
        circulo.setStrokeWidth(destaque ? 4 : 2.5);
        circulo.setEffect(null);

        Text texto = new Text(nome);
        texto.setFont(Font.font("System", FontWeight.BOLD, 14));
        texto.setFill(Color.web("#111827"));
        texto.setMouseTransparent(true);
        texto.setX(-texto.getLayoutBounds().getWidth() / 2);
        texto.setY(5);

        Group grupo = new Group(circulo, texto);
        grupo.setLayoutX(ponto.getX());
        grupo.setLayoutY(ponto.getY());
        grupo.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                grupo.setUserData(new Point2D(event.getX(), event.getY()));
            }
        });
        grupo.setOnMouseDragged(event -> {
            Point2D novoPonto = limitar(new Point2D(event.getSceneX() - areaGrafo.localToScene(0, 0).getX(), event.getSceneY() - areaGrafo.localToScene(0, 0).getY()));
            posicoes.put(nome, novoPonto);
            renderizarGrafo();
        });

        areaGrafo.getChildren().add(grupo);
    }

    private Point2D proximaPosicao() {
        double largura = Math.max(areaGrafo.getWidth(), 600);
        double altura = Math.max(areaGrafo.getHeight(), 360);
        int indice = Math.max(posicoes.size(), 0);
        double angulo = (2 * Math.PI * indice / Math.max(grafo.vertices().size(), 1)) - Math.PI / 2;
        double raio = Math.min(largura, altura) * 0.32;
        return new Point2D(largura / 2 + Math.cos(angulo) * raio, altura / 2 + Math.sin(angulo) * raio);
    }

    private Point2D limitar(Point2D ponto) {
        double largura = Math.max(areaGrafo.getWidth(), 420);
        double altura = Math.max(areaGrafo.getHeight(), 320);
        double x = Math.max(RAIO_VERTICE + 10, Math.min(largura - RAIO_VERTICE - 10, ponto.getX()));
        double y = Math.max(RAIO_VERTICE + 10, Math.min(altura - RAIO_VERTICE - 10, ponto.getY()));
        return new Point2D(x, y);
    }

    private void destacarVertices(List<String> ordem) {
        verticesDestacados.clear();
        arestasDestacadas.clear();
        verticesDestacados.addAll(ordem);
        renderizarGrafo();
    }

    private void destacarCaminho(List<String> caminho) {
        verticesDestacados.clear();
        arestasDestacadas.clear();
        verticesDestacados.addAll(caminho);
        for (int i = 0; i < caminho.size() - 1; i++) {
            arestasDestacadas.add(new ArestaChave(caminho.get(i), caminho.get(i + 1)));
        }
        renderizarGrafo();
    }

    private void limparDestaques() {
        verticesDestacados.clear();
        arestasDestacadas.clear();
    }

    private Optional<String> verticeSelecionado(ComboBox<String> combo, String mensagemErro) {
        String vertice = combo.getValue();
        if (vertice == null || !grafo.contemVertice(vertice)) {
            informar(mensagemErro);
            return Optional.empty();
        }
        return Optional.of(vertice);
    }

    private void informar(String mensagem) {
        resultado.setText(mensagem);
    }

    private String normalizarNome(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase();
    }

    private Label secao(String texto) {
        Label label = new Label(texto);
        label.setFont(Font.font("System", FontWeight.BOLD, 13));
        label.setTextFill(Color.web("#111827"));
        return label;
    }

    private Region separador() {
        Region linha = new Region();
        linha.setMinHeight(1);
        linha.setPrefHeight(1);
        linha.setMaxHeight(1);
        linha.setStyle("-fx-background-color: #e5e7eb;");
        VBox.setMargin(linha, new Insets(6, 0, 6, 0));
        return linha;
    }

    private Button botaoPrimario(String texto) {
        Button botao = new Button(texto);
        botao.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 12;");
        return botao;
    }

    private Button botaoSecundario(String texto) {
        Button botao = new Button(texto);
        botao.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #1e40af; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 12;");
        return botao;
    }

    private Button botaoPerigo(String texto) {
        Button botao = new Button(texto);
        botao.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 12;");
        return botao;
    }

    private String formatarPeso(double peso) {
        if (peso == Math.rint(peso)) {
            return String.valueOf((long) peso);
        }
        return String.format("%.2f", peso);
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static final class Grafo {
        private final Set<String> vertices = new LinkedHashSet<>();
        private final List<Aresta> arestas = new ArrayList<>();
        private boolean direcionado;

        boolean adicionarVertice(String nome) {
            return vertices.add(nome);
        }

        void adicionarAresta(String origem, String destino, double peso) {
            adicionarVertice(origem);
            adicionarVertice(destino);

            for (int i = 0; i < arestas.size(); i++) {
                Aresta existente = arestas.get(i);
                boolean mesmaDirecao = existente.origem().equals(origem) && existente.destino().equals(destino);
                boolean inversaNaoDirecionada = !direcionado && existente.origem().equals(destino) && existente.destino().equals(origem);
                if (mesmaDirecao || inversaNaoDirecionada) {
                    arestas.set(i, new Aresta(origem, destino, peso));
                    return;
                }
            }

            arestas.add(new Aresta(origem, destino, peso));
        }

        List<String> bfs(String inicio) {
            List<String> ordem = new ArrayList<>();
            Set<String> visitados = new LinkedHashSet<>();
            ArrayDeque<String> fila = new ArrayDeque<>();
            fila.add(inicio);
            visitados.add(inicio);

            Map<String, List<Aresta>> adjacencia = adjacencia();
            while (!fila.isEmpty()) {
                String atual = fila.removeFirst();
                ordem.add(atual);
                for (Aresta aresta : adjacencia.getOrDefault(atual, List.of())) {
                    if (visitados.add(aresta.destino())) {
                        fila.addLast(aresta.destino());
                    }
                }
            }

            return ordem;
        }

        List<String> dfs(String inicio) {
            List<String> ordem = new ArrayList<>();
            Set<String> visitados = new LinkedHashSet<>();
            dfsRecursivo(inicio, visitados, ordem, adjacencia());
            return ordem;
        }

        private void dfsRecursivo(String atual, Set<String> visitados, List<String> ordem, Map<String, List<Aresta>> adjacencia) {
            visitados.add(atual);
            ordem.add(atual);
            for (Aresta aresta : adjacencia.getOrDefault(atual, List.of())) {
                if (!visitados.contains(aresta.destino())) {
                    dfsRecursivo(aresta.destino(), visitados, ordem, adjacencia);
                }
            }
        }

        Caminho menorCaminho(String inicio, String destino) {
            Map<String, Double> distancia = new HashMap<>();
            Map<String, String> anterior = new HashMap<>();
            for (String vertice : vertices) {
                distancia.put(vertice, Double.POSITIVE_INFINITY);
            }
            distancia.put(inicio, 0.0);

            PriorityQueue<EntradaFila> fila = new PriorityQueue<>(Comparator.comparingDouble(EntradaFila::distancia));
            fila.add(new EntradaFila(inicio, 0));
            Map<String, List<Aresta>> adjacencia = adjacencia();

            while (!fila.isEmpty()) {
                EntradaFila atual = fila.poll();
                if (atual.distancia() > distancia.get(atual.vertice())) {
                    continue;
                }
                if (atual.vertice().equals(destino)) {
                    break;
                }
                for (Aresta aresta : adjacencia.getOrDefault(atual.vertice(), List.of())) {
                    double novaDistancia = atual.distancia() + aresta.peso();
                    if (novaDistancia < distancia.get(aresta.destino())) {
                        distancia.put(aresta.destino(), novaDistancia);
                        anterior.put(aresta.destino(), atual.vertice());
                        fila.add(new EntradaFila(aresta.destino(), novaDistancia));
                    }
                }
            }

            if (distancia.get(destino) == Double.POSITIVE_INFINITY) {
                return new Caminho(List.of(), Double.POSITIVE_INFINITY);
            }

            ArrayList<String> caminho = new ArrayList<>();
            String atual = destino;
            while (atual != null) {
                caminho.add(0, atual);
                atual = anterior.get(atual);
            }
            return new Caminho(caminho, distancia.get(destino));
        }

        List<String> descreverAdjacencia() {
            Map<String, List<Aresta>> adjacencia = adjacencia();
            List<String> linhas = new ArrayList<>();
            for (String vertice : vertices) {
                List<Aresta> vizinhos = adjacencia.getOrDefault(vertice, List.of());
                if (vizinhos.isEmpty()) {
                    linhas.add(vertice + " -> sem vizinhos");
                    continue;
                }
                StringBuilder linha = new StringBuilder(vertice).append(" -> ");
                for (int i = 0; i < vizinhos.size(); i++) {
                    Aresta aresta = vizinhos.get(i);
                    if (i > 0) {
                        linha.append(", ");
                    }
                    linha.append(aresta.destino()).append(" (").append(formatarPesoEstatico(aresta.peso())).append(")");
                }
                linhas.add(linha.toString());
            }
            return linhas;
        }

        Map<String, List<Aresta>> adjacencia() {
            Map<String, List<Aresta>> mapa = new LinkedHashMap<>();
            for (String vertice : vertices) {
                mapa.put(vertice, new ArrayList<>());
            }
            for (Aresta aresta : arestas) {
                mapa.computeIfAbsent(aresta.origem(), chave -> new ArrayList<>()).add(aresta);
                if (!direcionado) {
                    mapa.computeIfAbsent(aresta.destino(), chave -> new ArrayList<>()).add(new Aresta(aresta.destino(), aresta.origem(), aresta.peso()));
                }
            }
            return mapa;
        }

        boolean contemVertice(String vertice) {
            return vertices.contains(vertice);
        }

        Set<String> vertices() {
            return vertices;
        }

        List<Aresta> arestas() {
            return arestas;
        }

        boolean isDirecionado() {
            return direcionado;
        }

        void setDirecionado(boolean direcionado) {
            this.direcionado = direcionado;
        }

        void limpar() {
            vertices.clear();
            arestas.clear();
        }

        private static String formatarPesoEstatico(double peso) {
            if (peso == Math.rint(peso)) {
                return String.valueOf((long) peso);
            }
            return String.format("%.2f", peso);
        }
    }

    private record Aresta(String origem, String destino, double peso) {
    }

    private record ArestaChave(String origem, String destino) {
    }

    private record EntradaFila(String vertice, double distancia) {
    }

    private record Caminho(List<String> vertices, double distancia) {
    }
}
