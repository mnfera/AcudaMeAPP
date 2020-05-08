package com.mgtech.acudame.model;

import com.google.firebase.database.DatabaseReference;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;

public class Produto {

    private String idUsuario;
    private String idProduto;
    private String nome;
    private String descricao;
    private String preco;
    private String status = "ativo";
    private String categoria;
    private String statusCategoria;

    public Produto() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference produtoRef = firebaseRef.child("produtos");
        setIdProduto(produtoRef.push().getKey());
    }

    public void salvar() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference produtoRef = firebaseRef.child("produtos")
                .child(getIdUsuario())
                .child(getIdProduto());
        produtoRef.setValue(this);
    }

    public void remover() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference produtoRef = firebaseRef.child("produtos")
                .child(getIdUsuario())
                .child(getIdProduto());
        produtoRef.removeValue();
    }

    public String getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(String idProduto) {
        this.idProduto = idProduto;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getPreco() {
        return preco;
    }

    public void setPreco(String preco) {
        this.preco = preco;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getStatusCategoria() {
        return statusCategoria;
    }

    public void setStatusCategoria(String status, String categoria) {
        this.statusCategoria = status + "_" + categoria;
    }
}
