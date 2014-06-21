package database;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import vo.*;

public class DBHelper {
	public java.sql.Connection myConnection = null;

	/**
	 * DBHelper构造函数
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public DBHelper() throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		myConnection = DriverManager.getConnection(DBInfo.linkStr);
	}

	/**
	 * 验证用户名和密码
	 * 
	 * @param username
	 * @param password
	 * @return -1，验证失败，否则为user_id
	 * @throws SQLException
	 */
	public int validate_user(String username, String password)
			throws SQLException {
		Statement sqlStatement = myConnection.createStatement();
		String query = "select user_id from MySchedule.users where username = '"
				+ username + "' and password='" + password + "';";
		ResultSet sqlResult = sqlStatement.executeQuery(query);
		int user_id = -1;
		if (sqlResult.next()) {
			user_id = sqlResult.getInt("user_id");
		}
		// myConnection.commit();
		sqlResult.close();
		sqlStatement.close();
		return user_id;
	}

	/**
	 * 检查用户名是否存在
	 * 
	 * @param username
	 * @return -1，不存在，否则存在
	 * @throws SQLException
	 */
	public int check_username_exists(String username) throws SQLException {
		Statement sqlStatement = myConnection.createStatement();
		String query = "select user_id from MySchedule.users where username = '"
				+ username + "';";
		ResultSet sqlResult = sqlStatement.executeQuery(query);
		int user_id = -1;
		if (sqlResult.next()) {
			user_id = sqlResult.getInt("user_id");
		}
		// myConnection.commit();
		sqlResult.close();
		sqlStatement.close();
		return user_id;
	}

	/**
	 * 获取备份列表
	 * 
	 * @param user_id
	 * @return List<BackupRecord>
	 * @throws SQLException
	 */
	public List<BackupRecord> get_backup_list(int user_id) throws SQLException {
		Statement sqlStatement = myConnection.createStatement();
		String query = "select backup_time, schedule_count, note_count, category_count "
				+ " from MySchedule.backup_records where user_id ="
				+ user_id + " order by backup_time desc;";
		ResultSet sqlResult = sqlStatement.executeQuery(query);
		List<BackupRecord> retList = new ArrayList<BackupRecord>();
		while (sqlResult.next()) {
			BackupRecord backupRecord = new BackupRecord(user_id,
					sqlResult.getString("backup_time"),
					sqlResult.getInt("schedule_count"),
					sqlResult.getInt("note_count"),
					sqlResult.getInt("category_count"));
			retList.add(backupRecord);
		}
		// myConnection.commit();
		sqlResult.close();
		sqlStatement.close();
		return retList;
	}

	/**
	 * 插入多个User对象到表中
	 * 
	 * @param users
	 * @return
	 * @throws SQLException
	 */
	public int insert_users(List<User> users) throws SQLException {
		if (null == users || users.size() == 0) {
			return 0;
		}
		Statement sqlStatement = myConnection.createStatement();
		StringBuilder insert_sql_str = new StringBuilder(
				"insert into MySchedule.users "
						+ "(name, gender, age, username, password, email) "
						+ " values "); // + "(%s, %s, %s, %s, %s, %s) ",
										// users_list;
		for (User u : users) {
			insert_sql_str.append("('" + u.getName() + "','" + u.getGender()
					+ "'," + u.getAge() + ",'" + u.getUsername() + "','"
					+ u.getPassword() + "','" + u.getEmail() + "'),");
		}
		insert_sql_str.setCharAt(insert_sql_str.length() - 1, ';');
		System.out.println("[INFO] insert sql: " + insert_sql_str.toString());
		int count = sqlStatement.executeUpdate(insert_sql_str.toString());
		// myConnection.commit();
		sqlStatement.close();
		return count;
	}

