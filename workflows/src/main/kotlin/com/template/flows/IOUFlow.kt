package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.IOUContract
import com.template.states.IOUState
import jdk.nashorn.internal.runtime.regexp.joni.Config.log
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.utilities.ProgressTracker
import net.corda.core.identity.Party
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
        val command = Command(IOUContract.Commands.Action(), ourIdentity.owningKey)
        // commands indicate the intent of a transaction:  issuance, transfer, redemption, revocation.

        // We create a transaction builder and add the components.
        val txBuilder = TransactionBuilder(notary = notary)
                .addOutputState(outputState, IOUContract.ID)
                .addCommand(command)

        // We sign the transaction.
        // Once the transaction is signed, no-one will be able to modify the transaction without invalidating this signature
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        // Creating a session with the other party.
        val otherPartySession = initiateFlow(otherParty)

        // We finalise the transaction and then send it to the counterparty.
        subFlow(FinalityFlow(signedTx, otherPartySession))
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
        log.println("Responder Flow was called...")
        subFlow(ReceiveFinalityFlow(otherPartySession))
    }
}
