package com.example

import net.corda.client.rpc.CordaRPCClient
import net.corda.core.utilities.NetworkHostAndPort.Companion.parse
import net.corda.core.utilities.loggerFor

/**
 * Connects to a Corda node via RPC and performs RPC operations on the node.
 **/
fun main(args: Array<String>) = Client().main()

private class Client {

    companion object {
        val logger = loggerFor<Client>()
    }

    fun main() {
        // Create an RPC connection to the node.
        val nodeAddress = parse("localhost:10006") // connection to O=PartyA, L=London, C=GB
        // val nodeAddress = parse("localhost:10009") // connection to O=PartyB, L=New York, C=GB
        val rpcUsername = "user1"
        val rpcPassword = "test"
        val client = CordaRPCClient(nodeAddress)
        val proxy = client.start(rpcUsername, rpcPassword).proxy

        // Interact with the node.
        // For example, here we print the nodes on the network.
        val nodes = proxy.networkMapSnapshot()
        nodes.forEach { logger.info("{}", it) }

        /*
        I 14:53:59 1 Client.main - NodeInfo(addresses=[localhost:10005], legalIdentitiesAndCerts=[O=PartyA, L=London, C=GB], platformVersion=4, serial=1572961415697)
        I 14:53:59 1 Client.main - NodeInfo(addresses=[localhost:10002], legalIdentitiesAndCerts=[O=Notary, L=London, C=GB], platformVersion=4, serial=1572961413009)
        I 14:53:59 1 Client.main - NodeInfo(addresses=[localhost:10008], legalIdentitiesAndCerts=[O=PartyB, L=New York, C=US], platformVersion=4, serial=1572961415692)
        */
    }
}