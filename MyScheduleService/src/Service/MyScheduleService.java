package Service;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import utility.JSONHelper;
import vo.BackupRecord;
import vo.Category;
import vo.Note;
import vo.Schedule;
import vo.User;
import database.DBHelper;
import encryption.RSA;
import encryption.ServerEncryption;

public class MyScheduleService {
	private DBHelper mDBHelper = null;
	private ServerEncryption mServerEncryption = null;
	private static String DBERROR = "DB Error.";
	private static String UEREXISTS = "User Exists.";
	private static String SUCCEED = "Succeeded";
	private static String FAILED = "Failed";
	private static String DATAEMPTY = "Empty";
	private static String JSONERROR = "JSON Error";
	private static String DESKEYERROR = "DES Key Error";
	private static String ENCRYPTIONERROR = "encryption Error";

	public MyScheduleService() {
		try {
			mDBHelper = new DBHelper();
			mServerEncryption = new ServerEncryption();
			System.out.println("\nServerEncryption 对象构造成功.\n");
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | SQLException e) {
			System.out.println(DBERROR + ": " + e.toString());
			mDBHelper = null;
			e.printStackTrace();
		}
	}

	// private String decryption(String ctext, String c_deskeyStr)
	// throws NumberFormatException {
	// BigInteger c_deskey = new BigInteger(c_deskeyStr);
	// return mServerEncryption.decrypt(ctext, c_deskey);
	// }

