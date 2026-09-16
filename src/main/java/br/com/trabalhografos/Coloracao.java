package br.com.trabalhografos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Coloracao {

    private ListaA lista;
    private StringBuilder testeMesa = new StringBuilder();
    private ArrayList<EtapaAlgoritmo> etapas = new ArrayList<>();

    public Coloracao(ListaA lista) {
        this.lista = lista;
    }

    public Map<String, Integer> colorir() {

        testeMesa.setLength(0);
        etapas.clear();
        validarGrafo();

        ArrayList<Vertice> ordem = new ArrayList<>(lista.getVertices());
        ordem.sort((v1, v2) -> {
            int diferencaGrau = grau(v2) - grau(v1);
            if (diferencaGrau != 0) {
                return diferencaGrau;
            }
            return lista.compararRotulos(v1.getRotulo(), v2.getRotulo());
        });

        Map<String, Integer> coresCalculadas = new LinkedHashMap<>();

        testeMesa.append("Ordem: maior grau primeiro.\nEmpate: menor rotulo.\n");
        for (int i = 0; i < ordem.size(); i++) {
            if (i > 0) testeMesa.append(" -> ");
            testeMesa.append(ordem.get(i).getRotulo());
        }
        testeMesa.append("\n\n");
        registrar("Inicio: todos os vertices estao sem cor.\n" + testeMesa, null, null, coresCalculadas);

        for (int i = 0; i < ordem.size(); i++) {

            Vertice vertice = ordem.get(i);
            Set<Integer> coresVizinhos = new HashSet<>();
            Aresta aresta = vertice.getInicio();
            testeMesa.append(i + 1).append(") Vertice ").append(vertice.getRotulo())
                    .append(" | grau=").append(grau(vertice)).append("\n");
            if (aresta == null) testeMesa.append("   Sem vizinhos.\n");

            while (aresta != null) {
                Vertice vizinho = lista.buscarVertice(aresta.getDestino());
                if (vizinho != null && coresCalculadas.containsKey(vizinho.getRotulo())) {
                    coresVizinhos.add(coresCalculadas.get(vizinho.getRotulo()));
                }
                //mostra a cor que o vizinho tinha neste momento, nao a cor final
                if (vizinho != null) {
                    testeMesa.append("   Vizinho ").append(vizinho.getRotulo()).append(": ");
                    if (coresCalculadas.containsKey(vizinho.getRotulo())) {
                        testeMesa.append("cor ").append(coresCalculadas.get(vizinho.getRotulo()));
                    } else {
                        testeMesa.append("ainda sem cor");
                    }
                    testeMesa.append("\n");
                }
                aresta = aresta.getProx();
            }

            int cor = 1;
            ArrayList<Integer> bloqueadas = new ArrayList<>(coresVizinhos);
            bloqueadas.sort(null);
            testeMesa.append("   Cores bloqueadas: ").append(bloqueadas).append("\n");
            while (coresVizinhos.contains(cor)) {
                testeMesa.append("   Tenta cor ").append(cor).append(": bloqueada\n");
                cor++;
            }

            coresCalculadas.put(vertice.getRotulo(), cor);
            testeMesa.append("   Tenta cor ").append(cor).append(": livre -> escolhida\n\n");
            //um clique por vertice: junta a analise dos vizinhos com a escolha da cor
            registrar("Vertice " + vertice.getRotulo() + " (grau " + grau(vertice) + "). "
                    + "Cores dos vizinhos ja coloridos: " + (bloqueadas.isEmpty() ? "nenhuma" : bloqueadas)
                    + ".\nRecebe a cor " + cor + ", a menor disponivel.", vertice, null, coresCalculadas);
        }

        Map<String, Integer> coresNaOrdemOriginal = new LinkedHashMap<>();
        for (int i = 0; i < lista.getVertices().size(); i++) {
            Vertice vertice = lista.getVertices().get(i);
            coresNaOrdemOriginal.put(vertice.getRotulo(), coresCalculadas.get(vertice.getRotulo()));
        }

        registrar("Coloracao concluida. Vizinhos possuem cores diferentes.\n"
                + "O algoritmo guloso nao garante o minimo de cores em todo grafo.", null, null, coresCalculadas);
        return coresNaOrdemOriginal;
    }

    private void registrar(String texto, Vertice atual, Vertice vizinho, Map<String, Integer> cores) {
        etapas.add(new EtapaAlgoritmo(lista, texto, atual == null ? null : atual.getRotulo(),
                vizinho == null ? null : vizinho.getRotulo(), false, cores));
    }

    public ArrayList<EtapaAlgoritmo> getEtapas() {
        return new ArrayList<>(etapas);
    }

    public String getTesteMesa() {
        return testeMesa.toString();
    }

    public Map<Integer, List<String>> agruparPorCor(Map<String, Integer> cores) {

        Map<Integer, List<String>> grupos = new LinkedHashMap<>();
        int maiorCor = 0;

        for (Integer cor : cores.values()) {
            if (cor > maiorCor) {
                maiorCor = cor;
            }
        }

        for (int cor = 1; cor <= maiorCor; cor++) {
            grupos.put(cor, new ArrayList<>());
        }

        for (String rotulo : cores.keySet()) {
            int cor = cores.get(rotulo);
            grupos.get(cor).add(rotulo);
        }

        return grupos;
    }

    private int grau(Vertice vertice) {

        int grau = 0;
        Aresta aresta = vertice.getInicio();

        while (aresta != null) {
            grau++;
            aresta = aresta.getProx();
        }

        return grau;
    }

    private void validarGrafo() {

        for (int i = 0; i < lista.getVertices().size(); i++) {

            Vertice vertice = lista.getVertices().get(i);

            for (int j = 0; j < i; j++) {
                if (lista.getVertices().get(j).getRotulo().equalsIgnoreCase(vertice.getRotulo())) {
                    throw new IllegalArgumentException("Vertice repetido: " + vertice.getRotulo());
                }
            }

            Aresta aresta = vertice.getInicio();
            while (aresta != null) {

                Vertice destino = lista.buscarVertice(aresta.getDestino());
                if (destino == null || destino == vertice) {
                    throw new IllegalArgumentException("Destino inexistente ou laco em " + vertice.getRotulo());
                }

                if (!lista.existeAresta(destino.getRotulo(), vertice.getRotulo())) {
                    throw new IllegalArgumentException("Falta a volta de " + destino.getRotulo() + " para " + vertice.getRotulo());
                }

                Aresta outra = aresta.getProx();
                while (outra != null) {
                    if (outra.getDestino().equalsIgnoreCase(aresta.getDestino())) {
                        throw new IllegalArgumentException("Aresta repetida em " + vertice.getRotulo());
                    }
                    outra = outra.getProx();
                }

                aresta = aresta.getProx();
            }
        }
    }
}
