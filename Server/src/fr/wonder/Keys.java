package fr.wonder;

import static org.lwjgl.glfw.GLFW.*;

import fr.wonder.gl.GLWindow;

public class Keys {

	public static final int KEY_START = GLFW_KEY_ESCAPE;
	public static final int KEY_QUIT1 = GLFW_KEY_Z;
	public static final int KEY_QUIT2 = GLFW_KEY_X;
	
	public static final int KEY_LEFT = GLFW_KEY_LEFT;
	public static final int KEY_RIGHT = GLFW_KEY_RIGHT;
	public static final int KEY_DOWN = GLFW_KEY_DOWN;
	public static final int KEY_UP = GLFW_KEY_UP;

	public static final int[] KEY_BUTTONS = {
			GLFW_KEY_R, GLFW_KEY_T, GLFW_KEY_Y,
			GLFW_KEY_F, GLFW_KEY_G, GLFW_KEY_H,
			
			GLFW_KEY_U, GLFW_KEY_I, GLFW_KEY_O,
			GLFW_KEY_J, GLFW_KEY_K, GLFW_KEY_L,
	};
	
	public static final int[] KEY_DIRECTIONS = { KEY_LEFT, KEY_RIGHT, KEY_UP, KEY_DOWN };
	
	public static boolean isKeyPressed(int key) {
		return glfwGetKey(GLWindow.getWindowHandle(), key) == GLFW_PRESS;
	}
	
}
