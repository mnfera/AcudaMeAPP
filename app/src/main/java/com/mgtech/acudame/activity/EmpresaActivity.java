package com.mgtech.acudame.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.mgtech.acudame.R;
import com.mgtech.acudame.adapter.AdapterProduto;
import com.mgtech.acudame.adapter.AdapterProdutoEmpresa;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;
import com.mgtech.acudame.helper.UsuarioFirebase;
import com.mgtech.acudame.listener.RecyclerItemClickListener;
import com.mgtech.acudame.model.Empresa;
import com.mgtech.acudame.model.Pedido;
import com.mgtech.acudame.model.Produto;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class EmpresaActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private RecyclerView recyclerProdutos;
    private AdapterProdutoEmpresa adapterProduto;
    private List<Produto> produtos = new ArrayList<>();
    private DatabaseReference firebaseRef;
    private String idUsuarioLogado, idPro;
    private int posicaoItem, produtoOpcao;
    private Empresa empresa;
    private AlertDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empresa);

        // conf iniciais
        inicializarComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();

        //Salvando token da empresa
        recuperarToken ();



        // configurações toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Acuda-me || Empresa");
        setSupportActionBar(toolbar);

        // conf recyclerview
        recyclerProdutos.setLayoutManager(new LinearLayoutManager(this));
        recyclerProdutos.setHasFixedSize(true);
        adapterProduto = new AdapterProdutoEmpresa(produtos, this);
        recyclerProdutos.setAdapter(adapterProduto);

        // recupera os produtos da empresa
        recuperarProdutos();

        // add evento de clique no recycler view
        recyclerProdutos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerProdutos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, final int position) {

                                //posicaoItem = position;
                                AlertDialog.Builder builder = new AlertDialog.Builder(EmpresaActivity.this);
                                builder.setTitle("Selecione uma opção para o produto");

                                CharSequence[] itens = new CharSequence[]{
                                        "Editar produto", "Excluir produto"
                                };
                                builder.setSingleChoiceItems(itens, 0, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        produtoOpcao = which;
                                    }
                                });

                                builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.O)
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if(produtoOpcao == 0){
                                            Produto produto = produtos.get(position);
                                            idPro = produto.getIdProduto();
                                            Intent i = new Intent(EmpresaActivity.this, ConfiguracoesProdutoActivity.class);
                                            i.putExtra("produto", idPro);
                                            startActivity(i);
                                        }else{
                                            Produto produtoSelecionado = produtos.get(position);
                                            produtoSelecionado.remover();
                                            Toast.makeText(EmpresaActivity.this,
                                                    "Produto Excluído com sucesso!",
                                                    Toast.LENGTH_SHORT).show();
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

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );
    }

    private void recuperarProdutos(){

        dialog = new SpotsDialog.Builder()
                .setContext(EmpresaActivity.this)
                .setMessage("Carregando Dados")
                .setCancelable(false)
                .build();
        dialog.show();

        DatabaseReference produtosRef = firebaseRef
                .child("produtos")
                .child(idUsuarioLogado);
        produtosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                produtos.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    produtos.add(ds.getValue(Produto.class));
                }
                adapterProduto.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void inicializarComponentes() {
        recyclerProdutos = findViewById(R.id.recyclerProdutos);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_empresa, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuSair :
                deslogarUsuario();
                break;
            case R.id.menuConfiguracoes :
                abrirConfiguracoes();
                break;
            case R.id.menuPedidosRecebidos :
                abrirPedidosRecebidos();
                break;
            case R.id.menuPedidosFinalizados :
                abrirPedidosFinalizados();
                break;
            case R.id.menuPedidosCancelados :
                abrirPedidosCancelados();
                break;
            case R.id.menuNovoProduto :
                abrirNovoProduto();
                break;
            case R.id.menuPedidos :
                abrirPedidos();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deslogarUsuario() {
        try {

            autenticacao.signOut();
            finish();

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void abrirPedidos() {
        startActivity(new Intent(EmpresaActivity.this, PedidosActivity.class));
    }

    private void abrirConfiguracoes() {
        startActivity(new Intent(EmpresaActivity.this, ConfiguracoesEmpresaActivity.class));
    }

    private void abrirPedidosRecebidos() {
        startActivity(new Intent(EmpresaActivity.this, PedidosRecebidosEmpresaActivity.class));
    }

    private void abrirPedidosFinalizados() {
        startActivity(new Intent(EmpresaActivity.this, PedidosFinalizadosEmpresaActivity.class));
    }

    private void abrirPedidosCancelados() {
        startActivity(new Intent(EmpresaActivity.this, PedidosCanceladosEmpresaActivity.class));
    }

    private void abrirNovoProduto() {
        startActivity(new Intent(EmpresaActivity.this, NovoProdutoEmpresaActivity.class));
    }

    public void recuperarToken (){

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                //recuperando token
                String token = instanceIdResult.getToken();

                //salvando
                empresa = new Empresa();
                empresa.setTokenEmpresa(token);
                empresa.setIdUsuario(idUsuarioLogado);
                empresa.salvarTokenEmpresa();

            }
        });
    }
}
