package br.com.trabalhografos;

public class Aresta {

    private String destino;
    private Integer distancia;
    private Aresta prox;

    public Aresta(String destino) {
        this.destino = destino;
        this.distancia = null;
        this.prox = null;
    }

    public Aresta(String destino, int distancia) {
        this.destino = destino;
        this.distancia = distancia;
        this.prox = null;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public Integer getPeso() {
        return distancia;
    }

    public void setPeso(Integer distancia) {
        this.distancia = distancia;
    }

    public Aresta getProx() {
        return prox;
    }

    public void setProx(Aresta prox) {
        this.prox = prox;
    }
}