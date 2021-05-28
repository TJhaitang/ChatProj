package server;

import java.io.*;

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

	public static void fileReplace(String path, int key, String newValue) {
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
}
