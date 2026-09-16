package br.com.trabalhografos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class EtapaAlgoritmo {

    private String explicacao;
    private String atual;
    private String vizinho;
    private boolean articulacao;
    private ArrayList<Vertice> estados = new ArrayList<>();
    private Map<String, Integer> cores = new LinkedHashMap<>();

    public EtapaAlgoritmo(ListaA lista, String explicacao, String atual, String vizinho,
            boolean articulacao, Map<String, Integer> cores) {
        this.explicacao = explicacao;
        this.atual = atual;
        this.vizinho = vizinho;
        this.articulacao = articulacao;
        this.cores.putAll(cores);

        //guarda uma copia dos valores deste momento, sem copiar as arestas
        //assim os passos seguintes nao mudam o que ja foi guardado
        for (Vertice v : lista.getVertices()) {
            Vertice copia = new Vertice(v.getRotulo());
            if (articulacao) {
                copia.setVisitado(v.isVisitado());
                copia.setPrenum(v.getPrenum());
                copia.setMenor(v.getMenor());
                copia.setArticulacao(v.isArticulacao());
                if (v.getPai() != null) copia.setPai(new Vertice(v.getPai().getRotulo()));
            }
            estados.add(copia);
        }
    }

    public void aplicar(ListaA lista, Map<String, Integer> coresNaTela) {
        //restaura so os valores da etapa, sem alterar a lista de adjacencia
        for (Vertice estado : estados) {
            Vertice v = lista.buscarVertice(estado.getRotulo());
            v.setVisitado(estado.isVisitado());
            v.setPrenum(estado.getPrenum());
            v.setMenor(estado.getMenor());
            v.setArticulacao(estado.isArticulacao());
            v.setPai(estado.getPai() == null ? null : lista.buscarVertice(estado.getPai().getRotulo()));
        }
        coresNaTela.clear();
        coresNaTela.putAll(cores);
    }

    public String getTabela() {
        StringBuilder texto = new StringBuilder("ESTADO NESTA ETAPA\n");
        for (Vertice v : estados) {
            texto.append(v.getRotulo()).append(": ");
            if (articulacao) {
                texto.append(v.isVisitado() ? "visitado" : "nao visitado")
                        .append(" | pre=").append(v.getPrenum())
                        .append(" | menor=").append(v.getMenor())
                        .append("\n   pai=").append(v.getPai() == null ? "-" : v.getPai().getRotulo())
                        .append(" | articulacao=").append(v.isArticulacao() ? "marcada" : "nao marcada");
            } else {
                texto.append(cores.containsKey(v.getRotulo()) ? "cor " + cores.get(v.getRotulo()) : "ainda sem cor");
            }
            texto.append("\n");
        }
        return texto.toString();
    }

    public String getExplicacao() { return explicacao; }
    public String getAtual() { return atual; }
    public String getVizinho() { return vizinho; }
    public boolean isArticulacao() { return articulacao; }
}
