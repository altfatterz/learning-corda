package com.example.states

import com.example.contracts.IOUContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.Party
import java.time.LocalDate

// *********
// * State *
// *********
@BelongsToContract(IOUContract::class)
data class IOUState(
        val value: Int,
        val lender: Party,
        val borrower: Party,
        val date: LocalDate) : ContractState {
    override val participants get() = listOf(lender, borrower)
}