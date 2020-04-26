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
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mgtech.acudame.R;
import com.mgtech.acudame.adapter.AdapterPedido;
import com.mgtech.acudame.api.NotificacaoService;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;
import com.mgtech.acudame.helper.UsuarioFirebase;
import com.mgtech.acudame.listener.RecyclerItemClickListener;
import com.mgtech.acudame.messenger.MessengerDialog;
import com.mgtech.acudame.model.Notificacao;
import com.mgtech.acudame.model.NotificacaoDados;
import com.mgtech.acudame.model.Pedido;
import com.mgtech.acudame.model.Usuario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PedidosRecebidosEmpresaActivity extends AppCompatActivity {

    private RecyclerView recyclerPedidos;
    private AdapterPedido adapterPedido;
    private List<Pedido> pedidos = new ArrayList<>();
    private AlertDialog dialog;
    private DatabaseReference firebaseRef;
    private String idEmpresa, idUsu, idPed;
    private int pedidoEntrega;
    private int posicaoItem;
    private Retrofit retrofit;
    private String baseUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos_recebidos_empresa);

        // conf iniciais
        inicializarComponentes();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idEmpresa = UsuarioFirebase.getIdUsuario();
        baseUrl = "https://fcm.googleapis.com/fcm/";
        retrofit = new Retrofit.Builder()
                .baseUrl( baseUrl )
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // configurações toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Pedidos Recebidos");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // conf recyclerview
        recyclerPedidos.setLayoutManager(new LinearLayoutManager(this));
        recyclerPedidos.setHasFixedSize(true);
        adapterPedido = new AdapterPedido(pedidos, PedidosRecebidosEmpresaActivity.this);
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

                                posicaoItem = position;
                                AlertDialog.Builder builder = new AlertDialog.Builder(PedidosRecebidosEmpresaActivity.this);
                                builder.setTitle("Selecione uma opção para o pedido:");

                                CharSequence[] itens = new CharSequence[]{
                                        "Finalizar Pedido", "Cancelar Pedido"
                                };
                                builder.setSingleChoiceItems(itens, 0, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pedidoEntrega = which;
                                    }
                                });

                                builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.O)
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if(pedidoEntrega == 0) {
                                            Pedido pedido = pedidos.get(posicaoItem);
                                            idUsu = pedido.getIdUsuario();
                                            idPed = pedido.getIdPedido();
                                            pedido.setStatus("finalizado");
                                            startActivity(new Intent(PedidosRecebidosEmpresaActivity.this, PedidosRecebidosEmpresaActivity.class));
                                            pedido.atualizarStatus();
                                            pedido.atualizarStatusPedidoUsuario(idUsu, idPed);
                                            Toast.makeText(PedidosRecebidosEmpresaActivity.this, "Pedido finalizado",
                                                    Toast.LENGTH_SHORT).show();
                                            enviarNotificacao("ATENÇÃO", "Seu pedido foi/será entregue e finalizado");

                                        }else{
                                            Pedido pedido = pedidos.get(posicaoItem);
                                            idUsu = pedido.getIdUsuario();
                                            idPed = pedido.getIdPedido();
                                            pedido.setStatus("cancelado");
                                            startActivity(new Intent(PedidosRecebidosEmpresaActivity.this, PedidosRecebidosEmpresaActivity.class));
                                            pedido.atualizarStatus();
                                            pedido.atualizarStatusPedidoUsuario(idUsu, idPed);
                                            Toast.makeText(PedidosRecebidosEmpresaActivity.this, "Pedido cancelado",
                                                    Toast.LENGTH_SHORT).show();
                                            enviarNotificacao("ATENÇÃO", "Seu pedido foi cancelado");
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
                .equalTo("recebido");

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

    public void enviarNotificacao(String titulo, String corpo){

        //recuperar token usuario
        DatabaseReference usuarioRef = firebaseRef
                .child("usuarios")
                .child(idUsu);
        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    Usuario usuario = dataSnapshot.getValue(Usuario.class);
                    String to;
                    String token = usuario.getTokenUsuario();

                    to = token;

                    //Monta o objeto notificação
                    Notificacao notificacao = new Notificacao(titulo, corpo);
                    NotificacaoDados notificacaoDados = new NotificacaoDados(to, notificacao);

                    NotificacaoService service = retrofit.create(NotificacaoService.class);
                    Call<NotificacaoDados> call = service.salvarNotificacao( notificacaoDados );

                    call.enqueue(new Callback<NotificacaoDados>() {
                        @Override
                        public void onResponse(Call<NotificacaoDados> call, Response<NotificacaoDados> response) {
                            if(response.isSuccessful()){
                                Toast.makeText(PedidosRecebidosEmpresaActivity.this, "Pedido confirmado"
                                        , Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<NotificacaoDados> call, Throwable t) {

                        }
                    });


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
