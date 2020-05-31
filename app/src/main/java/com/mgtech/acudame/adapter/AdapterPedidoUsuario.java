package com.mgtech.acudame.adapter;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mgtech.acudame.R;
import com.mgtech.acudame.model.ItemPedido;
import com.mgtech.acudame.model.Pedido;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AdapterPedidoUsuario extends RecyclerView.Adapter<AdapterPedidoUsuario.MyViewHolderUser> {

    private List<Pedido> pedidos;
    private Context context;

    public AdapterPedidoUsuario(){}

    public AdapterPedidoUsuario(List<Pedido> pedidos, Context context) {
        this.pedidos = pedidos;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolderUser onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_adapter_pedido_usuario, parent, false);
        return new MyViewHolderUser(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolderUser holder, int i) {

        Pedido pedido = pedidos.get(i);

        holder.empresa.setText(pedido.getNomeEmpresa());
        holder.endereco.setText( "Endereço: "+pedido.getEndereco() );
        holder.numero.setText("Número: "+pedido.getNumero());
        holder.referencia.setText("Referência: "+pedido.getReferencia());
        holder.observacao.setText( "Obs.: "+ pedido.getObservacao() );
        holder.telefone.setText(pedido.getTelEmpresa());
        holder.horario.setText("Realizado às: " + pedido.getHora());
        holder.status.setText("Status: "+pedido.getStatus().toUpperCase());
        switch (pedido.getStatus()) {
            case "pendente":
                holder.status.setTextColor(Color.parseColor("#FF9800"));
                break;
            case "confirmado":
                holder.status.setTextColor(Color.parseColor("#FF4CAF50"));
                break;
            case "recebido":
                holder.status.setTextColor(Color.parseColor("#FF00BCD4"));
                break;
            case "finalizado":
                holder.status.setTextColor(Color.parseColor("#FFD700"));
                break;
            case "cancelado":
                holder.status.setTextColor(Color.parseColor("#FFF44336"));
                break;
        }

        List<ItemPedido> itens = new ArrayList<>();
        itens = pedido.getItens();
        String descricaoItens = "";

        int numeroItem = 1;
        Double total = 0.00;
        DecimalFormat df = new DecimalFormat(",##0.00");

        for( ItemPedido itemPedido : itens ){

            int qtde = itemPedido.getQuantidade();
            Double preco = itemPedido.getPreco();
            total += (qtde * preco);


            String nome = itemPedido.getNomeProduto();
            String complementos = itemPedido.getComplemento();
            String sabores = itemPedido.getSabor();
            if(sabores == null) {
                if (complementos == null) {
                    descricaoItens += numeroItem + ") " + nome + " / (" + qtde + " x R$ " + df.format(preco) + ") \n";
                } else {
                    descricaoItens += numeroItem + ") " + nome + " / (" + qtde + " x R$ " + df.format(preco) + ") \nCom: " + complementos + "\n";
                }
            } else {
                descricaoItens += numeroItem + ") " + nome + " / (" + qtde + " x R$ " + df.format(preco) + ") \nDe: " + sabores + "\n";
            }
            numeroItem++;
        }

        descricaoItens += "Total: R$ " + df.format(total);
        holder.itens.setText(descricaoItens);

        int metodoPagamento = pedido.getMetodoPagamento();
        String pagamento = metodoPagamento == 0 ? "Dinheiro" : "Máquina cartão" ;
        holder.pgto.setText( "Pgto.: " + pagamento );

        String texto = "Meu pedido:\n"+descricaoItens+"\n"
                        +"Endereço: "+pedido.getEndereco()+"\n"
                        +"Número: "+pedido.getNumero()+"\n"
                        +"Referência: "+pedido.getReferencia()+"\n"
                        +"Pgto.: "+pagamento+"\n"
                        +"Obs.: "+pedido.getObservacao();

        holder.imageCelular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("tel:" + pedido.getTelEmpresa());
                Intent intent = new Intent(Intent.ACTION_CALL, uri);
                if (ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) context.getApplicationContext(), new String[]{Manifest.permission.CALL_PHONE}, 1);
                    return;
                }
                context.startActivity(intent);
            }
        });

        holder.imageWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String link = "https://api.whatsapp.com/send?phone=+55"+pedido.getTelEmpresa()+
                        "&text=Olá%20"+pedido.getNomeEmpresa()+"\n"+texto;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    public class MyViewHolderUser extends RecyclerView.ViewHolder {

        TextView empresa;
        TextView endereco;
        TextView numero;
        TextView referencia;
        TextView pgto;
        TextView observacao;
        TextView telefone;
        TextView status;
        TextView horario;
        TextView itens;
        ImageView imageCelular;
        ImageView imageWhatsapp;


        public MyViewHolderUser(View itemView) {
            super(itemView);

            empresa     = itemView.findViewById(R.id.textPedidoEmpresa);
            endereco    = itemView.findViewById(R.id.textPedidoEndereco);
            numero      = itemView.findViewById(R.id.textPedidoNumero);
            referencia  = itemView.findViewById(R.id.textPedidoReferencia);
            pgto        = itemView.findViewById(R.id.textPedidoPgto);
            observacao  = itemView.findViewById(R.id.textPedidoObs);
            telefone    = itemView.findViewById(R.id.textPedidoTelefone);
            status      = itemView.findViewById(R.id.textPedidoStatus);
            horario      = itemView.findViewById(R.id.textPedidoHorario);
            itens       = itemView.findViewById(R.id.textPedidoItens);
            imageCelular = itemView.findViewById(R.id.imageCelular);
            imageWhatsapp = itemView.findViewById(R.id.imageWhatsapp);
        }
    }
}