	/**
	 * 插入多个BackupRecord对象到表中
	 * 
	 * @param backup_records
	 * @return
	 * @throws SQLException
	 */
	public int insert_backup_records(List<BackupRecord> backup_records)
			throws SQLException {
		if (null == backup_records || backup_records.size() == 0) {
			return 0;
		}
		Statement sqlStatement = myConnection.createStatement();
		StringBuilder insert_sql_str = new StringBuilder(
				"insert into MySchedule.backup_records "
						+ "(user_id, backup_time, schedule_count, note_count, category_count) "
						+ " values ");
		for (BackupRecord b : backup_records) {
			insert_sql_str.append("(" + b.getUser_id() + ",'"
					+ b.getBackup_time() + "'," + b.getSchedule_count() + ","
					+ b.getNote_count() + "," + b.getCategory_count() + "),");
		}
		insert_sql_str.setCharAt(insert_sql_str.length() - 1, ';');
		System.out.println("[INFO] insert sql: " + insert_sql_str.toString());
		int count = sqlStatement.executeUpdate(insert_sql_str.toString());
		// myConnection.commit();
		sqlStatement.close();
		return count;
	}

	/**
	 * 插入多个Category对象到表中
	 * 
	 * @param categorys
	 * @return
	 * @throws SQLException
	 */
	public int insert_categorys(List<Category> categorys) throws SQLException {
		if (null == categorys || categorys.size() == 0) {
			return 0;
		}
		Statement sqlStatement = myConnection.createStatement();
		StringBuilder insert_sql_str = new StringBuilder(
				"insert into MySchedule.categorys "
						+ "(_id, category_name, priority_level, user_id, backup_time)"
						+ " values ");
		for (Category c : categorys) {
			insert_sql_str.append("(" + c.get_id() + ",'"
					+ c.getCategory_name() + "'," + c.getPriority_level() + ","
					+ c.getUser_id() + ",'" + c.getBackup_time() + "'),");
		}
		insert_sql_str.setCharAt(insert_sql_str.length() - 1, ';');
		System.out.println("[INFO] insert sql: " + insert_sql_str.toString());
		int count = sqlStatement.executeUpdate(insert_sql_str.toString());
		// myConnection.commit();
		sqlStatement.close();
		return count;
	}

	/**
	 * 插入多个Note对象到表中
	 * 
	 * @param notes
	 * @return 插入条数 
	 * @throws SQLException
	 */
	public int insert_notes(List<Note> notes) throws SQLException {
		if (null == notes || notes.size() == 0) {
			return 0;
		}
		Statement sqlStatement = myConnection.createStatement();
		StringBuilder insert_sql_str = new StringBuilder(
				"insert into MySchedule.notes "
						+ "(_id, note_text, create_time, user_id, backup_time)"
						+ " values ");
		for (Note n : notes) {
			insert_sql_str.append("(" + n.get_id() + ",'" + n.getNote_text()
					+ "','" + n.getCreate_time() + "'," + n.getUser_id() + ",'"
					+ n.getBackup_time() + "'),");
		}
		insert_sql_str.setCharAt(insert_sql_str.length() - 1, ';');
		System.out.println("[INFO] insert sql: " + insert_sql_str.toString());
		int count = sqlStatement.executeUpdate(insert_sql_str.toString());
		// myConnection.commit();
		sqlStatement.close();
		return count;
	}

	/**
	 * 插入多个Schedule对象到表中
	 * 
	 * @param schedules
	 * @return
	 * @throws SQLException
	 */
	public int insert_schedules(List<Schedule> schedules) throws SQLException {
		if (null == schedules || schedules.size() == 0) {
			return 0;
		}
		Statement sqlStatement = myConnection.createStatement();
		StringBuilder insert_sql_str = new StringBuilder(
				"insert into MySchedule.schedules "
						+ "(_id, year, month, day, hour, minutes, daysofweek, alarmtime, "
						+ "enabled, vibrate, message, alert, category, sort_key, "
						+ "user_id, backup_time)" + " values ");
		for (Schedule s : schedules) {
			insert_sql_str.append("(" + s.get_id() + "," + s.getYear() + ","
					+ s.getMonth() + "," + s.getDay() + "," + s.getHour() + ","
					+ s.getMinutes() + "," + s.getDaysofweek() + ","
					+ s.getAlarmtime() + "," + s.getEnabled() + ","
					+ s.getVibrate() + ",'" + s.getMessage() + "','"
					+ s.getAlert() + "'," + s.getCategory() + ",'"
					+ s.getSort_key() + "'," + s.getUser_id() + ",'"
					+ s.getBackup_time() + "'),");
		}
		insert_sql_str.setCharAt(insert_sql_str.length() - 1, ';');
		System.out.println("[INFO] insert sql: " + insert_sql_str.toString());
		int count = sqlStatement.executeUpdate(insert_sql_str.toString());
		// myConnection.commit();
		sqlStatement.close();
		return count;
	}

