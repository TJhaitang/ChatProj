package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ChatServer
{
	HashMap<String, TargetConnection> UserMap = new HashMap<String, TargetConnection>();

	public static void main(String[] args)
	{
		ChatServer cs = new ChatServer();
	}

	ChatServer()
	{
		try
		{
			ServerSocket serverSocket = new ServerSocket(12138);
			while (true)
			{
				Socket msg = serverSocket.accept();
				Socket file = serverSocket.accept();
				System.out.println("client IP:" + msg.getInetAddress().getHostAddress());
				TargetConnection t = new TargetConnection(msg, file);// 将传输过程打包
				if (!t.check())
				{// 需要在这里check还是login？
					System.out.println("client IP:" + msg.getInetAddress().getHostAddress() + "-连接失败");
					continue;
				}
				Login NewClinet = new Login(t);
				new Thread(NewClinet).start();// 建立线程实现多用户使用
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	class Login implements Runnable, Flag
	{
		TargetConnection t;

		Login(TargetConnection tar)
		{
			this.t = tar;
		}

		@Override

		public void run()
		{
			while (true)
			{
				int sign;
				String username;
				String password;
				try
				{// 从用户端接收登录信息
					sign = t.getMsgFromClient().readInt();// 接收登录or注册信号:登录1，注册2
					username = t.getMsgFromClient().readUTF();
					password = t.getMsgFromClient().readUTF();

					File user = new File("src/server/users/" + username + "/userinfo.txt");
					if (sign == Flag.LOGIN)
					{// 登录
						if (!user.exists())
						{// 如果没有此账号
							t.getMsgToClient().writeInt(Flag.FAIL);
							continue;
						}
						// 对照密码
						BufferedReader br = new BufferedReader(new FileReader(user));
						String pswd = br.readLine();
						if (pswd.equals(password))
						{// 登录成功
							t.getMsgToClient().writeInt(Flag.SUCCESS);// 向用户发送成功信号
							t.setUsername(username);
							UserMap.put(username, t);// 将用户放入hashmap__还需要拿出来
							HandleASession hand = new HandleASession(t);//
							new Thread(hand).start();
							br.close();
							break;
						} else
						{
							t.getMsgToClient().writeInt(Flag.FAIL);
						}
						br.close();
					}
					if (sign == Flag.SIGNUP)
					{// 注册
						if (user.exists())
						{// 如果已经有此账号
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
						CreateNewUser(parent);
						t.getMsgToClient().writeInt(Flag.SUCCESS);
						continue;
					}

				}
				catch (IOException e)
				{
					return;
				}
			}
		}

		private void CreateNewUser(File f)
		{// 新建用户的文件格式在这里声明，目前已有userinfo

		}
	}

	class HandleASession implements Runnable
	{// 在这里实现信息交互
		TargetConnection t;

		HandleASession(TargetConnection t)
		{
			this.t = t;
		}

		@Override
		public void run()
		{

		}
	}
}

class TargetConnection
{// 建立一个类用以存放与用户的连接
	private Socket MsgSocket;
	private Socket FileSocket;
	private String username;
	private DataInputStream MsgFromClient;
	private DataOutputStream MsgToClient;
	private DataInputStream FileFromClient;
	private DataOutputStream FileToClient;

	TargetConnection(Socket msg, Socket file)
	{
		this.MsgSocket = msg;
		this.FileSocket = file;
		try
		{
			MsgFromClient = new DataInputStream(MsgSocket.getInputStream());
			MsgToClient = new DataOutputStream(MsgSocket.getOutputStream());
			FileFromClient = new DataInputStream(FileSocket.getInputStream());
			FileToClient = new DataOutputStream(FileSocket.getOutputStream());
		}
		catch (IOException e2)
		{
			e2.printStackTrace();
		}
	}

	boolean check()
	{// 判断两个Socket是否连接到同一个用户
		int rand = (int) (Math.random() * 100);
		int tip = Flag.FAIL;
		try
		{
			MsgToClient.writeInt(rand);
			FileToClient.writeInt(rand);
			tip = MsgFromClient.readInt();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return (tip == Flag.SUCCESS);
	}

	void setUsername(String str)
	{
		this.username = str;
	}

	String getUsername()
	{
		return this.username;
	}

	DataInputStream getMsgFromClient()
	{
		return MsgFromClient;
	}

	DataOutputStream getMsgToClient()
	{
		return MsgToClient;
	}

	DataInputStream getFileFromClient()
	{
		return FileFromClient;
	}

	DataOutputStream getFileToClient()
	{
		return FileToClient;
	}

}
