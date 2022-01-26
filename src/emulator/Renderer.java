package emulator;

import org.lwjgl.system.*;

import java.io.IOException;
import java.nio.*;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBImage.*;

public class Renderer {

	// The window handle
	private Window window;
	// The shader program we will use to draw
	private ShaderProgram shaderProgram;
	// Our vertex array object
	private int vao;
	// Texture currently being used for 2D Rendering
	private int texture;

	int[] image = new int[64 * 32];	// this will be used to create our texture

	public Renderer(Window win) {
		window = win;

		// Set the clear color
		glClearColor(0.2f, 0.3f, 0.3f, 1.0f);

		// Initialize shaders and shader program
		try {
			shaderProgram = new ShaderProgram("shaders/vertexShader.vs", "shaders/fragmentShader.fs");
		} catch (IOException e) {
			System.err.println("ShaderProgram failed to initialize!");
			e.printStackTrace();
		}
		// Initialize vertex array object
		init_VAO();


		stbi_set_flip_vertically_on_load(true); 
		// generate texture
		texture = generateTexture(GL_TEXTURE0);

		shaderProgram.use();
		shaderProgram.setUniformi("texture", 0);

	}

	public void draw() {
		// Updating texture
		if(Chip8.drawFlag) {
			updateTexture();
		}
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

		// Rendering
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, texture);

		glBindVertexArray(vao);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

		window.swapBuffers();
		window.pollEvents();
	}

	private void init_VAO() {
		// Initializing vertex array object
		vao = glGenVertexArrays();
		glBindVertexArray(vao);
		// Initializing vertex buffer object
		float vertices[] = {
				// positions          // colors          // texture coords
				1.0f,  1.0f, 0.0f,   1.0f, 0.0f, 0.0f,  1.0f, 1.0f,	// top right
				1.0f, -1.0f, 0.0f,   0.0f, 1.0f, 0.0f,  1.0f, 0.0f,	// bottom right
				-1.0f, -1.0f, 0.0f,   0.0f, 0.0f, 1.0f,  0.0f, 0.0f,	// bottom left
				-1.0f,  1.0f, 0.0f,   1.0f, 1.0f, 0.0f,  0.0f, 1.0f		// top left 
		};

		try (MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer vertexBuffer = stack.mallocFloat(vertices.length);
			vertexBuffer.put(vertices).flip();

			int vbo = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, vbo);
			glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW);
		}
		// Initializing element buffer object
		int indices[] = {
				0, 1, 3,   // first triangle
				1, 2, 3    // second triangle
		};

		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer indexBuffer = stack.mallocInt(2 * 3);
			indexBuffer.put(indices).flip();

			int ebo = glGenBuffers();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_DYNAMIC_DRAW);
		}


		// Linking vertex data to attributes
		int floatSize = 4;
		// position attributes
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * floatSize, 0);
		glEnableVertexAttribArray(0);
		// color attributes
		glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * floatSize, 3 * floatSize);
		glEnableVertexAttribArray(1);
		// texture attributes
		glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * floatSize, 6 * floatSize);
		glEnableVertexAttribArray(2);

		glBindVertexArray(0);
	}

	private int generateTexture(int textureunit) {
		int texture;

		// create Texture object
		texture = glGenTextures();
		glActiveTexture(textureunit);
		glBindTexture(GL_TEXTURE_2D, texture);
		// set the texture wrapping/filtering options (on the currently bound texture object)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

		// generate texture from image
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 64, 32, 0, GL_RGBA, GL_UNSIGNED_BYTE, Chip8.gfx);
		//		glGenerateMipmap(GL_TEXTURE_2D);
		//		stbi_image_free(image);

		return texture;
	}

	private void updateTexture() {
		Chip8.drawFlag = false;

		glBindTexture(GL_TEXTURE_2D, texture);
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 64, 32, GL_RGBA, GL_UNSIGNED_BYTE, Chip8.gfx);
	}

}