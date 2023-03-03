package fr.wonder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import fr.wonder.GameInfo.Highscore;
import fr.wonder.commons.files.FilesUtils;
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
		
		loadPlaytimes(games);
		
		return games;
	}
	
	private static void computeGeneratedInfos(File gameFile, GameInfo info) throws IOException {
		info.gameDirectory = new File(gameFile, "game");
		info.metaDirectory = new File(gameFile, "meta");
		
		// if the first launch argument is an exe file or any file in the /game directory
		// append the directory relative path to it, java expects paths relative to the
		// current working directory when calling runtime#exec()
		if(Arrays.asList(info.gameDirectory.list()).contains(info.runCommand[0]))
			info.runCommand[0] = new File(info.gameDirectory, info.runCommand[0]).getPath();
		Arrays.parallelSort(info.tags, (t1,t2) -> t1.name().compareTo(t2.name()));

		// load images
		info.vignette = loadOptionalTexture(info, "vignette.png");
		info.cartridge = loadOptionalTexture(info, "cartridge.png");
		info.controls = loadOptionalTexture(info, "controls.png");
		reloadHighscores(info);
	}
	
	private static Texture loadOptionalTexture(GameInfo game, String fileName) throws IOException {
		File imageFile = new File(game.metaDirectory, fileName);
		return imageFile.exists() ? Texture.loadTexture(imageFile) : null;
	}
	
	public static void reloadHighscores(GameInfo info) {
		info.highscores = null;
		try {
			info.highscores = loadHighscores(info);
		} catch (IOException e) {
			logger.log("$rCould not load highscores: %s", e);
		}
	}
	
	private static Highscore[] loadHighscores(GameInfo game) throws IOException {
		File scoreFile = new File(game.gameDirectory, "scores.txt");
		if(!scoreFile.exists())
			return null;
		String content = FilesUtils.read(scoreFile);
		
		List<Highscore> scores = new ArrayList<>();
		for(String line : content.split("\n")) {
			if(line.startsWith("#") || line.isBlank())
				continue;
			scores.add(parseScoreLine(line));
		}
		
		Comparator<Highscore> score = Comparator.comparingInt(h->h.score);
		Comparator<Highscore> date = Comparator.comparing(h->h.date);
		scores.removeIf(s -> s.score <= 0);
		scores.sort(score.reversed().thenComparing(date));
		
		return scores.toArray(Highscore[]::new);
	}
	
	private static Highscore parseScoreLine(String line) throws IOException {
		String[] parts = line.replaceAll("[^a-zA-Z ,\\-0-9;]", "").split(";");
		if(parts.length < 2 || parts.length > 3)
			throw new IOException("Invalid score line '" + line + "'");
		String name = parts[0];
		int score = Integer.parseInt(parts[1]);
		String date = parts.length > 2 ? parts[2] : "";
		return new Highscore(name, score, date);
	}
	
	public static void loadPlaytimes(List<GameInfo> infos) {
		try {
			String[] lines = FilesUtils.read(Launcher.TIMESTAMPS_FILE).split("\n");
			Map<String, Integer> playtimes = new HashMap<>();
			for(String l : lines) {
				String[] parts = l.split(";");
				int playtime = Integer.parseInt(parts[1]);
				String gameName = parts[2];
				playtimes.compute(gameName, (_k, v) -> v==null ? playtime : v+playtime);
			}
			for(GameInfo i : infos) {
				i.totalPlaytime = playtimes.getOrDefault(i.title, 0);
			}
		} catch (Throwable t) { // catch all exceptions, an error here must not crash anything
			logger.log("$rCould not read playtime: %s", t.getMessage());
		}
	}

	public static void writePlaytime(GameInfo game, int playTime) {
		try (OutputStream os = new FileOutputStream(Launcher.TIMESTAMPS_FILE, true)) {
			LocalDate today = LocalDate.now();
			os.write(String.format("%02d-%02d-%04d;%d;%s\n",
					today.getDayOfMonth(), today.getMonthValue(), today.getYear(), playTime, game.title).getBytes());
		} catch (IOException e) {
			logger.log("$rCould not write playtime: %s", e.getMessage());
		}
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
				if(!raw.isEnum())
					return null;
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
		});
		
		return gb.create();
	}

}
