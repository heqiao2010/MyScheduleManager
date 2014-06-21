package vo;

public class User implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5697674130194670629L;
	private int user_id = 0;
	private String name = "";
	private String gender = "male";
	private int age = 22;
	private String username = "";
	private String password = "";
	private String email = "123@qq.com";

	public int getUser_id() {
		return user_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public User(int user_id, String name, String gender, int age,
			String username, String password, String email) {
		super();
		this.user_id = user_id;
		this.name = name;
		this.gender = gender;
		this.age = age;
		this.username = username;
		this.password = password;
		this.email = email;
	}

	public User() {
	}
}
