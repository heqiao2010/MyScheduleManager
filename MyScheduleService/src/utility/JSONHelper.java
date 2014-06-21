package utility;

import java.util.ArrayList;
import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import vo.BackupRecord;
import vo.Category;
import vo.Note;
import vo.Schedule;

public class JSONHelper {
	// JSON字符串转化为对象
	public static List<Schedule> jsonStr2ScheduleList(String scheduleJsonStr,
			int user_id) {
		JSONArray scheduleJsonArray = JSONArray.fromObject(scheduleJsonStr);
		List<Schedule> retList = new ArrayList<Schedule>();
		for (int i = 0; i < scheduleJsonArray.size(); i++) {
			JSONObject jsonObj = (JSONObject) scheduleJsonArray.opt(i);
			Schedule tem = (Schedule) JSONObject
					.toBean(jsonObj, Schedule.class);
			tem.setUser_id(user_id);
			retList.add(tem);
		}
		return retList;
	}

	public static List<Category> jsonStr2CategoryList(String categoryJsonStr,
			int user_id) {
		JSONArray categoryJsonArray = JSONArray.fromObject(categoryJsonStr);
		List<Category> retList = new ArrayList<Category>();
		for (int i = 0; i < categoryJsonArray.size(); i++) {
			JSONObject jsonObj = (JSONObject) categoryJsonArray.opt(i);
			Category tem = (Category) JSONObject
					.toBean(jsonObj, Category.class);
			tem.setUser_id(user_id);
			retList.add(tem);
		}
		return retList;
	}

	public static List<Note> jsonStr2NoteList(String noteJsonStr, int user_id) {
		JSONArray noteJsonArray = JSONArray.fromObject(noteJsonStr);
		List<Note> retList = new ArrayList<Note>();
		for (int i = 0; i < noteJsonArray.size(); i++) {
			JSONObject jsonObj = (JSONObject) noteJsonArray.opt(i);
			Note tem = (Note) JSONObject.toBean(jsonObj, Note.class);
			tem.setUser_id(user_id);
			retList.add(tem);
		}
		return retList;
	}

	// 将对象转化为JSON字符串
	public static JSONArray backupRecord2JSONArray(
			List<BackupRecord> backup_list) {
		// JSONArray.fromObject(backup_list)
		// JSONArray ret = new JSONArray();
		// ret.add(backup_list);
		return JSONArray.fromObject(backup_list);
	}

	public static JSONArray schedule2JSONArray(List<Schedule> schedule_list) {
		// JSONArray ret = new JSONArray();
		// ret.add(schedule_list);
		return JSONArray.fromObject(schedule_list);
	}

	public static JSONArray category2JSONArray(List<Category> category_list) {
		// JSONArray ret = new JSONArray();
		// ret.add(category_list);
		return JSONArray.fromObject(category_list);
	}

	public static JSONArray note2JSONArray(List<Note> note_list) {
		// JSONArray ret = new JSONArray();
		// ret.add(note_list);
		return JSONArray.fromObject(note_list);
	}

	// 测试
	public static void main(String[] args) {
		List<Note> noteList = jsonStr2NoteList(
				"[{\"id\": 1, \"note_text\": 'balaba', "
						+ "\"create_time\":'2014-05-21 18:41:00', "
						+ "\"backup_time\": '2014-05-21 18:41:00'},"
						+ "{\"id\": 2, \"note_text\": 'balaba', "
						+ "\"create_time\":'2014-05-21 18:41:02', "
						+ "\"backup_time\": '2014-05-21 18:41:00'}]", 0);

		String info = noteList.get(0).getBackup_time()
				+ noteList.get(0).getCreate_time()
				+ noteList.get(0).getNote_text();
		System.out.println(noteList.size() + ":" + info);
	}
}
