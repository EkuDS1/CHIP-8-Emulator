package emulator;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Window {
	public long ID;
	public InputHandler inputHandler;

	public Window () {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		//glfwWindowHint(GLFW_FLOATING, GLFW_TRUE); // FOR DEBUGGING
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

		// Create the window
		ID = glfwCreateWindow(640, 320, "CHIP-8 Emulator", NULL, NULL);
		if ( ID == NULL )
			throw new RuntimeException("Failed to create the GLFW window");
		
		// Initialize input handler
		inputHandler = new InputHandler();
		
		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(ID, (window, key, scancode, action, mods) -> {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
			
			if(action != GLFW_REPEAT)
				inputHandler.getInput(window, key, scancode, action, mods);

		});

		// Get the thread stack and push a new frame
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(ID, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
					ID,
					(vidmode.width() - pWidth.get(0)) / 2,
					(vidmode.height() - pHeight.get(0)) / 2
					);
		} // the stack frame is popped automatically

		// Keep the aspect ratio constant to 2:1
		glfwSetWindowAspectRatio(ID, 2, 1);

		// Make the OpenGL context current
		glfwMakeContextCurrent(ID);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(ID);

		// ESSENTIAL for making sure that we can use gl functions
		GL.createCapabilities();

		// Resizes viewport when we resize the window
		glfwSetFramebufferSizeCallback(ID, (ID, width, height) -> {
			glViewport(0, 0, width, height);
		});
	}

	public void close() {
		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(ID);
		glfwDestroyWindow(ID);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}
	public void swapBuffers() {
		glfwSwapBuffers(ID);
	}
	public void pollEvents() {
		glfwPollEvents();
	}
}
