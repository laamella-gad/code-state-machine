package com.laamella.code_state_machine.util;

public class Assert {
	public static void notNull(final Object o) {
		if (o == null) {
			throw new IllegalStateException();
		}
	}

	public static void notEmpty(final Object[] o) {
		if (o.length == 0) {
			throw new IllegalStateException();
		}
	}

	public static void notNull(final Object o, final String message) {
		if (o == null) {
			throw new IllegalStateException(message);
		}
	}
}
