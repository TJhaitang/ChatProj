package client;

import javax.swing.*;
import java.net.Socket;

// 传进用户ID，用s连接到服务器,创建界面（做按钮、界面列表、个人信息、朋友圈入口）
// 按钮连接到聊天框、朋友圈界面
class ClientWindow extends JFrame {
	private ChatWindow w;

	ClientWindow(int ID, Socket s) {
		new Thread(new C()).start();
	}

	private class C implements Runnable {

		@Override
		public void run() {

		}
	}
}

//
class ChatWindow extends JFrame {
}

// 私聊 创建好友聊天界面，将本地聊天数据读取到这里，按照时间顺序制作窗口
class qwe extends ChatWindow {
	qwe(int friendID, Socket s) {

	}
}