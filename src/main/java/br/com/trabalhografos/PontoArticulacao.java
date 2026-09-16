package br.com.trabalhografos;

import java.util.ArrayList;
import java.util.Map;

public class PontoArticulacao {

    private ListaA lista;

    //o contador vai dizer a ordem em que cada vertice foi visitado
    private int contador;
    private StringBuilder testeMesa = new StringBuilder();
    private int passo;
    private ArrayList<EtapaAlgoritmo> etapas = new ArrayList<>();
    private ArrayList<Vertice> ordemFinalizacao = new ArrayList<>();
    private ArrayList<Vertice> ordemVisita = new ArrayList<>();
    private String fase;

    public PontoArticulacao(ListaA lista) {
        this.lista = lista;
        contador = 0;
    }

    public void visitaVertices(){

        //limpa o texto para nao misturar uma execucao com a outra
        testeMesa.setLength(0);
        etapas.clear();
        ordemFinalizacao.clear();
        ordemVisita.clear();
        fase = "FASE 1/3 - VISITAR TODOS";
        passo = 0;
        validarGrafo();
        contador = 0;

        //aqui limpa os vertices caso o algoritmo ja tenha sido executado antes
        for (int i = 0; i < lista.getVertices().size(); i++) {
            Vertice vertice = lista.getVertices().get(i);
            vertice.setVisitado(false);
            vertice.setPrenum(0);
            vertice.setMenor(0);
            vertice.setPai(null);
            vertice.setArticulacao(false);
        }

        //procura o menor vertice que ainda nao foi visitado
        //começa pelo menor numero ou pela primeira letra em ordem alfabetica
        registrar("Inicio: vamos visitar todos e definir prenum e pai. Menor fica em 0 (ainda nao calculado). "
                + "Escolhemos primeiro o menor rotulo disponivel.", null, null);
        Vertice vertice = buscarMenorVerticeNaoVisitado();

        //depois que terminar uma parte do grafo ele procura se ficou algum sem visitar
        //isso tambem faz funcionar caso o grafo seja desconexo
        while (vertice != null) {
            registrarDetalhe("Nova raiz da DFS: " + vertice.getRotulo(), vertice, null);
            visitar(vertice);
            vertice = buscarMenorVerticeNaoVisitado();
        }
        fase = "FASE 2/3 - CALCULAR MENOR";
        registrar("Todos os vertices ja foram visitados. Agora partimos das folhas da arvore DFS. "
                + "Cada filho termina seu calculo antes do pai. Menor = 0 significa ainda nao calculado.", null, null);
        for (Vertice atual : ordemFinalizacao) {
            calcularMenor(atual);
        }

        fase = "FASE 3/3 - TESTAR ARTICULACOES";
        registrar("Todos os valores de menor estao prontos. So agora vamos fazer as comparacoes "
                + "para descobrir os pontos de articulacao.", null, null);
        for (Vertice atual : ordemVisita) {
            testarArticulacao(atual);
        }
        registrar("Busca concluida. Os vertices com borda vermelha sao pontos de articulacao.", null, null);
    }

    private void visitar(Vertice vertice) {

        //quando entra no vertice aumenta a ordem da visita
        contador++;

        //o prenum recebe exatamente a ordem em que esse vertice foi encontrado
        vertice.setPrenum(contador);
        ordemVisita.add(vertice);

        //marca como visitado para nao entrar nele de novo e ficar em loop
        vertice.setVisitado(true);
        registrar("Visita " + vertice.getRotulo() + ": prenum = " + contador
                + ", pai = " + (vertice.getPai() == null ? "nenhum (raiz)" : vertice.getPai().getRotulo())
                + ". Menor sera calculado depois da DFS.", vertice, vertice.getPai());
        registrarEstado(vertice);

        //entre todas as ligações desse vertice procura o menor vizinho nao visitado
        Vertice vizinho = buscarMenorVizinhoNaoVisitado(vertice);

        //vai visitando os vizinhos e quando volta procura o proximo menor
        while (vizinho != null) {
            vizinho.setPai(vertice);
            registrarDetalhe("Desce de " + vertice.getRotulo() + " para " + vizinho.getRotulo()
                    + ", o menor vizinho nao visitado. Pai de " + vizinho.getRotulo()
                    + " = " + vertice.getRotulo(), vertice, vizinho);
            visitar(vizinho);

            registrarDetalhe("Volta de " + vizinho.getRotulo() + " para " + vertice.getRotulo()
                    + " apenas para procurar outro vizinho nao visitado. Sem fazer contas nesta fase.", vertice, vizinho);
            vizinho = buscarMenorVizinhoNaoVisitado(vertice);
        }

        //guarda quem terminou primeiro: assim os filhos aparecem antes dos pais
        //na proxima fase usamos essa ordem para calcular de baixo para cima
        ordemFinalizacao.add(vertice);
        registrarDetalhe("Finaliza a visita de " + vertice.getRotulo()
                + ". Prenum e pai definidos. O calculo de menor fica para a fase 2.", vertice, null);
        registrarEstado(vertice);
    }

