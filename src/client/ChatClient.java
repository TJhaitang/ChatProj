package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class ChatClient {
	private Socket MsgSocket;
	private Socket FileSocket;

	public static void main(String[] args) {
		new ChatClient();
	}

	ChatClient() {
		try {
			MsgSocket = new Socket("localhost", 8888);// 建立连接
			FileSocket = new Socket("localhost", 8888);
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

	private final ServerConnection s;

	private JPanel usernamePanel = new JPanel();
	private JPanel passwordPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();
	private JPanel textPanel = new JPanel();
	private JLabel label1 = new JLabel("username");
	private JLabel label2 = new JLabel("password");
	private JButton loginButton = new JButton("登录");
	private JButton SigninButton = new JButton("注册");
	private final JTextArea usernameTextArea = new JTextArea("admin");
	private final JPasswordField passwordField = new JPasswordField("123456");

	private int IsSending = 0;

	private class sendMesg implements Runnable {
		private final String username;
		private final String password;
		private final int met;

		sendMesg(String str1, String str2, int met) {
			this.username = str1;
			this.password = str2;
			this.met = met;
		}

		private Boolean loginClientWindow() {
			// 在此处获得自己的名字
			s.setSelfName(username);
			ClientWindow clientWindow = new ClientWindow(s);
			clientWindow.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});

			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			clientWindow.setBounds(screen.width - 500, (screen.height - 800) / 2, 400, 800);
			clientWindow.setVisible(true);
			// clientWindow.setResizable(false);
			return true;
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
						if (loginClientWindow()) {
							lg.setVisible(false);
						} else {
							JOptionPane.showMessageDialog(lg, "登录失败");
						}
					} else if (check == Flag.FAIL) {
						JOptionPane.showMessageDialog(lg, "用户名或密码错误");
					}
				} else if (met == Flag.SIGNUP) {
					if (check == Flag.SUCCESS) {
						// 创建文件
						createNewUser(username);
						JOptionPane.showMessageDialog(lg, "注册成功");

					} else if (check == Flag.FAIL) {
						JOptionPane.showMessageDialog(lg, "用户已存在");
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			IsSending = 0;
		}
	}

	private void createNewUser(String name) {
		File user = new File(System.getProperty("user.dir") + "/src/client/users/" + name);
		if (user.mkdir()) {
			try {
				new File(user.getAbsolutePath() + "/config.txt").createNewFile();
				new File(user.getAbsolutePath() + "/friendList.txt").createNewFile();
				new File(user.getAbsolutePath() + "/groupList.txt").createNewFile();
				new File(user.getAbsolutePath() + "/update.txt").createNewFile();
				new File(user.getAbsolutePath() + "/groupMsg").mkdir();
				new File(user.getAbsolutePath() + "/friendMsg").mkdir();
				new File(user.getAbsolutePath() + "/file").mkdir();
				new File(user.getAbsolutePath() + "/cache").mkdir();
				new File(user.getAbsolutePath() + "/image").mkdir();
				new File(user.getAbsolutePath() + "/friendIcon").mkdir();

			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "创建失败！");
				e.printStackTrace();
			}
		}
	}

	Login(ServerConnection s) {
		this.s = s;
		this.lg = this;

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
				if (usernameTextArea.getText() == null || usernameTextArea.getText().equals("")
						|| passwordField.getPassword().length == 0) {
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
				if (usernameTextArea.getText() == null || usernameTextArea.getText().equals("")
						|| passwordField.getPassword().length == 0) {
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

		usernameTextArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (IsSending == 1) {
						return;
					}
					if (usernameTextArea.getText() == null || usernameTextArea.getText().equals("")
							|| passwordField.getPassword().length == 0) {
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
				} else if (e.getKeyCode() == KeyEvent.VK_TAB) {// 可能还有问题，用户名后可能有\t和\n
					passwordField.requestFocus();
				}
			}
		});

		passwordField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (IsSending == 1) {
						return;
					}
					if (usernameTextArea.getText() == null || usernameTextArea.getText().equals("")
							|| passwordField.getPassword().length == 0) {
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
			}
		});

		buttonPanel.add(loginButton);
		buttonPanel.add(SigninButton);

		this.add(textPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);

		JLabel titleLabel = new JLabel("聊天室");
		this.add(titleLabel, BorderLayout.NORTH);

		this.setSize(300, 150);
		this.setLocation(600, 300);

		this.lg = this;
		this.setTitle("登录");

		this.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		setIconImage(
				Toolkit.getDefaultToolkit().getImage(System.getProperty("user.dir") + "/src/client/system/icon.png"));
		this.setVisible(true);
	}
}

class ServerConnection {
	private String SelfName;
	private Socket MsgSocket;
	private Socket FileSocket;
	private DataInputStream MsgFromServer;
	private DataOutputStream MsgToServer;
	private DataInputStream FileFromServer;
	private DataOutputStream fileToServer;
	private final File parentFile = new File(System.getProperty("user.dir") + "/src/client/users");
	// 怎么获取当前代码文件的路径？

	ServerConnection() {
	}

	ServerConnection(Socket msg, Socket file) {
		System.out.println(parentFile.getAbsolutePath());
		this.MsgSocket = msg;
		this.FileSocket = file;
		try {
			MsgFromServer = new DataInputStream(MsgSocket.getInputStream());
			MsgToServer = new DataOutputStream(MsgSocket.getOutputStream());
			FileFromServer = new DataInputStream(FileSocket.getInputStream());
			fileToServer = new DataOutputStream(FileSocket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
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
		return fileToServer;
	}

	void setSelfName(String s) {
		this.SelfName = s;
	}

	String getSelfName() {
		return this.SelfName;
	}

	File getParentFile() {
		return this.parentFile;
	}

	void uploadFile(String filePath) {
		try {
			// 先发送文件大小
			long length = new File(filePath).length();
			fileToServer.writeLong(length);
			// 发送文件
			FileInputStream fis = new FileInputStream(filePath);
			byte[] buffer = new byte[8192];
			int read;
			while ((read = fis.read(buffer, 0, 8192)) >= 0) {
				fileToServer.write(buffer, 0, read);
			}
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void receiveFile(String filePath) {
		DataInputStream fileFromServer = getFileFromServer();
		try {
			// 先得到长度
			long length = fileFromServer.readLong();
			FileOutputStream fos = new FileOutputStream(filePath);
			byte[] buffer = new byte[8192];
			int read, transferred = 0;
			while (length > transferred) {
				read = fileFromServer.read(buffer, 0, 8192);
				fos.write(buffer, 0, read);
				transferred += read;
			}
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("缓存失败");
		}
	}
}
