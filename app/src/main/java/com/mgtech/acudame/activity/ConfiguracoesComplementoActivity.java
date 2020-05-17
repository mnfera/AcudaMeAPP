package com.mgtech.acudame.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mgtech.acudame.R;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;
import com.mgtech.acudame.helper.UsuarioFirebase;
import com.mgtech.acudame.model.Complemento;

import dmax.dialog.SpotsDialog;

public class ConfiguracoesComplementoActivity extends AppCompatActivity {

    private EditText editComplementoNome;
    private Switch tipoStatus;
    private Button buttonSalvar;
    private String idEmpresa, idComplemento;
    private DatabaseReference firebaseRef;
    private AlertDialog dialog;
    private AdView anuncio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes_complemento);

        // conf iniciais
        inicializarComponentes();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idEmpresa = UsuarioFirebase.getIdUsuario();

        //Anuncio
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        anuncio.loadAd(adRequest);

        // recuperar id do complemento selecionado
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            idComplemento = (String) bundle.getSerializable("complemento");
        }

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

        // Recuperar dados do complemento
        recuperarDadosComplemento();
    }

    private void recuperarDadosComplemento() {

        dialog = new SpotsDialog.Builder()
                .setContext(ConfiguracoesComplementoActivity.this)
                .setMessage("Carregando Dados")
                .setCancelable(false)
                .build();
        dialog.show();

        DatabaseReference complementoRef = firebaseRef
                .child("complementos")
                .child(idEmpresa)
                .child(idComplemento);

        complementoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    Complemento complemento = dataSnapshot.getValue(Complemento.class);
                    editComplementoNome.setText(complemento.getNome());
                }

                dialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
            complemento.setIdComplemento(idComplemento);
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
        anuncio = findViewById(R.id.confComplementoAnuncio);
    }
}
