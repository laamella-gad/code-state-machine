package com.laamella.kode_state_machine

internal class WasteTimeAction : TaskAction<Char?>({ Thread.sleep(500) })

internal class TaskActionTest {
    @org.junit.jupiter.api.Test
    fun wiejf() {
        val builder: DslStateMachineBuilder<kotlin.Char?, kotlin.Char?, Int?>? =
            object : DslStateMachineBuilder<kotlin.Char?, kotlin.Char?, Int?>(0) {
                protected override fun executeBuildInstructions() {
                    val wasteTimeAction = com.laamella.kode_state_machine.WasteTimeAction()
                    state('A').isAStartState()
                        .`when`(always()).action(wasteTimeAction).then('B')
                    state('B').`when`(wasteTimeAction).then('C')
                    state('C').isAnEndState()
                }
            }
        val machine: StateMachine<kotlin.Char?, kotlin.Char?, Int?> = builder.build()
        com.laamella.kode_state_machine.TaskActionTest.Companion.log.debug(machine.toString())
        machine.poll()
        com.laamella.kode_state_machine.TaskActionTest.Companion.log.debug(machine.toString())
        assertEquals("active states: B", machine.toString())
        await().atMost(1, TimeUnit.SECONDS).until({
            machine.poll()
            val string = machine.toString()
            kotlin.io.println(string)
            string == "active states: C"
        })
    }

    companion object {
        private val log: org.slf4j.Logger =
            org.slf4j.LoggerFactory.getLogger(com.laamella.kode_state_machine.TaskActionTest::class.java)
    }
}
