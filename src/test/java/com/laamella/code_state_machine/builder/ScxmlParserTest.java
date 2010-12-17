package com.laamella.code_state_machine.builder;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.laamella.code_state_machine.Action;
import com.laamella.code_state_machine.Precondition;
import com.laamella.code_state_machine.StateMachine.Builder;
import com.laamella.code_state_machine.io.DotOutput;
import com.laamella.code_state_machine.priority.Priority;

public class ScxmlParserTest {
	@Test
	public void readTestScxml() throws ParserConfigurationException, SAXException, IOException {
		final ScxmlParser<String, String, Priority> scxmlParser = new ScxmlParser<String, String, Priority>(
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
			protected Precondition<String> interpretCondition(final String attribute) {
				return null;
			}
		};
		final Builder<String, String, Priority> builder = scxmlParser.parse(new InputSource(ScxmlParser.class
				.getResourceAsStream("/test.scxml")));
		System.out.println(new DotOutput<String, String, Priority>(builder.getMetaInformation()).getOutput());
		//		final StateMachine<String, String, Priority> stateMachine = builder.build();
	}
}
