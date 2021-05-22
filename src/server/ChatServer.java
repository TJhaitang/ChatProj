package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ChatServer {
	HashMap<String, TargetConnection> UserMap = new HashMap<>();

	public static void main(String[] args) {
		ChatServer cs = new ChatServer();
	}

	ChatServer() {
		try {
			ServerSocket serverSocket = new ServerSocket(12138);
			while (true) {
				Socket msg = serverSocket.accept();
				Socket file = serverSocket.accept();
				System.out.println("client IP:" + msg.getInetAddress().getHostAddress());
				TargetConnection t = new TargetConnection(msg, file);// 将传输过程打包
				if (!t.check()) {// 需要在这里check还是login？
					System.out.println("client IP:" + msg.getInetAddress().getHostAddress() + "-连接失败");
					continue;
				}
				Login NewClinet = new Login(t);
				new Thread(NewClinet).start();// 建立线程实现多用户使用
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class Login implements Runnable, Flag {
		TargetConnection t;

		Login(TargetConnection tar) {
			this.t = tar;
		}

		@Override
		public void run() {
			int sign;
			String username;
			String password;
			while (true) {
				try {// 从用户端接收登录信息
					sign = t.getMsgFromClient().readInt();// 接收登录or注册信号:登录1，注册2
					username = t.getMsgFromClient().readUTF();
					password = t.getMsgFromClient().readUTF();
					// 为了我们的文件目录统一，使用getProperty得到项目目录——这里用相对目录就可以了
					File user = new File(
							System.getProperty("user.dir") + "/src/server/users/" + username + "/userinfo.txt");
					if (sign == Flag.LOGIN) {// 登录
						if (!user.exists()) {// 如果没有此账号
							t.getMsgToClient().writeInt(Flag.FAIL);
							continue;
						}
						// 对照密码
						BufferedReader br = new BufferedReader(new FileReader(user));
						String pswd = br.readLine();
						if (pswd.equals(password)) {// 登录成功
							t.getMsgToClient().writeInt(Flag.SUCCESS);// 向用户发送成功信号
							t.setUsername(username);
							UserMap.put(username, t);// 将用户放入hashmap__还需要拿出来
							System.out.println(t.getMsgSocket().getInetAddress().getHostAddress() + ":登录为 " + username);
							HandleASession hand = new HandleASession(t);//
							new Thread(hand).start();
							br.close();
							break;
						} else {
							t.getMsgToClient().writeInt(Flag.FAIL);
						}
						br.close();
					}
					if (sign == Flag.SIGNUP) {// 注册
						if (user.exists()) {// 如果已经有此账号
							t.getMsgToClient().writeInt(Flag.FAIL);
							continue;
						}
						File parent = user.getParentFile();
						parent.mkdirs();
						user.createNewFile();
						// 保存密码
						BufferedWriter bw = new BufferedWriter(new FileWriter(user));
						bw.write(password);
						bw.close();
						CreateNewUser(parent);// 创建用户文件夹
						t.getMsgToClient().writeInt(Flag.SUCCESS);
					}

				} catch (IOException e) {
					System.out.println(t.getMsgSocket().getInetAddress().getHostAddress() + ":退出");
					return;
				}
			}
		}

		private void CreateNewUser(File f) {// 新建用户的文件格式在这里声明，目前已有userinfo
			try {
				new File(f.getAbsolutePath() + "/groupList.txt").createNewFile();
				new File(f.getAbsolutePath() + "/friendList.txt").createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class HandleASession implements Runnable {// 在这里实现信息交互
		TargetConnection t;

		HandleASession(TargetConnection t) {
			this.t = t;
		}

		private void SendMsg()// 发消息用不同函数实现可以么？是不是有点不优雅
		{// 先直接发，后面写存文件待发送
			String TargetName = null, Msg = null;
			try {
				TargetName = t.getMsgFromClient().readUTF();
				Msg = t.getMsgFromClient().readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// String[] split = Msg.split("\\|");
			if (isFriend(TargetName)) {
				if (UserMap.containsKey(TargetName)) {
					System.out.println("测试！");
					TargetConnection t2 = UserMap.get(TargetName);
					try {
						t2.getMsgToClient().writeInt(Flag.SENDTEXT);
						t2.getMsgToClient().writeUTF(Msg);
					} catch (IOException e)// 这里执行一下删map和存文件——按理说不应该不存在
					{
						e.printStackTrace();
						try {
							t.getMsgToClient().writeInt(Flag.FAIL);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						return;
					}
					try {
						t.getMsgToClient().writeInt(Flag.SUCCESS);
					} catch (IOException e) {
						e.printStackTrace();
					} // 再写一下fail的
				} else {
					try {
						t.getMsgToClient().writeInt(Flag.FAIL);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {

			}
		}

		private boolean isFriend(String tar)// 判断这个目标是好友or群组
		{
			return true;
		}

		@Override
		public void run() {
			while (true) {
				int sign;
				try {
					sign = t.getMsgFromClient().readInt();
					switch (sign) {
					case Flag.SENDTEXT: {
						SendMsg();
						break;
					}
					case Flag.SENDFILE:/* 往下先等等 */
					{
						break;
					}
					default:
						break;
					}
				} catch (IOException e)// 如果是收信者下线该怎么办？_写函数，别在这里实现逻辑
				{
					System.out.println(t.getMsgSocket().getInetAddress().getHostAddress() + ":退出");
					// 从map中删掉
					UserMap.remove(t.getUsername());
					return;
				}
			}
		}
	}
}

class TargetConnection {// 建立一个类用以存放与用户的连接
	private Socket MsgSocket;
	private Socket FileSocket;
	private String username;
	private DataInputStream MsgFromClient;
	private DataOutputStream MsgToClient;
	private DataInputStream FileFromClient;
	private DataOutputStream FileToClient;

	TargetConnection(Socket msg, Socket file) {
		this.MsgSocket = msg;
		this.FileSocket = file;
		try {
			MsgFromClient = new DataInputStream(MsgSocket.getInputStream());
			MsgToClient = new DataOutputStream(MsgSocket.getOutputStream());
			FileFromClient = new DataInputStream(FileSocket.getInputStream());
			FileToClient = new DataOutputStream(FileSocket.getOutputStream());
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}

	boolean check() {// 判断两个Socket是否连接到同一个用户
		int rand = (int) (Math.random() * 100);
		int tip = Flag.FAIL;
		try {
			MsgToClient.writeInt(rand);
			FileToClient.writeInt(rand);
			tip = MsgFromClient.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (tip == Flag.SUCCESS);
	}

	void setUsername(String str) {
		this.username = str;
	}

	String getUsername() {
		return this.username;
	}

	DataInputStream getMsgFromClient() {
		return MsgFromClient;
	}

	DataOutputStream getMsgToClient() {
		return MsgToClient;
	}

	DataInputStream getFileFromClient() {
		return FileFromClient;
	}

	DataOutputStream getFileToClient() {
		return FileToClient;
	}

	Socket getMsgSocket() {
		return this.MsgSocket;
	}

}
