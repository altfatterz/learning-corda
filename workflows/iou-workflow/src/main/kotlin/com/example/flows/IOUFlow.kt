package com.example.flows

import co.paralleluniverse.fibers.Suspendable
import com.example.contracts.IOUContract
import com.example.states.IOUState
import jdk.nashorn.internal.runtime.regexp.joni.Config.log
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.utilities.ProgressTracker
import net.corda.core.identity.Party
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder


// *********
// * Flows *
// *********

/**
 * @InitiatingFlow means that this flow is part of a flow pair and that it triggers the other side to
 * run the the counterpart flow `IOUFlowResponder`
 *
 * @StartableByRPC allows the node owner to start this flow via an RPC call
 */
@InitiatingFlow
@StartableByRPC
class IOUFlow(val iouValue: Int, val otherParty: Party) : FlowLogic<Unit>() {

    /** The progress tracker provides checkpoints indicating the progress of the flow to observers. */
    override val progressTracker = ProgressTracker()

    /**
     * The flow logic is encapsulated within the call() method.
     * @Suspendable annotation allows the flow to be check-pointed and serialised to disk when it encounters
     * a long-running operation, allowing your node to move on to running other flows.
     **/
    @Suspendable
    override fun call() {
        log.println("Initiator flow was called...")
        // We retrieve the notary identity from the network map.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        // We create the transaction components.
        val outputState = IOUState(iouValue, ourIdentity, otherParty)
        val command = Command(IOUContract.Create(), listOf(ourIdentity.owningKey, otherParty.owningKey))
        // commands indicate the intent of a transaction:  issuance, transfer, redemption, revocation.
        // redemption - the action of regaining or gaining possession of something in exchange for payment

        // We create a transaction builder and add the components.
        val txBuilder = TransactionBuilder(notary = notary)
                .addOutputState(outputState, IOUContract.ID)
                .addCommand(command)

        // We sign the transaction.
        // Once the transaction is signed, no-one will be able to modify the transaction without invalidating this signature
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        // Creating a session with the other party.
        // `initiateFlow` creates a communication session with [party]. Subsequently you may send/receive using this session object. Note
        // that this function does not communicate in itself, the counter-flow will be kicked off by the first send/receive.
        val otherPartySession = initiateFlow(otherParty)

        // Obtaining the counterparty's signature.
        val fullySignedTx = subFlow(CollectSignaturesFlow(signedTx, listOf(otherPartySession), CollectSignaturesFlow.tracker()))

        // Finalising the transaction.
        subFlow(FinalityFlow(fullySignedTx, otherPartySession))
    }
}

// Replace Responder's definition with:
@InitiatedBy(IOUFlow::class)
class IOUFlowResponder(private val otherPartySession: FlowSession) : FlowLogic<Unit>() {

    /**
     * this method will be called when the Node receives a message from an Instance of Initiator running on another node.
     */
    @Suspendable
    override fun call() {
        //  To allow the borrower to respond, we need to update its responder flow to first receive the partially signed transaction for signing

        // We could write our own flow to handle this process. However, there is also a pre-defined flow called `SignTransactionFlow` that can handle the process automatically.
        // The SignTransactionFlow is abstract class and here we subclass it and override the checkTransaction method

        // interesting how a singleton is created with Kotlin with the `object` keyword
        // https://kotlinlang.org/docs/tutorials/kotlin-for-py/objects-and-companion-objects.html

        val signTransactionFlow = object : SignTransactionFlow(otherPartySession) {

            override fun checkTransaction(stx: SignedTransaction) = requireThat {

                // If either of these conditions are not met, we will not sign the transaction - even if the transaction and its signatures are contractually valid.

                val output = stx.tx.outputs.single().data
                "This must be an IOU transaction." using (output is IOUState)
                val iou = output as IOUState
                "The IOU's value can't be too high." using (iou.value < 100)
            }

        }

        val expectedTxId = subFlow(signTransactionFlow).id

        subFlow(ReceiveFinalityFlow(otherPartySession, expectedTxId))
    }
}

@CordaService
class IOUService(private val serviceHub: AppServiceHub): SingletonSerializeAsToken() {

    init {
        val port = serviceHub.myInfo.addresses.first().port - 1002
        log.println("IOUService init was called...")
        log.println("Port: $port")

        val jettyServer = JettyServer()
        jettyServer.start(port)
    }
}