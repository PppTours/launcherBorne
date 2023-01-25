package fr.wonder;

import static org.lwjgl.glfw.GLFW.*;

import fr.wonder.gl.GLWindow;

public class Keys {

	public static final int KEY_START = GLFW_KEY_ESCAPE;
	public static final int KEY_LEFT = GLFW_KEY_LEFT;
	public static final int KEY_RIGHT = GLFW_KEY_RIGHT;
	public static final int KEY_DOWN = GLFW_KEY_DOWN;
	public static final int KEY_UP = GLFW_KEY_UP;
	
	public static final int KEY_QUIT1 = GLFW_KEY_Z;
	public static final int KEY_QUIT2 = GLFW_KEY_X;
	
	public static boolean isKeyPressed(int key) {
		return glfwGetKey(GLWindow.getWindowHandle(), key) == GLFW_PRESS;
	}
	
}
