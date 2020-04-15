package com.mgtech.acudame.message;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDialogFragment;

public class MessengerDialog extends AppCompatDialogFragment {

    private String titulo;
    private String conteudo;

    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getTitulo());
        builder.setMessage(getConteudo());
        //define um botão como positivo
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {

            }
        });
        return builder.create();
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getConteudo() {
        return conteudo;
    }
}
