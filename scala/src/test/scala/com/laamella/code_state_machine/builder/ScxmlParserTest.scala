package com.laamella.code_state_machine.builder

import com.laamella.code_state_machine.{UnitSpec, Action, Condition}
import com.laamella.code_state_machine.action.LogAction
import com.laamella.code_state_machine.condition.AlwaysCondition
import com.laamella.code_state_machine.io.DotOutput
import com.laamella.code_state_machine.priority.AutomaticPriority
import org.xml.sax.InputSource
import org.scalatest._
import org.scalatest.Assertions._

class ScxmlParserTest extends UnitSpec {

  behavior of "the SCXML parser"

  it should "correctly read an SCXML file" in {
    val inputSource = new InputSource(classOf[ScxmlStateMachineBuilder[String, String]].getResourceAsStream("/test.scxml"))
    val scxmlParser = new ScxmlStateMachineBuilder[String, String](inputSource) {
      override def interpretStateName(name: String): String = name

      override def interpretEvent(attribute: String): Action = new LogAction(attribute)

      override def interpretCondition(attribute: String): Condition[String] = new AlwaysCondition[String]()
    }
    val stateMachine = scxmlParser.build()
    System.out.println(new DotOutput[String, String, AutomaticPriority]().getOutput(stateMachine))
//    val stateMachine = builder.build()
    // TODO finish tests
  }
}
