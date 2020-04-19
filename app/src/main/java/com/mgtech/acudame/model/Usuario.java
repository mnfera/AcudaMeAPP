package com.mgtech.acudame.model;

import com.google.firebase.database.DatabaseReference;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;

public class Usuario {

    private String idUsuario;
    private String nome;
    private String endereco;
    private String numero;
    private String referencia;
    private String telefone;

    public Usuario() {
    }

    public void salvar() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference usuarioRef = firebaseRef.child("usuarios")
                .child(getIdUsuario());
        usuarioRef.setValue(this);
    }

    public String getTelefone() { return telefone; }

    public void setTelefone(String telefone) { this.telefone = telefone; }

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

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
}
