package server;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatServer {
	public static void main(String[] args) {
		ChatServer cs = new ChatServer();
	}

	ChatServer() {
		try {
			ServerSocket serverSocket = new ServerSocket(12138);
			while (true) {
				Socket client = serverSocket.accept();
				System.out.println("client IP:" + client.getInetAddress().getHostAddress());
				Login NewClinet = new Login(client);
				new Thread(NewClinet).start();// 建立线程实现多用户使用
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class Login implements Runnable {
	Socket s = null;
	private DataInputStream fromClient;
	private DataOutputStream toClient;

	Login(Socket s) {
		this.s = s;
		try {
			fromClient = new DataInputStream(s.getInputStream());
			toClient = new DataOutputStream(s.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override

	public void run() {
		while (true) {
			int sign;
			String username;
			String password;
			try {// 从用户端接收登录信息
				sign = fromClient.readInt();// 接收登录or注册信号:登录1，注册2
				username = fromClient.readUTF();
				password = fromClient.readUTF();

				File user = new File("src/server/users/" + username + "/userinfo.txt");
				if (sign == 1) {// 登录
					if (!user.exists()) {// 如果没有此账号
						toClient.writeInt(0);
						continue;
					}
					// 对照密码
					BufferedReader br = new BufferedReader(new FileReader(user));
					String pswd = br.readLine();
					if (pswd.equals(password)) {
						toClient.writeInt(1);
						Server server = new Server(s);
						new Thread(server).start();
						br.close();
						break;
					} else {
						toClient.writeInt(0);
					}
					br.close();
				}
				if (sign == 2) {// 注册
					if (user.exists()) {// 如果已经有此账号
						toClient.writeInt(0);
						continue;
					}
					File parent = user.getParentFile();
					parent.mkdirs();
					user.createNewFile();
					// 保存密码
					BufferedWriter bw = new BufferedWriter(new FileWriter(user));
					bw.write(password);
					bw.close();
					toClient.writeInt(1);
					continue;
				}

			} catch (IOException e) {
				return;
			}
		}
	}
}

class Server implements Runnable {
	Server(Socket s) {

	}

	@Override
	public void run() {

	}
}
