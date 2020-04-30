package com.mgtech.acudame.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mgtech.acudame.R;
import com.mgtech.acudame.adapter.AdapterPedidoUsuario;
import com.mgtech.acudame.api.NotificacaoService;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;
import com.mgtech.acudame.model.Empresa;
import com.mgtech.acudame.model.Notificacao;
import com.mgtech.acudame.model.NotificacaoDados;
import com.mgtech.acudame.model.Pedido;
import com.google.firebase.database.Query;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CarrinhoActivity extends AppCompatActivity {

    private RecyclerView recyclerCarrinho;
    private AdapterPedidoUsuario adapterPedidoUsuario;
    private List<Pedido> pedidos = new ArrayList<>();
    private AlertDialog dialog;
    private DatabaseReference firebaseRef;
    private String idEmpresaSelecionada, idPedidoSelecionado;
    private Pedido pedidoSelecionado;
    private Button botaoComprar, botaoExcluir;
    private int metodoPagamento;
    private Retrofit retrofit;
    private String baseUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrinho);

        // conf iniciais
        inicializarComponentes();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        baseUrl = "https://fcm.googleapis.com/fcm/";
        retrofit = new Retrofit.Builder()
                .baseUrl( baseUrl )
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // recuperar pedido selecionado
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {

            pedidoSelecionado = (Pedido) bundle.getSerializable("pedido");
            idEmpresaSelecionada = pedidoSelecionado.getIdEmpresa();
            idPedidoSelecionado = pedidoSelecionado.getIdPedido();

        }

        // configurações toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Carrinho");
        setSupportActionBar(toolbar);

        // conf recyclerview
        recyclerCarrinho.setLayoutManager(new LinearLayoutManager(this));
        recyclerCarrinho.setHasFixedSize(true);
        adapterPedidoUsuario = new AdapterPedidoUsuario(pedidos, CarrinhoActivity.this);
        recyclerCarrinho.setAdapter(adapterPedidoUsuario);

        // recuperar os pedidos
        recuperarPedidos();

        botaoComprar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                confirmarPedido();

            }
        });

        botaoExcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(CarrinhoActivity.this);
                builder.setTitle("Tem certeza que deseja excluir esse pedido?");

                builder.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        pedidoSelecionado.remover();
                        pedidoSelecionado = null;

                        Toast.makeText(CarrinhoActivity.this, "Pedido Excluído!"
                                , Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(CarrinhoActivity.this, HistoricoPedidosUsuarioActivity.class));

                    }
                });

                builder.setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });
    }

    private void recuperarPedidos() {
        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Carregando Pedido")
                .setCancelable(false)
                .build();
        dialog.show();

        DatabaseReference pedidosRef = firebaseRef
                .child("pedidos_usuario")
                .child(idEmpresaSelecionada);

        Query pedidoPesquisa = pedidosRef.orderByChild("idPedido")
                .equalTo(idPedidoSelecionado);

        pedidoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                pedidos.clear();
                if(dataSnapshot.getValue() != null) {

                    for(DataSnapshot ds: dataSnapshot.getChildren()) {
                        Pedido pedido = ds.getValue(Pedido.class);
                        pedidos.add(pedido);
                    }

                    adapterPedidoUsuario.notifyDataSetChanged();
                }

                dialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void confirmarPedido() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione um método de pagamento");

        CharSequence[] itens = new CharSequence[]{
                "Dinheiro", "Máquina de cartão"
        };
        builder.setSingleChoiceItems(itens, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                metodoPagamento = which;
            }
        });

        final EditText editObservacao = new EditText(this);
        editObservacao.setHint("Digite uma observação. Ex.: Troco para 50");
        builder.setView(editObservacao);

        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // data/hora atual
                LocalDateTime agora = LocalDateTime.now();

                // formatar a data
                DateTimeFormatter formatterData = DateTimeFormatter.ofPattern("dd/MM/uuuu");
                String dataFormatada = formatterData.format(agora);

                // formatar a hora
                DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm:ss");
                String horaFormatada = formatterHora.format(agora);

                String observacao = editObservacao.getText().toString();
                pedidoSelecionado.setMetodoPagamento(metodoPagamento);
                pedidoSelecionado.setObservacao(observacao);
                pedidoSelecionado.setStatus("confirmado");
                pedidoSelecionado.setData(dataFormatada);
                pedidoSelecionado.setHora(horaFormatada);
                pedidoSelecionado.confirmar();
                pedidoSelecionado.criarHistorico();
                pedidoSelecionado.remover();
                pedidoSelecionado = null;

                startActivity(new Intent(CarrinhoActivity.this, HistoricoPedidosUsuarioActivity.class));

                //enviar notificação do pedido
                enviarNotificacaoUsuario();

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

    private void inicializarComponentes() {
        recyclerCarrinho = findViewById(R.id.recyclerCarrinho);
        botaoComprar = findViewById(R.id.buttonComprar);
        botaoExcluir = findViewById(R.id.buttonExcluir);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_carrinho, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuHome :
                abrirHome();
                break;
            case R.id.menuPedidos :
                abrirPedidos();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void abrirHome() {
        startActivity(new Intent(CarrinhoActivity.this, HomeActivity.class));
        finish();
    }

    private void abrirPedidos() {
        startActivity(new Intent(CarrinhoActivity.this, HistoricoPedidosUsuarioActivity.class));
        finish();
    }

    public void enviarNotificacaoUsuario(){

        //recuperar token empresa
        DatabaseReference empresaRef = firebaseRef
                .child("empresas")
                .child(idEmpresaSelecionada);
        empresaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    Empresa empresa = dataSnapshot.getValue(Empresa.class);
                    String to;
                    String token = empresa.getTokenEmpresa();

                    to = token;

                    //Monta o objeto notificação
                    Notificacao notificacao = new Notificacao("ATENÇÃO","Você tem um novo pedido");
                    NotificacaoDados notificacaoDados = new NotificacaoDados(to, notificacao);

                    NotificacaoService service = retrofit.create(NotificacaoService.class);
                    Call<NotificacaoDados> call = service.salvarNotificacao( notificacaoDados );

                    call.enqueue(new Callback<NotificacaoDados>() {
                        @Override
                        public void onResponse(Call<NotificacaoDados> call, Response<NotificacaoDados> response) {
                            if(response.isSuccessful()){
                                Toast.makeText(CarrinhoActivity.this, "Pedido realizado com sucesso!"
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
