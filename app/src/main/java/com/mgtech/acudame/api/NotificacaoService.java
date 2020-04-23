package com.mgtech.acudame.api;


import com.mgtech.acudame.model.NotificacaoDados;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificacaoService {

    @Headers({
            "Authorization: key=AAAAgUphJ6g:APA91bGv4uwbMkMZ9AyH8-3bx9x1eM0UFDaruMTwruSIc-7k_hi3wwQ8xE1cEKNHW4HKtCMetwLiJCqbA7AgI-yo5Qz4AyG9kVpsbzo8jvueLfTYgbSDsQuyRamu2uH114d9wiWyUYel",
            "Content-Type:application/json"
    })
     @POST("send")
    Call<NotificacaoDados> salvarNotificacao(@Body NotificacaoDados notificacaoDados);
}
