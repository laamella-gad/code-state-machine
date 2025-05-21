package com.laamella.kode_state_machine.builder

import com.laamella.kode_state_machine.action.LogAction
import com.laamella.kode_state_machine.condition.AlwaysCondition
import com.laamella.kode_state_machine.io.dotOutput
import org.junit.jupiter.api.Test
import org.xml.sax.InputSource

class ScxmlParserTest {
    @Test
    fun readTestScxml() {
        val inputSource = InputSource(ScxmlParserTest::class.java.getResourceAsStream("/test.scxml"))
        val scxmlParser = ScxmlStateMachineReader<String, String>(
            inputSource,
            { event -> LogAction(event) },
            { condition -> AlwaysCondition() },
            { stateName -> stateName }
        )
        val stateMachine = scxmlParser.build()
        println(dotOutput(stateMachine))
        //		final StateMachine<String, String, Priority> stateMachine = builder.build();
        // TODO finish tests
    }
}
