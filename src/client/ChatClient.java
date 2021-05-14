package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatClient {
	private Socket s;
	private DataInputStream fromServer = null;
	private DataOutputStream toServer = null;

	public static void main(String[] args) {
		ChatClient cc = new ChatClient();
	}

	ChatClient() {
		try {
			s = new Socket("localhost", 12138);
			fromServer = new DataInputStream(s.getInputStream());
			toServer = new DataOutputStream(s.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Login lg = new Login(fromServer, toServer);
		lg.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}
}

class Login extends JFrame {
	private Login lg;

	private DataInputStream fromServer;
	private DataOutputStream toServer;

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
			try {
				toServer.writeUTF(username);
				toServer.writeUTF(password);
				int check = fromServer.readInt();

				if (met == 1) {
					if (check == 1) {// 登陆成功
						 ClientWindow cw = new ClientWindow(username, s);// 打开用户界面
						 cw.addWindowListener(new WindowAdapter() {
						 public void windowClosing(WindowEvent e) {
						 System.exit(0);
						 }
						 });
						 lg.setVisible(false);
						JOptionPane.showMessageDialog(lg, "没问题,请退出");// 测试代码用
					} else if (check == 0) {
						JOptionPane.showMessageDialog(lg, "用户名或密码错误");
					}
				} else if (met == 2) {
					if (check == 1)
						JOptionPane.showMessageDialog(lg, "注册成功");
					else if (check == 0)
						JOptionPane.showMessageDialog(lg, "用户已存在");
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	Login(DataInputStream fServer, DataOutputStream tServer) {// 这里还是写socket吧，可能需要用到判断是否连接之类的功能
		this.fromServer = fServer;
		this.toServer = tServer;

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
				try {
					toServer.writeInt(1);
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
				try {
					toServer.writeInt(2);
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
