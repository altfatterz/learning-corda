package com.example.contracts

import com.example.states.IOUState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction


// ************
// * Contract *
// ************
class IOUContract : Contract {

    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.example.contracts.IOUContract"
    }

    // Commands serve two functions:
    // - They indicate the transaction’s intent, for example, a transaction proposing the creation of an IOU could have to meet different constraints to one redeeming an IOU
    // - They allow us to define the required signers for the transaction. IOU creation might require signatures from the lender only, whereas the transfer of an IOU might require signatures from both the IOU’s borrower and lender

    // Our Create command.
    class Create : CommandData

    // What would a good contract for an IOUState look like? - There is no right or wrong answer - it depends on how you want your CorDapp to behave.
    // In our case we only want to allow the creation of IOUs, we don't want transfer and redeem (possession for something else) them for cash.
    // One way to enforce this:
    // - A transaction involving IOUs must consume zero inputs, and create one output of type `IOUState`
    // - The transaction should also include a Create command, indicating the transaction’s intent
    // We might also want to impose some constraints on the properties of the issued IOUState:
    // - Its value must be non-negative
    // - The lender and the borrower cannot be the same entity
    // We also want to impose constraints on who is required to sign the transaction:
    // - The IOU’s lender must sign
    // - The IOU’s borrower must sign

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Create>()

        requireThat {
            // Constraints on the shape of the transaction.
            "No inputs should be consumed when issuing an IOU." using (tx.inputs.isEmpty())
            "There should be one output state of type IOUState." using (tx.outputs.size == 1)

            // IOU-specific constraints.
            val output = tx.outputsOfType<IOUState>().single()
            "The IOU's value must be non-negative." using (output.value > 0)
            "The lender and the borrower cannot be the same entity." using (output.lender != output.borrower)

            // Constraints on the signers.
            val expectedSigners = listOf(output.borrower.owningKey, output.lender.owningKey)
            "There must be two signers." using (command.signers.toSet().size == 2)
            "The borrower and lender must be signers." using (command.signers.containsAll(expectedSigners))
        }
    }

    // We’ve now written an IOUContract constraining the evolution of each IOUState over time:
    // - An `IOUState` can only be created, not transferred or redeemed
    // - Creating an `IOUState` requires an issuance transaction with no inputs, a single `IOUState` output, and a `Create` command
    // - The `IOUState` created by the issuance transaction must have a non-negative value, and the lender and borrower must be different entities

//    // Used to indicate the transaction's intent.
//    interface Commands : CommandData {
//        class Action : Commands
//    }
}