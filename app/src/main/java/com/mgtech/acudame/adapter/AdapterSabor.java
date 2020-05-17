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
import com.mgtech.acudame.model.Sabor;

import java.util.List;

public class AdapterSabor extends RecyclerView.Adapter<AdapterSabor.MyViewHolder> {

    private List<Sabor> sabores;
    private Context context;

    public AdapterSabor(){}

    public AdapterSabor(List<Sabor> sabores, Context context) {
        this.sabores = sabores;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_sabor, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterSabor.MyViewHolder holder, int i) {
        Sabor sabor = sabores.get(i);
        holder.nome.setText(sabor.getNome());
        holder.status.setText("Status: " + sabor.getStatus().toUpperCase());
        switch (sabor.getStatus()) {
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
        return sabores.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nome;
        TextView status;

        public MyViewHolder(View itemView) {
            super(itemView);

            nome    = itemView.findViewById(R.id.textNomeSabor);
            status  = itemView.findViewById(R.id.textSaborStatus);
        }
    }
}
