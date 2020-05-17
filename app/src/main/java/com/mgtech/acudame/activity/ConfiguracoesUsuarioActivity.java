package com.mgtech.acudame.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.mgtech.acudame.R;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;
import com.mgtech.acudame.helper.UsuarioFirebase;
import com.mgtech.acudame.model.Usuario;
import com.santalu.maskedittext.MaskEditText;

import dmax.dialog.SpotsDialog;

public class ConfiguracoesUsuarioActivity extends AppCompatActivity {

    private EditText editUsuarioNome, editUsuarioEndereco,
                    editTextUsuarioNumero, editTextUsuarioReferencia;
    private MaskEditText editUsuarioTelefone;
    private Button buttonSalvar;
    private String idUsuario;
    private DatabaseReference firebaseRef;
    private AlertDialog dialog;
    private Usuario usuario = new Usuario();
    private AdView anuncio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes_usuario);

        // conf iniciais
        inicializarComponentes();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idUsuario = UsuarioFirebase.getIdUsuario();

        // configurações toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Configurações");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        buttonSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarDados();
            }
        });

        //Anuncio
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        anuncio.loadAd(adRequest);

        // Recuperar dados da empresa
        recuperarDadosUsuario();
    }

    private void recuperarDadosUsuario() {

        dialog = new SpotsDialog.Builder()
                .setContext(ConfiguracoesUsuarioActivity.this)
                .setMessage("Carregando Dados")
                .setCancelable(false)
                .build();
        dialog.show();

        DatabaseReference usuarioRef = firebaseRef
                .child("usuarios")
                .child(idUsuario);
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    Usuario usuario = dataSnapshot.getValue(Usuario.class);
                    editUsuarioNome.setText(usuario.getNome());
                    editUsuarioEndereco.setText(usuario.getEndereco());
                    editTextUsuarioNumero.setText(usuario.getNumero());
                    editTextUsuarioReferencia.setText(usuario.getReferencia());
                    editUsuarioTelefone.setText(usuario.getTelefone());
                }
                dialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void exibirMensagem(String texto){
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }

    private void inicializarComponentes() {
        editUsuarioNome = findViewById(R.id.editUsuarioNome);
        editUsuarioEndereco = findViewById(R.id.editUsuarioEndereco);
        buttonSalvar = findViewById(R.id.buttonSalvar);
        editUsuarioTelefone = findViewById(R.id.editUsuarioTelefone);
        editTextUsuarioNumero = findViewById(R.id.editUsuarioNumero);
        editTextUsuarioReferencia = findViewById(R.id.editUsuarioReferencia);
        anuncio = findViewById(R.id.confUsuarioAnuncio);
    }

    public void salvarDados(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {

                // verifica se os campos foram preenchidos
                String nome = editUsuarioNome.getText().toString();
                String endereco = editUsuarioEndereco.getText().toString();
                String telefone = editUsuarioTelefone.getText().toString();
                String numero = editTextUsuarioNumero.getText().toString();
                String referencia = editTextUsuarioReferencia.getText().toString();
                //Pegando token
                String token = task.getResult().getToken();

                if(!nome.isEmpty()) {
                    if(!endereco.isEmpty()) {
                        if(!numero.isEmpty()) {
                            if (!referencia.isEmpty()) {

                                if (!telefone.isEmpty()) {

                                    usuario.setIdUsuario(idUsuario);
                                    usuario.setNome(nome);
                                    usuario.setEndereco(endereco);
                                    usuario.setNumero(numero);
                                    usuario.setReferencia(referencia);
                                    usuario.setTelefone(telefone);
                                    usuario.setTokenUsuario(token);
                                    usuario.salvar();

                                    exibirMensagem("Dados atualizados com sucesso!");
                                    finish();

                                } else {
                                    exibirMensagem("Digite seu telefone");
                                }
                            } else {
                                exibirMensagem("Digite um ponto de referência ou característica");
                            }
                        }else {
                            exibirMensagem("Digite um número ou zero para sem número");
                        }
                    }else {
                        exibirMensagem("Digite o nome da rua");
                    }
                }else {
                    exibirMensagem("Digite seu nome!");
                }

            }
        });
    }
}
