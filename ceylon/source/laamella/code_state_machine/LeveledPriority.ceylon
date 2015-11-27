import ceylon.language.meta {
	classDeclaration
}
"A generic priority type."
shared abstract class LeveledPriority(Integer i) of lowest | low | normal | high | highest satisfies Comparable<LeveledPriority> {
	shared actual Comparison compare(LeveledPriority other) => i <=> other.i;
	
	shared actual String string => classDeclaration(this).name;
}
shared object highest extends LeveledPriority(1) {}
shared object high extends LeveledPriority(2) {}
shared object normal extends LeveledPriority(3) {}
shared object low extends LeveledPriority(4) {}
shared object lowest extends LeveledPriority(5) {}
