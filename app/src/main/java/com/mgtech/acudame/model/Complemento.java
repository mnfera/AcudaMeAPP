package com.mgtech.acudame.model;

import com.google.firebase.database.DatabaseReference;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;

import java.io.Serializable;

public class Complemento implements Serializable {

    private String idEmpresa;
    private String idComplemento;
    private String nome;
    private String status = "ativo";

    public Complemento() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference complementoRef = firebaseRef.child("complementos");
        setIdComplemento(complementoRef.push().getKey());
    }

    public void salvarComplemento() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference complementoRef = firebaseRef.child("complementos")
                .child(getIdEmpresa())
                .child(getIdComplemento());
        complementoRef.setValue(this);
    }

    public void removerComplemento() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference complementoRef = firebaseRef.child("complementos")
                .child(getIdEmpresa())
                .child(getIdComplemento());
        complementoRef.removeValue();
    }

    public String getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(String idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getIdComplemento() {
        return idComplemento;
    }

    public void setIdComplemento(String idComplemento) {
        this.idComplemento = idComplemento;
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
