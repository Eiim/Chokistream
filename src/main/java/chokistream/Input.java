package chokistream;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Input {
    private int keyCode;
    private int modifiers;
    private String asStr;
    private static Map<Integer, String> downMasks = null;
    private static Map<Integer, String> keyMasks = null;

    public Input(int keyCode, int modifiers) {
        this.keyCode = keyCode;
        this.modifiers = modifiers;
    }

    public Input(String keyRep) throws InputParseException {
        String[] keys = keyRep.split("\\+");
        String mainKey = keys[keys.length-1];
        try {
            // Reflection to static field
            keyCode = KeyEvent.class.getDeclaredField("VK_"+mainKey.toUpperCase()).getInt(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new InputParseException("Failed to parse "+mainKey+" as key name");
        }

        modifiers = 0;
        for(int i = 0; i < keys.length-1; i++) {
            try {
                // Reflection to static field
                modifiers += InputEvent.class.getDeclaredField(keys[i].toUpperCase()+"_DOWN_MASK").getInt(null);
            } catch(NoSuchFieldException | IllegalAccessException e) {
                throw new InputParseException("Failed to parse "+keys[i]+" as modifier");
            }
        }

        asStr = keyRep.toLowerCase();
    }

    public boolean matches(KeyEvent ke) {
        // Check that same key is being pressed and all required modifiers are present
        // Checking that all required modifiers are present involves taking the bitwise inverse and bitwise anding with expected
        // Essentially, we make sure that there are no modifiers which are required and not pressed
        return ke.getKeyCode() == keyCode && (modifiers & ~ke.getModifiersEx()) == 0;
    }

    private String getStringForm() throws InputParseException {
        // Put all modifiers in a map, if not already done
        constructDownMasks();
        // Put all key codes in a map, if not already done
        constructKeyMasks();
        
        String key = keyMasks.get(keyCode);
        if(key == null) throw new InputParseException("Couldn't find key with code "+keyCode);

        String mods = "";

        for(int i = 1; i < modifiers*2; i*=2) { // Loop over bits
            if((i & modifiers) > 0) { // If the bit is set in modifiers
                if(downMasks.containsKey(i)) {
                    mods += downMasks.get(i)+"+";
                } else {
                    throw new InputParseException("Couldn't find modifier with value "+i);
                }
            }
        }

        return mods + key;
    }
    
    private static void constructDownMasks() throws InputParseException {
    	if(downMasks != null) return; // don't recreate it if we already have it
    	downMasks = new HashMap<>();
    	
    	for(Field f : InputEvent.class.getDeclaredFields()) {
            if(f.getName().endsWith("_DOWN_MASK")) {
                try {
                    downMasks.put(f.getInt(null), f.getName().substring(f.getName().length()-10).toLowerCase());
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new InputParseException("Error parsing field "+f.getName()+" (shouldn't be possible!)");
                }
            }
        }
    }
    
    private static void constructKeyMasks() throws InputParseException {
    	if(keyMasks != null) return; // don't recreate it if we already have it
    	keyMasks = new HashMap<>();
    	
    	for(Field f : KeyEvent.class.getDeclaredFields()) {
            if(f.getName().startsWith("VK_")) {
                try {
                	keyMasks.put(f.getInt(null), f.getName().substring(3));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new InputParseException("Error parsing field "+f.getName()+" (shouldn't be possible!)");
                }
            }
        }
    }
    
    @Override
    public String toString() {
        if(asStr == null) {
            try {
                asStr = getStringForm();
            } catch (InputParseException e ) {
                return null;
            }
        }
        return asStr;
    }

    public static class InputParseException extends Exception {
        // Probably not important but might as well have
		private static final long serialVersionUID = 4718016172464839873L;
		
		String message;

        public InputParseException(String m) {
            message = m;
		}
		
		@Override
		public String getMessage() {
			return message;
		}
    }
}
