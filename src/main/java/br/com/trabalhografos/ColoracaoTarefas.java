package br.com.trabalhografos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ColoracaoTarefas {

    private ListaA lista;

    public ColoracaoTarefas(ListaA lista) {
        this.lista = lista;
    }

    public Map<String, Integer> colorir() {

        validarGrafo();

        ArrayList<Vertice> ordem = new ArrayList<>(lista.getVertices());
        ordem.sort((v1, v2) -> {
            int diferencaGrau = grau(v2) - grau(v1);
            if (diferencaGrau != 0) {
                return diferencaGrau;
            }
            return compararRotulos(v1.getRotulo(), v2.getRotulo());
        });

        Map<String, Integer> coresCalculadas = new LinkedHashMap<>();

        for (int i = 0; i < ordem.size(); i++) {

            Vertice vertice = ordem.get(i);
            Set<Integer> coresVizinhos = new HashSet<>();
            Aresta aresta = vertice.getInicio();

            while (aresta != null) {
                Vertice vizinho = lista.buscarVertice(aresta.getDestino());
                if (vizinho != null && coresCalculadas.containsKey(vizinho.getRotulo())) {
                    coresVizinhos.add(coresCalculadas.get(vizinho.getRotulo()));
                }
                aresta = aresta.getProx();
            }

            int cor = 1;
            while (coresVizinhos.contains(cor)) {
                cor++;
            }

            coresCalculadas.put(vertice.getRotulo(), cor);
        }

        Map<String, Integer> coresNaOrdemOriginal = new LinkedHashMap<>();
        for (int i = 0; i < lista.getVertices().size(); i++) {
            Vertice vertice = lista.getVertices().get(i);
            coresNaOrdemOriginal.put(vertice.getRotulo(), coresCalculadas.get(vertice.getRotulo()));
        }

        return coresNaOrdemOriginal;
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

    private int compararRotulos(String primeiro, String segundo) {
        try {
            return Integer.compare(Integer.parseInt(primeiro), Integer.parseInt(segundo));
        } catch (NumberFormatException erro) {
            return primeiro.compareToIgnoreCase(segundo);
        }
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
