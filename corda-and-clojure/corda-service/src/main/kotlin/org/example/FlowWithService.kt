package org.example

import clojure.java.api.Clojure
import clojure.lang.IFn
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

    private val requiringResolve = Clojure.`var`("clojure.core/requiring-resolve")

    private fun resolve(symbolName: String): IFn? { // use new thread safe require / resolve combo
        return requiringResolve.invoke(Clojure.read(symbolName)) as IFn
    }

    init {
        log.info("Creating instance of ExampleService...")

        val plus = Clojure.`var`("clojure.core", "+")
        val result = plus.invoke(1, 2)
        log.info("Result of Clojure calculation: $result")

        val server = Clojure.`var`("server/start")
        log.info("server: $server")

        // Attempting to call unbound fn: #'server/start
//        val invoke = resolve("server/start")?.invoke()
//        log.info("invoke: $invoke")

        // java.io.FileNotFoundException: Could not locate server__init.class, server.clj or server.cljc on classpath.
//        val invoke = resolve("server/-main")?.invoke()
//        log.info("invoke: $invoke")

        // Not a qualified symbol: clojure_service.server
//        val invoke = resolve("clojure_service.server")?.invoke()
//        log.info("invoke: $invoke")

        // Attempting to call unbound fn: #'server/start
//        val invoke = resolve("server/start")?.invoke("runnable-service")
//        log.info("invoke: $invoke")

//         Attempting to call unbound fn: #'server/start
//        val invoke = resolve("server/start runnable service")?.invoke()
//        log.info("invoke: $invoke")
    }

    fun greet(): String {
        return "Hello"
    }
}
