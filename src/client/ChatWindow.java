package client;

import javax.swing.*;
import java.net.Socket;

// 私聊 创建好友聊天界面，将本地聊天数据读取到这里，按照时间顺序制作窗口
abstract class ChatWindow extends JFrame {
	ChatWindow() {

	}
}

class FriendWindow extends ChatWindow {
	FriendWindow(String friendId, ServerConnection sc)
	{
		System.out.println("111");
	}
}
