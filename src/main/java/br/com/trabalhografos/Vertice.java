package br.com.trabalhografos;

public class Vertice {

    private String rotulo;
    private Aresta inicio;

    public Vertice(String rotulo) {
        this.rotulo = rotulo;
        this.inicio = null;
    }

    public void adicionarAresta(Aresta nova){
        if(inicio == null)
            inicio = nova;
        else{
            Aresta aux = inicio;
            while(aux.getProx() != null)
                aux = aux.getProx();
            aux.setProx(nova);
        }
    }

    public String getRotulo() {
        return rotulo;
    }

    public void setRotulo(String rotulo) {
        this.rotulo = rotulo;
    }

    public Aresta getInicio() {
        return inicio;
    }

    public void setInicio(Aresta inicio) {
        this.inicio = inicio;
    }
}