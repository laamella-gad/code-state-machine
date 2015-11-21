import ceylon.test { ... }

test
void shouldAlwaysSuccess() {
	assert(1 == 1);
}

test
void shouldAlwaysFail() {
	fail("crash !!!");
}