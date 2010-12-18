package com.laamella.code_state_machine.builder;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.laamella.code_state_machine.Action;
import com.laamella.code_state_machine.Condition;
import com.laamella.code_state_machine.StateMachine;
import com.laamella.code_state_machine.io.DotOutput;
import com.laamella.code_state_machine.priority.Priority;

public class ScxmlParserTest {
	@Test
	public void readTestScxml() throws ParserConfigurationException, SAXException, IOException {
		final ScxmlStateMachineBuilder<String, String, Priority> scxmlParser = new ScxmlStateMachineBuilder<String, String, Priority>(
				Priority.HIGH) {
			@Override
			protected String interpretStateName(final String name) {
				return name;
			}

			@Override
			protected Action interpretEvent(final String attribute) {
				return null;
			}

			@Override
			protected Condition<String> interpretCondition(final String attribute) {
				return null;
			}
		};
		final StateMachine<String, String, Priority> builder = scxmlParser.parse(new InputSource(
				ScxmlStateMachineBuilder.class.getResourceAsStream("/test.scxml")));
		System.out.println(new DotOutput<String, String, Priority>().getOutput(builder));
		//		final StateMachine<String, String, Priority> stateMachine = builder.build();
	}
}
