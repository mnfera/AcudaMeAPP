package com.mgtech.acudame.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.mgtech.acudame.R;
import com.mgtech.acudame.helper.UsuarioFirebase;
import com.mgtech.acudame.model.Produto;

public class NovoProdutoEmpresaActivity extends AppCompatActivity {

    private EditText editProdutoNome, editProdutoDescricao, editProdutoPreco;
    private Switch tipoStatus;
    private Button buttonSalvar;
    private Spinner spinnerCatogoria;
    private String idUsuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_produto_empresa);

        // conf iniciais
        inicializarComponentes();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();

        // configurações toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo Produto");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // conf spinner
        String[] lsCategoria = getResources().getStringArray(R.array.lista_categoria_produto);
        spinnerCatogoria.setAdapter(new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, lsCategoria));

        buttonSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarDadosProduto();
            }
        });
    }

    private void validarDadosProduto() {

        // verifica se os campos foram preenchidos
        String nome = editProdutoNome.getText().toString();
        String descricao = editProdutoDescricao.getText().toString();
        String preco = editProdutoPreco.getText().toString();
        String categoria = spinnerCatogoria.getSelectedItem().toString();
        String status = "ativo";

        // verifica estado do switch
        if(tipoStatus.isChecked()) {
            status = "inativo";
        }

        if(!nome.isEmpty()) {
            if(!descricao.isEmpty()) {
                if(!preco.isEmpty()) {

                    //Inserindo os produtos na tabela do banco
                    Produto produto = new Produto();
                    produto.setIdUsuario(idUsuarioLogado);
                    produto.setNome(nome);
                    produto.setDescricao(descricao);
                    produto.setPreco(Double.parseDouble(preco));
                    produto.setCategoria(categoria);
                    produto.setStatus(status);
                    produto.setStatusCategoria(status, categoria);
                    produto.salvar();
                    finish();
                    exibirMensagem("Produto salvo com sucesso!");

                }else {
                    exibirMensagem("Informe um preço para o produto");
                }
            }else {
                exibirMensagem("Digite uma descrição para o produto");
            }
        }else {
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
        spinnerCatogoria = findViewById(R.id.spinnerCategoria);
    }
}
