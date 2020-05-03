package com.mgtech.acudame.viewPager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mgtech.acudame.R;
import com.mgtech.acudame.activity.ConfiguracoesUsuarioActivity;
import com.mgtech.acudame.adapter.AdapterProduto;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;
import com.mgtech.acudame.listener.RecyclerItemClickListener;
import com.mgtech.acudame.model.Empresa;
import com.mgtech.acudame.model.ItemPedido;
import com.mgtech.acudame.model.Pedido;
import com.mgtech.acudame.model.Produto;
import com.mgtech.acudame.model.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class Sorvetes extends Fragment {

    private String idUsuario;
    private View view;
    private RecyclerView recyclerProdutosCardapio;
    private AdapterProduto adapterProduto;
    private List<Produto> produtos = new ArrayList<>();
    private DatabaseReference databaseReference = ConfiguracaoFirebase.getFirebase();
    private String idEmpresa;
    private DatabaseReference produtosRef, pedidoRef;
    private Pedido pedidoRecuperado;
    private List<ItemPedido> itensCarrinho = new ArrayList<>();
    private Usuario usuario;
    private Empresa empresa;

    public Sorvetes(String idEmpresa, String idUsuario) {
        this.idEmpresa = idEmpresa;
        this.idUsuario = idUsuario;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_sorvetes, container, false);
        recyclerProdutosCardapio = view.findViewById(R.id.recyclerProdutosCardapio);
        adapterProduto = new AdapterProduto(produtos, getActivity());

        // conf recyclerview
        recyclerProdutosCardapio.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerProdutosCardapio.setHasFixedSize(true);
        adapterProduto.notifyDataSetChanged();
        recyclerProdutosCardapio.setAdapter(adapterProduto);

        // recuperar dados da empresa selecionada
        recuperarEmpesa();

        // recuperar dados do usuario
        recuperarDadosUsuario();

        // recuperar produtos
        recuperarProdutos();

        // recuperar pedido
        recuperarPedido();


        // conf evento de clique
        recyclerProdutosCardapio.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
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

        return  view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void recuperarEmpesa() {

        final DatabaseReference empresaRef = databaseReference
                .child("empresas")
                .child(idEmpresa);

        empresaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    empresa = dataSnapshot.getValue(Empresa.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void recuperarDadosUsuario() {

        final DatabaseReference usuariosRef = databaseReference
                .child("usuarios")
                .child(idUsuario);
        usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    usuario = dataSnapshot.getValue(Usuario.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void recuperarPedido() {

        pedidoRef = databaseReference
                .child("pedidos_usuario")
                .child(idEmpresa)
                .child(idUsuario);

        pedidoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                itensCarrinho = new ArrayList<>();

                if(dataSnapshot.getValue() != null) {
                    pedidoRecuperado = dataSnapshot.getValue(Pedido.class);
                    itensCarrinho = pedidoRecuperado.getItens();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void confirmarQuantidade(final int posicao) {

        if( usuario == null ){

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Nenhum endereço cadastrado");
            builder.setMessage("Por favor, cadastre um endereço para fazer um pedido.");

            builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(getActivity(), ConfiguracoesUsuarioActivity.class));
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        }else{

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Quantidade");
            builder.setMessage("Digite a quantidade");

            final EditText editQuantidade = new EditText(getActivity());
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
                            pedidoRecuperado = new Pedido(idUsuario, idEmpresa);
                        }

                        pedidoRecuperado.setNome(usuario.getNome());
                        pedidoRecuperado.setEndereco(usuario.getEndereco());
                        pedidoRecuperado.setNomeEmpresa(empresa.getNome());
                        pedidoRecuperado.setNumero(usuario.getNumero());
                        pedidoRecuperado.setReferencia(usuario.getReferencia());
                        pedidoRecuperado.setTelEmpresa(empresa.getTelefone());
                        pedidoRecuperado.setTelUsuario(usuario.getTelefone());
                        pedidoRecuperado.setItens(itensCarrinho);
                        pedidoRecuperado.salvar();

                        Toast.makeText(getActivity(), "Pedido adicionado ao carrinho!"
                                , Toast.LENGTH_SHORT).show();

                    }else{
                        Toast.makeText(getActivity(),"Quantidade está em branco!"
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

    public void recuperarProdutos(){

        produtosRef = databaseReference
                .child("produtos")
                .child(idEmpresa);

        Query produtoPesquisa = produtosRef.orderByChild("statusCategoria")
                .equalTo("ativo_sorvete");

        produtoPesquisa.addValueEventListener(new ValueEventListener() {
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
}
