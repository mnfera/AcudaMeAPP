package com.mgtech.acudame.model;

import com.google.firebase.database.DatabaseReference;
import com.mgtech.acudame.helper.ConfiguracaoFirebase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class Pedido implements Serializable {

    private String idUsuario;
    private String idEmpresa;
    private String idPedido;
    private String nome;
    private String nomeEmpresa;
    private String endereco;
    private String numero;
    private String referencia;
    private String telEmpresa;
    private String telUsuario;
    private List<ItemPedido> itens;
    private Double total;
    private String status = "pendente";
    private int metodoPagamento;
    private String observacao;
    private String data;
    private String hora;

    public Pedido() {
    }

    public Pedido(String idUsu, String idEmp) {
        setIdUsuario(idUsu);
        setIdEmpresa(idEmp);

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference pedidoRef = firebaseRef
                .child("pedidos_usuario")
                .child(idEmp)
                .child(idUsu);
        setIdPedido(pedidoRef.push().getKey());
    }

    public void salvar() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference pedidoRef = firebaseRef
                .child("pedidos_usuario")
                .child(getIdEmpresa())
                .child(getIdUsuario());
        pedidoRef.setValue(this);
    }

    public void remover() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference pedidoRef = firebaseRef
                .child("pedidos_usuario")
                .child(getIdEmpresa())
                .child(getIdUsuario());
        pedidoRef.removeValue();
    }

    public void criarHistorico() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference pedidoRef = firebaseRef
                .child("historico_pedidos_usuario")
                .child(getIdUsuario())
                .child(getIdPedido());
        pedidoRef.setValue(this);
    }

    public void confirmar() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference pedidoRef = firebaseRef
                .child("pedidos")
                .child(getIdEmpresa())
                .child(getIdPedido());
        pedidoRef.setValue(this);
    }

    public void atualizarStatus(){

        HashMap<String, Object> status = new HashMap<>();
        status.put("status", getStatus());

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference pedidoRef = firebaseRef
                .child("pedidos")
                .child(getIdEmpresa())
                .child(getIdPedido());
        pedidoRef.updateChildren(status);
    }

    public void atualizarStatusPedidoUsuario(String idUsu, String idPed) {

        HashMap<String, Object> status = new HashMap<>();
        status.put("status", getStatus());

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference pedidoRef = firebaseRef
                .child("historico_pedidos_usuario")
                .child(idUsu)
                .child(idPed);
        pedidoRef.updateChildren(status);

    }

    public String getNomeEmpresa() {
        return nomeEmpresa;
    }

    public void setNomeEmpresa(String nomeEmpresa) {
        this.nomeEmpresa = nomeEmpresa;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getTelEmpresa() {
        return telEmpresa;
    }

    public void setTelEmpresa(String telEmpresa) {
        this.telEmpresa = telEmpresa;
    }

    public String getTelUsuario() {
        return telUsuario;
    }

    public void setTelUsuario(String telUsuario) {
        this.telUsuario = telUsuario;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(String idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(String idPedido) {
        this.idPedido = idPedido;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(int metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

}