    private void calcularMenor(Vertice vertice) {

        //agora sim começa pelo proprio prenum e confere os outros caminhos
        vertice.setMenor(vertice.getPrenum());
        StringBuilder calculo = new StringBuilder("Menor de " + vertice.getRotulo()
                + ": considera prenum " + vertice.getPrenum());
        registrarDetalhe("Calcula menor de " + vertice.getRotulo() + ": comeca com seu prenum = "
                + vertice.getPrenum() + ". Os filhos, se houver, ja estao calculados.", vertice, null);

        Aresta aresta = vertice.getInicio();
        while (aresta != null) {
            Vertice vizinho = lista.buscarVertice(aresta.getDestino());
            int menorAntes = vertice.getMenor();
            if (vizinho == vertice.getPai()) {
                registrarDetalhe("Ligacao com o pai " + vizinho.getRotulo()
                        + ": nao vale como retorno alternativo.", vertice, vizinho);
            } else if (vizinho.getPai() == vertice) {
                //do filho usa o menor pronto, e nao o prenum dele
                if (vizinho.getMenor() < vertice.getMenor()) vertice.setMenor(vizinho.getMenor());
                calculo.append(", menor do filho ").append(vizinho.getRotulo()).append(" = ").append(vizinho.getMenor());
                registrarDetalhe("Usa menor do filho " + vizinho.getRotulo() + " em " + vertice.getRotulo()
                        + ": min(" + menorAntes + ", " + vizinho.getMenor() + ") = "
                        + vertice.getMenor(), vertice, vizinho);
            } else if (vizinho.getPrenum() < vertice.getPrenum()) {
                //no retorno para um ancestral usa o prenum, nao o menor dele
                if (vizinho.getPrenum() < vertice.getMenor()) vertice.setMenor(vizinho.getPrenum());
                calculo.append(", prenum do ancestral ").append(vizinho.getRotulo()).append(" = ").append(vizinho.getPrenum());
                registrarDetalhe("Retorno para o ancestral " + vizinho.getRotulo() + ": usa prenum "
                        + vizinho.getPrenum() + ". Menor de " + vertice.getRotulo() + " = min("
                        + menorAntes + ", " + vizinho.getPrenum() + ") = " + vertice.getMenor(), vertice, vizinho);
            } else {
                registrarDetalhe("Ligacao com " + vizinho.getRotulo()
                        + ": descendente que nao e filho direto. Nao e retorno para ancestral.", vertice, vizinho);
            }
            aresta = aresta.getProx();
        }
        registrar(calculo + ".\nEscolhe o menor desses valores: " + vertice.getMenor() + ".", vertice, null);
        registrarEstado(vertice);
    }

    private void testarArticulacao(Vertice vertice) {

        int filhos = 0;
        StringBuilder comparacoes = new StringBuilder();
        Vertice filhoDestaque = null;
        for (Vertice filho : ordemVisita) {
            if (filho.getPai() == vertice) {
                filhos++;
                if (vertice.getPai() != null) {
                    boolean teste = filho.getMenor() >= vertice.getPrenum();
                    if (teste) vertice.setArticulacao(true);
                    if (teste && filhoDestaque == null) filhoDestaque = filho;
                    if (!comparacoes.isEmpty()) comparacoes.append("; ");
                    comparacoes.append("filho ").append(filho.getRotulo()).append(": ")
                            .append(filho.getMenor()).append(" >= ").append(vertice.getPrenum())
                            .append(teste ? " (sim)" : " (nao)");
                    registrarDetalhe("Teste de " + vertice.getRotulo() + " com filho " + filho.getRotulo()
                            + ": menor[filho] >= prenum[vertice]\n   " + filho.getMenor()
                            + " >= " + vertice.getPrenum() + " = " + teste
                            + (teste ? " -> marca articulacao" : " -> este filho nao marca articulacao"), vertice, filho);
                }
            }
        }

        //a raiz tem uma regra diferente: precisa ter mais de um filho na DFS
        if (vertice.getPai() == null && filhos > 1) {
            vertice.setArticulacao(true);
        }
        if (vertice.getPai() == null) {
            comparacoes.append("Raiz: ").append(filhos).append(" filho(s) na DFS. Precisa de mais de 1.");
            registrarDetalhe("Teste da raiz " + vertice.getRotulo() + ": filhos na DFS > 1\n   "
                    + filhos + " > 1 = " + (filhos > 1)
                    + (filhos > 1 ? " -> marca articulacao" : " -> raiz nao e articulacao"), vertice, null);
        }
        if (vertice.getPai() != null && filhos > 0) comparacoes.insert(0, "menor[filho] >= prenum[vertice]: ");
        registrar("Vertice " + vertice.getRotulo() + ": " + comparacoes + "\n"
                + (vertice.isArticulacao() ? "e ponto de articulacao."
                : "nao e ponto de articulacao.")
                + (filhos == 0 ? " Nao tem filhos na arvore DFS." : ""), vertice, filhoDestaque);
        registrarEstado(vertice);
    }

