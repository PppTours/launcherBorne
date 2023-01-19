package fr.wonder.gl;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.Point;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GLUtils {
	
	/**
	 * @param size the size in bytes of the buffer
	 */
	public static ByteBuffer createBuffer(int size) {
		return ByteBuffer.allocateDirect(size).order(ByteOrder.LITTLE_ENDIAN);
	}
	
	/**
	 * Creates an index buffer containing the indices for n quads.
	 */
	public static IndexBuffer createQuadIndexBuffer(int quadCount) {
		return new IndexBuffer(createQuadIndices(quadCount));
	}

	/**
	 * Creates an the indices needed to draw n quads.
	 * The returned array contains 6 times the number of quads as indices.
	 */
	public static int[] createQuadIndices(int quadCount) {
		int[] indices = new int[quadCount*6];
		for(int i = 0; i < quadCount; i++) {
			// 3-2
			// |/|
			// 0-1
			indices[6*i+0] = 4*i+0; // 0
			indices[6*i+1] = 4*i+1; // 1
			indices[6*i+2] = 4*i+2; // 2
			indices[6*i+3] = 4*i+2; // 2
			indices[6*i+4] = 4*i+3; // 3
			indices[6*i+5] = 4*i+0; // 0
		}
		return indices;
	}
	
	public static int[] createLineIndices(int lineCount) {
		int[] indices = new int[lineCount*2];
		for(int i = 0; i < indices.length; i++)
			indices[i] = i;
		return indices;
	}

	public static float[] createQuadVertices(float minX, float maxX) {
		return new float[] {
				minX, minX,
				maxX, minX,
				maxX, maxX,
				minX, maxX,
		};
	}
	
	public static void enableBlend(boolean enable) {
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		if(enable)
			glEnable(GL_BLEND);
		else
			glDisable(GL_BLEND);
	}
	
	public static void clear() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	public static void dcTriangles(int count) {
		glDrawElements(GL_TRIANGLES, count, GL_UNSIGNED_INT, NULL);
	}

	public static void dcQuads(int quadCount) {
		dcTriangles(6 * quadCount);
	}
	
	public static void dcLines(int lineCount) {
		glDrawElements(GL_LINES, lineCount*2, GL_UNSIGNED_INT, NULL);
	}
	
	public static boolean isKeyPressed(char key) {
		return glfwGetKey(GLWindow.getWindowHandle(), key) == GLFW_PRESS;
	}
	
	public static Point getCursorPosition() {
		double[] x = new double[1];
		double[] y = new double[1];
		glfwGetCursorPos(GLWindow.getWindowHandle(), x, y);
		return new Point((int)x[0], GLWindow.getWinHeight() - (int)y[0]);
	}
	
	public static boolean isButtonPressed(int button) {
		return glfwGetMouseButton(GLWindow.getWindowHandle(), button) == GLFW_PRESS;
	}
	
}
