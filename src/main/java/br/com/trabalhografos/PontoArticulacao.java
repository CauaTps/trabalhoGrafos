package br.com.trabalhografos;

public class PontoArticulacao {

    private ListaA lista;

    //o contador vai dizer a ordem em que cada vertice foi visitado
    private int contador;

    public PontoArticulacao(ListaA lista) {
        this.lista = lista;
        contador = 0;
    }

    public void visitaVertices(){

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
        //na primeira vez vai pegar o vertice 1
        Vertice vertice = buscarMenorVerticeNaoVisitado();

        //depois que terminar uma parte do grafo ele procura se ficou algum sem visitar
        //isso tambem faz funcionar caso o grafo seja desconexo
        while (vertice != null) {
            visitar(vertice);
            vertice = buscarMenorVerticeNaoVisitado();
        }
    }

    private void visitar(Vertice vertice) {

        //quando entra no vertice aumenta a ordem da visita
        contador++;

        //o prenum recebe exatamente a ordem em que esse vertice foi encontrado
        vertice.setPrenum(contador);
        //no começo ele so conhece o proprio prenum
        vertice.setMenor(contador);

        //marca como visitado para nao entrar nele de novo e ficar em loop
        vertice.setVisitado(true);

        //entre todas as ligações desse vertice procura o menor vizinho nao visitado
        Vertice vizinho = buscarMenorVizinhoNaoVisitado(vertice);
        int filhos = 0;

        //vai visitando os vizinhos e quando volta procura o proximo menor
        while (vizinho != null) {
            filhos++;
            vizinho.setPai(vertice);
            visitar(vizinho);

            //quando volta da busca, o filho conta ate onde conseguiu voltar
            if (vizinho.getMenor() < vertice.getMenor()) {
                vertice.setMenor(vizinho.getMenor());
            }

            //se o filho nao consegue voltar antes desse vertice, ele é articulação
            if (vertice.getPai() != null && vizinho.getMenor() >= vertice.getPrenum()) {
                vertice.setArticulacao(true);
            }
            vizinho = buscarMenorVizinhoNaoVisitado(vertice);
        }

        //agora confere as ligações de retorno, sem usar a ligação com o pai
        Aresta aresta = vertice.getInicio();
        while (aresta != null) {
            vizinho = lista.buscarVertice(aresta.getDestino());
            if (vizinho != vertice.getPai() && vizinho.getPrenum() < vertice.getMenor()) {
                vertice.setMenor(vizinho.getPrenum());
            }
            aresta = aresta.getProx();
        }

        //a raiz tem uma regra diferente: precisa ter mais de um filho na DFS
        if (vertice.getPai() == null && filhos > 1) {
            vertice.setArticulacao(true);
        }
    }

    public void validarGrafo() {
        //esse algoritmo usa grafo simples nao orientado e rotulos numericos
        for (int i = 0; i < lista.getVertices().size(); i++) {
            Vertice v = lista.getVertices().get(i);
            try {
                Integer.parseInt(v.getRotulo());
            } catch (NumberFormatException erro) {
                throw new IllegalArgumentException("Use numeros inteiros nos rotulos dos vertices.");
            }
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
                //se tinha encontrado compara os numeros para ficar com o menor deles
                if (menorVertice == null || Integer.parseInt(atual.getRotulo())
                        < Integer.parseInt(menorVertice.getRotulo())) {
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

                //compara os rotulos como numeros e guarda sempre o menor vizinho
                if (menorVizinho == null || Integer.parseInt(vizinho.getRotulo())
                        < Integer.parseInt(menorVizinho.getRotulo())) {
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
