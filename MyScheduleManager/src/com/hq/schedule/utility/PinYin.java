package com.hq.schedule.utility;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinYin {
	/**
	 * 返回一个字的拼音
	 * 
	 * @param hanzi
	 * @return
	 */
	public static String toPinYin(char hanzi) {
		HanyuPinyinOutputFormat hanyuPinyin = new HanyuPinyinOutputFormat();
		hanyuPinyin.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		hanyuPinyin.setToneType(HanyuPinyinToneType.WITH_TONE_MARK);
		hanyuPinyin.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
		String[] pinyinArray = null;
		try {
			// 是否在汉字范围内
			if (hanzi >= 0x4e00 && hanzi <= 0x9fa5) {
				pinyinArray = PinyinHelper.toHanyuPinyinStringArray(hanzi,
						hanyuPinyin);
			} else if (('a' <= hanzi && hanzi <= 'z')
					|| ('A' <= hanzi && hanzi <= 'Z')) {
				return String.valueOf(hanzi);// 如果是字母，直接返回
			} else {
				return "#";
			}
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
		}
		// 将获取到的拼音返回
		return pinyinArray[0];
	}
}
