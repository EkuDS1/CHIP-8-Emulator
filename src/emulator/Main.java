package emulator;

import static org.lwjgl.glfw.GLFW.*;

public class Main {

	Chip8 myChip8;
	Window window;
	Renderer renderer;

	// variables for limiting the loop to 60 emulation cycles per second
	private float delta, accumulator = 0f, interval = 1f /60f;
	private double lastLoopTime;
	
	// number of operations per cycle
	private int numOps = 10;	


	public void run() {
		// our CPU
		myChip8 = new Chip8();

		// our window will handle all input (storing key states with callbacks) and output using GLFW
		window = new Window();

		// initialize and set up OpenGL for rendering
		renderer = new Renderer(window); 

		// opens file chooser and loads game into memory
		myChip8.loadGame();

		// get initial time
		lastLoopTime = glfwGetTime();	

		while(!glfwWindowShouldClose(window.ID)) {

			delta = getDelta();
			accumulator += delta;
			// slows down the loop so that there is a fixed amount of emulation cycles
			while(accumulator >= interval) {
				//System.out.println(accumulator);
				
				// Emulate operations per cycle
				for(int i = 0; i < numOps; i++) {
					myChip8.emulateCycle();
				}

				accumulator -= interval;
				
				// timers count down to zero
				if (myChip8.delay_timer > 0) {
					myChip8.delay_timer--;
				}
				if (myChip8.sound_timer > 0) {
					myChip8.sound_timer--;
				}
			}

			// Update the screen
			renderer.draw();
		}
	}

	public static void main(String[] args) {
		new Main().run();
	}
	
	// gets time elapsed since last loop
	private float getDelta() {
		double time = glfwGetTime();
		float elapsedTime = (float) (time - lastLoopTime);
		lastLoopTime = time;

		return elapsedTime;
	}

}
