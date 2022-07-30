package chokistream;

/**
 * This is a wrapper class for App.
 * JavaFX normally runs as a module and hooks into the main class, but this configuration
 * would prevent us from building a jar with JavaFX included on the classpath. You'd instead
 * need to run a custom JDK or some such on the machine, which obviously isn't an option.
 * Instead, we use this as a wrapper, disconnected from JavaFX, which allows it to be loaded
 * properly, so we can compile a nice standalone jar. This apparently isn't supported but works.
 */
public class Main {
	public static void main(String[] args) {
		App.main(args);
	}
}
