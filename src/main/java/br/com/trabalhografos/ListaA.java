package br.com.trabalhografos;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class ListaA {

    private File arquivo;
    private ArrayList<Vertice> vertices;

    public ListaA(File arquivo) throws Exception {
        this.arquivo = arquivo;
        vertices = new ArrayList<>();
        leArquivo(this.arquivo);
    }

    public void leArquivo(File arq) throws Exception {

        Scanner leitor = new Scanner(arq);

        if (!leitor.hasNextLine()) {
            leitor.close();
            throw new Exception("O arquivo esta vazio.");
        }

        String primeiraLinha = leitor.nextLine().trim();
        String[] rotulos = primeiraLinha.split("\\s+");

        for (int i = 0; i < rotulos.length; i++) {
            Vertice novo = new Vertice(rotulos[i]);
            vertices.add(novo);
        }

        while (leitor.hasNextLine()) {

            String linha = leitor.nextLine().trim();

            if (!linha.isEmpty()) {

                String[] valores = linha.split("\\s+");

                Vertice v = buscarVertice(valores[0]);

                if (v == null) {
                    leitor.close();
                    throw new Exception("Vertice " + valores[0] + " nao encontrado na primeira linha.");
                }

                for (int i = 1; i < valores.length; i++) {

                    String[] dados = valores[i].split(",");

                    String destino = dados[0];

                    Aresta nova;

                    if (dados.length == 2) {

                        int peso = Integer.parseInt(dados[1]);
                        nova = new Aresta(destino, peso);

                    } else {

                        nova = new Aresta(destino);
                    }

                    v.adicionarAresta(nova);
                }

            }
        }

        leitor.close();
    }

    public Vertice buscarVertice(String rotulo) {

        Vertice encontrado = null;
        int i = 0;

        while (i < vertices.size() && encontrado == null) {

            if (vertices.get(i).getRotulo().equalsIgnoreCase(rotulo)) {
                encontrado = vertices.get(i);
            }

            i++;
        }

        return encontrado;
    }

    public void exibirLista() {

        for (int i = 0; i < vertices.size(); i++) {

            Vertice v = vertices.get(i);

            System.out.print(v.getRotulo());

            Aresta a = v.getInicio();

            while (a != null) {

                System.out.print(" -> " + a.getDestino());

                if (a.getPeso() != null)
                    System.out.print("," + a.getPeso());

                a = a.getProx();
            }

            System.out.println();
        }
    }

    public boolean existeAresta(String origem, String destino) {

        boolean existe = false;
        Vertice v = null;

        for (int i = 0; i < vertices.size(); i++) {

            if (vertices.get(i).getRotulo().equalsIgnoreCase(origem))
                v = vertices.get(i);
        }

        if (v != null) {

            Aresta a = v.getInicio();

            while (a != null && !existe) {

                if (a.getDestino().equalsIgnoreCase(destino))
                    existe = true;

                a = a.getProx();
            }
        }

        return existe;
    }

    public ArrayList<Vertice> getVertices() {
        return vertices;
    }

    public int compararRotulos(String primeiro, String segundo) {

        //confere se cada rotulo é um numero inteiro ou um texto
        boolean primeiroNumero = primeiro.matches("[+-]?[0-9]+");
        boolean segundoNumero = segundo.matches("[+-]?[0-9]+");

        if (primeiroNumero && segundoNumero) {
            //compara pelo valor, assim o 2 vem antes do 10
            //BigInteger tambem aceita numeros que nao cabem em um int
            int resultado = new java.math.BigInteger(primeiro)
                    .compareTo(new java.math.BigInteger(segundo));
            if (resultado != 0) {
                return resultado;
            }
        }

        //se misturar numeros e letras, os numeros ficam primeiro
        if (primeiroNumero && !segundoNumero) {
            return -1;
        }
        if (!primeiroNumero && segundoNumero) {
            return 1;
        }

        //para letras usa ordem alfabetica, sem diferenciar maiuscula e minuscula
        return primeiro.compareToIgnoreCase(segundo);
    }
}
