package com.mgtech.acudame.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.mgtech.acudame.R;
import com.mgtech.acudame.adapter.AdapterSpinner;
import com.mgtech.acudame.helper.UsuarioFirebase;
import com.mgtech.acudame.model.CostumItem;
import com.mgtech.acudame.model.Produto;

import java.util.ArrayList;

public class NovoProdutoEmpresaActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText editProdutoNome, editProdutoDescricao, editProdutoPreco;
    private Switch tipoStatus;
    private Button buttonSalvar;
    private Spinner spinnerCatogoria;
    private ArrayList<CostumItem> costumItems;
    private int width = 150;
    private String idUsuarioLogado;
    private String categoriaEmpresa;

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
        spinnerCatogoria = findViewById(R.id.spinnerCategoria);
        costumItems = getCustomList();
        AdapterSpinner adapterSpinner = new AdapterSpinner(NovoProdutoEmpresaActivity.this, costumItems);
        spinnerCatogoria.setAdapter(adapterSpinner);
        spinnerCatogoria.setOnItemSelectedListener(this);

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
        String categoria = categoriaEmpresa;
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        try {
            LinearLayout linearLayout = findViewById(R.id.custom);
            width=linearLayout.getWidth();
        }catch (Exception e){

        }
        spinnerCatogoria.setDropDownWidth(width);
        CostumItem item = (CostumItem) parent.getSelectedItem();
        categoriaEmpresa = item.getSpinnerItemName().toLowerCase();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    private ArrayList<CostumItem> getCustomList() {
        costumItems = new ArrayList<>();
        costumItems.add(new CostumItem("Comida", R.drawable.ic_c));
        costumItems.add(new CostumItem("Bebida", R.drawable.ic_b));
        costumItems.add(new CostumItem("Sorvete", R.drawable.ic_s));
        costumItems.add(new CostumItem("Pizza", R.drawable.ic_pizza_black_24dp));
        return costumItems;
    }
}
