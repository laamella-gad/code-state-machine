package com.laamella.kode_state_machine.builder

import com.laamella.kode_state_machine.*
import com.laamella.kode_state_machine.priority.PriorityDeterminizer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import javax.xml.parsers.DocumentBuilderFactory

/**
 * A State machine builder that attempts to read the [SCXML](http://www.w3.org/TR/scxml/) format. Since many features are
 * mismatched, it does not do a very good job.
 *
 * | Supported  | Feature|
 * | :---- | :---- |
 * | ✓     | normal/start/end states      |
 * | ✓ / ✗     | on entry/on exit events (interpretation is up to the user)      |
 * | ✓ / ✗     | event (action) on transition (interpretation is up to the user)      |
 * | ✓ / ✗     | (pre)conditions for transitions (interpretation is up to the user)      |
 * | ✗     | clusters, compound states, or substate machines (treated as normal states. Substates are mixed into the main state machine)      |
 * | ✗     | executable content      |
 * | ✗     | parallel states (treated as normal states)      |
 */
abstract class ScxmlStateMachineBuilder<T, E>(private val inputSource: InputSource)  {
    private val documentBuilderFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()

    override fun build(newMachine: StateMachineBuilder<T, E, Int>): StateMachine<T, E, Int> {
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()
        val root = documentBuilder.parse(inputSource).childNodes.item(0) as Element

        parseState(root, newMachine.Internals())
        return newMachine
    }

    override fun build(): StateMachineBuilder<T, E, Int> {
        return build(StateMachineBuilder(defaultPriority))
    }

    private fun parseState(stateElement: Element, builder: StateMachine<T, E, Int>.Internals): T {
        if (stateElement.hasAttribute(INITIAL_ATTRIBUTE)) {
            val initialState = interpretStateName(stateElement.getAttribute(INITIAL_ATTRIBUTE))
            builder.addStartState(initialState)
        }
        val stateName = stateElement.getAttribute(ID_ATTRIBUTE)
        val state = interpretStateName(stateName)

        val childNodes = stateElement.childNodes
        for (i in 0..<childNodes.length) {
            val subNode = childNodes.item(i)
            if (subNode.nodeType == Node.ELEMENT_NODE) {
                val subElement = subNode as Element
                val subNodeName = subElement.nodeName
                when (subNodeName) {
                    STATE_ELEMENT, PARALLEL_ELEMENT, ROOT_STATE_MACHINE_ELEMENT -> parseState(subElement, builder)
                    FINAL_STATE_ELEMENT -> builder.addEndState(parseState(subElement, builder))
                    TRANSITION_ELEMENT -> if (subElement.hasAttribute(TARGET_ATTRIBUTE)) {
                        val targetState = interpretStateName(subElement.getAttribute(TARGET_ATTRIBUTE))

                        val conditions = Conditions<E>()
                        if (subElement.hasAttribute(CONDITION_ATTRIBUTE)) {
                            conditions.add(interpretCondition(subElement.getAttribute(CONDITION_ATTRIBUTE)))
                        }

                        val actions = Actions()
                        if (subElement.hasAttribute(EVENT_ATTRIBUTE)) {
                            actions.add(interpretEvent(subElement.getAttribute(EVENT_ATTRIBUTE)))
                        }

                        // TODO do something about priorities
                        builder.addTransition(
                            Transition(
                                state,
                                targetState,
                                conditions,
                                PriorityDeterminizer.nextPriority(),
                                actions
                            )
                        )
                    } else {
                        log.warn("State $stateName has a transition going nowhere.")
                    }

                    "onentry" -> builder.addEntryActions(state, interpretEvent(subNode.textContent))
                    "onexit" -> builder.addExitActions(state, interpretEvent(subNode.textContent))
                }
            }
        }
        return state
    }

    protected abstract fun interpretEvent(attribute: String): Action

    protected abstract fun interpretCondition(attribute: String): Condition<E>

    protected abstract fun interpretStateName(name: String): T

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ScxmlStateMachineBuilder::class.java)

        private const val TRANSITION_ELEMENT = "transition"
        private const val PARALLEL_ELEMENT = "parallel"
        private const val CONDITION_ATTRIBUTE = "cond"
        private const val EVENT_ATTRIBUTE = "event"
        private const val TARGET_ATTRIBUTE = "target"
        private const val ROOT_STATE_MACHINE_ELEMENT = "scxml"
        private const val INITIAL_ATTRIBUTE = "initial"
        private const val ID_ATTRIBUTE = "id"
        private const val STATE_ELEMENT = "state"
        private const val FINAL_STATE_ELEMENT = "final"
    }
}
