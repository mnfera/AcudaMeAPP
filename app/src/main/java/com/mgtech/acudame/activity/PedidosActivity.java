package com.mgtech.acudame.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mgtech.acudame.R;
import com.mgtech.acudame.adapter.AdapterPedido;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;
import com.mgtech.acudame.helper.UsuarioFirebase;
import com.mgtech.acudame.listener.RecyclerItemClickListener;
import com.mgtech.acudame.message.MessengerDialog;
import com.mgtech.acudame.model.Pedido;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class PedidosActivity extends AppCompatActivity {

    private RecyclerView recyclerPedidos;
    private AdapterPedido adapterPedido;
    private List<Pedido> pedidos = new ArrayList<>();
    private AlertDialog dialog;
    private DatabaseReference firebaseRef;
    private String idEmpresa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos);

        // conf iniciais
        inicializarComponentes();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idEmpresa = UsuarioFirebase.getIdUsuario();

        // configurações toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Pedidos");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // conf recyclerview
        recyclerPedidos.setLayoutManager(new LinearLayoutManager(this));
        recyclerPedidos.setHasFixedSize(true);
        adapterPedido = new AdapterPedido(pedidos);
        recyclerPedidos.setAdapter(adapterPedido);

        // recuperar os pedidos
        recuperarPedidos();

        // add evento de clique recyclerview
        recyclerPedidos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerPedidos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                                Pedido pedido = pedidos.get(position);
                                pedido.setStatus("finalizado");
                                pedido.atualizarStatus();

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );
    }

    private void recuperarPedidos() {

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Carregando Pedidos")
                .setCancelable(false)
                .build();
        dialog.show();

        DatabaseReference pedidosRef = firebaseRef
                .child("pedidos")
                .child(idEmpresa);

        Query pedidoPesquisa = pedidosRef.orderByChild("status")
                .equalTo("confirmado");

        pedidoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                pedidos.clear();
                if(dataSnapshot.getValue() != null) {

                    for(DataSnapshot ds: dataSnapshot.getChildren()) {
                        Pedido pedido = ds.getValue(Pedido.class);
                        pedidos.add(pedido);
                    }

                    adapterPedido.notifyDataSetChanged();
                }else {
                    alertaSimples("Não há nenhum pedido", getApplicationContext(), "Pedidos ");
                }
                dialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void inicializarComponentes() {
        recyclerPedidos = findViewById(R.id.recyclerPedidos);
    }

    public void alertaSimples(String conteudo, Context context, String titulo){
        MessengerDialog dialog = new MessengerDialog();
        dialog.setConteudo(conteudo);
        dialog.setTitulo(titulo);
        dialog.show(getSupportFragmentManager(), "exemplo dialog");
    }

}
