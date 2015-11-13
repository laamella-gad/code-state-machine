shared class Logger() {
	shared void warn(String logText) {}
	shared void trace(String logText) {}
	shared void debug(String logText) {}

}

shared object loggerFactory {
	shared Logger getLogger(Anything c) {
		return Logger();
	}
}
