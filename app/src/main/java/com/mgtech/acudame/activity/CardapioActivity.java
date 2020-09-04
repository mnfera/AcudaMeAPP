package com.mgtech.acudame.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mgtech.acudame.R;
import com.mgtech.acudame.adapter.ViewPagerAdapter;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;
import com.mgtech.acudame.helper.UsuarioFirebase;
import com.mgtech.acudame.model.Empresa;
import com.mgtech.acudame.model.ItemPedido;
import com.mgtech.acudame.model.Pedido;
import com.mgtech.acudame.model.Produto;
import com.mgtech.acudame.model.Usuario;
import com.mgtech.acudame.viewPager.Bebidas;
import com.mgtech.acudame.viewPager.Comidas;
import com.mgtech.acudame.viewPager.Pizzas;
import com.mgtech.acudame.viewPager.Sorvetes;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class CardapioActivity extends AppCompatActivity {

    private ImageView imageEmpresaCardapio, imageCelular, imageWhatsapp;
    private TextView textNomeEmpresaCardapio, textTelefoneEmpresaCardapio, textStatusEmpresaCardapio;
    private Empresa empresaSelecionada;
    private AlertDialog dialog, dialog2, dialog3;
    private TextView textCarrinhoQtde, textCarrinhoTotal, textVerCarrinho;
    private DatabaseReference firebaseRef;
    private String idEmpresaSelecionada;
    private String idUsuarioLogado;
    private Usuario usuario;
    private Pedido pedidoRecuperado;
    private List<ItemPedido> itensCarrinho = new ArrayList<>();
    private int qtdItensCarrinho;
    private Double totalCarrinho;
    private int index = 0;
    private int comidaIndex = -1, pizzaIndex = -1, bebidaIndex = -1, sorveteIndex = -1;
    private boolean comida = false, pizza = false, bebida = false, sorvete = false;
    private AdView anuncio;
    private Button button_Confirmar_Pedido;
    private ViewPagerAdapter viewPagerAdapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Button button_Ver_Carrinho;
    private List<Produto> produtos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardapio);

        // configurações toolbar
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Cardápio");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //conf iniciais
        inicializarComponentes();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();

        //Anuncio
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        anuncio.loadAd(adRequest);

        //recuperar empresa selecionada
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            empresaSelecionada = (Empresa) bundle.getSerializable("empresa");

            textNomeEmpresaCardapio.setText(empresaSelecionada.getNome());
            textTelefoneEmpresaCardapio.setText(empresaSelecionada.getTelefone());
            idEmpresaSelecionada = empresaSelecionada.getIdUsuario();
            if(empresaSelecionada.getStatus() != null) {
                if (empresaSelecionada.getStatus()) {
                    textStatusEmpresaCardapio.setTextColor(Color.parseColor("#f1f1f1"));
                } else {
                    textStatusEmpresaCardapio.setText("FECHADO");
                    textStatusEmpresaCardapio.setTextColor(Color.parseColor("#f1f1f1"));
                }
            }

            String url = empresaSelecionada.getUrlImagem();
            Picasso.get().load(url).into(imageEmpresaCardapio);
        }

        // carregando os dados da viewPager
        recuperarProdutos();

        // recupera os dados do usuário e seus pedidos
        recuperarDadosUsuario();

        button_Ver_Carrinho.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verCarrinho();
            }
        });

        imageCelular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("tel:" + empresaSelecionada.getTelefone());
                Intent intent = new Intent(Intent.ACTION_CALL, uri);
                if (ActivityCompat.checkSelfPermission(CardapioActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CardapioActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                    return;
                }
                startActivity(intent);
            }
        });

        button_Confirmar_Pedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verCarrinho();
            }
        });

        imageWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String link = "https://api.whatsapp.com/send?phone=+55"+empresaSelecionada.getTelefone()+"&text=Olá%20"+empresaSelecionada.getNome();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                startActivity(intent);
            }
        });
    }

    private void inicializarComponentes() {
        imageEmpresaCardapio = findViewById(R.id.imageEmpresaCardapio);
        textNomeEmpresaCardapio = findViewById(R.id.textNomeEmpresaCardapio);
        textTelefoneEmpresaCardapio = findViewById(R.id.textTelefoneEmpresaCardapio);
        textCarrinhoQtde = findViewById(R.id.textCarrinhoQtd);
        textCarrinhoTotal = findViewById(R.id.textCarrinhoTotal);
        button_Ver_Carrinho = findViewById(R.id.button_Ver_Carrinho);
        imageCelular = findViewById(R.id.imageCelular);
        imageWhatsapp = findViewById(R.id.imageWhatsapp);
        anuncio = findViewById(R.id.cardapioAnuncio);
        textStatusEmpresaCardapio = findViewById(R.id.textStatusEmpresaCardapio);
        button_Confirmar_Pedido = findViewById(R.id.button_Confirmar_Pedido);
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
                totalCarrinho = 0.00;
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

                textCarrinhoQtde.setText("Itens: " + qtdItensCarrinho);
                DecimalFormat df = new DecimalFormat(",##0.00");
                textCarrinhoTotal.setText("R$ " + df.format(totalCarrinho));

                dialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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

    private void recuperarProdutos(){

        dialog3 = new SpotsDialog.Builder()
                .setContext(CardapioActivity.this)
                .setMessage("Carregando Dados")
                .setCancelable(false)
                .build();
        dialog3.show();

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
                if(!produtos.isEmpty()) {
                    for (int i = 0; i < produtos.size(); i++) {
                        switch (produtos.get(i).getCategoria()) {
                            case "comida":
                                comida = true;
                                break;
                            case "bebida":
                                bebida = true;
                                break;
                            case "pizza":
                                pizza = true;
                                break;
                            case "sorvete":
                                sorvete = true;
                                break;
                        }
                    }
                }
                // criar e mostrar viewPager
                criarMostrarViewPager();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void criarMostrarViewPager() {

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPage);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        if(comida) {
            viewPagerAdapter.addFragment(new Comidas(idEmpresaSelecionada, idUsuarioLogado), "" );
            comidaIndex = index;
            index++;
        }
        if(pizza) {
            viewPagerAdapter.addFragment(new Pizzas(idEmpresaSelecionada, idUsuarioLogado), "" );
            pizzaIndex = index;
            index++;
        }
        if(bebida){
            viewPagerAdapter.addFragment(new Bebidas(idEmpresaSelecionada, idUsuarioLogado), "" );
            bebidaIndex = index;
            index++;
        }
        if(sorvete){
            viewPagerAdapter.addFragment(new Sorvetes(idEmpresaSelecionada, idUsuarioLogado), "" );
            sorveteIndex = index;
            index++;
        }

        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getResources().getDisplayMetrics();

        ViewGroup.LayoutParams params = tabLayout.getLayoutParams();
        //Change the height in 'Pixels'
        params.height = 155;
        tabLayout.setLayoutParams(params);

        if(comidaIndex >= 0) {
            tabLayout.getTabAt(comidaIndex).setIcon(R.drawable.comida);
            tabLayout.getTabAt(comidaIndex).setText("Comidas");
        }

        if(pizzaIndex >= 0) {
            tabLayout.getTabAt(pizzaIndex).setIcon(R.drawable.ic_pizzas);
            tabLayout.getTabAt(pizzaIndex).setText("Pizzas");
        }

        if(bebidaIndex >= 0) {
            tabLayout.getTabAt(bebidaIndex).setIcon(R.drawable.ic_conjunto_de_bebidas);
            tabLayout.getTabAt(bebidaIndex).setText("Bebidas");
        }

        if(sorveteIndex >= 0) {
            tabLayout.getTabAt(sorveteIndex).setIcon(R.drawable.ic_sorvete);
            tabLayout.getTabAt(sorveteIndex).setText("Sorvetes");
        }

        dialog3.dismiss();
    }
}