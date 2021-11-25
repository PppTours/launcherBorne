import java.util.*;

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
	
	/**
	 * Constructor 
	 */
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
	
	/**
	 * Add a category to the array list of categories
	 * @param cat The Category (an enum) we add
	 */
	public void addCategory(Category cat){
		categories.add(cat);
	}
	
	/**
	 * Remove a category of the array list of categories
	 * @param cat The Category (an enum) we remove
	 */
	public void removeCategory(Category cat){
		categories.remove(cat);
	}
	
	//
	//	SETTER
	//
	
	/**
	 * Set the road to an image as a String
	 * @param i
	 */
	public void setImage(String i){
		image = i;
	}
	
	/**
	 * Set the road to a music as a String
	 * @param m
	 */
	public void setMusic(String m){
		music = m;
	}
	
	/**
	 * Set a description of the game
	 * @param desc
	 */
	public void setDescription(String desc){
		description = desc;
	}
	
	//
	//	GETTER
	//
	
	/**
	 * Get the name of the game as a String
	 * @return The name of the game
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Get the name(s) of the creator(s) of the game
	 * @return The name(s) of the creator(s)
	 */
	public String getCreator(){
		return creator;
	}
	
	/**
	 * Get the date of creation of the game
	 * @return The date of creation
	 */
	public Date getCreationDate(){
		return creation_date;
	}
	
	/**
	 * Verify if the game should be hide
	 * @return True if the game have a password, else False
	 */
	public boolean isEasterEgg(){
		boolean is_easter_egg = password!=null;
		if(!is_easter_egg){
			is_easter_egg = password.equals("");
		}
		return is_easter_egg;
	}
	
	/**
	 * Get the road to the image
	 * @return
	 */
	public String getImage(){
		return image;
	}
	
	/**
	 * Get the password
	 * @return the password
	 */
	public String getPassword(){
		return password;
	}
	
	/**
	 * Get the list of categories of the game
	 * @return an ArrayList of Category
	 */
	public ArrayList<Category> getAllCategories(){
		return categories;
	}
	
	/**
	 * Verify if the game have a certain category
	 * @param cat A Category
	 * @return True if the game have the Category cat, else False
	 */
	public boolean haveCategory(Category cat){
		return categories.indexOf(cat) != -1;
	}
	
	/**
	 * Get the road to the executable of the game
	 * @return 
	 */
	public String getExecutable(){
		return executable;
	}
	
	/**
	 * Get the road to the music
	 * @return
	 */
	public String getMusic(){
		return music;
	}

	/**
	 * Get the description of the game
	 * @return
	 */
	public String getDescription(){
		return description;
	}


}