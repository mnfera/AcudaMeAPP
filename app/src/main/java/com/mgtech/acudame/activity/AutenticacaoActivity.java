package com.mgtech.acudame.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
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
import com.google.firebase.database.DatabaseReference;
import com.mgtech.acudame.R;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;
import com.mgtech.acudame.helper.UsuarioFirebase;
import com.mgtech.acudame.messenger.MessengerDialog;


import dmax.dialog.SpotsDialog;

public class AutenticacaoActivity extends AppCompatActivity {

    private Button botaoAcessar;
    private EditText campoEmail, campoSenha, campoConfirmarSenha;
    private Switch tipoAcesso;
    private LinearLayout linearTipoUsuario;
    private AlertDialog dialog;
    private FirebaseAuth autenticacao;
    final Handler handler = new Handler();
    private TextView recuperarSenha;
    private DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autenticacao);


        inicializarComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        reference = ConfiguracaoFirebase.getFirebase();


        // verificar se usuario está logado
        verificarUsuarioLogado();

        tipoAcesso.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    linearTipoUsuario.setVisibility(View.VISIBLE);
                }else {
                    linearTipoUsuario.setVisibility(View.GONE);
                }
            }
        });

        recuperarSenha.setClickable(true);
        recuperarSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recuperarEmail();
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
                    String confirmarSenha = campoConfirmarSenha.getText().toString();

                    if (!email.isEmpty()) {
                        if (!senha.isEmpty()) {

                            // verifica estado do switch
                            if (tipoAcesso.isChecked()) { // cadastro

                                if (senha.equals(confirmarSenha)) {

                                    autenticacao.createUserWithEmailAndPassword(
                                            email, senha
                                    ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {

                                            if (task.isSuccessful()) {

                                                //Enviar link email para verificação
                                                autenticacao.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful()){
                                                            alertaSimples("Cadastro realizado com sucesso! Por favor, verifique seu email!", getApplicationContext(), "Cadastro");

                                                            String tipoUsuario = "U";
                                                            UsuarioFirebase.atualizarTipoUsuario(tipoUsuario);

                                                        }else{
                                                            Toast.makeText(AutenticacaoActivity.this,
                                                                    task.getException().getMessage(),
                                                                    Toast.LENGTH_SHORT).show();
                                                        }

                                                    }
                                                });

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
                                }else {

                                    Toast.makeText(AutenticacaoActivity.this,
                                            "As senhas não são iguais!",
                                            Toast.LENGTH_SHORT).show();
                                }

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

                                            String tipoUsuario = task.getResult().getUser().getDisplayName();

                                            if(tipoUsuario == null){
                                                tipoUsuario = "E";
                                                UsuarioFirebase.atualizarTipoUsuario(tipoUsuario);
                                            }

                                            if(tipoUsuario .equals("E")){ //Se for empresa logando

                                                Toast.makeText(AutenticacaoActivity.this,
                                                        "Logado com sucesso!",
                                                        Toast.LENGTH_SHORT).show();

                                                abrirTelaPrincipal(tipoUsuario);

                                            }else{ //Usuario logando
                                                //Verificando vizualização do link enviado para o email
                                                if(autenticacao.getCurrentUser().isEmailVerified()){
                                                    Toast.makeText(AutenticacaoActivity.this,
                                                            "Logado com sucesso!",
                                                            Toast.LENGTH_SHORT).show();

                                                    abrirTelaPrincipal(tipoUsuario);

                                                }else {
                                                    alertaSimples("Por favor, verifique seu endereço de email", getApplicationContext(), "Email não verificado");
                                                }
                                            }

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

        if(usuarioAtual != null){

            String tipoUsuario = usuarioAtual.getDisplayName();

            if (tipoUsuario.equals("E")) {
                abrirTelaPrincipal(tipoUsuario);
            } else {
                if (usuarioAtual.isEmailVerified()) {
                    abrirTelaPrincipal(tipoUsuario);
                }
            }
        }
    }

    private void abrirTelaPrincipal(String tipoUsuario){
        if(tipoUsuario.equals("U")){ // usuario
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        }else { // empresa
            startActivity(new Intent(getApplicationContext(), EmpresaActivity.class));
        }

    }

    private void inicializarComponentes(){
                campoEmail = findViewById(R.id.editCadastroEmail);
                campoSenha = findViewById(R.id.editCadastroSenha);
                campoConfirmarSenha = findViewById(R.id.editSenhaConfirmar);
                botaoAcessar = findViewById(R.id.buttonAcesso);
                tipoAcesso = findViewById(R.id.switchAcesso);
                linearTipoUsuario = findViewById(R.id.linearTipoUsuario);
                recuperarSenha = findViewById(R.id.textEsqueciSe);

    }

    public void alertaSimples(String conteudo, Context context, String titulo){
        MessengerDialog dialog = new MessengerDialog();
        dialog.setConteudo(conteudo);
        dialog.setTitulo(titulo);
        dialog.show(getSupportFragmentManager(), "exemplo dialog");
    }

    public void recuperarSenha(){

        autenticacao.sendPasswordResetEmail(campoEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if( task.isSuccessful() ){
                    alertaSimples("Verifique seu email para recuperar sua senha", getApplicationContext(), "Recuperação da senha");
                }else{
                    alertaSimples("Falha na recuperação! Tente novamente!", getApplicationContext(), "Recuperação da senha");
                }
            }
        });
    }

    //Função para capturar o email do usário para a recuperação da senha
    public void recuperarEmail(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recuperar senha");
        builder.setMessage("Por favor, digite seu email cadastrado no sistema");

        final EditText email = new EditText(this);

        builder.setView(email);

        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(!email.getText().toString().isEmpty()){
                    recuperarSenha();
                }else{

                }
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}