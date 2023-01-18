package fr.wonder.gl;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;

public class GLWindow {
	
	private static long window;
	private static int winWidth, winHeight;
	
	private static final List<Callback> closeableCallbacks = new ArrayList<>();
	
	private static BiConsumer<Integer, Integer> resizeCallback;
	private static Consumer<Integer> keyCallback;

	public static void createWindow(int width, int height) {
		winWidth = width;
		winHeight = height;

		GLFWErrorCallback.createPrint(System.err).set();
		
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW !");

		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
		glfwWindowHint(GLFW_SAMPLES, 4);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
		glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
		
		window = glfwCreateWindow(width, height, "Shader Display", NULL, NULL);

		if (window == NULL)
			throw new IllegalStateException("Unable to create a window !");

		glfwMakeContextCurrent(window);
		glfwShowWindow(window);
		glfwFocusWindow(window);
		glfwSwapInterval(1);

		GL.createCapabilities();
		
		Callback errorCallback = GLUtil.setupDebugMessageCallback(System.err);
		if(errorCallback != null)
			closeableCallbacks.add(errorCallback);
		
		glViewport(0, 0, width, height);
		glClearColor(0, 0, 0, 1);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		glfwSetWindowSizeCallback(window, (win, w, h) -> {
			glViewport(0, 0, w, h);
			winWidth = w;
			winHeight = h;
			if(resizeCallback != null)
				resizeCallback.accept(w, h);
		});
		
		glfwSetKeyCallback(window, (win, key, scanCode, action, mods) -> {
			if(action == GLFW_PRESS && key == GLFW_KEY_ESCAPE) {
				glfwSetWindowShouldClose(window, true);
			}
			if(action != GLFW_RELEASE)
				keyCallback.accept(key);
		});
	}
	
	public static void setResizeCallback(BiConsumer<Integer, Integer> resizeCallback) {
		GLWindow.resizeCallback = resizeCallback;
	}

	public static void setKeyCallback(Consumer<Integer> keyCallback) {
		GLWindow.keyCallback = keyCallback;
	}
	
	public static long getWindowHandle() {
		return window;
	}
	
	public static int getWinWidth() {
		return winWidth;
	}
	
	public static int getWinHeight() {
		return winHeight;
	}

	public static boolean shouldDispose() {
		return glfwWindowShouldClose(window);
	}
	
	public static void dispose() {
		Callbacks.glfwFreeCallbacks(window);
		glfwSetErrorCallback(null).free();
		for(Callback callback : closeableCallbacks)
			callback.free();
		GL.setCapabilities(null);
		GL.destroy();
		glfwDestroyWindow(window);
		glfwTerminate();
		window = 0;
	}
	
	public static void setWindowTitle(String title) {
		glfwSetWindowTitle(window, title);
	}
	
	public static void resizeWindow(int width, int height) {
		winWidth = width <= 0 ? winWidth : width;
		winHeight = height <= 0 ? winHeight : height;
		glfwSetWindowSize(window, winWidth, winHeight);
	}

	public static void sendFrame() {
		glfwSwapBuffers(window);
		glfwPollEvents();
	}
	
}
