package com.laamella.code_state_machine.builder

import javax.xml.parsers.DocumentBuilderFactory

import com.laamella.code_state_machine._
import com.laamella.code_state_machine.priority.AutomaticPriority
import grizzled.slf4j.Logging
import org.w3c.dom.{Element, Node}
import org.xml.sax.InputSource

import scala.collection.mutable

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
abstract class ScxmlStateMachineBuilder[T, E](inputSource: InputSource) extends StateMachineBuilder[T, E, AutomaticPriority] with Logging {
  private val TRANSITION_ELEMENT = "transition"
  private val PARALLEL_ELEMENT = "parallel"
  private val CONDITION_ATTRIBUTE = "cond"
  private val EVENT_ATTRIBUTE = "event"
  private val TARGET_ATTRIBUTE = "target"
  private val ROOT_STATE_MACHINE_ELEMENT = "scxml"
  private val INITIAL_ATTRIBUTE = "initial"
  private val ID_ATTRIBUTE = "id"
  private val STATE_ELEMENT = "state"
  private val FINAL_STATE_ELEMENT = "final"

  private val documentBuilderFactory = DocumentBuilderFactory.newInstance()

  def build(machine: StateMachine[T, E, AutomaticPriority]): StateMachine[T, E, AutomaticPriority] = {
    val documentBuilder = documentBuilderFactory.newDocumentBuilder()
    val root = documentBuilder.parse(inputSource).getChildNodes.item(0).asInstanceOf[Element]

    parseState(root)
    machine
  }

  private def parseState(stateElement: Element): T = {
    if (stateElement.hasAttribute(INITIAL_ATTRIBUTE)) {
      val initialState = interpretStateName(stateElement.getAttribute(INITIAL_ATTRIBUTE))
      addStartState(initialState)
    }
    val stateName = stateElement.getAttribute(ID_ATTRIBUTE)
    val state = interpretStateName(stateName)

    val childNodes = stateElement.getChildNodes
    for (i <- 0 until childNodes.getLength) {
      val subNode = childNodes.item(i)
      if (subNode.getNodeType == Node.ELEMENT_NODE) {
        val subElement = subNode.asInstanceOf[Element]
        val subNodeName = subElement.getNodeName
        if (subNodeName == STATE_ELEMENT || subNodeName == STATE_ELEMENT || subNodeName == PARALLEL_ELEMENT || subNodeName == ROOT_STATE_MACHINE_ELEMENT) {
          parseState(subElement)
        } else if (subNodeName == FINAL_STATE_ELEMENT) {
          addEndState(parseState(subElement))
        } else if (subNodeName == TRANSITION_ELEMENT) {
          if (subElement.hasAttribute(TARGET_ATTRIBUTE)) {
            val targetState = interpretStateName(subElement.getAttribute(TARGET_ATTRIBUTE))

            val conditions = new mutable.MutableList[Condition[E]]()
            if (subElement.hasAttribute(CONDITION_ATTRIBUTE)) {
              conditions += interpretCondition(subElement.getAttribute(CONDITION_ATTRIBUTE))
            }

            val actions = new mutable.MutableList[() => Unit]()
            if (subElement.hasAttribute(EVENT_ATTRIBUTE)) {
              actions += interpretEvent(subElement.getAttribute(EVENT_ATTRIBUTE))
            }

            // TODO do something about priorities
            addTransition(new Transition[T, E, AutomaticPriority](state, targetState, conditions, new AutomaticPriority, actions))
          } else {
            warn("State " + stateName + " has a transition going nowhere.")
          }
        } else if (subNodeName == "onentry") {
          addEntryActions(state, Seq(interpretEvent(subNode.getTextContent)))
        } else if (subNodeName == "onexit") {
          addExitActions(state, Seq(interpretEvent(subNode.getTextContent)))
        }
      }
    }
    state
  }

  protected def interpretEvent(attribute: String): () => Unit

  protected def interpretCondition(attribute: String): Condition[E]

  protected def interpretStateName(name: String): T

}
