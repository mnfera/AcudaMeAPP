package com.mgtech.acudame.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mgtech.acudame.R;
import com.mgtech.acudame.model.Produto;

import java.text.DecimalFormat;
import java.util.List;

public class AdapterProdutoEmpresa extends RecyclerView.Adapter<AdapterProdutoEmpresa.MyViewHolder>{

    private List<Produto> produtos;
    private Context context;

    public AdapterProdutoEmpresa(List<Produto> produtos, Context context) {
        this.produtos = produtos;
        this.context = context;
    }

    @NonNull
    @Override
    public AdapterProdutoEmpresa.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_adapter_produto_empresa, parent, false);
        return new AdapterProdutoEmpresa.MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterProdutoEmpresa.MyViewHolder holder, int i) {
        Produto produto = produtos.get(i);
        holder.nome.setText(produto.getNome());
        holder.descricao.setText(produto.getDescricao());
        DecimalFormat df = new DecimalFormat(",##0.00");
        holder.valor.setText("R$ " + df.format(produto.getPreco()));


        if(produto.getCategoria() != null) {
            holder.categoria.setText("Categoria: " + produto.getCategoria().toUpperCase());
        }
        holder.status.setText("Status: " + produto.getStatus().toUpperCase());
        switch (produto.getStatus()) {
            case "ativo":
                holder.status.setTextColor(Color.parseColor("#FF4CAF50"));
                break;
            case "inativo":
                holder.status.setTextColor(Color.parseColor("#FFF44336"));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return produtos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nome;
        TextView descricao;
        TextView valor;
        TextView categoria;
        TextView status;

        public MyViewHolder(View itemView) {
            super(itemView);

            nome = itemView.findViewById(R.id.textNomeRefeicao);
            descricao = itemView.findViewById(R.id.textDescricaoRefeicao);
            valor = itemView.findViewById(R.id.textPreco);
            categoria = itemView.findViewById(R.id.textCategoria);
            status = itemView.findViewById(R.id.textProdutoStatus);
        }
    }
}
