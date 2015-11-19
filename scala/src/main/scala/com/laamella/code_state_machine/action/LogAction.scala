package com.laamella.code_state_machine.action

import grizzled.slf4j.Logging

/**
 * This action logs a line.
 */
final class LogAction(logText: String) extends (() => Unit) with Logging {
  override def apply() = trace(logText)

  override def toString() = s"log ($logText)"
}