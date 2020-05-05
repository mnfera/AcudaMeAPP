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
import com.mgtech.acudame.model.Complemento;

import java.util.List;

public class AdapterComplementoActivity extends RecyclerView.Adapter<AdapterComplementoActivity.MyViewHolder> {

    private List<Complemento> complementos;
    private Context context;

    public AdapterComplementoActivity(List<Complemento> complementos, Context context) {
        this.complementos = complementos;
        this.context = context;
    }

    @NonNull
    @Override
    public AdapterComplementoActivity.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_complemento, parent, false);
        return new AdapterComplementoActivity.MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterComplementoActivity.MyViewHolder holder, int i) {
        Complemento complemento = complementos.get(i);
        holder.nome.setText(complemento.getNome());
        holder.status.setText("Status: " + complemento.getStatus().toUpperCase());
        switch (complemento.getStatus()) {
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
        return complementos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nome;
        TextView status;

        public MyViewHolder(View itemView) {
            super(itemView);

            nome    = itemView.findViewById(R.id.textNomeComplemento);
            status  = itemView.findViewById(R.id.textComplementoStatus);
        }
    }
}
