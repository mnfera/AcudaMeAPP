package com.mgtech.acudame.viewPager;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mgtech.acudame.R;
import com.mgtech.acudame.adapter.AdapterProduto;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;
import com.mgtech.acudame.model.Produto;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class Sorvetes extends Fragment {

    private View view;
    private RecyclerView recyclerProdutosCardapio;
    private AdapterProduto adapterProduto;
    private List<Produto> produtos = new ArrayList<>();
    private Context context;
    private DatabaseReference databaseReference;
    private String idEmpresa;
    private DatabaseReference produtosRef;
    private ChildEventListener childEventListenerProdutos;

    public Sorvetes(String idEmpresa) {
        this.idEmpresa = idEmpresa;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_comida, container, false);
        recyclerProdutosCardapio = view.findViewById(R.id.recyclerProdutosCardapio);
        adapterProduto = new AdapterProduto(produtos, getActivity());
        //Código

        // conf recyclerview
        recyclerProdutosCardapio.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerProdutosCardapio.setHasFixedSize(true);
        adapterProduto.notifyDataSetChanged();
        recyclerProdutosCardapio.setAdapter(adapterProduto);

        databaseReference = ConfiguracaoFirebase.getFirebase();
        produtosRef = databaseReference
                .child("produtos")
                .child(idEmpresa);

        return  view;
    }

    @Override
    public void onStart() {
        Toast.makeText(getContext(), "to aqui", Toast.LENGTH_LONG).show();
        super.onStart();
        recuperarProdutos();
    }

    public void recuperarProdutos(){

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
}
