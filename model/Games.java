
public class Games {

	private String creator;
	private Date creation_date;
	private String name;
	private ArrayList<Category> categories;
	private String image;
	private String password;
	private String executable;
	private String music;
	private String description;
	
	public Games(String n, String c, Date cd, String pswd, String exe){
		name = n;
		creator = c;
		creation_date = cd;
		password = pswd;
		executable = exe;
		image = null;
		music = null;
		description = null;
	}
	
	public void addCategory(Category cat){
		categories.add(cat);
	}
	
	public void removeCategory(Category cat){
		categories.remove(cat);
	}
	
	//
	//	SETTER
	//
	
	public void setImage(String i){
		image = i;
	}
	
	public void setMusic(String m){
		music = m;
	}
	
	public void setDescription(String desc){
		description = desc;
	}
	
	//
	//	GETTER
	//
	
	public String getName(){
		return name;
	}
	
	public String getCreator(){
		return creator;
	}
	
	public Date getCreationDate(){
		return creation_date;
	}
	
	public boolean isEasterEgg(){
		boolean is_easter_egg = password!=null;
		if(!is_easter_egg){
			is_easter_egg = password.equals("");
		}
		return is_easter_egg;
	}
	
	public String getImage(){
		return image;
	}
	
	public String getPassword(){
		return password;
	}
	
	public ArrayList<Category> getAllCategory(){
		return categories;
	}
	
	public boolean haveCategory(Category cat){
		return categories.indexOf(cat) != -1;
	}
	
	public String getExecutable(){
		return executable;
	}
	
	public String getMusic(){
		return music;
	}


}