package com.mgtech.acudame.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
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
import com.mgtech.acudame.adapter.AdapterComplementoActivity;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;
import com.mgtech.acudame.helper.UsuarioFirebase;
import com.mgtech.acudame.listener.RecyclerItemClickListener;
import com.mgtech.acudame.messenger.MessengerDialog;
import com.mgtech.acudame.model.Complemento;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class ComplementosActivity extends AppCompatActivity {

    private RecyclerView recyclerComplementos;
    private AdapterComplementoActivity adapterComplemento;
    private List<Complemento> complementos = new ArrayList<>();
    private AlertDialog dialog;
    private DatabaseReference firebaseRef;
    private String idEmpresa, idComplemento;
    private int complementoPosicao;
    private AdView anuncio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complementos);

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

        // configurações toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Complementos Sorvetes || Açaís");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // conf recyclerview
        recyclerComplementos.setLayoutManager(new LinearLayoutManager(this));
        recyclerComplementos.setHasFixedSize(true);
        adapterComplemento = new AdapterComplementoActivity(complementos, ComplementosActivity.this);
        recyclerComplementos.setAdapter(adapterComplemento);

        // recuperar os complementos
        recuperarComplementos();

        recyclerComplementos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerComplementos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                //posicaoItem = position;
                                AlertDialog.Builder builder = new AlertDialog.Builder(ComplementosActivity.this);
                                builder.setTitle("Selecione uma opção para o complemento");

                                CharSequence[] itens = new CharSequence[]{
                                        "Editar complemento", "Excluir complemento"
                                };
                                builder.setSingleChoiceItems(itens, 0, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        complementoPosicao = which;
                                    }
                                });

                                builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.O)
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if(complementoPosicao == 0){
                                            Complemento complemento = complementos.get(position);
                                            idComplemento = complemento.getIdComplemento();
                                            Intent i = new Intent(ComplementosActivity.this, ConfiguracoesComplementoActivity.class);
                                            i.putExtra("complemento", idComplemento);
                                            startActivity(i);
                                        }else{
                                            Complemento complementoSelecionado = complementos.get(position);
                                            complementoSelecionado.removerComplemento();
                                            startActivity(new Intent(ComplementosActivity.this, ComplementosActivity.class));
                                            Toast.makeText(ComplementosActivity.this,
                                                    "Complemento Excluído com sucesso!",
                                                    Toast.LENGTH_SHORT).show();
                                            finish();
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
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
        }));
    }

    private void recuperarComplementos() {
        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Carregando Complementos")
                .setCancelable(false)
                .build();
        dialog.show();

        DatabaseReference complementosPesquisa = firebaseRef
                .child("complementos")
                .child(idEmpresa);

        complementosPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                complementos.clear();
                if (dataSnapshot.getValue() != null) {

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Complemento complemento = ds.getValue(Complemento.class);
                        complementos.add(complemento);
                    }
                    adapterComplemento.notifyDataSetChanged();

                } else {
                    alertaSimples("Não há nenhum complemento", getApplicationContext(), "Complementos");
                }
                dialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void alertaSimples(String conteudo, Context context, String titulo) {
        MessengerDialog dialog = new MessengerDialog();
        dialog.setConteudo(conteudo);
        dialog.setTitulo(titulo);
        dialog.show(getSupportFragmentManager(), "exemplo dialog");
    }

    private void inicializarComponentes() {
        recyclerComplementos = findViewById(R.id.recyclerComplementos);
        anuncio = findViewById(R.id.complementosAnuncio);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_complemento, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuNovoComplemento :
                startActivity(new Intent(ComplementosActivity.this, NovoComplementoActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
