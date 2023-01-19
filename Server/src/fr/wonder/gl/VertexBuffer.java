package fr.wonder.gl;

import static org.lwjgl.opengl.GL15.*;

import java.nio.ByteBuffer;

public class VertexBuffer {
	
	private final int bufferId;
	
	public VertexBuffer(float... data) {
		this.bufferId = glGenBuffers();
		bind();
		setData(data);
	}
	
	private VertexBuffer(int id) {
		this.bufferId = id;
	}
	
	public static VertexBuffer emptyBuffer() {
		return new VertexBuffer(glGenBuffers());
	}

	/** Buffer must be bound */
	public void setData(float... data) {
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
	}
	
	/** Buffer must be bound */
	public void setData(ByteBuffer data) {
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
	}
	
	/** Buffer must be bound */
	public void updateData(ByteBuffer data) {
		glBufferSubData(GL_ARRAY_BUFFER, 0, data);
	}
	
	/** Buffer must be bound */
	public void readData(ByteBuffer dataStore) {
		glGetBufferSubData(GL_ARRAY_BUFFER, 0, dataStore);
	}
	
	public void bind() {
		glBindBuffer(GL_ARRAY_BUFFER, bufferId);
	}
	
	public static void unbind() {
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	public void dispose() {
		glDeleteBuffers(bufferId);
	}
	
	public int getBufferId() {
		return bufferId;
	}
	
}
