package fr.wonder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import fr.wonder.gl.Texture;

public class GameInfoParser {
	
	private static final Logger logger = new Logger("parser");
	
	public static List<GameInfo> parseGameInfos() {
		Gson gb = buildGson();
		List<GameInfo> games = new ArrayList<>();
		
		logger.log("Reading game infos");
		
		for(File f : Launcher.GAMES_DIR.listFiles()) {
			try (FileReader fr = new FileReader(new File(f, "meta/game.json"))) {
				GameInfo info = gb.fromJson(fr, GameInfo.class);
				computeGeneratedInfos(f, info);
				games.add(info);
			} catch (IOException e) {
				logger.log("$rCould not read game $w%s$r info: %s", f.getName(), e);
			}
		}
		
		return games;
	}
	
	private static void computeGeneratedInfos(File gameFile, GameInfo info) throws IOException {
		info.gameDirectory = new File(gameFile, "game");
		info.metaDirectory = new File(gameFile, "meta");
		
		// if the first launch argument is an exe file or any file in the /game directory
		// append the directory relative path to it, java expects paths relative to the
		// current working directory when calling runtime#exec()
		if(Arrays.asList(info.gameDirectory.list()).contains(info.launchArgs[0]))
			info.launchArgs[0] = new File(info.gameDirectory, info.launchArgs[0]).getPath();

		// load the vignette
		if(info.vignette != null)
			info.vignetteTexture = Texture.loadTexture(new File(info.metaDirectory, info.vignette));
	}
	
	private static Gson buildGson() {
		GsonBuilder gb = new GsonBuilder();
		// "a_json_member_name"
		gb.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
		// adapt strings to File objects (no longer used)
		gb.registerTypeAdapter(File.class, new TypeAdapter<File>() {
			@Override
			public void write(JsonWriter out, File value) throws IOException {
				out.value(value.getCanonicalPath());
			}
			@Override
			public File read(JsonReader in) throws IOException {
				return new File(in.nextString());
			}
		});
		// adapt lower case strings to enums
		gb.registerTypeAdapterFactory(new TypeAdapterFactory() {
			@Override
			public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
				Class<?> raw = type.getRawType();
				if(raw.isEnum()) {
					return new TypeAdapter<T>() {
						@Override
						public void write(JsonWriter out, T value) throws IOException {
							out.value(value.toString().toLowerCase());
						}
						@SuppressWarnings({ "rawtypes", "unchecked" })
						@Override
						public T read(JsonReader in) throws IOException {
							return (T) Enum.valueOf((Class)raw, in.nextString().toUpperCase());
						}
					};
				}
				return null;
			}
		});
		
		return gb.create();
	}

}
