package br.com.trabalhografos;

public class Vertice {

    private String rotulo;
    private Aresta inicio;
    private boolean visitado;
    private int prenum;
    private int menor;
    private Vertice pai;
    private boolean articulacao;

    public Vertice(String rotulo) {
        this.rotulo = rotulo;
        this.inicio = null;
        this.visitado = false;
        this.prenum = 0;
        this.menor = 0;
        this.pai = null;
        this.articulacao = false;
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

    public boolean isVisitado() {
        return visitado;
    }

    public void setVisitado(boolean visitado) {
        this.visitado = visitado;
    }

    public int getPrenum() {
        return prenum;
    }

    public void setPrenum(int prenum) {
        this.prenum = prenum;
    }

    public int getMenor() {
        return menor;
    }

    public void setMenor(int menor) {
        this.menor = menor;
    }

    public Vertice getPai() {
        return pai;
    }

    public void setPai(Vertice pai) {
        this.pai = pai;
    }

    public boolean isArticulacao() {
        return articulacao;
    }

    public void setArticulacao(boolean articulacao) {
        this.articulacao = articulacao;
    }
}
