package com.mgtech.acudame.service;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mgtech.acudame.R;
import com.mgtech.acudame.activity.HistoricoPedidosUsuarioActivity;
import com.mgtech.acudame.activity.PedidosActivity;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private FirebaseAuth autenticacao;
    private String data = "";

    @Override
    public void onMessageReceived(RemoteMessage notificacao) {
        super.onMessageReceived(notificacao);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        FirebaseUser user = autenticacao.getCurrentUser();


        //Verificação diferente de null
        if( notificacao != null && user != null && notificacao.getData().isEmpty())
        {

            //Recuperando informações da notificação firebase
            data = notificacao.getData().toString();
            String titulo = notificacao.getNotification().getTitle();
            String corpo = notificacao.getNotification().getBody();


            enviarNotificacao(titulo, corpo);

        }

    }

    public void enviarNotificacao(String titulo, String corpo){

        //Configurações para notificação
        String canal = getString(R.string.default_notification_channel_id);
        Uri som = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent intent = null;

        if(corpo.equals("Seu pedido foi recebido pela empresa e está sendo preparado")){

            intent = new Intent(this, HistoricoPedidosUsuarioActivity.class);

        }

        if(corpo.equals("Seu pedido foi/será entregue e finalizado")){

            intent = new Intent(this, HistoricoPedidosUsuarioActivity.class);

        }

        if(corpo.equals("Seu pedido foi cancelado")){

            intent = new Intent(this, HistoricoPedidosUsuarioActivity.class);

        }

        if(corpo.equals("Você tem um novo pedido")) {

            intent = new Intent(this, PedidosActivity.class);

        }

        PendingIntent pendingIntent  = PendingIntent.getActivities(this , 100, new Intent[]{intent}, PendingIntent.FLAG_ONE_SHOT);


        //Criar notificação
        @SuppressLint("ResourceAsColor") NotificationCompat.Builder notficacao = new NotificationCompat.Builder(this, canal)
                .setContentTitle( titulo )
                .setContentText( corpo )
                .setSmallIcon( R.drawable.ic_delivery)
                //.setColor(R.color.vermelhoEscuro)
                .setSound( som )
                .setAutoCancel( true )
                .setContentIntent( pendingIntent );


        //Recuperar notificationManager
        NotificationManager notificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        //Verificar versão do android a partir do Oreo para configurar o canal de notificação
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){
            NotificationChannel channel = new NotificationChannel(canal, "canal", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel( channel );
        }

        //Enviar notificação
        notificationManager.notify(0, notficacao.build());

    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }
}