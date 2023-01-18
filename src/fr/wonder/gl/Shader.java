package fr.wonder.gl;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL43.GL_COMPUTE_SHADER;

import java.io.IOException;
import java.io.InputStream;

public class Shader {
	
	public enum ShaderType {
		
		VERTEX  (GL_VERTEX_SHADER  ),
		FRAGMENT(GL_FRAGMENT_SHADER),
		COMPUTE (GL_COMPUTE_SHADER ),
		GEOMETRY(GL_GEOMETRY_SHADER)
		;
		
		private final int glType;
		
		private ShaderType(int glType) {
			this.glType = glType;
		}
		
	}
	
	public final int id;
	
	public Shader(String resourcePath, ShaderType type) {
		int shader = 0;
		
		try (InputStream is = Shader.class.getResourceAsStream(resourcePath)) {
			if(is == null)
				throw new IOException("Shader does not exist: " + resourcePath);
			String source = new String(is.readAllBytes());
			shader = glCreateShader(type.glType);
			
			glShaderSource(shader, source);
			glCompileShader(shader);
			
			if(glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
				throw new IOException("Unable to parse the " + resourcePath + " shader ! " + glGetShaderInfoLog(shader));
			}
			
			this.id = shader;
		} catch (IOException e) {
			throw new RuntimeException("Could not load shader " + resourcePath + " from resources", e);
		}
	}
	
	public void dispose() {
		glDeleteShader(id);
	}
	
}
