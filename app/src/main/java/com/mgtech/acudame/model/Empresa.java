package com.mgtech.acudame.model;

import com.google.firebase.database.DatabaseReference;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;

import java.io.Serializable;
import java.util.HashMap;

public class Empresa implements Serializable {

    private String idUsuario;
    private String urlImagem;
    private String nome;
    private String cidade;
    private String categoria;
    private String horario;
    private String dias;
    private String telefone;
    private String tokenEmpresa;
    private Boolean status;

    public Empresa() {
    }

    public void salvar() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference empresaRef = firebaseRef.child("empresas")
                .child(getIdUsuario());
        empresaRef.child("idUsuario").setValue(getIdUsuario());
        empresaRef.child("urlImagem").setValue(getUrlImagem());
        empresaRef.child("nome").setValue(getNome());
        empresaRef.child("categoria").setValue(getCategoria());
        empresaRef.child("horario").setValue(getHorario());
        empresaRef.child("dias").setValue(getDias());
        empresaRef.child("telefone").setValue(getTelefone());
    }

    public void salvarTokenEmpresa() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference empresaRef = firebaseRef.child("empresas")
                .child(getIdUsuario());
        empresaRef.child("tokenEmpresa").setValue(getTokenEmpresa());
    }

    public void atualizarTokenEmpresa() {

        HashMap<String, Object> tokenEmpresa = new HashMap<>();
        tokenEmpresa.put("tokenEmpresa", getTokenEmpresa());

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference empresaRef = firebaseRef
                .child("empresas")
                .child(getIdUsuario());
        empresaRef.updateChildren(tokenEmpresa);
    }

    public void atualizarStatusEmpresa2() {

        HashMap<String, Object> status = new HashMap<>();
        status.put("status", getStatus());

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference empresaRef = firebaseRef
                .child("empresas")
                .child(getIdUsuario());
        empresaRef.updateChildren(status);

    }

    public void atualizarStatusEmpresa() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference empresaRef = firebaseRef.child("empresas")
                .child(getIdUsuario());
        empresaRef.child("status").setValue(getStatus());

    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getTelefone() { return telefone; }

    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUrlImagem() {
        return urlImagem;
    }

    public void setUrlImagem(String urlImagem) {
        this.urlImagem = urlImagem;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getDias() {
        return dias;
    }

    public void setDias(String dias) {
        this.dias = dias;
    }

    public String getTokenEmpresa() {return tokenEmpresa;}

    public void setTokenEmpresa(String tokenEmpresa) {this.tokenEmpresa = tokenEmpresa;}

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
