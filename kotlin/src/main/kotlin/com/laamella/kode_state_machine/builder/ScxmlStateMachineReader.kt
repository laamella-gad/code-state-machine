package com.laamella.kode_state_machine.builder

import com.laamella.kode_state_machine.*
import com.laamella.kode_state_machine.action.NoAction
import com.laamella.kode_state_machine.condition.AlwaysCondition
import com.laamella.kode_state_machine.priority.PriorityDeterminizer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import javax.xml.parsers.DocumentBuilderFactory

private val logger = KotlinLogging.logger {}

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
class ScxmlStateMachineReader<T, E>(
    private val inputSource: InputSource,
    private val interpretEvent: (attribute: String) -> Action,
    private val interpretCondition: (attribute: String) -> Condition<E>,
    private val interpretStateName: (name: String) -> T
) {
    private val documentBuilderFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()

    fun build(newMachine: StateMachineBuilder<T, E, Int>) {
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()
        val root = documentBuilder.parse(inputSource).childNodes.item(0) as Element

        parseState(root, newMachine)
    }

    fun build(): StateMachine<T, E, Int> {
        val stateMachineBuilder = stateMachine<T, E, Int>(0) {}
        build(stateMachineBuilder)
        return stateMachineBuilder.build()
    }

    private fun parseState(stateElement: Element, builder: StateMachineBuilder<T, E, Int>): T {
        if (stateElement.hasAttribute(INITIAL_ATTRIBUTE)) {
            val initialState = interpretStateName(stateElement.getAttribute(INITIAL_ATTRIBUTE))
            builder.more { state(initialState) { isAStartState() } }
        }
        val stateName = stateElement.getAttribute(ID_ATTRIBUTE)
        val state = interpretStateName(stateName)

        stateElement.childNodes.forEach { subNode ->
            if (subNode.nodeType == Node.ELEMENT_NODE) {
                val subElement = subNode as Element
                val subNodeName = subElement.nodeName
                when (subNodeName) {
                    STATE_ELEMENT, PARALLEL_ELEMENT, ROOT_STATE_MACHINE_ELEMENT -> parseState(subElement, builder)
                    FINAL_STATE_ELEMENT -> builder.more { state(parseState(subElement, builder)) { isAnEndState() } }
                    TRANSITION_ELEMENT -> if (subElement.hasAttribute(TARGET_ATTRIBUTE)) {
                        val targetState = interpretStateName(subElement.getAttribute(TARGET_ATTRIBUTE))

                        var conditions: Condition<E> = AlwaysCondition()
                        if (subElement.hasAttribute(CONDITION_ATTRIBUTE)) {
                            conditions = interpretCondition(subElement.getAttribute(CONDITION_ATTRIBUTE))
                        }

                        var actions: Action = NoAction()
                        if (subElement.hasAttribute(EVENT_ATTRIBUTE)) {
                            actions = interpretEvent(subElement.getAttribute(EVENT_ATTRIBUTE))
                        }

                        // TODO do something about priorities
                        builder.more {
                            state(state) {
                                transitionsTo(
                                    targetState,
                                    condition = conditions,
                                    action = actions,
                                    priority = PriorityDeterminizer.nextPriority(),
                                )
                            }
                        }
                    } else {
                        logger.warn{"State $stateName has a transition going nowhere."}
                    }

                    "onentry" -> builder.more { state(state) { onEntry(interpretEvent(subNode.textContent)) } }
                    "onexit" -> builder.more { state(state) { onExit(interpretEvent(subNode.textContent)) } }
                }
            }
        }
        return state
    }

    companion object {
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

fun NodeList.forEach(action: (Node) -> Unit) {
    (0 until this.length)
        .asSequence()
        .map { this.item(it) }
        .forEach { action(it) }
}
