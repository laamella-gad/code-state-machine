package com.laamella.code_state_machine

sealed trait GameEvent

case object LEFT extends GameEvent

case object RIGHT extends GameEvent

case object UP extends GameEvent

case object DOWN extends GameEvent

case object START extends GameEvent

case object DONE extends GameEvent

case object DEAD extends GameEvent

case object COMPLETE extends GameEvent

case object ESCAPE extends GameEvent

case object FIRE_A extends GameEvent

case object FIRE_B extends GameEvent
