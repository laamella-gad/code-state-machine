package com.laamella.code_state_machine

sealed trait GameState

case object LOADER extends GameState
case object INTRO extends GameState
case object MENU extends GameState
case object CONFIGURATION extends GameState
case object GET_READY extends GameState
case object LEVEL extends GameState
case object LEVEL_FINISH extends GameState
case object GAME_OVER extends GameState
case object EXIT extends GameState