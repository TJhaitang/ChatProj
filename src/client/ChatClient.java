package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatClient {
	private Socket MsgSocket;
	private Socket FileSocket;

	public static void main(String[] args) {
		ChatClient cc = new ChatClient();
	}

	ChatClient() {
		try {
			MsgSocket = new Socket("localhost", 12138);// 建立连接
			FileSocket = new Socket("localhost", 12138);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "未与服务器建立连接");
			System.exit(0);
		}
		ServerConnection s = new ServerConnection(MsgSocket, FileSocket);
		if (!s.check()) {
			JOptionPane.showMessageDialog(null, "未与服务器建立连接");
			System.exit(0);
		}
		Login lg = new Login(s);
		lg.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}
}

class Login extends JFrame implements Flag {
	private Login lg;

	private ServerConnection s;

	private JPanel usernamePanel = new JPanel();
	private JPanel passwordPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();
	private JPanel textPanel = new JPanel();
	private JLabel label1 = new JLabel("username");
	private JLabel label2 = new JLabel("password");
	private JButton loginButton = new JButton("Login");
	private JButton SigninButton = new JButton("Sign in");
	private JTextArea usernameTextArea = new JTextArea();
	private JPasswordField passwordField = new JPasswordField();

	private int IsSending = 0;

	private class sendMesg implements Runnable {
		private String username;
		private String password;
		private int met;

		sendMesg(String str1, String str2, int met) {
			this.username = str1;
			this.password = str2;
			this.met = met;
		}

		@Override
		public void run() {
			IsSending = 1;
			try {
				s.getMsgToServer().writeUTF(username);
				s.getMsgToServer().writeUTF(password);
				int check = s.getMsgFromServer().readInt();

				if (met == Flag.LOGIN) {
					if (check == Flag.SUCCESS) {// 登陆成功
						// ClientWindow cw = new ClientWindow(s);// 打开用户界面
						// cw.addWindowListener(new WindowAdapter() {
						// public void windowClosing(WindowEvent e) {
						// System.exit(0);
						// }
						// });
						// lg.setVisible(false);
						JOptionPane.showMessageDialog(lg, "没问题");
					} else if (check == Flag.FAIL) {
						JOptionPane.showMessageDialog(lg, "用户名或密码错误");
					}
				} else if (met == Flag.SIGNUP) {
					if (check == Flag.SUCCESS)
						JOptionPane.showMessageDialog(lg, "注册成功");
					else if (check == Flag.FAIL)
						JOptionPane.showMessageDialog(lg, "用户已存在");
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			IsSending = 0;
		}
	}

	Login(ServerConnection s) {// 这里还是写socket吧，可能需要用到判断是否连接之类的功能
		this.s = s;

		this.setLayout(new BorderLayout());
		usernamePanel.add(label1);
		usernameTextArea.setColumns(10);
		usernamePanel.add(usernameTextArea);

		passwordPanel.add(label2);
		passwordField.setColumns(10);
		passwordPanel.add(passwordField);

		textPanel.setLayout(new FlowLayout());
		textPanel.add(usernamePanel);
		textPanel.add(passwordPanel);
		textPanel.setSize(100, 50);

		loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (IsSending == 1) {
					return;
				}
				try {
					s.getMsgToServer().writeInt(1);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				sendMesg messenger = new sendMesg(usernameTextArea.getText(),
						String.valueOf(passwordField.getPassword()), 1);
				new Thread(messenger).start();
			}
		});

		SigninButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (IsSending == 1) {
					return;
				}
				try {
					s.getMsgToServer().writeInt(2);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				sendMesg messenger = new sendMesg(usernameTextArea.getText(),
						String.valueOf(passwordField.getPassword()), 2);
				new Thread(messenger).start();
			}
		});

		buttonPanel.add(loginButton);
		buttonPanel.add(SigninButton);

		this.add(textPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);

		this.setSize(300, 150);
		this.setLocation(600, 300);

		this.lg = this;

		this.setVisible(true);
	}
}

class ServerConnection {
	private Socket MsgSocket;
	private Socket FileSocket;
	private DataInputStream MsgFromServer;
	private DataOutputStream MsgToServer;
	private DataInputStream FileFromServer;
	private DataOutputStream FileToServer;

	ServerConnection(Socket msg, Socket file) {
		this.MsgSocket = msg;
		this.FileSocket = file;
		try {
			MsgFromServer = new DataInputStream(MsgSocket.getInputStream());
			MsgToServer = new DataOutputStream(MsgSocket.getOutputStream());
			FileFromServer = new DataInputStream(FileSocket.getInputStream());
			FileToServer = new DataOutputStream(FileSocket.getOutputStream());
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}

	boolean check() {// 判断两个Socket是否连接到同一个用户
		int tip = Flag.FAIL;
		try {
			int a = MsgFromServer.readInt();
			int b = FileFromServer.readInt();
			if (a == b) {
				tip = Flag.SUCCESS;
				MsgToServer.writeInt(Flag.SUCCESS);
			} else {
				tip = Flag.FAIL;
				MsgToServer.writeInt(Flag.FAIL);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (tip == Flag.SUCCESS);
	}

	DataInputStream getMsgFromServer() {
		return MsgFromServer;
	}

	DataOutputStream getMsgToServer() {
		return MsgToServer;
	}

	DataInputStream getFileFromServer() {
		return FileFromServer;
	}

	DataOutputStream getFileToServer() {
		return FileToServer;
	}
}