    //vai anotando o que aconteceu de verdade durante a busca
    private void registrar(String texto, Vertice atual, Vertice vizinho) {
        registrarDetalhe(texto, atual, vizinho);
        //so os momentos importantes viram um clique na interface
        etapas.add(new EtapaAlgoritmo(lista, fase + "\n" + texto, atual == null ? null : atual.getRotulo(),
                vizinho == null ? null : vizinho.getRotulo(), true, Map.of()));
    }

    private void registrarDetalhe(String texto, Vertice atual, Vertice vizinho) {
        texto = fase + "\n" + texto;
        passo++;
        testeMesa.append(passo).append(") ").append(texto).append("\n");
    }

    public ArrayList<EtapaAlgoritmo> getEtapas() {
        return new ArrayList<>(etapas);
    }

    private void registrarEstado(Vertice vertice) {
        String pai = "-";
        if (vertice.getPai() != null) pai = vertice.getPai().getRotulo();
        testeMesa.append("   prenum=").append(vertice.getPrenum())
                .append(" | menor=").append(vertice.getMenor())
                .append(" | pai=").append(pai)
                .append("\n   articulacao=").append(vertice.isArticulacao()).append("\n\n");
    }

    public String getTesteMesa() {
        return testeMesa.toString();
    }

    public void validarGrafo() {
        //esse algoritmo usa grafo simples nao orientado, com numeros ou letras
        for (int i = 0; i < lista.getVertices().size(); i++) {
            Vertice v = lista.getVertices().get(i);
            for (int j = 0; j < i; j++) {
                if (lista.getVertices().get(j).getRotulo().equalsIgnoreCase(v.getRotulo())) {
                    throw new IllegalArgumentException("Vertice repetido: " + v.getRotulo());
                }
            }
            Aresta a = v.getInicio();
            while (a != null) {
                Vertice destino = lista.buscarVertice(a.getDestino());
                if (destino == null || destino == v) {
                    throw new IllegalArgumentException("Destino inexistente ou laco em " + v.getRotulo());
                }
                if (!lista.existeAresta(destino.getRotulo(), v.getRotulo())) {
                    throw new IllegalArgumentException("Falta a volta de " + destino.getRotulo() + " para " + v.getRotulo());
                }
                Aresta outra = a.getProx();
                while (outra != null) {
                    if (outra.getDestino().equalsIgnoreCase(a.getDestino())) {
                        throw new IllegalArgumentException("Aresta repetida em " + v.getRotulo());
                    }
                    outra = outra.getProx();
                }
                a = a.getProx();
            }
        }
    }

    private Vertice buscarMenorVerticeNaoVisitado() {

        //começa sem ter encontrado nenhum vertice
        Vertice menorVertice = null;

        //passa por todos os vertices que vieram da primeira linha do arquivo
        for (int i = 0; i < lista.getVertices().size(); i++) {

            Vertice atual = lista.getVertices().get(i);

            //so compara se esse vertice ainda nao foi visitado
            if (!atual.isVisitado()) {

                //se ainda nao tinha encontrado nenhum ele ja vira o menor
                //se tinha encontrado compara os rotulos para ficar com o menor deles
                if (menorVertice == null || lista.compararRotulos(atual.getRotulo(),
                        menorVertice.getRotulo()) < 0) {
                    menorVertice = atual;
                }
            }
        }

        return menorVertice;
    }

    private Vertice buscarMenorVizinhoNaoVisitado(Vertice vertice) {

        //aqui vamos guardar o menor vizinho encontrado
        Vertice menorVizinho = null;

        //pega a primeira aresta da lista ligada desse vertice
        Aresta aresta = vertice.getInicio();

        //passa por todas as arestas ate chegar no final da lista ligada
        while (aresta != null) {

            //a aresta guarda o destino como String
            //entao usa o rotulo para pegar o objeto Vertice verdadeiro na ListaA
            Vertice vizinho = lista.buscarVertice(aresta.getDestino());

            //so pode escolher esse vizinho se ele existe e ainda nao foi visitado
            if (vizinho != null && !vizinho.isVisitado()) {

                //compara numeros em ordem crescente e letras em ordem alfabetica
                if (menorVizinho == null || lista.compararRotulos(vizinho.getRotulo(),
                        menorVizinho.getRotulo()) < 0) {
                    menorVizinho = vizinho;
                }
            }

            //vai para a proxima aresta da lista ligada
            aresta = aresta.getProx();
        }

        //se nao encontrou nenhum vizinho nao visitado vai retornar null
        return menorVizinho;
    }
}
