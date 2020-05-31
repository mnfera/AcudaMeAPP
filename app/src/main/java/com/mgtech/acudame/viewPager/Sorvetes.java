package com.mgtech.acudame.viewPager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
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
import com.mgtech.acudame.model.Complemento;
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
    private DatabaseReference produtosRef, pedidoRef, complementosRef;
    private Pedido pedidoRecuperado;
    private List<ItemPedido> itensCarrinho = new ArrayList<>();
    private Usuario usuario;
    private Empresa empresa;
    private String[] listaItens;
    private List<Complemento> complementos = new ArrayList<>();
    private boolean[] checkedItens;
    private ArrayList<Integer> mSeletectItems = new ArrayList<>();
    private String item = "";

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

        // recuperar complementos
        recuperarComplementos();

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
                                escolherComplementos(position);
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

    private void recuperarComplementos() {

        complementosRef = databaseReference
                .child("complementos")
                .child(idEmpresa);

        Query complementosPesquisa = complementosRef.orderByChild("status")
                .equalTo("ativo");

        complementosPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                complementos.clear();
                if (dataSnapshot.getValue() != null) {

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Complemento complemento = ds.getValue(Complemento.class);
                        complementos.add(complemento);
                    }
                }
                // carregando lista de complementos
                if(complementos.isEmpty()){
                    listaItens = getResources().getStringArray(R.array.items);
                }else {
                    listaItens = new String[complementos.size()];
                    for (int i = 0; i < complementos.size(); i++) {
                        listaItens[i] = complementos.get(i).getNome();
                    }
                }
                checkedItens = new boolean[listaItens.length];
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

    private void recuperarProdutos(){

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

    private void escolherComplementos(final int posicao){

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
        builder.setTitle("Selecione os itens");
        builder.setMultiChoiceItems(listaItens, checkedItens, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position, boolean isChecked) {

                if ( isChecked ){
                    if( !mSeletectItems.contains( position )){
                        mSeletectItems.add( position );
                    }
                }else  if( mSeletectItems.contains(position) ){
                    mSeletectItems.remove((Integer) position);
                }
            }
        });

        builder.setCancelable(false);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                for( int i = 0; i < mSeletectItems.size(); i++ ){
                    item = item + listaItens[mSeletectItems.get(i)];
                    if( i != mSeletectItems.size() - 1 ){
                        item = item + ", ";
                    }
                }
                if(item.isEmpty()){
                    item = "zero adicionais";
                }
                if( usuario.getEndereco() == null ){

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Nenhum endereço cadastrado");
                    builder.setMessage("Por favor, cadastre um endereço para fazer um pedido.");

                    builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(getActivity(), ConfiguracoesUsuarioActivity.class));
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                }else{

                    final EditText editQuantidade = new EditText(getActivity());
                    editQuantidade.setRawInputType(InputType.TYPE_CLASS_NUMBER);
                    editQuantidade.setText("1");

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Quantidade")
                            .setMessage("Informe a quantidade do produto")
                            .setCancelable(false)
                            .setView(editQuantidade)
                            .setPositiveButton("Confirmar", null)
                            .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                    final AlertDialog mDialog = builder.create();

                    mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(final DialogInterface dialog) {
                            Button postive = mDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            postive.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //aqui você trata o evento
                                    if (editQuantidade.getText().length() == 0) {
                                        editQuantidade.setError("Campo obrigatório");
                                        editQuantidade.setFocusable(true);
                                        editQuantidade.requestFocus();
                                    } else {
                                        //Como ao clicar no botão positivo a condição é verificada
                                        //caso esteja tudo certo com o edittext, então chama o seu método
                                        //enviar email

                                        if(editQuantidade.getText().toString().equals("0")){
                                            editQuantidade.setError("Digite um valor diferente de 0");
                                            editQuantidade.setFocusable(true);
                                            editQuantidade.requestFocus();
                                        }else{
                                            int valor = 0;

                                            try {
                                                valor = Integer.parseInt(editQuantidade.getText().toString());

                                                String quantidade = editQuantidade.getText().toString();
                                                Produto produtoSelecionado = produtos.get(posicao);
                                                ItemPedido itemPedido = new ItemPedido();
                                                itemPedido.setIdProduto(produtoSelecionado.getIdProduto());
                                                itemPedido.setNomeProduto(produtoSelecionado.getNome());
                                                itemPedido.setPreco(produtoSelecionado.getPreco());
                                                itemPedido.setQuantidade(Integer.parseInt(quantidade));
                                                itemPedido.setComplemento(item);

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
                                                //No final como já completou a condição pode dar dismiss
                                                //no alertdialog, pois está tudo ok
                                                dialog.dismiss();

                                                Toast.makeText(getActivity(), "Pedido adicionado ao carrinho!"
                                                        , Toast.LENGTH_SHORT).show();

                                            } catch (NumberFormatException e) {
                                                editQuantidade.setError("Digite somente números");
                                                editQuantidade.setFocusable(true);
                                                editQuantidade.requestFocus();
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    });
                    mDialog.show();
                    }
                }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNeutralButton("Limpar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for( int i = 0; i < checkedItens.length; i++ ){
                    checkedItens[i] = false;
                    mSeletectItems.clear();
                }
            }
        });

        androidx.appcompat.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
