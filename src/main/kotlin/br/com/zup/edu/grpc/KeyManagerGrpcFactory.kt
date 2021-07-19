package br.com.zup.edu.grpc

import br.com.zupedu.grpc.PixServiceGrpc
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import javax.inject.Singleton

@Factory
class KeyManagerGrpcFactory(@GrpcChannel("keyPixGrpc") val channel: ManagedChannel) {

    @Singleton
    fun registerPixClient() = PixServiceGrpc.newBlockingStub(channel)
}