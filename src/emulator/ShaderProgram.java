package emulator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL33.*;

public class ShaderProgram {
	public int ID;
	
	public ShaderProgram(String vShaderPath, String fShaderPath) throws IOException {
		String vertexSource = new String(Files.readAllBytes(Paths.get(vShaderPath)));
		String fragmentSource = new String(Files.readAllBytes(Paths.get(fShaderPath)));
		
		int vertexShader = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertexShader, vertexSource);
		glCompileShader(vertexShader);
		
		if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) != GL_TRUE) {
		    throw new RuntimeException(glGetShaderInfoLog(vertexShader));
		}
		// Initializing fragment shader
		int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragmentShader, fragmentSource);
		glCompileShader(fragmentShader);
		
		if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) != GL_TRUE) {
		    throw new RuntimeException(glGetShaderInfoLog(fragmentShader));
		}
		// Initializing shader program
		ID = glCreateProgram();
		glAttachShader(ID, vertexShader);
		glAttachShader(ID, fragmentShader);
		glLinkProgram(ID);
		
		if(glGetProgrami(ID, GL_LINK_STATUS) != GL_TRUE) {
			throw new RuntimeException(glGetProgramInfoLog(ID));
		}
		glDeleteShader(vertexShader);
		glDeleteShader(fragmentShader);
	}
	
	public void setUniformf(String name, float val) {	// utility function
		glUniform1f(glGetUniformLocation(ID, name), val);
	}
	
	public void setUniformi(String name, int val) {	// utility function
		glUniform1i(glGetUniformLocation(ID, name), val);
	}
	
	public void use() {
		glUseProgram(ID);
	}
}
