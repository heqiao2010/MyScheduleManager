package vo;

public class BackupRecord implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4465950315459328509L;
	private int user_id = 0;
	private String backup_time = "2014-4-23 19:00";
	private int schedule_count = 0;
	private int note_count = 0;
	private int category_count = 0;

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

	public int getSchedule_count() {
		return schedule_count;
	}

	public void setSchedule_count(int schedule_count) {
		this.schedule_count = schedule_count;
	}

	public int getNote_count() {
		return note_count;
	}

	public void setNote_count(int note_count) {
		this.note_count = note_count;
	}

	public int getCategory_count() {
		return category_count;
	}

	public void setCategory_count(int category_count) {
		this.category_count = category_count;
	}

	public BackupRecord(int user_id, String backup_time, int schedule_count,
			int note_count, int category_count) {
		super();
		this.user_id = user_id;
		this.backup_time = backup_time;
		this.schedule_count = schedule_count;
		this.note_count = note_count;
		this.category_count = category_count;
	}

	public BackupRecord() {

	}
}
