package vo;

public class Schedule implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5776217145904423466L;
	private int _id = 0;
	private int year = 2014;
	private int month = 4;
	private int day = 23;
	private int hour = 19;
	private int minutes = 8;
	private int daysofweek = 0;
	private long alarmtime = 0;
	private int enabled = 0;
	private int vibrate = 0;
	private String message = "";
	private String alert = "";
	private int category = 0;
	private String sort_key = "";
	private int user_id = 0;
	private String backup_time = "2014-4-23 19:00";

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public int getDaysofweek() {
		return daysofweek;
	}

	public void setDaysofweek(int daysofweek) {
		this.daysofweek = daysofweek;
	}

	public long getAlarmtime() {
		return alarmtime;
	}

	public void setAlarmtime(long alarmtime) {
		this.alarmtime = alarmtime;
	}

	public int getEnabled() {
		return enabled;
	}

	public void setEnabled(int enabled) {
		this.enabled = enabled;
	}

	public int getVibrate() {
		return vibrate;
	}

	public void setVibrate(int vibrate) {
		this.vibrate = vibrate;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getAlert() {
		return alert;
	}

	public void setAlert(String alert) {
		this.alert = alert;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public String getSort_key() {
		return sort_key;
	}

	public void setSort_key(String sort_key) {
		this.sort_key = sort_key;
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

	public Schedule(int _id, int year, int month, int day, int hour,
			int minutes, int daysofweek, long alarmtime, int enabled,
			int vibrate, String message, String alert, int category,
			String sort_key, int user_id, String backup_time) {
		super();
		this._id = _id;
		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
		this.minutes = minutes;
		this.daysofweek = daysofweek;
		this.alarmtime = alarmtime;
		this.enabled = enabled;
		this.vibrate = vibrate;
		this.message = message;
		this.alert = alert;
		this.category = category;
		this.sort_key = sort_key;
		this.user_id = user_id;
		this.backup_time = backup_time;
	}

	public Schedule() {
	}
}
