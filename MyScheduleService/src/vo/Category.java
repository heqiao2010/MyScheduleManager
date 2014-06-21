package vo;

public class Category implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -276944948501871757L;
	private int _id = 0;
	private String category_name = "";
	private int priority_level = 0;
	private int user_id = 0;
	private String backup_time = "2014-4-23 19:00";

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String getCategory_name() {
		return category_name;
	}

	public void setCategory_name(String category_name) {
		this.category_name = category_name;
	}

	public int getPriority_level() {
		return priority_level;
	}

	public void setPriority_level(int priority_level) {
		this.priority_level = priority_level;
	}

	public int getUser_id() {
		return user_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}

	public String getBackup_time() {
		return backup_time;
	}

	public void setBackup_time(String backup_time) {
		this.backup_time = backup_time;
	}

	public Category(int _id, String category_name, int priority_level,
			int user_id, String backup_time) {
		super();
		this._id = _id;
		this.category_name = category_name;
		this.priority_level = priority_level;
		this.user_id = user_id;
		this.backup_time = backup_time;
	}

	public Category() {

	}
}
