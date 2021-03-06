package com.mgtech.acudame.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.mgtech.acudame.R;
import com.mgtech.acudame.adapter.AdapterEmpresa;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;
import com.mgtech.acudame.helper.UsuarioFirebase;
import com.mgtech.acudame.listener.RecyclerItemClickListener;
import com.mgtech.acudame.model.Empresa;
import com.mgtech.acudame.model.Usuario;
import com.mgtech.acudame.token.TokenUsuario;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;
import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

import com.mgtech.acudame.feedback.Feedback;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private MaterialSearchView searchView;
    private RecyclerView recyclerEmpresas;
    private AdapterEmpresa adapterEmpresa;
    private List<Empresa> empresas = new ArrayList<>();
    private DatabaseReference firebaseRef;
    private AlertDialog dialog;
    private AdView anuncioHome;
    private Usuario usuario;
    private String token;
    private String idUsuarioLogado;
    private ArrayList<String> items = new ArrayList<>();
    private ArrayList<String> itemsCidades = new ArrayList<>();
    private SpinnerDialog spinnerDialogEstados;
    private SpinnerDialog spinnerDialogCidades;
    private ImageView imgFilter;
    private TextView textCidade;
    private static final long MIN_CLICK_INTERVAL=600;
    private long mLastClickTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Anuncio
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        //conf inicias
        inicializarComponentes();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();
        initItems();

        //Pegando token
        recuperarToken();

        AdRequest adRequest = new AdRequest.Builder().build();
        anuncioHome.loadAd(adRequest);

        // configurações toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Acuda-me");
        setSupportActionBar(toolbar);

        // conf recyclerview
        recyclerEmpresas.setLayoutManager(new LinearLayoutManager(this));
        recyclerEmpresas.setHasFixedSize(true);
        adapterEmpresa = new AdapterEmpresa(empresas);
        recyclerEmpresas.setAdapter(adapterEmpresa);

        // recupera as empresas cadastradas
        recuperarEmpresas();

        // conf search view (botao pesquisar)
        searchView.setHint("Pesquisar restaurantes");
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                pesquisarEmpresas(newText);
                return true;
            }
        });

        // conf evento de clique na empresa
        recyclerEmpresas.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerEmpresas,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Empresa empresaSelecionada = empresas.get(position);
                                Intent i = new Intent(HomeActivity.this, CardapioActivity.class);
                                i.putExtra("empresa", empresaSelecionada);
                                startActivity(i);
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

    private void pesquisarEmpresas(String pesquisa) {

        // deixando a primeira letra da palavra em maiuscula
        if(!pesquisa.isEmpty()){
            pesquisa = pesquisa.substring(0,1).toUpperCase().concat(pesquisa.substring(1));
        }

        DatabaseReference empresaRef = firebaseRef.child("empresas");
        Query query = empresaRef.orderByChild("nome")
                .startAt(pesquisa)
                .endAt(pesquisa + "\uf8ff");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                empresas.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    empresas.add(ds.getValue(Empresa.class));
                }
                adapterEmpresa.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void recuperarEmpresas(){

        dialog = new SpotsDialog.Builder()
                .setContext(HomeActivity.this)
                .setMessage("Carregando Dados")
                .setCancelable(false)
                .build();
        dialog.show();

        DatabaseReference empresaRef = firebaseRef.child("empresas");
        empresaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                empresas.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    empresas.add(ds.getValue(Empresa.class));
                }
                adapterEmpresa.notifyDataSetChanged();

                dialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_usuario, menu);

        // configurar botao pesquisa
        MenuItem item = menu.findItem(R.id.menuPesquisa);
        searchView.setMenuItem(item);

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
            case R.id.menuHistoricoPedidos :
                abrirHistoricoPedidos();
                break;
            case R.id.menuFaleconosco :
                abrirHistoricoFeedback();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void inicializarComponentes() {
        searchView = findViewById(R.id.materialSearchView);
        recyclerEmpresas = findViewById(R.id.recyclerEmpresas);
        anuncioHome = findViewById(R.id.homeAnuncio);
        imgFilter = findViewById(R.id.imgFilter);
        textCidade = findViewById(R.id.textCidade);
    }

    private void deslogarUsuario() {
        try {

            autenticacao.signOut();
            startActivity(new Intent(HomeActivity.this, AutenticacaoActivity.class));
            finish();

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void abrirConfiguracoes() {
        startActivity(new Intent(HomeActivity.this, ConfiguracoesUsuarioActivity.class));
    }

    private void abrirHistoricoPedidos() {
        startActivity(new Intent(HomeActivity.this, HistoricoPedidosUsuarioActivity.class));
    }

    private void abrirHistoricoFeedback() {
        startActivity(new Intent(HomeActivity.this, Feedback.class));
    }

    public void recuperarToken(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {

                        //token
                        token = task.getResult().getToken();
                        TokenUsuario tokenUsuario = new TokenUsuario();
                        tokenUsuario.setToken(token);
                        tokenUsuario.setIdUsuario(idUsuarioLogado);
                        tokenUsuario.salvarTokenUsuario();

                    }
                });
    }

    private void initItems() {
        items.add("Rio Grande do Norte");
        items.add("Ceará");
        items.add("Paraíba");
        itemsCidades.add("José da Penha");
        itemsCidades.add("Pau dos Ferros");
        itemsCidades.add("Major Sales");
        itemsCidades.add("Luiz Gomes");
    }

    public final void onClick(View v) {
        //Evitar clique duplo para não abrir dois alert
        long currentClickTime=SystemClock.uptimeMillis();
        long elapsedTime=currentClickTime-mLastClickTime;
        mLastClickTime=currentClickTime;

        if(elapsedTime<=MIN_CLICK_INTERVAL){

            spinnerDialogEstados = new SpinnerDialog(HomeActivity.this, items, "Selecione o Estado");
            spinnerDialogEstados.bindOnSpinerListener(new OnSpinerItemClick() {

                @Override
                public void onClick(String s, int i) {

                    spinnerDialogCidades = new SpinnerDialog(HomeActivity.this, itemsCidades, "Selecione a cidade");
                    spinnerDialogCidades.showSpinerDialog();
                    spinnerDialogCidades.bindOnSpinerListener(new OnSpinerItemClick() {
                        @Override
                        public void onClick(String s, int i) {
                            Toast.makeText(HomeActivity.this, "Cidade : "  + s, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            });

            spinnerDialogEstados.showSpinerDialog();
        }
    }
}
