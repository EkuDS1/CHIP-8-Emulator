package emulator;

import java.util.HashMap;
import static org.lwjgl.glfw.GLFW.*;

public class InputHandler {
	private HashMap<Integer, Integer> keys;
	static int latestKey;
	private byte states[];


	public InputHandler() {
		latestKey = 0;
		states = new byte[16];

		keys = new HashMap<Integer, Integer>();

		keys.put(GLFW_KEY_1, 0x01);
		keys.put(GLFW_KEY_2, 0x02);
		keys.put(GLFW_KEY_3, 0x03);
		keys.put(GLFW_KEY_4, 0x0C);
		keys.put(GLFW_KEY_Q, 0x04);
		keys.put(GLFW_KEY_W, 0x05);
		keys.put(GLFW_KEY_E, 0x06);
		keys.put(GLFW_KEY_R, 0x0D);
		keys.put(GLFW_KEY_A, 0x07);
		keys.put(GLFW_KEY_S, 0x08);
		keys.put(GLFW_KEY_D, 0x09);
		keys.put(GLFW_KEY_F, 0x0E);
		keys.put(GLFW_KEY_Z, 0x0A);
		keys.put(GLFW_KEY_X, 0x00);
		keys.put(GLFW_KEY_C, 0x0B);
		keys.put(GLFW_KEY_V, 0x0F);
	}


	public void getInput(long window, int key, int scancode, int action, int mods) {
		// store key state
		if (keys.containsKey(key)) {
			latestKey = keys.get(key);

			if (action == GLFW_PRESS)
				states[latestKey] = 0x01;
			else if (action == GLFW_RELEASE)
				states[latestKey] = 0x00;
			
			Chip8.key = states;
		}
		
	}
}
