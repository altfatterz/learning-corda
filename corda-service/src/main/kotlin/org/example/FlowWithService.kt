package org.example

import clojure.java.api.Clojure
import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import org.slf4j.LoggerFactory


@StartableByRPC
class ExampleFlow() : FlowLogic<String>() {

    @Suspendable
    override fun call(): String {
        val service = serviceHub.cordaService(ExampleService::class.java)
        return service.greet()
    }

}

@CordaService
class ExampleService(private val serviceHub: AppServiceHub) : SingletonSerializeAsToken() {

    private val log = LoggerFactory.getLogger(ExampleService::class.java)

    init {
        val plus = Clojure.`var`("clojure.core", "+")
        val result = plus.invoke(1, 2)

        log.info("Result of Clojure calculation: $result")
        log.info("Creating instance of ExampleService...")
    }

    fun greet(): String {
        return "Hello"
    }
}
