package com.mgtech.acudame.token;

import com.google.firebase.database.DatabaseReference;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;

import java.util.Map;

public class TokenUsuario {
    private String idUsuario;
    private String token;



    public TokenUsuario(){

    }

    public void salvarTokenUsuario() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference tokenUsuarioRef = firebaseRef.child("tokenUsuarios")
                .child(getIdUsuario());
        tokenUsuarioRef.child("token").setValue(getToken());
    }


    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
