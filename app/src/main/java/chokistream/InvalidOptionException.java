package chokistream;

public class InvalidOptionException extends Exception {
	private static final long serialVersionUID = -5064758657094893010L;

	/**
	 * @param option The options whose value was invalid
	 * @param value The invalid value
	 */
	public InvalidOptionException(String option, String value) {
		super("The value of "+option+" ("+value+") is invalid.");
	}
}
