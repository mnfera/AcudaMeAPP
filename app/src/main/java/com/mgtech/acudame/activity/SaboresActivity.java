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
import com.mgtech.acudame.adapter.AdapterSabor;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;
import com.mgtech.acudame.helper.UsuarioFirebase;
import com.mgtech.acudame.listener.RecyclerItemClickListener;
import com.mgtech.acudame.messenger.MessengerDialog;
import com.mgtech.acudame.model.Sabor;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class SaboresActivity extends AppCompatActivity {

    private RecyclerView recyclerSabores;
    private AdapterSabor adapterSabor;
    private List<Sabor> sabores = new ArrayList<>();
    private AlertDialog dialog;
    private DatabaseReference firebaseRef;
    private String idEmpresa, idSabor;
    private int saborPosicao;
    private AdView anuncio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sabores);

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
        toolbar.setTitle("Sabores das Pizzas");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // conf recyclerview
        recyclerSabores.setLayoutManager(new LinearLayoutManager(this));
        recyclerSabores.setHasFixedSize(true);
        adapterSabor = new AdapterSabor(sabores, SaboresActivity.this);
        recyclerSabores.setAdapter(adapterSabor);

        // recuperar os complementos
        recuperarSabores();

        recyclerSabores.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerSabores,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                //posicaoItem = position;
                                AlertDialog.Builder builder = new AlertDialog.Builder(SaboresActivity.this);
                                builder.setTitle("Selecione uma opção para o sabor");

                                CharSequence[] itens = new CharSequence[]{
                                        "Editar o sabor", "Excluir o sabor"
                                };
                                builder.setSingleChoiceItems(itens, 0, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        saborPosicao = which;
                                    }
                                });

                                builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.O)
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if(saborPosicao == 0){
                                            Sabor sabor = sabores.get(position);
                                            idSabor = sabor.getIdSabor();
                                            Intent i = new Intent(SaboresActivity.this, ConfiguracoesSaboresActivity.class);
                                            i.putExtra("sabor", idSabor);
                                            startActivity(i);
                                        }else{
                                            Sabor saborSelecionado = sabores.get(position);
                                            saborSelecionado.removerSabor();
                                            startActivity(new Intent(SaboresActivity.this, SaboresActivity.class));
                                            finish();
                                            Toast.makeText(SaboresActivity.this,
                                                    "Sabor Excluído com sucesso!",
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
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
        }));
    }

    private void recuperarSabores() {
        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Carregando Sabores")
                .setCancelable(false)
                .build();
        dialog.show();

        DatabaseReference saboresPesquisa = firebaseRef
                .child("sabores")
                .child(idEmpresa);

        saboresPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sabores.clear();
                if (dataSnapshot.getValue() != null) {

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Sabor sabor = ds.getValue(Sabor.class);
                        sabores.add(sabor);
                    }
                    adapterSabor.notifyDataSetChanged();

                } else {
                    alertaSimples("Não há nenhum sabor de pizza cadastrado", getApplicationContext(), "Sabores");
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
        recyclerSabores = findViewById(R.id.recyclerSabores);
        anuncio = findViewById(R.id.saboresAnuncio);
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
                startActivity(new Intent(SaboresActivity.this, NovoSaborActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
