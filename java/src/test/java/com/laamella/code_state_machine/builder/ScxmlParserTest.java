package com.laamella.code_state_machine.builder;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.laamella.code_state_machine.Action;
import com.laamella.code_state_machine.Condition;
import com.laamella.code_state_machine.StateMachine;
import com.laamella.code_state_machine.action.LogAction;
import com.laamella.code_state_machine.condition.AlwaysCondition;
import com.laamella.code_state_machine.io.DotOutput;

public class ScxmlParserTest {
    @Test
    public void readTestScxml() throws ParserConfigurationException, SAXException, IOException {
        final InputSource inputSource = new InputSource(ScxmlStateMachineBuilder.class.getResourceAsStream("/test.scxml"));
        final ScxmlStateMachineBuilder<String, String> scxmlParser = new ScxmlStateMachineBuilder<>(inputSource) {
            @Override
            protected String interpretStateName(final String name) {
                return name;
            }

            @Override
            protected Action interpretEvent(final String attribute) {
                return new LogAction(attribute);
            }

            @Override
            protected Condition<String> interpretCondition(final String attribute) {
                return new AlwaysCondition<>();
            }
        };
        final StateMachine<String, String, Integer> stateMachine = scxmlParser.build();
        System.out.println(new DotOutput<String, String, Integer>().getOutput(stateMachine));
        //		final StateMachine<String, String, Priority> stateMachine = builder.build();
        // TODO finish tests
    }
}
