package fr.wonder;

import fr.wonder.audio.Sound;
import fr.wonder.audio.SoundSource;
import fr.wonder.audio.SourceRingBuffer;

public class Audio {

	public static final Sound SFX_SELECTION = Sound.loadSound("/audio/selection.ogg");
	public static final Sound SFX_TRANSITION = Sound.loadSound("/audio/transition.ogg");
	public static final Sound SFX_GAME_START = Sound.loadSound("/audio/game_start.ogg");
	public static final Sound MUSIC = Sound.loadSound("/audio/music.ogg");
	
	public static final SourceRingBuffer SFX_SOURCES = new SourceRingBuffer(6);
	public static final SoundSource MUSIC_SOURCE = new SoundSource();

}
