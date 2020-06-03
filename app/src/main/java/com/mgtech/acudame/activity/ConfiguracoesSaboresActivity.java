package com.mgtech.acudame.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import com.mgtech.acudame.model.Sabor;

import dmax.dialog.SpotsDialog;

public class ConfiguracoesSaboresActivity extends AppCompatActivity {

    private EditText editSaborNome;
    private Switch tipoStatus;
    private Button buttonSalvar;
    private String idEmpresa, idSabor;
    private DatabaseReference firebaseRef;
    private AlertDialog dialog;
    private AdView anuncio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes_sabores);

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

        // recuperar id do sabor selecionado
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            idSabor = (String) bundle.getSerializable("sabor");
        }

        // configurações toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo Sabor");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        buttonSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarDadosSabor();
            }
        });

        // Recuperar dados do sabor
        recuperarDadosSabor();
    }

    private void recuperarDadosSabor() {

        dialog = new SpotsDialog.Builder()
                .setContext(ConfiguracoesSaboresActivity.this)
                .setMessage("Carregando Dados")
                .setCancelable(false)
                .build();
        dialog.show();

        DatabaseReference saborRef = firebaseRef
                .child("sabores")
                .child(idEmpresa)
                .child(idSabor);

        saborRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    Sabor sabor = dataSnapshot.getValue(Sabor.class);
                    editSaborNome.setText(sabor.getNome());
                }

                dialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void validarDadosSabor() {

        // verifica se os campos foram preenchidos
        String nome = editSaborNome.getText().toString();
        String status = "ativo";

        // verifica estado do switch
        if(tipoStatus.isChecked()) {
            status = "inativo";
        }

        if(!nome.isEmpty()) {

            //Inserindo o sabor na tabela do banco
            Sabor sabor = new Sabor();
            sabor.setIdEmpresa(idEmpresa);
            sabor.setNome(nome);
            sabor.setStatus(status);
            sabor.salvarSabor();
            finish();
            exibirMensagem("Sabor da pizza salvo com sucesso!");

        }else {
            exibirMensagem("Digite um nome para o sabor da pizza");
        }

    }

    private void exibirMensagem(String texto){
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }

    private void inicializarComponentes() {
        editSaborNome = findViewById(R.id.editSaborNome);
        tipoStatus = findViewById(R.id.switchStatus);
        buttonSalvar = findViewById(R.id.buttonSalvar);
        anuncio = findViewById(R.id.confSaborAnuncio);
    }
}
