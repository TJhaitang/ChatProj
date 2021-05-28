package client;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * -*- coding: utf-8 -*-
 * Time    : 2021/5/27 17:08
 *
 * @author : nieyuzhou
 * File    : MyUtil.java
 * Software: IntelliJ IDEA
 */
public class MyUtil {
	// 返回true需要更新
	public static Boolean compareFile(String firstFile, String secondFile) {
		return new File(firstFile).hashCode() == new File(secondFile).hashCode();
	}

	public static void fileReplaceLine(String path, int key, String newValue) {
		String temp;
		StringBuilder builder = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));

			// 读入这一行时传入修改量
			int i = 0;
			while ((temp = br.readLine()) != null) {

				if (i == key) {
					builder.append(newValue);
				} else {
					builder.append(temp);
				}
				builder.append(System.getProperty("line.separator"));
				i++;
			}
			br.close();

			PrintWriter pw = new PrintWriter(new FileWriter(path, false));
			pw.write(builder.toString().toCharArray());
			pw.flush();
			pw.close();

		} catch (IOException e) {
			e.printStackTrace();

		}

	}

	public static String generateTimeStamp() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
		return df.format(new Date());// 0为未读
	}

	public static String readLastLine(File file, String charset) {
		if (!file.exists() || file.isDirectory() || !file.canRead()) {
			return null;
		}
		try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
			long len = raf.length();
			if (len == 0L) {
				return "";
			} else {
				long pos = len - 1;
				while (pos > 0) {
					pos--;
					raf.seek(pos);
					if (raf.readByte() == '\n') {
						break;
					}
				}
				if (pos == 0) {
					raf.seek(0);
				}
				byte[] bytes = new byte[(int) (len - pos)];
				raf.read(bytes);
				if (charset == null) {
					return new String(bytes);
				} else {
					return new String(bytes, charset);
				}
			}
		} catch (IOException ignored) {
		}
		return null;
	}
}