	/**
	 * 查询Schedule表，返回Schedule对象
	 * 
	 * @param user_id
	 * @param backup_time
	 * @return
	 * @throws SQLException
	 */
	public ArrayList<Schedule> get_schedule_info(int user_id, String backup_time)
			throws SQLException {
		Statement sqlStatement = myConnection.createStatement();
		String query = "select _id, year, month, day, hour, minutes, daysofweek, alarmtime, "
				+ "enabled, vibrate, message, alert, category, sort_key "
				+ "from MySchedule.schedules "
				+ "where user_id="
				+ user_id
				+ " and backup_time='" + backup_time + "';";
		ResultSet sqlResult = sqlStatement.executeQuery(query);
		ArrayList<Schedule> retList = new ArrayList<Schedule>();
		while (sqlResult.next()) {
			Schedule schedule = new Schedule(sqlResult.getInt("_id"),
					sqlResult.getInt("year"), sqlResult.getInt("month"),
					sqlResult.getInt("day"), sqlResult.getInt("hour"),
					sqlResult.getInt("minutes"),
					sqlResult.getInt("daysofweek"),
					sqlResult.getLong("alarmtime"),
					sqlResult.getInt("enabled"), sqlResult.getInt("vibrate"),
					sqlResult.getString("message"),
					sqlResult.getString("alert"), sqlResult.getInt("category"),
					sqlResult.getString("sort_key"), user_id, backup_time);
			retList.add(schedule);
		}
		// myConnection.commit();
		sqlResult.close();
		sqlStatement.close();
		return retList;
	}

	/**
	 * 查询Note表，返回Note对象
	 * 
	 * @param user_id
	 * @param backup_time
	 * @return
	 * @throws SQLException
	 */
	public ArrayList<Note> get_note_info(int user_id, String backup_time)
			throws SQLException {
		Statement sqlStatement = myConnection.createStatement();
		String query = "select _id, note_text, create_time "
				+ "from MySchedule.notes " + "where user_id=" + user_id
				+ " and backup_time='" + backup_time + "';";
		ResultSet sqlResult = sqlStatement.executeQuery(query);
		ArrayList<Note> retList = new ArrayList<Note>();
		while (sqlResult.next()) {
			Note note = new Note(sqlResult.getInt("_id"),
					sqlResult.getString("note_text"),
					sqlResult.getString("create_time"), user_id, backup_time);
			retList.add(note);
		}
		// myConnection.commit();
		sqlResult.close();
		sqlStatement.close();
		return retList;
	}

	/**
	 * 查询Category表，返回Category对象
	 * 
	 * @param user_id
	 * @param backup_time
	 * @return
	 * @throws SQLException
	 */
	public ArrayList<Category> get_category_info(int user_id, String backup_time)
			throws SQLException {
		Statement sqlStatement = myConnection.createStatement();
		String query = "select _id, category_name, priority_level "
				+ "from MySchedule.categorys " + "where user_id=" + user_id
				+ " and backup_time='" + backup_time + "';";
		ResultSet sqlResult = sqlStatement.executeQuery(query);
		ArrayList<Category> retList = new ArrayList<Category>();
		while (sqlResult.next()) {
			Category category = new Category(sqlResult.getInt("_id"),
					sqlResult.getString("category_name"),
					sqlResult.getInt("priority_level"), user_id, backup_time);
			retList.add(category);
		}
		// myConnection.commit();
		sqlResult.close();
		sqlStatement.close();
		return retList;
	}

