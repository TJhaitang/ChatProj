package client;

import javax.swing.*;
import java.net.Socket;

/**
 * -*- coding: utf-8 -*-
 * Time    : 2021/5/14 17:28
 *
 * @author : nieyuzhou
 * File    : ChatWindow.java
 * Software: IntelliJ IDEA
 */

// 私聊 创建好友聊天界面，将本地聊天数据读取到这里，按照时间顺序制作窗口
abstract class ChatWindow extends JPanel
{
	ChatWindow(int friendId, Socket s)
	{
		this.setBounds(800, 100, 600, 600);
		this.setVisible(true);
		this.setLayout(null);
	}
}
