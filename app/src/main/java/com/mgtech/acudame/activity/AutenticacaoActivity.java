package com.mgtech.acudame.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.mgtech.acudame.R;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;
import com.mgtech.acudame.helper.UsuarioFirebase;
import com.mgtech.acudame.message.MessengerDialog;

import dmax.dialog.SpotsDialog;

public class AutenticacaoActivity extends AppCompatActivity {

    private Button botaoAcessar;
    private EditText campoEmail, campoSenha;
    private Switch tipoAcesso, tipoUsuario;
    private LinearLayout linearTipoUsuario;
    private AlertDialog dialog;
    private FirebaseAuth autenticacao;
    final Handler handler = new Handler();
    private AlertDialog alerta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autenticacao);

        inicializarComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        //autenticacao.signOut();

        // verificar se usuario está logado
        verificarUsuarioLogado();

        tipoAcesso.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){ // empresa
                    linearTipoUsuario.setVisibility(View.VISIBLE);
                }else { // usuario
                    linearTipoUsuario.setVisibility(View.GONE);
                }
            }
        });

        botaoAcessar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                botaoAcessar.setClickable(false);
                    dialog = new SpotsDialog.Builder()
                            .setContext(AutenticacaoActivity.this)
                            .setMessage("Carregando Dados")
                            .setCancelable(false)
                            .build();
                    dialog.show();


                    String email = campoEmail.getText().toString();
                    String senha = campoSenha.getText().toString();

                    if (!email.isEmpty()) {
                        if (!senha.isEmpty()) {

                            // verifica estado do switch
                            if (tipoAcesso.isChecked()) { // cadastro

                                autenticacao.createUserWithEmailAndPassword(
                                        email, senha
                                ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {

                                        if (task.isSuccessful()) {

                                            Toast.makeText(AutenticacaoActivity.this,
                                                    "Cadastro realizado com sucesso!",
                                                    Toast.LENGTH_SHORT).show();
                                            String tipoUsuario = getTipoUsuario();
                                            UsuarioFirebase.atualizarTipoUsuario(tipoUsuario);
                                            abrirTelaPrincipal(tipoUsuario);

                                        } else {
                                            String erroExcecao = "";
                                            String titulo = "";

                                            try {
                                                throw task.getException();
                                            } catch (FirebaseAuthWeakPasswordException e) {
                                                erroExcecao = "Digite uma senha mais forte!";
                                                titulo = "Senha";
                                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                                erroExcecao = "E-mail inválido! Informe um e-mail válido!";
                                                titulo = "Email";
                                            } catch (FirebaseAuthUserCollisionException e) {
                                                erroExcecao = "Esta conta já foi cadastrada";
                                                titulo = "Conta";
                                            } catch (Exception e) {
                                                erroExcecao = "ao cadastrar usuário: " + e.getMessage();
                                               titulo = "Usuário";
                                            }

                                            alertaSimples(erroExcecao, getApplicationContext(), titulo);
                                        }
                                    }
                                });

                            } else { //login

                            /*final ProgressDialog progressDialog = new ProgressDialog(AutenticacaoActivity.this,
                                    R.style.Theme_AppCompat_Light_DarkActionBar);
                            progressDialog.setIndeterminate(true);
                            progressDialog.setMessage("Autenticando...");
                            progressDialog.show();*/


                                autenticacao.signInWithEmailAndPassword(
                                        email, senha
                                ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {

                                            //botaoAcessar.setEnabled(false);

                                            Toast.makeText(AutenticacaoActivity.this,
                                                    "Logado com sucesso!",
                                                    Toast.LENGTH_SHORT).show();
                                            String tipoUsuario = task.getResult().getUser().getDisplayName();
                                            abrirTelaPrincipal(tipoUsuario);

                                        } else {
                                            alertaSimples(task.getException().toString(), getApplicationContext(), "Falha ao tentar logar");
                                        }
                                    }
                                });

                                //progressDialog.dismiss();

                            }

                        } else {
                            alertaSimples("Senha é obrigatória!", getApplicationContext(), "Campo Senha está vazio");
                        }
                    } else {
                        alertaSimples("Email é obrigatório!", getApplicationContext(), "Campo Email está vazio");
                    }

                    dialog.dismiss();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 5s = 5000ms
                       botaoAcessar.setClickable(true);
                    }
                }, 1500);
            }
        });


    }

    private void verificarUsuarioLogado(){
        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();
        if(usuarioAtual != null) {
            String tipoUsuario = usuarioAtual.getDisplayName();
            abrirTelaPrincipal(tipoUsuario);
        }

    }

    private String getTipoUsuario(){
        return tipoUsuario.isChecked() ? "E" : "U";
    }

    private void abrirTelaPrincipal(String tipoUsuario){
        if(tipoUsuario.equals("E")){ // empresa
            startActivity(new Intent(getApplicationContext(), EmpresaActivity.class));
        }else { // usuario
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        }

    }

    private void inicializarComponentes(){
                campoEmail = findViewById(R.id.editCadastroEmail);
                campoSenha = findViewById(R.id.editCadastroSenha);
                botaoAcessar = findViewById(R.id.buttonAcesso);
                tipoAcesso = findViewById(R.id.switchAcesso);
                linearTipoUsuario = findViewById(R.id.linearTipoUsuario);
                botaoAcessar.setClickable(true);
    }

    public void alertaSimples(String conteudo, Context context, String titulo){
        MessengerDialog dialog = new MessengerDialog();
        dialog.setConteudo(conteudo);
        dialog.setTitulo(titulo);
        dialog.show(getSupportFragmentManager(), "exemplo dialog");
    }

}