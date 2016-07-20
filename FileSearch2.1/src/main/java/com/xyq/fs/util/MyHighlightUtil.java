package com.xyq.fs.util;

public class MyHighlightUtil {

	public static String highlight(String sourceStr, String patternStr) {

		StringBuilder sb = new StringBuilder();
		int num = 0;
		String last = "";
		while (true) {
			num = StringContains_SunDay(sourceStr, patternStr);
			if (num == -1)
				break;
			else {
				sb.append(sourceStr.substring(0, num));
				sb.append("<FONT color=red>"
						+ sourceStr.substring(num, num + patternStr.length())
						+ "</font>");
				last = sourceStr.substring(num+patternStr.length());
				sourceStr = sourceStr.substring(num + patternStr.length());
				num = StringContains_SunDay(sourceStr, patternStr);
			}
		}
		if(sb.length() == 0) return sourceStr;
		if(!"".equals(last)) sb.append(last);
		return sb.toString();
	}

	public static void main(String[] args) {
		
		System.out.println(highlight("11JAVA444444444444", "11JAVA444444444444"));

	}

	/***
	 * 这是匹配一次的
	 * 
	 * @param sourceString
	 * @param patternString
	 * @return
	 */
	public static int StringContains_SunDay(String sourceString,
			String patternString) {

		sourceString = sourceString.toLowerCase();
		patternString = patternString.toLowerCase();
		// Covert the char array
		char[] sourceList = sourceString.toCharArray();
		char[] patternList = patternString.toCharArray();

		int sourceLength = sourceList.length;
		int patternLength = patternList.length;
		// System.out.println(sourceLength + "  " + patternLength);
		int sCount = 0, pCount = 0;
		int loc = 0;

		if (sourceLength < patternLength) {
			return -1;
		}

		while (sCount < sourceLength && pCount < patternLength) {
			// if equals to move next character
			if (sourceList[sCount] == patternList[pCount]) {
				sCount++;
				pCount++;
			} else {
				// sAim:the location of char to judge
				// pAim:the last location of the pattern string
				int sAim = sCount + patternLength;
				if (sAim <= sourceLength - 1) {

				} else
					break;
				char aimChar = sourceList[sAim];
				int pAim = patternLength - 1;
				// to judge char from back to front,the pAim is the equal
				// location
				while (pAim > 0) {
					if (patternList[pAim] == aimChar) {
						break;
					}
					pAim--;
				}
				// record the equal location with loc.
				// sCount:move the judge location of source string
				// pCount:move the begin of the pattern string
				sCount = sCount + patternLength - pAim;
				loc = sCount;
				pCount = 0;
			}
		}
		// if pattern string don't match completed,return -1
		if (pCount < patternLength) {
			return -1;
		}
		// else return the location
		return loc;
	}

}
