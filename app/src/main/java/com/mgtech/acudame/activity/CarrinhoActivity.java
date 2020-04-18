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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mgtech.acudame.R;
import com.mgtech.acudame.adapter.AdapterPedido;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;
import com.mgtech.acudame.helper.UsuarioFirebase;
import com.mgtech.acudame.messenger.MessengerDialog;
import com.mgtech.acudame.model.Pedido;
import com.google.firebase.database.Query;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class CarrinhoActivity extends AppCompatActivity {

    private RecyclerView recyclerCarrinho;
    private AdapterPedido adapterPedido;
    private List<Pedido> pedidos = new ArrayList<>();
    private AlertDialog dialog;
    private DatabaseReference firebaseRef;
    private String idUsuario, idEmpresaSelecionada, idPedidoSelecionado;
    private Pedido pedidoSelecionado;
    private Button botaoComprar, botaoExcluir;
    private int metodoPagamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrinho);

        // conf iniciais
        inicializarComponentes();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idUsuario = UsuarioFirebase.getIdUsuario();

        // recuperar pedido selecionado
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {

            pedidoSelecionado = (Pedido) bundle.getSerializable("pedido");
            idEmpresaSelecionada = pedidoSelecionado.getIdEmpresa();
            idPedidoSelecionado = pedidoSelecionado.getIdPedido();

        }else {
            alertaSimples("Não há nenhum pedido", getApplicationContext(), "Pedidos ");
        }

        // configurações toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Carrinho");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // conf recyclerview
        recyclerCarrinho.setLayoutManager(new LinearLayoutManager(this));
        recyclerCarrinho.setHasFixedSize(true);
        adapterPedido = new AdapterPedido(pedidos);
        recyclerCarrinho.setAdapter(adapterPedido);

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

                        finish();

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
        editObservacao.setHint("Digite uma observação como ou troco necessário, por exemplo");
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

                Toast.makeText(CarrinhoActivity.this, "Pedido realizado com sucesso!"
                        , Toast.LENGTH_SHORT).show();

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

    public void alertaSimples(String conteudo, Context context, String titulo){
        MessengerDialog dialog = new MessengerDialog();
        dialog.setConteudo(conteudo);
        dialog.setTitulo(titulo);
        dialog.show(getSupportFragmentManager(), "exemplo dialog");
    }
}
