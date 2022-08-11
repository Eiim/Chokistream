package chokistream.props;

public interface EnumProp {
	public String getLongName();
	
	/*
	 * Warning: somewhat complicated code ahead
	 * For a class which is both an EnumProp and an Enum (since we can't force EnumProps to be Enums),
	 * we loop through all possible values, and if any match the provided string we return it.
	 * This is the inverse of the getLongName() class.
	 */
	public static <E extends Enum<E> & EnumProp> E fromLongName(Class<E> eClass, String name) {
		for(E e : eClass.getEnumConstants()) {
			if(e.getLongName().equals(name)) {
				return e;
			}
		}
		throw new RuntimeException("Invalid name "+name+" for property "+eClass.getName());
	}
}
