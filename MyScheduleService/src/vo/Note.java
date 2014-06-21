package vo;

public class Note implements java.io.Serializable{
	private static final long serialVersionUID = -5120486695962913889L;
	private int _id = 0;
	private String note_text = "";
	private String create_time = "2014-4-23 19:00";
	private int user_id = 0;
	private String backup_time = "2014-4-23 19:00";
	//get方法
	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String getNote_text() {
		return note_text;
	}

	public void setNote_text(String note_text) {
		this.note_text = note_text;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
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

	public Note(int _id, String note_text, String create_time, int user_id,
			String backup_time) {
		super();
		this._id = _id;
		this.note_text = note_text;
		this.create_time = create_time;
		this.user_id = user_id;
		this.backup_time = backup_time;
	}

	public Note() {
	}
}
