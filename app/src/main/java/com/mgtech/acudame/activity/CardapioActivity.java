package com.mgtech.acudame.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mgtech.acudame.R;
import com.mgtech.acudame.adapter.AdapterProduto;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;
import com.mgtech.acudame.helper.UsuarioFirebase;
import com.mgtech.acudame.listener.RecyclerItemClickListener;
import com.mgtech.acudame.model.Empresa;
import com.mgtech.acudame.model.ItemPedido;
import com.mgtech.acudame.model.Pedido;
import com.mgtech.acudame.model.Produto;
import com.mgtech.acudame.model.Usuario;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class CardapioActivity extends AppCompatActivity {

    private RecyclerView recyclerProdutosCardapio;
    private ImageView imageEmpresaCardapio;
    private TextView textNomeEmpresaCardapio;
    private Empresa empresaSelecionada;
    private AlertDialog dialog;
    private TextView textCarrinhoQtde, textCarrinhoTotal, textVerCarrinho;

    private AdapterProduto adapterProduto;
    private List<Produto> produtos = new ArrayList<>();
    private List<ItemPedido> itensCarrinho = new ArrayList<>();
    private DatabaseReference firebaseRef;
    private String idEmpresaSelecionada;
    private String idUsuarioLogado;
    private Usuario usuario;
    private Pedido pedidoRecuperado, pedidoEnviarCarrinho;
    private int qtdItensCarrinho;
    private Double totalCarrinho;
    private int metodoPagamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardapio);

        // conf iniciais
        inicializarComponentes();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();

        // recuperar empresa selecionada
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            empresaSelecionada = (Empresa) bundle.getSerializable("empresa");

            textNomeEmpresaCardapio.setText(empresaSelecionada.getNome());
            idEmpresaSelecionada = empresaSelecionada.getIdUsuario();

            String url = empresaSelecionada.getUrlImagem();
            Picasso.get().load(url).into(imageEmpresaCardapio);
        }

        // configurações toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Cardápio");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // conf recyclerview
        recyclerProdutosCardapio.setLayoutManager(new LinearLayoutManager(this));
        recyclerProdutosCardapio.setHasFixedSize(true);
        adapterProduto = new AdapterProduto(produtos, this);
        recyclerProdutosCardapio.setAdapter(adapterProduto);

        // conf evento de clique
        recyclerProdutosCardapio.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerProdutosCardapio,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                confirmarQuantidade(position);
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

        // recupera os produtos da empresa
        recuperarProdutos();
        recuperarDadosUsuario();

        textVerCarrinho.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verCarrinho();
            }
        });
    }

    private void verCarrinho() {

        if(pedidoRecuperado != null) {

            Intent i = new Intent(CardapioActivity.this, CarrinhoActivity.class);
            i.putExtra("pedido", pedidoRecuperado);
            startActivity(i);

        }else {
            Toast.makeText(CardapioActivity.this, "Seu carrinho está vazio!"
                    , Toast.LENGTH_SHORT).show();
        }

    }

    private void confirmarQuantidade(final int posicao) {

        if( usuario == null ){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Nenhum endereço cadastrado");
            builder.setMessage("Por favor, cadastre um endereço para fazer um pedido.");

            builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(CardapioActivity.this, ConfiguracoesUsuarioActivity.class));
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        }else{

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Quantidade");
            builder.setMessage("Digite a quantidade");

            final EditText editQuantidade = new EditText(this);
            editQuantidade.setRawInputType(InputType.TYPE_CLASS_NUMBER);
            editQuantidade.setText("1");

            builder.setView(editQuantidade);

            builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(DialogInterface dialog, int which) {

                     String quantidade = editQuantidade.getText().toString();

                     if(!quantidade.isEmpty()){

                         Produto produtoSelecionado = produtos.get(posicao);
                         ItemPedido itemPedido = new ItemPedido();
                         itemPedido.setIdProduto(produtoSelecionado.getIdProduto());
                         itemPedido.setNomeProduto(produtoSelecionado.getNome());
                         itemPedido.setPreco(produtoSelecionado.getPreco());
                         itemPedido.setQuantidade(Integer.parseInt(quantidade));

                         itensCarrinho.add(itemPedido);

                         if(pedidoRecuperado == null) {
                             pedidoRecuperado = new Pedido(idUsuarioLogado, idEmpresaSelecionada);
                         }

                         pedidoRecuperado.setNome(usuario.getNome());
                         pedidoRecuperado.setEndereco(usuario.getEndereco());
                         pedidoRecuperado.setItens(itensCarrinho);
                         pedidoRecuperado.salvar();

                         Toast.makeText(CardapioActivity.this, "Pedido adicionado ao carrinho!"
                                 , Toast.LENGTH_SHORT).show();

                     }else{
                         Toast.makeText(CardapioActivity.this, "Quantidade está em branco!"
                         , Toast.LENGTH_SHORT).show();
                         confirmarQuantidade(posicao);
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

        }

    private void recuperarDadosUsuario() {

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Carregando Dados")
                .setCancelable(false)
                .build();
        dialog.show();

        final DatabaseReference usuariosRef = firebaseRef
                .child("usuarios")
                .child(idUsuarioLogado);
        usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    usuario = dataSnapshot.getValue(Usuario.class);
                }
                recuperarPedido();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void recuperarPedido() {
        DatabaseReference pedidoRef = firebaseRef
                .child("pedidos_usuario")
                .child(idEmpresaSelecionada)
                .child(idUsuarioLogado);


        pedidoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                qtdItensCarrinho = 0;
                totalCarrinho = 0.0;
                itensCarrinho = new ArrayList<>();

                if(dataSnapshot.getValue() != null) {
                    pedidoRecuperado = dataSnapshot.getValue(Pedido.class);
                    itensCarrinho = pedidoRecuperado.getItens();

                    for(ItemPedido itemPedido: itensCarrinho) {

                        int qtde = itemPedido.getQuantidade();
                        Double preco = itemPedido.getPreco();

                        totalCarrinho += (qtde * preco);
                        qtdItensCarrinho += qtde;
                    }
                }

                DecimalFormat df = new DecimalFormat("0.00");

                textCarrinhoQtde.setText("Qtd: " + String.valueOf(qtdItensCarrinho));
                textCarrinhoTotal.setText("R$ " + df.format(totalCarrinho));

                dialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void recuperarProdutos(){
        DatabaseReference produtosRef = firebaseRef
                .child("produtos")
                .child(idEmpresaSelecionada);
        produtosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                produtos.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    produtos.add(ds.getValue(Produto.class));
                }
                adapterProduto.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_cardapio, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuPedido :
                verCarrinho();
                break;
        }

        return super.onOptionsItemSelected(item);
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
                pedidoRecuperado.setMetodoPagamento(metodoPagamento);
                pedidoRecuperado.setObservacao(observacao);
                pedidoRecuperado.setStatus("confirmado");
                pedidoRecuperado.setData(dataFormatada);
                pedidoRecuperado.setHora(horaFormatada);
                pedidoRecuperado.confirmar();
                pedidoRecuperado.criarHistorico();
                pedidoRecuperado.remover();
                pedidoRecuperado = null;

                startActivity(new Intent(CardapioActivity.this, HistoricoPedidosUsuarioActivity.class));

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
        recyclerProdutosCardapio = findViewById(R.id.recyclerProdutosCardapio);
        imageEmpresaCardapio = findViewById(R.id.imageEmpresaCardapio);
        textNomeEmpresaCardapio = findViewById(R.id.textNomeEmpresacardapio);
        textCarrinhoQtde = findViewById(R.id.textCarrinhoQtd);
        textCarrinhoTotal = findViewById(R.id.textCarrinhoTotal);
        textVerCarrinho = findViewById(R.id.textVerCarrinho);

    }

}
