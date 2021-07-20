package br.com.zup.edu.grpc

import br.com.zupedu.grpc.PixServiceGrpc
import br.com.zupedu.grpc.RemovePixServiceGrpc
import br.com.zupedu.grpc.RetrievePixServiceGrpc
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import javax.inject.Singleton

@Factory
class KeyManagerGrpcFactory(@GrpcChannel("keyPixGrpc") val channel: ManagedChannel) {

    @Singleton
    fun registerPixClient() = PixServiceGrpc.newBlockingStub(channel)

    @Singleton
    fun removePixClient() = RemovePixServiceGrpc.newBlockingStub(channel)

    @Singleton
    fun retrievePixClient() = RetrievePixServiceGrpc.newBlockingStub(channel)
}
