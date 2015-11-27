import ceylon.language.meta {
	classDeclaration
}
abstract class GameEvent() of left | right | up | down | start | done | dead | complete | escape | fireA | fireB {
	shared actual String string => classDeclaration(this).name;
}
object dead extends GameEvent() {}
object complete extends GameEvent() {}
object escape extends GameEvent() {}
object fireA extends GameEvent() {}
object done extends GameEvent() {}
object fireB extends GameEvent() {}
object start extends GameEvent() {}
object down extends GameEvent() {}
object up extends GameEvent() {}
object right extends GameEvent() {}
object left extends GameEvent() {}
