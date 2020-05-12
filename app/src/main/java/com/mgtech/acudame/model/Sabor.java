package com.mgtech.acudame.model;

import com.google.firebase.database.DatabaseReference;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;

import java.io.Serializable;

public class Sabor implements Serializable {

    private String idEmpresa;
    private String idSabor;
    private String nome;
    private String status = "ativo";

    public Sabor() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference saborRef = firebaseRef.child("sabores");
        setIdSabor(saborRef.push().getKey());
    }

    public void salvarSabor() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference saborRef = firebaseRef.child("sabores")
                .child(getIdEmpresa())
                .child(getIdSabor());
        saborRef.setValue(this);
    }

    public void removerSabor() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference saborRef = firebaseRef.child("sabores")
                .child(getIdEmpresa())
                .child(getIdSabor());
        saborRef.removeValue();
    }

    public String getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(String idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getIdSabor() {
        return idSabor;
    }

    public void setIdSabor(String idSabor) {
        this.idSabor = idSabor;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
