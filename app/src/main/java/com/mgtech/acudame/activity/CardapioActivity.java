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
    private AlertDialog dialog, dialog2;
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

    private ViewPagerAdapter viewPagerAdapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardapio);

        // configurações toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
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
                    textStatusEmpresaCardapio.setTextColor(Color.parseColor("#FF4CAF50"));
                } else {
                    textStatusEmpresaCardapio.setText("FECHADO");
                    textStatusEmpresaCardapio.setTextColor(Color.parseColor("#FFF44336"));
                }
            }

            String url = empresaSelecionada.getUrlImagem();
            Picasso.get().load(url).into(imageEmpresaCardapio);
        }

        // recupera os dados do usuário e seus pedidos
        recuperarDadosUsuario();

        // carregando os dados da viewPager
        carregarViewPagers();

        textVerCarrinho.setOnClickListener(new View.OnClickListener() {
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
        textVerCarrinho = findViewById(R.id.textVerCarrinho);
        imageCelular = findViewById(R.id.imageCelular);
        imageWhatsapp = findViewById(R.id.imageWhatsapp);
        anuncio = findViewById(R.id.cardapioAnuncio);
        textStatusEmpresaCardapio = findViewById(R.id.textStatusEmpresaCardapio);

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

                textCarrinhoQtde.setText("Qtd: " + qtdItensCarrinho);
                DecimalFormat df = new DecimalFormat(",##0.00");
                textCarrinhoTotal.setText("R$ " + df.format(totalCarrinho));

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

    private void carregarViewPagers() {

        dialog2 = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Carregando Dados")
                .setCancelable(false)
                .build();
        dialog2.show();

        DatabaseReference produtosRef = firebaseRef
                .child("produtos")
                .child(idEmpresaSelecionada);

        Query produtoComida = produtosRef.orderByChild("categoria")
                .equalTo("comida");

        Query produtoPizza = produtosRef.orderByChild("categoria")
                .equalTo("pizza");

        Query produtoBebida = produtosRef.orderByChild("categoria")
                .equalTo("bebida");

        Query produtoSorvete = produtosRef.orderByChild("categoria")
                .equalTo("sorvete");

        produtoComida.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null){
                    comida = true;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        produtoPizza.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null){
                    pizza = true;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        produtoBebida.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null){
                    bebida = true;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        produtoSorvete.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null){
                    sorvete = true;
                }

                // criar e mostrar viewPager
                criarMostrarViewPager();
                dialog2.dismiss();
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


        if(comida == true) {
            viewPagerAdapter.addFragment(new Comidas(idEmpresaSelecionada, idUsuarioLogado), "" );
            comidaIndex = index;
            index++;
        }
        if(pizza == true) {
            viewPagerAdapter.addFragment(new Pizzas(idEmpresaSelecionada, idUsuarioLogado), "" );
            pizzaIndex = index;
            index++;
        }
        if(bebida == true){
            viewPagerAdapter.addFragment(new Bebidas(idEmpresaSelecionada, idUsuarioLogado), "" );
            bebidaIndex = index;
            index++;
        }
        if(sorvete == true){
            viewPagerAdapter.addFragment(new Sorvetes(idEmpresaSelecionada, idUsuarioLogado), "" );
            sorveteIndex = index;
            index++;
        }

        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);

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
    }

}