package com.laamella.code_state_machine.builder;

import com.laamella.code_state_machine.*;
import com.laamella.code_state_machine.priority.PriorityDeterminizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * A State machine builder that attempts to read the <a
 * href="http://www.w3.org/TR/scxml/">SCXML</a> format. Since many features are
 * mismatched, it does not do a very good job.
 * <table>
 * <tr>
 * <td>Supported?
 * <td>Feature
 * </tr>
 * <tr>
 * <td>&#x2713;
 * <td>normal/start/end states
 * </tr>
 * <tr>
 * <td>&#x2713; / &#x2717;
 * <td>on entry/on exit events (interpretation is up to the user)
 * </tr>
 * <tr>
 * <td>&#x2713; / &#x2717;
 * <td>event (action) on transition (interpretation is up to the user)
 * </tr>
 * <tr>
 * <td>&#x2713; / &#x2717;
 * <td>(pre)conditions for transitions (interpretation is up to the user)
 * </tr>
 * <tr>
 * <td>&#x2717;
 * <td>clusters, compound states, or sub state machines (treated as normal
 * states. Substates are mixed into the main state machine)
 * </tr>
 * <tr>
 * <td>&#x2717;
 * <td>executable content
 * </tr>
 * <tr>
 * <td>&#x2717;
 * <td>parallel states (treated as normal states)
 * </tr>
 */
public abstract class ScxmlStateMachineBuilder<T, E> implements StateMachineBuilder<T, E, Integer> {
    private static final Logger log = LoggerFactory.getLogger(ScxmlStateMachineBuilder.class);

    private static final String TRANSITION_ELEMENT = "transition";
    private static final String PARALLEL_ELEMENT = "parallel";
    private static final String CONDITION_ATTRIBUTE = "cond";
    private static final String EVENT_ATTRIBUTE = "event";
    private static final String TARGET_ATTRIBUTE = "target";
    private static final String ROOT_STATE_MACHINE_ELEMENT = "scxml";
    private static final String INITIAL_ATTRIBUTE = "initial";
    private static final String ID_ATTRIBUTE = "id";
    private static final String STATE_ELEMENT = "state";
    private static final String FINAL_STATE_ELEMENT = "final";

    private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    private final InputSource inputSource;

    public ScxmlStateMachineBuilder(InputSource inputSource) {
        this.inputSource = inputSource;
    }

    @Override
    public StateMachine<T, E, Integer> build(StateMachine<T, E, Integer> machine)
            throws ParserConfigurationException, SAXException, IOException {
        var documentBuilder = documentBuilderFactory.newDocumentBuilder();
        var root = (Element) documentBuilder.parse(inputSource).getChildNodes().item(0);

        parseState(root, machine.new Internals());
        return machine;
    }

    @Override
    public StateMachine<T, E, Integer> build() throws ParserConfigurationException, SAXException, IOException {
        return build(new StateMachine<>());
    }

    private T parseState(final Element stateElement, final StateMachine<T, E, Integer>.Internals builder) {
        if (stateElement.hasAttribute(INITIAL_ATTRIBUTE)) {
            var initialState = interpretStateName(stateElement.getAttribute(INITIAL_ATTRIBUTE));
            builder.addStartState(initialState);
        }
        var stateName = stateElement.getAttribute(ID_ATTRIBUTE);
        var state = interpretStateName(stateName);

        var childNodes = stateElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            var subNode = childNodes.item(i);
            if (subNode.getNodeType() == Node.ELEMENT_NODE) {
                var subElement = (Element) subNode;
                var subNodeName = subElement.getNodeName();
                switch (subNodeName) {
                    case STATE_ELEMENT:
                    case PARALLEL_ELEMENT:
                    case ROOT_STATE_MACHINE_ELEMENT:
                        parseState(subElement, builder);
                        break;
                    case FINAL_STATE_ELEMENT:
                        builder.addEndState(parseState(subElement, builder));
                        break;
                    case TRANSITION_ELEMENT:
                        if (subElement.hasAttribute(TARGET_ATTRIBUTE)) {
                            var targetState = interpretStateName(subElement.getAttribute(TARGET_ATTRIBUTE));

                            var conditions = new Conditions<E>();
                            if (subElement.hasAttribute(CONDITION_ATTRIBUTE)) {
                                conditions.add(interpretCondition(subElement.getAttribute(CONDITION_ATTRIBUTE)));
                            }

                            var actions = new Actions();
                            if (subElement.hasAttribute(EVENT_ATTRIBUTE)) {
                                actions.add(interpretEvent(subElement.getAttribute(EVENT_ATTRIBUTE)));
                            }

                            // TODO do something about priorities
                            builder.addTransition(new Transition<>(state, targetState, conditions, PriorityDeterminizer.nextPriority(), actions));
                        } else {
                            log.warn("State " + stateName + " has a transition going nowhere.");
                        }
                        break;
                    case "onentry":
                        builder.addEntryActions(state, interpretEvent(subNode.getTextContent()));
                        break;
                    case "onexit":
                        builder.addExitActions(state, interpretEvent(subNode.getTextContent()));
                        break;
                }
            }
        }
        return state;
    }

    protected abstract Action interpretEvent(final String attribute);

    protected abstract Condition<E> interpretCondition(final String attribute);

    protected abstract T interpretStateName(final String name);
}
