package client;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
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

	// 外界使用的
	public static BufferedImage setRadius(BufferedImage srcImage) {
		int radius = (srcImage.getWidth() + srcImage.getHeight()) / 6;
		return setRadius(srcImage, radius, 2, 5);
	}

	public static BufferedImage setRadius(BufferedImage srcImage, int radius, int border, int padding) {
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		int canvasWidth = width + padding * 2;
		int canvasHeight = height + padding * 2;

		BufferedImage image = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gs = image.createGraphics();
		gs.setComposite(AlphaComposite.Src);
		gs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gs.setColor(Color.WHITE);
		gs.fill(new RoundRectangle2D.Float(0, 0, canvasWidth, canvasHeight, radius, radius));
		gs.setComposite(AlphaComposite.SrcAtop);
		gs.drawImage(setClip(srcImage, radius), padding, padding, null);
		if (border != 0) {
			gs.setColor(Color.GRAY);
			gs.setStroke(new BasicStroke(border));
			gs.drawRoundRect(padding, padding, canvasWidth - 2 * padding, canvasHeight - 2 * padding, radius, radius);
		}
		gs.dispose();
		return image;
	}

	public static BufferedImage setClip(BufferedImage srcImage, int radius) {
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gs = image.createGraphics();

		gs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gs.setClip(new RoundRectangle2D.Double(0, 0, width, height, radius, radius));
		gs.drawImage(srcImage, 0, 0, null);
		gs.dispose();
		return image;
	}

	static String[] imageArray = { "bmp", "dib", "gif", "jfif", "jpe", "jpeg", "jpg", "png", "tif", "tiff", "ico" };

	public static Boolean isImage(File srcFilePath) {
		String input = srcFilePath.getName();
		String tmpName = input.substring(input.lastIndexOf(".") + 1, input.length());
		for (String string : imageArray) {
			if (string.equals(tmpName)) {
				return true;
			}
		}
		return false;
	}
}