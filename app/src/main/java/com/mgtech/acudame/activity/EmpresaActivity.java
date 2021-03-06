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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.mgtech.acudame.R;
import com.mgtech.acudame.adapter.AdapterProdutoEmpresa;
import com.mgtech.acudame.feedback.Feedback;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;
import com.mgtech.acudame.helper.UsuarioFirebase;
import com.mgtech.acudame.listener.RecyclerItemClickListener;
import com.mgtech.acudame.model.Empresa;
import com.mgtech.acudame.model.Produto;
import com.mgtech.acudame.token.TokenEmpresa;

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
    private int produtoOpcao;
    private Empresa empresa;
    private AlertDialog dialog, dialog2;
    private AdView anuncio;
    private FloatingActionButton fab_actionProduto, fab_actionComplemento, fab_actionPizza;
    private TextView textStatus;
    private Button buttonStatus;
    private Boolean status = true;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empresa);

        // conf iniciais
        inicializarComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();

        //Recuperra token
        recuperarToken();

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
        toolbar.setTitle("Acuda-me || Empresa");
        setSupportActionBar(toolbar);

        // conf recyclerview
        recyclerProdutos.setLayoutManager(new LinearLayoutManager(this));
        recyclerProdutos.setHasFixedSize(true);
        adapterProduto = new AdapterProdutoEmpresa(produtos, this);
        recyclerProdutos.setAdapter(adapterProduto);

        // recupera os dados da empresa
        recuperarDadosEmpresa();

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
                                            Intent intent = getIntent();
                                            finish();
                                            startActivity(intent);
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

        // add evento do clique no botao status
        buttonStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(status){
                    //atualizando o status
                    empresa.setStatus(false);
                    empresa.atualizarStatusEmpresa();
                    finish();
                    overridePendingTransition( 0, 0);
                    startActivity(getIntent());
                    overridePendingTransition( 0, 0);
                }else {
                    //atualizando o status
                    empresa.setStatus(true);
                    empresa.atualizarStatusEmpresa();
                    finish();
                    overridePendingTransition( 0, 0);
                    startActivity(getIntent());
                    overridePendingTransition( 0, 0);
                }
            }
        });

        fab_actionProduto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirNovoProduto();
            }
        });

        fab_actionPizza.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirSabores();
            }
        });

        fab_actionComplemento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirComplementos();
            }
        });
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

    private void recuperarDadosEmpresa() {

        dialog2 = new SpotsDialog.Builder()
                .setContext(EmpresaActivity.this)
                .setMessage("Carregando Dados")
                .setCancelable(false)
                .build();
        dialog2.show();

        DatabaseReference empresaRef = firebaseRef
                .child("empresas")
                .child(idUsuarioLogado);
        empresaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    empresa = dataSnapshot.getValue(Empresa.class);
                    if(empresa.getStatus() != null) {
                        if (empresa.getStatus()) {
                            textStatus.setText("Sua Empresa Está: " + "ABERTA");
                        } else {
                            textStatus.setText("Sua Empresa Está: " + "FECHADA");
                            buttonStatus.setText("ABRIR");
                            buttonStatus.setBackgroundResource(R.drawable.bt_status_aberto);
                            status = false;
                        }
                    }
                }

                dialog2.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void inicializarComponentes() {
        recyclerProdutos = findViewById(R.id.recyclerProdutos);
        anuncio = findViewById(R.id.empresaAnuncio);
        fab_actionProduto = findViewById(R.id.fab_actionProduto);
        fab_actionComplemento = findViewById(R.id.fab_actionComplemento);
        fab_actionPizza = findViewById(R.id.fab_actionPizza);
        textStatus = findViewById(R.id.textEmpresaStatus);
        buttonStatus = findViewById(R.id.buttonEmpresaStatus);

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
            case R.id.menuFaleconosco :
                abrirFaleConosco();
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
            case R.id.menuPedidos :
                abrirPedidos();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deslogarUsuario() {
        try {

            autenticacao.signOut();
            startActivity(new Intent(EmpresaActivity.this, AutenticacaoActivity.class));
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

    private void abrirComplementos() {
        startActivity(new Intent(EmpresaActivity.this, ComplementosActivity.class));
    }

    private void abrirSabores() {
        startActivity(new Intent(EmpresaActivity.this, SaboresActivity.class));
    }

    private void abrirFaleConosco() {
        startActivity(new Intent(EmpresaActivity.this, Feedback.class));
    }

    public void recuperarToken(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {

                        //token
                        token = task.getResult().getToken();

                        TokenEmpresa tokenEmpresa = new TokenEmpresa();
                        tokenEmpresa.setToken(token);
                        tokenEmpresa.setIdEmpresa(idUsuarioLogado);
                        tokenEmpresa.salvarTokenEmpresa();
                    }
                });
    }


}