	/**
	 * 关闭数据库连接
	 * 
	 * @throws SQLException
	 */
	public void closeConnection() throws SQLException {
		if (null != myConnection) {
			myConnection.close();
		}
	}

	public static void main(String[] args) {
		try {
			DBHelper mDBHelper = new DBHelper();
			System.out.println(mDBHelper.validate_user("Joel", "he_qiao"));
			System.out.println(mDBHelper.check_username_exists("Joel"));
			List<BackupRecord> backuplist = mDBHelper.get_backup_list(1);
			BackupRecord backupRecord = backuplist.get(0);
			String testStr = backupRecord.getBackup_time() + ","
					+ backupRecord.getCategory_count() + ","
					+ backupRecord.getNote_count() + ","
					+ backupRecord.getSchedule_count() + ","
					+ backupRecord.getUser_id();
			System.out.println("lenght:" + backuplist.size() + "\ntest str:"
					+ testStr);

			List<User> users = new ArrayList<User>();
			User user1 = new User(0, "John Herb", "male", 22, "John", "123",
					"1234@qq.com");
			User user2 = new User(2, "Joy Herb", "male", 22, "Joy", "1234",
					"1234567@qq.com");
			users.add(user1);
			users.add(user2);
			// mDBHelper.insert_users(users);

			List<BackupRecord> backup_records = new ArrayList<BackupRecord>();
			BackupRecord backupRecord1 = new BackupRecord(1,
					"2014-05-21 18:41", 0, 0, 0);
			BackupRecord backupRecord2 = new BackupRecord(1,
					"2014-05-21 18:42", 0, 0, 0);
			backup_records.add(backupRecord1);
			backup_records.add(backupRecord2);
			// mDBHelper.insert_backup_records(backup_records);

			List<Category> categorys = new ArrayList<Category>();
			Category category1 = new Category(0, "default", 0, 1,
					"2014-05-21 18:41");
			Category category2 = new Category(0, "default", 0, 1,
					"2014-05-21 18:42");
			categorys.add(category1);
			categorys.add(category2);
			mDBHelper.insert_categorys(categorys);

			List<Note> notes = new ArrayList<Note>();
			Note note1 = new Note(0, "balabala", "2014-05-21 18:41", 1,
					"2014-05-21 18:41");
			Note note2 = new Note(1, "balabala", "2014-05-21 18:42", 1,
					"2014-05-21 18:42");
			notes.add(note1);
			notes.add(note2);
			mDBHelper.insert_notes(notes);

			List<Schedule> schedules = new ArrayList<Schedule>();
			Schedule schedule1 = new Schedule(0, 2014, 5, 21, 18, 53, 0,
					140000000, 0, 1, "message", "alert", 0, "a", 1,
					"2014-05-21 18:41");
			Schedule schedule2 = new Schedule(1, 2014, 5, 21, 18, 53, 0,
					140000000, 0, 1, "message", "alert", 0, "a", 1,
					"2014-05-21 18:42");
			schedules.add(schedule1);
			schedules.add(schedule2);
			mDBHelper.insert_schedules(schedules);

			List<Schedule> schedulelist = mDBHelper.get_schedule_info(1,
					"2014-05-21 18:42");
			Schedule schedule = schedulelist.get(0);
			testStr = schedule.getBackup_time() + "," + schedule.get_id() + ","
					+ schedule.getAlarmtime() + "," + schedule.getMessage()
					+ "," + schedule.getDaysofweek() + ","
					+ schedule.getUser_id();
			System.out.println("lenght:" + schedulelist.size() + "\ntest str:"
					+ testStr);
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | SQLException e) {
			System.out.println("exception:" + e.toString());
			e.printStackTrace();
		}
	}
}
