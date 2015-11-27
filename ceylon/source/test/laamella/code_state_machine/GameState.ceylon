import ceylon.language.meta {
	classDeclaration
}
abstract class GameState() of loader | intro | menu | configuration | getReady | level | levelFinish | gameOver | exit {
	shared actual String string => classDeclaration(this).name;
}
object level extends GameState() {}
object getReady extends GameState() {}
object configuration extends GameState() {}
object menu extends GameState() {}
object loader extends GameState() {}
object intro extends GameState() {}
object levelFinish extends GameState() {}
object gameOver extends GameState() {}
object exit extends GameState() {}
