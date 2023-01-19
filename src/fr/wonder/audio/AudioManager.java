package fr.wonder.audio;

import static org.lwjgl.openal.AL10.AL_NO_ERROR;
import static org.lwjgl.openal.AL10.alGetError;
import static org.lwjgl.openal.AL10.alGetString;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;

public class AudioManager {
	
	private static long device, context;
	
	public static void createSoundSystem() {
		device = alcOpenDevice((String) null);
		if(device == NULL)
			throw new IllegalStateException("Could not create an audio device");
		ALCCapabilities deviceCaps = ALC.createCapabilities(device);
		if(!deviceCaps.OpenALC10)
			throw new IllegalStateException("The audio device does not support OpenALC10");
		context = alcCreateContext(device, (IntBuffer)null);
		if(!alcMakeContextCurrent(context))
			throw new IllegalStateException("Could not set the AL context");
		AL.createCapabilities(deviceCaps);
		pollALCErrors();
		pollALErrors();
	}
	
	public static void pollALCErrors() {
		int err = alcGetError(device);
		if(err != ALC_NO_ERROR)
			throw new IllegalStateException("An AL error occured: " + alcGetString(device, err));
	}
	
	public static void pollALErrors() {
		int err = alGetError();
		if(err != AL_NO_ERROR)
			throw new IllegalStateException("An AL error occured: " + alGetString(err));
	}
	
	public static void dispose() {
		pollALCErrors();
		pollALErrors();
		alcMakeContextCurrent(NULL);
		AL.setCurrentProcess(null);
		alcDestroyContext(context);
		alcCloseDevice(device);
	}
	
}
