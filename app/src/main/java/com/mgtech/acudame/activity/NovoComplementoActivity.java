package com.mgtech.acudame.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.mgtech.acudame.R;
import com.mgtech.acudame.helper.UsuarioFirebase;
import com.mgtech.acudame.model.Complemento;
import com.mgtech.acudame.model.Produto;

public class NovoComplementoActivity extends AppCompatActivity {

    private EditText editComplementoNome;
    private Switch tipoStatus;
    private Button buttonSalvar;
    private String idEmpresa;
    private AdView anuncio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_complemento);

        // conf iniciais
        inicializarComponentes();
        idEmpresa = UsuarioFirebase.getIdUsuario();

        //Anuncio
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        anuncio.loadAd(adRequest);

        // configurações toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo Complemento");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        buttonSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarDadosComplemento();
            }
        });
    }

    private void validarDadosComplemento() {

        // verifica se os campos foram preenchidos
        String nome = editComplementoNome.getText().toString();
        String status = "ativo";

        // verifica estado do switch
        if(tipoStatus.isChecked()) {
            status = "inativo";
        }

        if(!nome.isEmpty()) {

            //Inserindo o complemento na tabela do banco
            Complemento complemento = new Complemento();
            complemento.setIdEmpresa(idEmpresa);
            complemento.setNome(nome);
            complemento.setStatus(status);
            complemento.salvarComplemento();
            finish();
            exibirMensagem("Complemento salvo com sucesso!");

        }else {
            exibirMensagem("Digite um nome para o complemento");
        }

    }

    private void exibirMensagem(String texto){
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }

    private void inicializarComponentes() {
        editComplementoNome = findViewById(R.id.editComplementoNome);
        tipoStatus = findViewById(R.id.switchStatus);
        buttonSalvar = findViewById(R.id.buttonSalvar);
        anuncio = findViewById(R.id.novoComplementoAnuncio);
    }
}
