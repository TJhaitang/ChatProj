package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ChatServer {
	HashMap<String, HandleASession> UserMap = new HashMap<>();

	public static void main(String[] args) {
		new ChatServer();
	}

	ChatServer() {
		try {
			ServerSocket serverSocket = new ServerSocket(8888);
			while (true) {
				Socket msg = serverSocket.accept();
				Socket file = serverSocket.accept();
				System.out.println("client IP:" + msg.getInetAddress().getHostAddress());
				TargetConnection t = new TargetConnection(msg, file);// 将传输过程打包
				if (!t.check()) {
					// 需要在这里check还是login？
					System.out.println("client IP:" + msg.getInetAddress().getHostAddress() + "-连接失败");
					continue;
				}
				Login NewClient = new Login(t);
				new Thread(NewClient).start();// 建立线程实现多用户使用
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class Login implements Runnable, Flag {
		TargetConnection t;
		String userPath;

		Login(TargetConnection tar) {
			this.t = tar;
		}

		@Override public void run() {
			int sign;
			String username;
			String password;
			while (true) {
				try {// 从用户端接收登录信息
					sign = t.getMsgFromClient().readInt();// 接收登录or注册信号:登录1，注册2
					username = t.getMsgFromClient().readUTF();
					password = t.getMsgFromClient().readUTF();
					byte[] passwordCode = TransPassword(password);
					// 为了我们的文件目录统一，使用getProperty得到项目目录
					File user = new File(
							System.getProperty("user.dir") + "/src/server/users/" + username + "/userinfo.key");
					if (sign == Flag.LOGIN) {// 登录
						if (!user.exists()) {// 如果没有此账号
							t.getMsgToClient().writeInt(Flag.FAIL);
							continue;
						}
						// 对照密码
						FileInputStream fis = new FileInputStream(user);
						byte[] pswd = fis.readAllBytes();
						if (Arrays.equals(pswd, passwordCode)) {// 登录成功
							t.getMsgToClient().writeInt(Flag.SUCCESS);// 向用户发送成功信号
							t.setUsername(username);
							System.out.println(t.getMsgSocket().getInetAddress().getHostAddress() + ":登录为 " + username);
							userPath = System.getProperty("user.dir") + "/src/server/users/" + username;
							// 检查更新
							if (!checkUpdate(t)) {
								System.out.println("更新失败-1");
							}
							// 开始消息传输
							HandleASession hand = new HandleASession(t);//
							UserMap.put(username, hand);// 将用户放入hashmap__还需要拿出来
							fis.close();
							break;
						} else {
							t.getMsgToClient().writeInt(Flag.FAIL);
						}
						fis.close();
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
						FileOutputStream fos = new FileOutputStream(user);
						fos.write(passwordCode, 0, passwordCode.length);
						fos.flush();
						fos.close();
						CreateNewUser(parent);// 创建用户文件夹
						t.getMsgToClient().writeInt(Flag.SUCCESS);
					}

				} catch (IOException e) {
					System.out.println(t.getMsgSocket().getInetAddress().getHostAddress() + ":退出");
					return;
				}
			}
		}

		private boolean checkUpdate(TargetConnection t) {
			String[] files = { "friendList.txt", "groupList.txt" };
			boolean flag = true;
			String code;
			try {
				int i = 0;
				// 首先接受hashcode,若相同则返回不更新，不同则返回更新信号，并发回文件
				if (t.getFileFromClient().readInt() == Flag.CHECKUPDATE) {
					while (!"end".equals(code = t.getFileFromClient().readUTF())) {
						if (Objects.equals(MyUtil.readLastLine(new File(userPath + "/" + files[i]), null), code)) {
							t.getFileToClient().writeInt(Flag.NOUPDATE);
						} else {
							t.getFileToClient().writeInt(Flag.LOCALUPDATE);
							t.sendFile(userPath + "/" + files[i]);
						}
						i++;
					}
				}
			} catch (IOException e) {
				System.out.println("更新失败-2");
				flag = false;
			}
			return flag;
		}

		private byte[] TransPassword(String password) {// 将明文密码转为md5码
			MessageDigest md5 = null;
			try {
				md5 = MessageDigest.getInstance("MD5");
				byte[] srcBytes = password.getBytes();
				md5.update(srcBytes);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			return md5.digest();
		}

		private void CreateNewUser(File f) {// 新建用户的文件格式在这里声明，目前已有userinfo
			try {
				new File(f.getAbsolutePath() + "/groupList.txt").createNewFile();
				new File(f.getAbsolutePath() + "/friendList.txt").createNewFile();
				new File(f.getAbsolutePath() + "/MsgQ.txt").createNewFile();
				new File(f.getAbsolutePath() + "/update.txt").createNewFile();
				new File(f.getAbsolutePath() + "/cache").mkdir();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class HandleASession {// 在这里实现信息交互
		TargetConnection t;
		final String path = System.getProperty("user.dir") + "/src/server/users/";
		Receiver receiver = new Receiver();
		Sender sender = new Sender();
		Queue<MsgPack> MsgQueue = new LinkedList<>();
		int go = 1;
		File filePath;

		HandleASession(TargetConnection t) {
			this.t = t;
			filePath = new File(System.getProperty("user.dir") + "/src/server/users/" + t.getUsername());
			new Thread(receiver).start();
			new Thread(sender).start();

		}

		private void AddMsgToFile(String username, MsgPack mp) {// 图片怎么办？，但应该差别不大-图片另说//目前仅考虑了TEXT
			File file = new File(filePath.getParentFile(), username + "/MsgQ.txt");
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			PrintWriter pw;
			try {
				pw = new PrintWriter(new FileOutputStream(file, true));
				pw.println("" + mp.flag + "\n" + mp.TargetName + "\n" + mp.MsgString);
				pw.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		private class Receiver implements Runnable {

			@Override public void run() {
				while (true) {// 接收到一个信息——信息格式是什么样的？——如果是图片、群聊呢
					try {
						int sign = t.getMsgFromClient().readInt();
						String TargetName = t.getMsgFromClient().readUTF();
						String Msg = t.getMsgFromClient().readUTF();
						MsgPack mp = new MsgPack(sign, TargetName, Msg);
						switch (sign) {
						case Flag.SENDTEXT: {
							SendMsg(mp);
							break;
						}
						case Flag.SENDFILE:/* 往下先等等 */ {
							readAndSendFile(mp);
							break;
						}
						case Flag.ADDFRIEND: {
							AddFriend(mp);
							break;
						}
						case Flag.ACCEPTFRIEND: {
							AcceptFriend(mp);
							break;
						}
						case Flag.CREATEGROUP: {
							CreateGroup(mp);
							break;
						}
						case Flag.ACCEPTGROUP: {
							AcceptGroup(mp);
							break;
						}
						default:
							break;
						}
					} catch (IOException e)// 如果是收信者下线该怎么办？_写函数，别在这里实现逻辑
					{
						System.out.println(t.getMsgSocket().getInetAddress().getHostAddress() + ":退出");
						// 从map中删掉
						sender.stop();
						UserMap.remove(t.getUsername());
						return;
					}
				}
			}

			private void readAndSendFile(MsgPack mp) {
				String[] split = mp.MsgString.split("\\|");
				String name = split[3];
				t.receiveFile(path + mp.TargetName + "/cache" + name);
				SendMsg(mp);
				// new File(path + mp.TargetName + "/cache" + name).delete();

			}

			private void AcceptGroup(MsgPack mp) {
				File file = new File(
						System.getProperty("user.dir") + "/src/server/groups/" + mp.MsgString.split("\\|")[1] + ".txt");
				// System.out.println("qwwe:" + file.getAbsolutePath() + " " + t.getUsername());

				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				PrintWriter pw;
				try {
					pw = new PrintWriter(new FileOutputStream(file, true));
					pw.println(t.getUsername());
					pw.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				try {
					pw = new PrintWriter(new FileOutputStream(
							System.getProperty("user.dir") + "/src/server/users/" + t.getUsername() + "/groupList.txt",
							true));
					pw.println(mp.MsgString.split("\\|")[1] + "\n" + mp.MsgString.split("\\|")[2]);
					pw.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}

			private void CreateGroup(MsgPack mp) {
				String[] users = mp.TargetName.split("\\|");
				String groupName = mp.MsgString.split("\\|")[1];
				String groupId;
				File groupMem;
				while (true) {
					groupId = "G" + (int) (Math.random() * 1000000);
					groupMem = new File(System.getProperty("user.dir") + "/src/server/groups/" + groupId + ".txt");
					if (!groupMem.exists()) {
						try {
							groupMem.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}
						break;
					}
				}
				PrintWriter pw;
				try {
					pw = new PrintWriter(new FileOutputStream(groupMem, true));
					pw.println(t.getUsername());
					pw.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				SendMsgToUser(new MsgPack(Flag.CREATEGROUP, t.getUsername(),
						t.getUsername() + "|" + groupId + "|" + groupName));
				try {
					pw = new PrintWriter(new FileOutputStream(
							System.getProperty("user.dir") + "/src/server/users/" + t.getUsername() + "/groupList.txt",
							true));
					pw.println(groupId + "\n" + mp.MsgString.split("\\|")[1]);
					pw.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				for (String user : users) {
					if (!user.equals(t.getUsername())) {
						SendMsgToUser(
								new MsgPack(Flag.CREATEGROUP, user, t.getUsername() + "|" + groupId + "|" + groupName));
					}
				}
			}

			private void AcceptFriend(MsgPack mp) {
				SendMsgToUser(new MsgPack(Flag.ACCEPTFRIEND, mp.TargetName, mp.MsgString));
				if (mp.MsgString.split("\\|")[2].equals("Refuse")) {
					return;
				}
				File file = new File(filePath, "/friendList.txt");
				if (!file.exists()) {
					try {
						file.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				PrintWriter pw;
				try {
					pw = new PrintWriter(new FileOutputStream(file, true));
					pw.println(mp.MsgString.split("\\|")[0]);
					pw.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}

			private void AddFriend(MsgPack mp) {// 这里没判断是否有这个好友
				String[] users = mp.TargetName.split("\\|");
				for (String user : users) {
					if (!user.equals(t.getUsername())) {
						SendMsgToUser(new MsgPack(Flag.ADDFRIEND, user, t.getUsername() + "|" + user));
					}
				}
			}

			private void SendMsg(MsgPack mp)// 发消息用不同函数实现可以么？是不是有点不优雅
			{// 先直接发，后面写存文件待发送
				String TargetName = mp.TargetName, Msg = mp.MsgString;
				// String[] split = Msg.split("\\|");
				if (isFriend(TargetName)) {// 这里是不是要加上判断这个人是不是对方好友-按理说不会有问题，但万一呢
					SendMsgToUser(new MsgPack(mp.flag, TargetName, Msg));
				} else {// 这里实现发群的逻辑
					File UserList = new File(
							System.getProperty("user.dir") + "/src/server/groups/" + TargetName + ".txt");
					try {
						BufferedReader br = new BufferedReader(new FileReader(UserList));
						String name;
						while ((name = br.readLine()) != null) {
							if (!name.equals(t.getUsername())) {
								SendMsgToUser(new MsgPack(mp.flag, name, Msg));
							}
						}
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}

			private void SendMsgToUser(MsgPack mp) {
				if (UserMap.containsKey(mp.TargetName)) {
					System.out.println("向" + mp.TargetName + "发送信息");
					HandleASession h2 = UserMap.get(mp.TargetName);
					h2.sender.PutMsg(mp);// str格式再想一下
				} else {
					AddMsgToFile(mp.TargetName, new MsgPack(mp.flag, mp.TargetName, mp.MsgString));
				}
			}

			private boolean isFriend(String tar)// 判断这个目标是好友or群组
			{
				return tar.toCharArray()[0] != 'G';
			}

		}

		private class Sender implements Runnable {

			public void stop() {
				go = 0;
			}

			public void PutMsg(MsgPack ss) {
				MsgQueue.add(ss);
				// System.out.println("测试一下");
			}

			@Override public void run() {
				this.display();
				while (go == 1) {
					try {
						Thread.sleep(5);// 为啥能用了？
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					if (!MsgQueue.isEmpty()) {// 这个改成锁
						// System.out.println("排好队！");
						MsgPack mp = MsgQueue.poll();
						try {
							t.getMsgToClient().writeInt(mp.flag);
							t.getMsgToClient().writeUTF(mp.TargetName);
							t.getMsgToClient().writeUTF(mp.MsgString);// 失败后写文件(吗？)
							if (mp.flag == Flag.ACCEPTFRIEND) {
								if (mp.MsgString.split("\\|")[2].equals("Accept")) {
									File file = new File(filePath, "/friendList.txt");
									if (!file.exists()) {
										try {
											file.createNewFile();
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
									PrintWriter pw;
									try {
										pw = new PrintWriter(new FileOutputStream(file, true));
										pw.println(mp.MsgString.split("\\|")[1]);
										pw.close();
									} catch (FileNotFoundException e) {
										e.printStackTrace();
									}
								}
							} else if (mp.flag == Flag.SENDFILE) {
								// System.out.println("SENDFILE");
								t.sendFile(path + mp.TargetName + "/cache" + mp.MsgString.split("\\|")[3]);
							}
							// System.out.println(mp.flag);

						} catch (IOException e) {// 写文件
							AddMsgToFile(t.getUsername(), mp);
							e.printStackTrace();
							System.out.println("Send to " + t.getUsername() + " error!");
						}
					}
				}
			}

			public void display() {
				// System.out.println("读文件！");
				File msgQ = new File(filePath, "MsgQ.txt");// 改文件应该是从上向下读取的
				BufferedReader br = null;
				try {
					br = new BufferedReader(new FileReader(msgQ));
				} catch (FileNotFoundException e) {// 不要没有该文件
					e.printStackTrace();
					try {
						msgQ.createNewFile();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				int sign;
				String str, tar;
				try {
					while ((str = br.readLine()) != null) {
						sign = Integer.parseInt(str);
						tar = br.readLine();
						str = br.readLine();
						MsgQueue.add(new MsgPack(sign, tar, str));
					}
					FileWriter fileWriter = new FileWriter(msgQ);
					fileWriter.write("");
					fileWriter.flush();
					fileWriter.close();
				} catch (IOException e) {
					System.out.println(t.getUsername() + "display——done!");
				}
			}

			Sender() {// 把文件中的指令扔进队列里

			}

		}

	}
}

class TargetConnection {// 建立一个类用以存放与用户的连接
	private final Socket MsgSocket;
	private Socket FileSocket;
	private String username;
	private DataInputStream msgFromClient;
	private DataOutputStream msgToClient;
	private DataInputStream fileFromClient;
	private DataOutputStream fileToClient;

	TargetConnection(Socket msg, Socket file) {
		this.MsgSocket = msg;
		this.FileSocket = file;
		try {
			msgFromClient = new DataInputStream(MsgSocket.getInputStream());
			msgToClient = new DataOutputStream(MsgSocket.getOutputStream());
			fileFromClient = new DataInputStream(FileSocket.getInputStream());
			fileToClient = new DataOutputStream(FileSocket.getOutputStream());
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}

	void sendFile(String filePath) {
		// System.out.println(filePath);
		try {
			// 先发送文件大小
			long length = new File(filePath).length();
			fileToClient.writeLong(length);
			// 发送文件
			FileInputStream fis = new FileInputStream(filePath);
			byte[] buffer = new byte[8192];
			int read;
			while ((read = fis.read(buffer, 0, 8192)) >= 0) {
				fileToClient.write(buffer, 0, read);
			}
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void receiveFile(String filePath) {
		try {
			// 先得到长度
			long length = fileFromClient.readLong();
			FileOutputStream fos = new FileOutputStream(filePath);
			byte[] buffer = new byte[8192];
			int read, transferred = 0;
			while (length > transferred) {
				read = fileFromClient.read(buffer, 0, 8192);
				fos.write(buffer, 0, read);
				transferred += read;
			}
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("缓存失败");
		}

	}

	boolean check() {// 判断两个Socket是否连接到同一个用户
		int rand = (int) (Math.random() * 100);
		int tip = Flag.FAIL;
		try {
			msgToClient.writeInt(rand);
			fileToClient.writeInt(rand);
			tip = msgFromClient.readInt();
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
		return msgFromClient;
	}

	DataOutputStream getMsgToClient() {
		return msgToClient;
	}

	DataInputStream getFileFromClient() {
		return fileFromClient;
	}

	DataOutputStream getFileToClient() {
		return fileToClient;
	}

	Socket getMsgSocket() {
		return this.MsgSocket;
	}

}

class MsgPack {
	int flag;
	String TargetName;
	String MsgString;

	MsgPack(int flag, String TargetName, String Msg) {
		this.flag = flag;
		this.TargetName = TargetName;
		this.MsgString = Msg;
	}
}