	/**
	 * 注册新用户
	 * 
	 * @param c_deskey
	 * @param username
	 * @param name
	 * @param gendar
	 * @param age
	 * @param email
	 * @param password
	 * @return 注册结果
	 */
	public String register(String c_deskeyStr, String username, String name,
			String gender, String age, String email, String password) {
		JSONObject ret = new JSONObject();

		try {
			if (null == mServerEncryption) {
				ret.accumulate("register", ENCRYPTIONERROR);
				return ret.toString();
			} else if (null == mDBHelper) {
				ret.accumulate("register", DBERROR);
				return ret.toString();
			} else { // 解密
				System.out.println("\n\n注册新用户:");
				System.out.println("c_deskey:" + c_deskeyStr);
				System.out.println("解密前的"
						+ "username, name, gender, age, email, password:"
						+ username + "," + name + "," + gender + "," + age
						+ "," + email + "," + password);
				BigInteger c_deskey = new BigInteger(c_deskeyStr); // 获取加密的DES密钥
				username = mServerEncryption.decrypt(username, c_deskey);
				name = mServerEncryption.decrypt(name, c_deskey);
				gender = mServerEncryption.decrypt(gender, c_deskey);
				age = mServerEncryption.decrypt(age, c_deskey);
				email = mServerEncryption.decrypt(email, c_deskey);
				password = mServerEncryption.decrypt(password, c_deskey);
				System.out.println("解密后的"
						+ "username, name, gender, age, email, password:"
						+ username + "," + name + "," + gender + "," + age
						+ "," + email + "," + password);
			}
			int user_id = mDBHelper.check_username_exists(username);
			if (-1 != user_id) {
				ret.accumulate("register", UEREXISTS);
				return ret.toString();
			} else {
				User newUser = new User(-1, name, gender, Integer.valueOf(age),
						username, password, email);
				List<User> user_list = new ArrayList<User>();
				user_list.add(newUser);
				int count = mDBHelper.insert_users(user_list);
				if (1 == count) {
					ret.accumulate("register", SUCCEED);
					return ret.toString();
				} else {
					System.out.println("WARNING: insert users: " + count
							+ "when register, username: " + username
							+ " password: " + password);
					ret.accumulate("register", FAILED);
					return ret.toString();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			ret.accumulate("register", DBERROR);
			return ret.toString();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			ret.accumulate("register", DESKEYERROR);
			return ret.toString();
		}
	}

	/**
	 * 验证用户信息
	 * @param c_deskeyStr
	 * @param username
	 * @param password
	 * @return JSON字串
	 */
	public String validate_user(String c_deskeyStr, String username,
			String password) {
		JSONObject ret = new JSONObject();
		try {
			if (null == mDBHelper) {
				ret.accumulate("validate_user", DBERROR);
				return ret.toString();
			} else if (null == mServerEncryption) {
				ret.accumulate("validate_user", ENCRYPTIONERROR);
				return ret.toString();
			} else { // 解密
				System.out.println("\n\n验证用户:");
				System.out.println("c_deskey:" + c_deskeyStr);
				System.out.println("解密前的" + "username, password:" + username
						+ ", " + password);
				BigInteger c_deskey = new BigInteger(c_deskeyStr); // 获取加密的DES密钥
				username = mServerEncryption.decrypt(username, c_deskey);
				password = mServerEncryption.decrypt(password, c_deskey);
				System.out.println("解密后的" + "username, password:" + username
						+ ", " + password);
			}
			int user_id = mDBHelper.validate_user(username, password);
			if (-1 == user_id) {
				ret.accumulate("validate_user", FAILED);
				return ret.toString();
			} else {
				ret.accumulate("validate_user", SUCCEED);
				return ret.toString();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			ret.accumulate("validate_user", DBERROR);
			return ret.toString();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			ret.accumulate("validate_user", DESKEYERROR);
			return ret.toString();
		}
	}

	public String backup_info(String c_deskeyStr, String username,
			String password, String info) {
		JSONObject ret = new JSONObject();
		BackupRecord mBackupRecord = new BackupRecord();
		try {
			if (null == mDBHelper) {
				ret.accumulate("backup_info", DBERROR);
				return ret.toString();
			} else if (null == mServerEncryption) {
				ret.accumulate("backup_info", ENCRYPTIONERROR);
				return ret.toString();
			} else { // 解密
				System.out.println("\n\n备份信息:");
				System.out.println("c_deskey:" + c_deskeyStr);
				System.out.println("解密前的" + "username, password, info:"
						+ username + ", " + password + "\n" + info);
				BigInteger c_deskey = new BigInteger(c_deskeyStr); // 获取加密的DES密钥
				username = mServerEncryption.decrypt(username, c_deskey);
				password = mServerEncryption.decrypt(password, c_deskey);
				info = mServerEncryption.decrypt(info, c_deskey);
				System.out.println("解密后的" + "username, password:" + username
						+ ", " + password + "\n" + info);
			}
			int user_id = mDBHelper.validate_user(username, password);
			if (-1 == user_id) {
				ret.accumulate("backup_info", FAILED);
				return ret.toString();
			} else {
				JSONObject mJSONObject = JSONObject.fromObject(info);
				mBackupRecord.setUser_id(user_id);
				ret.accumulate("user_id", user_id);
				String backup_time = mJSONObject.getString("backup_time");
				String scheduleJsonStr = mJSONObject.getString("schedule");
				List<Schedule> mSchedules = JSONHelper.jsonStr2ScheduleList(
						scheduleJsonStr, user_id);
				if (null != mSchedules) {
					int count = mDBHelper.insert_schedules(mSchedules);
					mBackupRecord.setSchedule_count(count);
					ret.accumulate("schedule_count", count);
				}
				String noteJsonStr = mJSONObject.getString("note");
				List<Note> mNotes = JSONHelper.jsonStr2NoteList(noteJsonStr,
						user_id);
				if (null != mNotes) {
					int count = mDBHelper.insert_notes(mNotes);
					ret.accumulate("note_count", count);
					mBackupRecord.setNote_count(count);
				}
				String categoryJsonStr = mJSONObject.getString("category");
				List<Category> mCategorys = JSONHelper.jsonStr2CategoryList(
						categoryJsonStr, user_id);
				if (null != mCategorys) {
					int count = mDBHelper.insert_categorys(mCategorys);
					ret.accumulate("category_count", count);
					mBackupRecord.setCategory_count(count);
				}
				ret.accumulate("backup_time", backup_time);
				mBackupRecord.setBackup_time(backup_time);
				List<BackupRecord> backup_list = new ArrayList<BackupRecord>();
				backup_list.add(mBackupRecord);
				int count = mDBHelper.insert_backup_records(backup_list);
				if (1 != count) {
					System.out.println("[WARNING]: Insert " + count
							+ "when backup_info.");
				} else {
					System.out.println("[INFO]: backup info: " + backup_time);
				}
				ret.accumulate("backup_info", SUCCEED);
				return ret.toString();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			ret.accumulate("backup_info", DBERROR);
			return ret.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			ret.accumulate("backup_info", JSONERROR);
			return ret.toString();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			ret.accumulate("backup_info", DESKEYERROR);
			return ret.toString();
		}
	}

	public String get_backup_list(String c_deskeyStr, String username,
			String password) {
		JSONObject ret = new JSONObject();
		if (null == mDBHelper) {
			ret.accumulate("get_backup_list", DBERROR);
			return ret.toString();
		} else if (null == mServerEncryption) {
			ret.accumulate("get_backup_list", ENCRYPTIONERROR);
			return ret.toString();
		} else { // 解密
			try {
				System.out.println("\n\n获取备份列表:");
				System.out.println("c_deskey:" + c_deskeyStr);
				System.out.println("解密前的" + "username, password:" + username
						+ ", " + password);
				BigInteger c_deskey = new BigInteger(c_deskeyStr); // 获取加密的DES密钥
				username = mServerEncryption.decrypt(username, c_deskey);
				password = mServerEncryption.decrypt(password, c_deskey);
				System.out.println("解密后的" + "username, password:" + username
						+ ", " + password);
				int user_id = mDBHelper.validate_user(username, password);
				if (-1 == user_id) {
					ret.accumulate("get_backup_list", FAILED);
					return ret.toString();
				} else {
					List<BackupRecord> backup_list = mDBHelper
							.get_backup_list(user_id);
					if (backup_list.size() == 0) {
						ret.accumulate("get_backup_list", DATAEMPTY);
					} else {
						ret.accumulate("get_backup_list", SUCCEED);
						ret.accumulate("backup_list",
								JSONHelper.backupRecord2JSONArray(backup_list));
					}
					return ret.toString();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				ret.accumulate("get_backup_list", DBERROR);
				return ret.toString();
			} catch (NumberFormatException e) {
				e.printStackTrace();
				ret.accumulate("get_backup_list", DESKEYERROR);
				return ret.toString();
			}
		}
	}

	// public List<Schedule> get_schedule_list(String c_deskeyStr,
	// String username, String password, String backup_time) {
	// Schedule errorRecord = new Schedule();
	// List<Schedule> retList = new ArrayList<Schedule>();
	// if (null == mDBHelper) {
	// errorRecord.setBackup_time(DBERROR);
	// retList.add(errorRecord);
	// return retList;
	// } else if (null == mServerEncryption) {
	// errorRecord.setBackup_time(ENCRYPTIONERROR);
	// retList.add(errorRecord);
	// return retList;
	// } else { // 解密
	// try {
	// System.out.println("\n\n获取日程:");
	// System.out.println("c_deskey:" + c_deskeyStr);
	// System.out.println("解密前的" + "username, password, backup_time:"
	// + username + ", " + password + ", " + backup_time);
	// BigInteger c_deskey = new BigInteger(c_deskeyStr); // 获取加密的DES密钥
	// username = mServerEncryption.decrypt(username, c_deskey);
	// password = mServerEncryption.decrypt(password, c_deskey);
	// backup_time = mServerEncryption.decrypt(backup_time, c_deskey);
	// System.out.println("解密后的" + "username, password, backup_time:"
	// + username + ", " + password + ", " + backup_time);
	// int user_id = mDBHelper.validate_user(username, password);
	// if (-1 == user_id) {
	// errorRecord.setBackup_time(FAILED);
	// retList.add(errorRecord);
	// return retList;
	// } else {
	// retList = mDBHelper.get_schedule_info(user_id, backup_time);
	// if (retList.size() == 0) {
	// errorRecord.setBackup_time(DATAEMPTY);
	// retList.add(errorRecord);
	// }
	// return retList;
	// }
	// } catch (SQLException e) {
	// e.printStackTrace();
	// errorRecord.setBackup_time(DBERROR);
	// retList.add(errorRecord);
	// return retList;
	// } catch (NumberFormatException e) {
	// e.printStackTrace();
	// errorRecord.setBackup_time(DESKEYERROR);
	// retList.add(errorRecord);
	// return retList;
	// }
	// }
	// }
	//
	// public List<Note> get_note_list(String c_deskeyStr, String username,
	// String password, String backup_time) {
	// Note errorRecord = new Note();
	// List<Note> retList = new ArrayList<Note>();
	// if (null == mDBHelper) {
	// errorRecord.setBackup_time(DBERROR);
	// retList.add(errorRecord);
	// return retList;
	// } else if (null == mServerEncryption) {
	// errorRecord.setBackup_time(ENCRYPTIONERROR);
	// retList.add(errorRecord);
	// return retList;
	// } else { // 解密
	// try {
	// System.out.println("\n\n获取笔记:");
	// System.out.println("c_deskey:" + c_deskeyStr);
	// System.out.println("解密前的" + "username, password, backup_time:"
	// + username + ", " + password + ", " + backup_time);
	// BigInteger c_deskey = new BigInteger(c_deskeyStr); // 获取加密的DES密钥
	// username = mServerEncryption.decrypt(username, c_deskey);
	// password = mServerEncryption.decrypt(password, c_deskey);
	// backup_time = mServerEncryption.decrypt(backup_time, c_deskey);
	// System.out.println("解密前的" + "username, password, backup_time:"
	// + username + ", " + password + ", " + backup_time);
	// int user_id = mDBHelper.validate_user(username, password);
	// if (-1 == user_id) {
	// errorRecord.setBackup_time(FAILED);
	// retList.add(errorRecord);
	// return retList;
	// } else {
	// retList = mDBHelper.get_note_info(user_id, backup_time);
	// if (retList.size() == 0) {
	// errorRecord.setBackup_time(DATAEMPTY);
	// retList.add(errorRecord);
	// }
	// return retList;
	// }
	// } catch (SQLException e) {
	// e.printStackTrace();
	// errorRecord.setBackup_time(DBERROR);
	// retList.add(errorRecord);
	// return retList;
	// } catch (NumberFormatException e) {
	// e.printStackTrace();
	// errorRecord.setBackup_time(DESKEYERROR);
	// retList.add(errorRecord);
	// return retList;
	// }
	// }
	// }
	//
	// public List<Festival> get_festival_list(String c_deskeyStr,
	// String username, String password, String backup_time) {
	// Festival errorRecord = new Festival();
	// List<Festival> retList = new ArrayList<Festival>();
	// if (null == mDBHelper) {
	// errorRecord.setBackup_time(DBERROR);
	// retList.add(errorRecord);
	// return retList;
	// } else if (null == mServerEncryption) {
	// errorRecord.setBackup_time(ENCRYPTIONERROR);
	// retList.add(errorRecord);
	// return retList;
	// } else { // 解密
	// try {
	// System.out.println("\n\n获取节日:");
	// System.out.println("c_deskey:" + c_deskeyStr);
	// System.out.println("解密前的" + "username, password, backup_time:"
	// + username + ", " + password + ", " + backup_time);
	// BigInteger c_deskey = new BigInteger(c_deskeyStr); // 获取加密的DES密钥
	// username = mServerEncryption.decrypt(username, c_deskey);
	// password = mServerEncryption.decrypt(password, c_deskey);
	// backup_time = mServerEncryption.decrypt(backup_time, c_deskey);
	// System.out.println("解密前的" + "username, password, backup_time:"
	// + username + ", " + password + ", " + backup_time);
	// int user_id = mDBHelper.validate_user(username, password);
	// if (-1 == user_id) {
	// errorRecord.setBackup_time(FAILED);
	// retList.add(errorRecord);
	// return retList;
	// } else {
	// retList = mDBHelper.get_festival_info(user_id, backup_time);
	// if (retList.size() == 0) {
	// errorRecord.setBackup_time(DATAEMPTY);
	// retList.add(errorRecord);
	// }
	// return retList;
	// }
	// } catch (SQLException e) {
	// e.printStackTrace();
	// errorRecord.setBackup_time(DBERROR);
	// retList.add(errorRecord);
	// return retList;
	// } catch (NumberFormatException e) {
	// e.printStackTrace();
	// errorRecord.setBackup_time(DESKEYERROR);
	// retList.add(errorRecord);
	// return retList;
	// }
	// }
	// }

	// public List<Category> get_category_list(String c_deskeyStr,
	// String username, String password, String backup_time) {
	// Category errorRecord = new Category();
	// List<Category> retList = new ArrayList<Category>();
	// if (null == mDBHelper) {
	// errorRecord.setBackup_time(DBERROR);
	// retList.add(errorRecord);
	// return retList;
	// } else if (null == mServerEncryption) {
	// errorRecord.setBackup_time(ENCRYPTIONERROR);
	// retList.add(errorRecord);
	// return retList;
	// } else { // 解密
	// try {
	// System.out.println("\n\n获取日程分类:");
	// System.out.println("c_deskey:" + c_deskeyStr);
	// System.out.println("解密前的" + "username, password, backup_time:"
	// + username + ", " + password + ", " + backup_time);
	// BigInteger c_deskey = new BigInteger(c_deskeyStr); // 获取加密的DES密钥
	// username = mServerEncryption.decrypt(username, c_deskey);
	// password = mServerEncryption.decrypt(password, c_deskey);
	// backup_time = mServerEncryption.decrypt(backup_time, c_deskey);
	// System.out.println("解密后的" + "username, password, backup_time:"
	// + username + ", " + password + ", " + backup_time);
	// int user_id = mDBHelper.validate_user(username, password);
	// if (-1 == user_id) {
	// errorRecord.setBackup_time(FAILED);
	// retList.add(errorRecord);
	// return retList;
	// } else {
	// retList = mDBHelper.get_category_info(user_id, backup_time);
	// if (retList.size() == 0) {
	// errorRecord.setBackup_time(DATAEMPTY);
	// retList.add(errorRecord);
	// }
	// return retList;
	// }
	// } catch (SQLException e) {
	// e.printStackTrace();
	// errorRecord.setBackup_time(DBERROR);
	// retList.add(errorRecord);
	// return retList;
	// } catch (NumberFormatException e) {
	// e.printStackTrace();
	// errorRecord.setBackup_time(DESKEYERROR);
	// retList.add(errorRecord);
	// return retList;
	// }
	// }
	// }

	public String get_backup_info(String c_deskeyStr, String username,
			String password, String backup_time) {
		JSONObject ret = new JSONObject();
		JSONObject backup_info = new JSONObject();
		if (null == mDBHelper) {
			ret.accumulate("get_backup_info", DBERROR);
			return ret.toString();
		} else if (null == mServerEncryption) {
			ret.accumulate("get_backup_info", ENCRYPTIONERROR);
			return ret.toString();
		} else { // 解密
			try {
				System.out.println("\n\n获取备份信息:");
				System.out.println("c_deskey:" + c_deskeyStr);
				System.out.println("解密前的" + "username, password, backup_time:"
						+ username + ", " + password + ", " + backup_time);
				BigInteger c_deskey = new BigInteger(c_deskeyStr); // 获取加密的DES密钥
				username = mServerEncryption.decrypt(username, c_deskey);
				password = mServerEncryption.decrypt(password, c_deskey);
				backup_time = mServerEncryption.decrypt(backup_time, c_deskey);
				System.out.println("解密后的" + "username, password, backup_time:"
						+ username + ", " + password + ", " + backup_time);
				int user_id = mDBHelper.validate_user(username, password);
				if (-1 == user_id) {
					ret.accumulate("get_backup_info", FAILED);
					return ret.toString();
				} else {
					ret.accumulate("get_backup_info", SUCCEED);
					BigInteger sdeskey = mServerEncryption.getRandomSeed();
					System.out.println("服务端生成DES密钥：" + sdeskey.toString());
					BigInteger c_sdeskey = mServerEncryption.getCRSA(username,
							password).encode(sdeskey);
					System.out.println("加密后的服务端DES密钥：" + c_sdeskey.toString());
					ret.accumulate("c_sdeskey", c_sdeskey.toString());
					ArrayList<Schedule> schedules = mDBHelper
							.get_schedule_info(user_id, backup_time);
					if (schedules.size() == 0) {
						backup_info.accumulate("schedule_info", DATAEMPTY);
					} else {
						backup_info.accumulate("schedule_info",
								JSONHelper.schedule2JSONArray(schedules));
					}
					ArrayList<Category> categorys = mDBHelper
							.get_category_info(user_id, backup_time);
					if (categorys.size() == 0) {
						backup_info.accumulate("category_info", DATAEMPTY);
					} else {
						backup_info.accumulate("category_info",
								JSONHelper.category2JSONArray(categorys));
					}
					ArrayList<Note> notes = mDBHelper.get_note_info(user_id,
							backup_time);
					if (notes.size() == 0) {
						backup_info.accumulate("note_info", DATAEMPTY);
					} else {
						backup_info.accumulate("note_info",
								JSONHelper.note2JSONArray(notes));
					}
					//服务端加密
					String text = backup_info.toString();
					System.out.println("加密前的明文:" + text);
					String ctext = mServerEncryption.encrypt(text, sdeskey);
					System.out.println("加密后的密文:" + ctext);
					ret.accumulate("backup_info", ctext);
					// // 开始服务端的加密过程
					// BigInteger sdeskey = mServerEncryption.getRandomSeed();
					// System.out.println("服务端生成DES密钥:" + sdeskey.toString());
					// // 加密DES密钥
					// RSA cRSA = mServerEncryption.getCRSA(username, password);
					// BigInteger c_sdeskey = cRSA.decode(sdeskey);
					// System.out.println("加密后的DES密钥:" + c_sdeskey.toString());
					// // mServerEncryption.decrypt(ctext, sdeskey);
					return ret.toString();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				ret.accumulate("get_backup_info", DBERROR);
				return ret.toString();
			} catch (NumberFormatException e) {
				e.printStackTrace();
				ret.accumulate("get_backup_info", DESKEYERROR);
				return ret.toString();
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				e.printStackTrace();
				ret.accumulate("get_backup_info", ENCRYPTIONERROR);
				return ret.toString();
			}
		}
	}

	// public ArrayList<Schedule> decrypts_chedules(ArrayList<Schedule> src,
	// BigInteger deskey){
	// ArrayList<Schedule> ret = new ArrayList<Schedule>();
	// for(Schedule s : src){
	// s.
	// }
	// return ret;
	// }

	/**
	 * BackupInfo
	 * 
	 * @author Joel 获取备份信息时，服务端返回的备份信息类，该类用户设定返回JSON格式
	 */
	// class BackupInfo implements java.io.Serializable {
	//
	// private static final long serialVersionUID = 6629181145057186505L;
	// private ArrayList<Schedule> schedule_info = null;
	// private ArrayList<Category> category_info = null;
	// private ArrayList<Note> note_info = null;
	// private ArrayList<Festival> festival_info = null;
	// private String backup_state = null;
	// private String c_deskey = null;
	//
	// public String getC_deskey() {
	// return c_deskey;
	// }
	//
	// public void setC_deskey(String c_deskey) {
	// this.c_deskey = c_deskey;
	// }
	//
	// public String getBackup_state() {
	// return backup_state;
	// }
	//
	// public void setBackup_state(String backup_state) {
	// this.backup_state = backup_state;
	// }
	//
	// public List<Schedule> getSchedule_info() {
	// return schedule_info;
	// }
	//
	// public void setSchedule_info(ArrayList<Schedule> schedule_info) {
	// this.schedule_info = schedule_info;
	// }
	//
	// public List<Category> getCategory_info() {
	// return category_info;
	// }
	//
	// public void setCategory_info(ArrayList<Category> category_info) {
	// this.category_info = category_info;
	// }
	//
	// public List<Note> getNote_info() {
	// return note_info;
	// }
	//
	// public void setNote_info(ArrayList<Note> note_info) {
	// this.note_info = note_info;
	// }
	//
	// public List<Festival> getFestival_info() {
	// return festival_info;
	// }
	//
	// public void setFestival_info(ArrayList<Festival> festival_info) {
	// this.festival_info = festival_info;
	// }
	//
	// public BackupInfo(String backup_state, String c_deskey,
	// ArrayList<Schedule> schedule_info,
	// ArrayList<Category> category_info, ArrayList<Note> note_info,
	// ArrayList<Festival> festival_info) {
	// super();
	// this.c_deskey = c_deskey;
	// this.backup_state = backup_state;
	// this.schedule_info = schedule_info;
	// this.category_info = category_info;
	// this.note_info = note_info;
	// this.festival_info = festival_info;
	// }
	//
	// public BackupInfo() {
	// super();
	// this.c_deskey = "";
	// this.backup_state = "";
	// this.schedule_info = null;
	// this.category_info = null;
	// this.note_info = null;
	// this.festival_info = null;
	// }
	// }
}
