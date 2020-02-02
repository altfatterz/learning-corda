package com.example.webserver

import com.example.flows.IOUFlow
import com.example.states.IOUState
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

/**
 * Define your API endpoints here.
 */
@RestController
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @GetMapping("/me")
    fun whoami(): String {
        return proxy.nodeInfo().legalIdentities.first().toString()
    }

    @GetMapping("/peers")
    fun getPeers(): Map<String, List<CordaX500Name>> {
        val nodeInfo = proxy.networkMapSnapshot()
        return mapOf("peers" to nodeInfo.map { it.legalIdentities.first().name })
    }

    @GetMapping("/ious")
    fun getIOUs(): List<IOU> {
        return proxy.vaultQueryBy<IOUState>().states
                .map {
                    IOU(it.state.data.value,
                            it.state.data.lender.toString(),
                            it.state.data.borrower.toString(),
                            it.state.data.date)
                }
    }

    // connect to PartyA and PartyB and check the difference
    @GetMapping("/my-ious")
    fun getMyIOUs(): List<IOU> {
        return proxy.vaultQueryBy<IOUState>().states
                .filter { it.state.data.lender.equals(proxy.nodeInfo().legalIdentities.first()) }
                .map { IOU(it.state.data.value,
                        it.state.data.lender.toString(),
                        it.state.data.borrower.toString(),
                        it.state.data.date) }
    }

    @PostMapping("/ious")
    fun createIOU(@RequestBody request: CreateIOURequest): ResponseEntity<String> {
        val partyX500Name = CordaX500Name.parse(request.partyName)
        val otherParty = proxy.wellKnownPartyFromX500Name(partyX500Name)
                ?: return ResponseEntity.badRequest().body("Party named ${request.partyName} cannot be found.\n")

        return try {
            proxy.startTrackedFlow(::IOUFlow, request.iouValue, otherParty, request.date).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction committed to ledger.\n")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message)
        }
    }

}

data class IOU(val amount: Int, val lender: String, val borrower: String, val date: LocalDate)

data class CreateIOURequest(val iouValue: Int, val partyName: String, val date: LocalDate)