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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mgtech.acudame.R;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;
import com.mgtech.acudame.helper.UsuarioFirebase;
import com.mgtech.acudame.model.Produto;
import dmax.dialog.SpotsDialog;

public class ConfiguracoesProdutoActivity extends AppCompatActivity {

    private EditText editProdutoNome, editProdutoDescricao, editProdutoPreco;
    private Switch tipoStatus;
    private Button buttonSalvar;
    private DatabaseReference firebaseRef;
    private String idEmpresaLogada, idPro;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes_produto);

        // conf iniciais
        inicializarComponentes();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idEmpresaLogada = UsuarioFirebase.getIdUsuario();

        // recuperar id do produto selecionado
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            idPro = (String) bundle.getSerializable("produto");
        }

        // configurações toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Editar Produto");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        buttonSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarDadosProduto();
            }
        });

        // Recuperar dados do produto
        recuperarDadosProduto();
    }

    private void recuperarDadosProduto() {

        dialog = new SpotsDialog.Builder()
                .setContext(ConfiguracoesProdutoActivity.this)
                .setMessage("Carregando Dados")
                .setCancelable(false)
                .build();
        dialog.show();

        DatabaseReference produtoRef = firebaseRef
                .child("produtos")
                .child(idEmpresaLogada)
                .child(idPro);

        produtoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    Produto produto = dataSnapshot.getValue(Produto.class);
                    editProdutoNome.setText(produto.getNome());
                    editProdutoDescricao.setText(produto.getDescricao());
                    editProdutoPreco.setText(produto.getPreco().toString());
                }

                dialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void validarDadosProduto() {

        // verifica se os campos foram preenchidos
        String nome = editProdutoNome.getText().toString();
        String descricao = editProdutoDescricao.getText().toString();
        String preco = editProdutoPreco.getText().toString();
        String status = "ativo";

        // verifica estado do switch
        if(tipoStatus.isChecked()) {
            status = "inativo";
        }

        if (!nome.isEmpty()) {
            if (!descricao.isEmpty()) {
                if (!preco.isEmpty()) {
                    Produto produto = new Produto();
                    produto.setIdUsuario(idEmpresaLogada);
                    produto.setIdProduto(idPro);
                    produto.setNome(nome);
                    produto.setDescricao(descricao);
                    produto.setPreco(Double.parseDouble(preco));
                    produto.setStatus(status);
                    produto.salvar();
                    finish();
                    exibirMensagem("Produto salvo com sucesso!");
                } else {
                    exibirMensagem("Informe o preço do produto");
                }
            } else {
                exibirMensagem("Digite uma descrição clara sobre o produto");
            }
        } else {
            exibirMensagem("Digite um nome para o produto");
        }
    }

    private void exibirMensagem(String texto){
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }

    private void inicializarComponentes() {
        editProdutoNome = findViewById(R.id.editProdutoNome);
        editProdutoDescricao = findViewById(R.id.editProdutoDescricao);
        editProdutoPreco = findViewById(R.id.editProdutoPreco);
        tipoStatus = findViewById(R.id.switchStatus);
        buttonSalvar = findViewById(R.id.buttonSalvar);
    }
}
