package fr.wonder.gl;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL40.*;

import java.util.HashMap;
import java.util.Map;

public class ShaderProgram {

	public final int id;
	
	private Map<String, Integer> uniformHash = new HashMap<String, Integer>();
	
	public ShaderProgram(Shader... shaders) {
		int id = glCreateProgram();
		
		for(Shader s : shaders)
			glAttachShader(id, s.id);
		
		glLinkProgram(id);
		glValidateProgram(id);
		
		if(glGetProgrami(id, GL_VALIDATE_STATUS) == GL_FALSE) {
			throw new IllegalStateException("Unable to validate a shader program ! " + glGetProgramInfoLog(id));
		}
		
		this.id = id;
	}
	
	public ShaderProgram(boolean disposeShaders, Shader... shaders) {
		this(shaders);
		if(disposeShaders) {
			for(Shader s : shaders)
				s.dispose();
		}
	}
	
	public void bind() {
		glUseProgram(id);
	}
	
	public static void unbind() {
		glUseProgram(0);
	}

	public void setUniform1i(String name, int i) {
		glUniform1i(getUniformLoc(name), i);
	}
	public ShaderProgram setUniform1f(String name, float f) {
		glUniform1f(getUniformLoc(name), f); return this;
	}
	public void setUniform2f(String name, float f1, float f2) {
		glUniform2f(getUniformLoc(name), f1, f2);
	}
	public void setUniform4f(String name, float f1, float f2, float f3, float f4) {
		glUniform4f(getUniformLoc(name), f1, f2, f3, f4);
	}
	public void setUniformMat4f(String name, float[] m) {
		glUniformMatrix4fv(getUniformLoc(name), true, m);
	}
	
	private int getUniformLoc(String name) {
		int loc;
		
		if(uniformHash.containsKey(name))
			loc = uniformHash.get(name);
		else {
			loc = glGetUniformLocation(id, name);
			uniformHash.put(name, loc);
			
			if(loc == -1)
				System.err.println("A uniform does not exist in a shader ! " + name);
		}
		
		return loc;
	}
	
}
