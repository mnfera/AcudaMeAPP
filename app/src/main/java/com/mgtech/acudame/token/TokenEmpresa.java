package com.mgtech.acudame.token;

import com.google.firebase.database.DatabaseReference;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;

public class TokenEmpresa{
    private String idEmpresa;
    private String token;

    public TokenEmpresa() {

    }

    public void salvarTokenEmpresa() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference tokenUsuarioRef = firebaseRef.child("tokenEmpresas")
                .child(getIdEmpresa());
        tokenUsuarioRef.child("token").setValue(getToken());
    }

    public String getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(String idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
