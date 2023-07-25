package chokistream.props;

import java.util.NoSuchElementException;

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
		throw new NoSuchElementException("Invalid name "+name+" for property "+eClass.getName());
	}
	
	/*
	 * Very similar, helper function to get all long names for an enum
	 */
	public static <E extends Enum<E> & EnumProp> String[] getLongNames(Class<E> eClass) {
		E[] values = eClass.getEnumConstants();
		String[] names = new String[values.length];
		for(int i = 0; i < values.length; i++) {
			names[i] = values[i].getLongName();
		}
		return names;
	}
}
