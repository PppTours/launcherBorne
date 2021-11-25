
public enum Category{
	SOLO("solo"),
	COOP("co-op"),
	VERSUS("versus"),
	SHOOT_THEM_UP("shoot them up"),
	RPG("rpg"),
	GESTION("gestion"),
	SIMULATION("simulation"),
	RYTHME("rythme"),
	STRAGEGIE("strategie"),
	PUZZLE("puzzle"),
	BEAT_THEM_UP("beat them up"),
	JEU_DE_COMBAT("jeu de combat");
	
	private String category_name;
	
	private Category(String str){
		category_name = str;
	}
	
	public String getName(){
		return category_name;
	}
}