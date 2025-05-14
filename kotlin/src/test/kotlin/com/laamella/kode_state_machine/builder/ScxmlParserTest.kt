package com.laamella.kode_state_machine.builder

import com.laamella.kode_state_machine.Action
import com.laamella.kode_state_machine.Condition
import com.laamella.kode_state_machine.action.LogAction
import com.laamella.kode_state_machine.condition.AlwaysCondition
import com.laamella.kode_state_machine.io.dotOutput
import org.junit.jupiter.api.Test
import org.xml.sax.InputSource

class ScxmlParserTest {
    @Test
    fun readTestScxml() {
        val inputSource = InputSource(ScxmlStateMachineBuilder::class.java.getResourceAsStream("/test.scxml"))
        val scxmlParser: ScxmlStateMachineBuilder<String, String> =
            object : ScxmlStateMachineBuilder<String, String>(inputSource) {
                override fun interpretStateName(name: String): String {
                    return name
                }

                override fun interpretEvent(attribute: String): Action {
                    return LogAction(attribute)
                }

                override fun interpretCondition(attribute: String): Condition<String> {
                    return AlwaysCondition()
                }
            }
        val stateMachine = scxmlParser.build()
        println(dotOutput(stateMachine))
        //		final StateMachine<String, String, Priority> stateMachine = builder.build();
        // TODO finish tests
    }
}
