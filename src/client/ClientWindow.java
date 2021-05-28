package client;

import javax.swing.*;
import javax.swing.plaf.metal.MetalIconFactory;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

// 传进用户ID，用s连接到服务器,创建界面（做按钮、界面列表、个人信息、朋友圈入口）
// 按钮连接到聊天框、朋友圈界面
class ClientWindow extends JFrame implements Flag {

	HashMap<String, ChatWindow> chatWindows = new HashMap<>();// 这里改成了ChatWindow
	private final ServerConnection sc;
	ClientWindow cw = this;
	private final String myPath;

	ClientWindow(ServerConnection s) {
		this.sc = s;
		myPath = sc.getParentFile().getAbsolutePath() + "/" + sc.getSelfName();
		// 同步消息
		if (!checkUpdate(sc)) {
			JOptionPane.showMessageDialog(cw, "服务器和客户端同步失败");
		}
		// 接受服务器消息
		new HandleASession(sc);
		// 创建选项卡
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
		tabbedPane.setPreferredSize(new Dimension(300, 300));
		tabbedPane.setBorder(null);
		tabbedPane.setBackground(Color.white);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		// 创建整个好友列表
		// createScrollPanel(tabbedPane, Flag.RECENTPANE);
		createScrollPanel(tabbedPane, Flag.FRIENDPANE);
		createScrollPanel(tabbedPane, Flag.GROUPPANE);
		// createScrollPanel(tabbedPane, Flag.PYQ);

		tabbedPane.setEnabledAt(1, true);
		tabbedPane.setSelectedIndex(0);
		tabbedPane.addMouseListener(new MouseAdapter() {
			@Override public void mouseEntered(MouseEvent e) {
				super.mouseEntered(e);
				tabbedPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			@Override public void mouseExited(MouseEvent e) {
				super.mouseExited(e);
				tabbedPane.setCursor(Cursor.getDefaultCursor());
			}
		});

		// 最后把选项卡放入frame
		setIconImage(
				Toolkit.getDefaultToolkit().getImage(System.getProperty("user.dir") + "/src/client/system/icon.png"));
		this.add(tabbedPane, BorderLayout.CENTER);

	}

	private boolean checkUpdate(ServerConnection s) {
		String[] files = { "friendList.txt", "groupList.txt" };
		String lastLine;
		try {
			// 发送请求，发送最后一行，接受信号
			s.getFileToServer().writeInt(Flag.CHECKUPDATE);
			for (String file : files) {
				lastLine = MyUtil.readLastLine(new File(myPath + "/" + file), null);
				if (lastLine == null) {
					return false;
				}
				s.getFileToServer().writeUTF(lastLine);
				// 接受是否更新的信号
				int a = s.getFileFromServer().readInt();
				switch (a) {
				//更新
				case Flag.LOCALUPDATE -> {
					String cachePath = myPath + "/cache/" + file;
					s.receiveFile(cachePath);
					Files.copy(new File(cachePath).toPath(), new File(myPath + "/" + file).toPath(),
							StandardCopyOption.REPLACE_EXISTING);
				}
				case Flag.NOUPDATE -> {
				}
				default -> throw new IllegalStateException("Unexpected value: " + a);
				}
			}
			// 结束后,发送end
			s.getFileToServer().writeUTF("end");


		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void createScrollPanel(JTabbedPane tabbedPane, int id) {
		switch (id) {
		case FRIENDPANE -> {
			createPane(tabbedPane, new MetalIconFactory.TreeControlIcon(true), "friendList.txt", "好友列表",
					"这里有你和所有好友聊天的信息", FRIENDPANE);
		}
		case GROUPPANE -> {
			createPane(tabbedPane, new MetalIconFactory.TreeLeafIcon(), "groupList.txt", "群组列表", "这里有你和所有好友聊天的信息",
					GROUPPANE);
		}
		case PYQ -> {
			createPane(tabbedPane, new MetalIconFactory.FolderIcon16(), "friendList.txt", "朋友圈", "这里有你和所有群组的信息", PYQ);
		}
		case RECENTPANE -> {
			createPane(tabbedPane, new MetalIconFactory.PaletteCloseIcon(), "", "最近消息", "这里有你和所有最近聊天的信息", RECENTPANE);
		}
		}
	}

	private void createPane(JTabbedPane tabbedPane, Icon paneIcon, String list, String title, String tip, int sign) {
		JPanel TargetPane = new JPanel();
		TargetPane.setLayout(new GridLayout(20, 1));
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(
					new File(sc.getParentFile(), sc.getSelfName() + "/" + list)));// 文件路径调用前面的，尽量不要重新写_改了个地方
			String tmp;
			while ((tmp = br.readLine()) != null) {
				if (sign == FRIENDPANE) {
					TargetPane.add(new chatPanel(tmp, tmp, sign));
				} else if (sign == GROUPPANE) {
					String name = br.readLine();
					TargetPane.add(new chatPanel(name, tmp, sign));
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		JScrollPane scrollPane = new JScrollPane(TargetPane);// 这里滚不起来，应该是tabbedPane的大小问题
		tabbedPane.addTab(title, paneIcon, scrollPane, tip);
	}

	public void deleteWindow(String ID) {
		chatWindows.remove(ID);
		// System.out.println(friendWindows.size());
	}

	class chatPanel extends JPanel {// 这是选项卡类，在这里面实现对好友、群组、最近消息框的创建
		chatPanel panel = this;
		String TargetId = "";
		String TargetName = "";
		int sign;
		JLabel filler;

		chatPanel() {
			panel.setLayout(new GridLayout(1, 1));
			panel.setBackground(Color.white);
			this.addMouseListener(new MouseListener() {
				@Override public void mouseClicked(MouseEvent e) {// 这里是不是应该释放一下？
					if (chatWindows.containsKey(TargetId)) {
						chatWindows.get(TargetId).setVisible(true);
					} else {
						if (sign == FRIENDPANE) {
							chatWindows.put(TargetId, new FriendWindow(sc, TargetId, cw));
						} else if (sign == GROUPPANE) {
							chatWindows.put(TargetId, new GroupWindow(sc, TargetId, TargetName, cw));
						}
					}
				}

				@Override public void mousePressed(MouseEvent e) {
					panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}

				@Override public void mouseReleased(MouseEvent e) {
					panel.setCursor(Cursor.getDefaultCursor());
				}

				@Override public void mouseEntered(MouseEvent e) {
					panel.setBackground(Color.lightGray);
				}

				@Override public void mouseExited(MouseEvent e) {
					panel.setBackground(Color.white);
				}
			});
		}

		chatPanel(String name, String id, int sign) {
			this();
			this.TargetId = id;
			this.TargetName = name;
			this.sign = sign;
			filler = new JLabel(name);
			panel.add(filler);
			filler.setHorizontalAlignment(JLabel.CENTER);
		}
	}

	class HandleASession {
		private File filePath;// 在这里实现信息交互
		ServerConnection s;
		Reciever reciever = new Reciever();
		Sender sender = new Sender();
		Queue<MsgPair> MsgQueue = new LinkedList<>();
		int go = 1;

		HandleASession(ServerConnection s) {
			this.s = s;
			new Thread(reciever).start();
			new Thread(sender).start();
			filePath = new File(System.getProperty("user.dir") + "/src/client/users/" + s.getSelfName());
		}

		private class MsgPair {
			int flag;
			String MsgString;

			MsgPair(int flag, String Msg) {
				this.flag = flag;
				this.MsgString = Msg;
			}
		}

		private void AddMsgToFile(String username, MsgPair mp) {// 图片怎么办？，但应该差别不大-图片另说//目前仅考虑了TEXT
			File file = new File(filePath.getParentFile(), username + "/MsgQ.txt");
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			BufferedWriter bw;
			try {
				bw = new BufferedWriter(new FileWriter(file));
				bw.write("" + mp.flag + "\n");
				bw.write(mp.MsgString + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private class Reciever implements Runnable {

			@Override public void run() {
				String message;
				while (true) {// 接收到一个信息——信息格式是什么样的？——如果是图片、群聊呢
					int sign;
					try {
						sign = s.getMsgFromServer().readInt();
						switch (sign) {
						case SENDFILE -> {

							// message = s.getFileFromServer().readNBytes();
						}
						case SENDTEXT -> // 先实现这部分功能尝试一下运行
								{
									message = s.getMsgFromServer().readUTF();
									String[] split = message.split("\\|");
									// 若为已打开窗口则写入窗口中
									if (chatWindows.containsKey(split[1])) {
										chatWindows.get(split[1]).AddMessage(message);
									}
									// 写入本地文件
									File chatRecord = new File(s.getParentFile(),
											s.getSelfName() + "/friendMsg/" + split[1] + ".txt");
									PrintWriter pw = new PrintWriter(new FileOutputStream(chatRecord, true));
									pw.println(message);
									pw.close();
									// 写入用户主窗口
								}
						// 收到请求加好友的信息
						case ADDFRIEND -> {

						}
						// 收到同意加好友的信息
						case ACCEPTFRIEND -> {

						}
						case CREATEGROUP -> {

						}
						case DELETEFRIEND -> {

						}
						case DELETEGROUP -> {

						}
						case QUITGROUP -> {

						}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private class Sender implements Runnable {

			public void stop() {
				go = 0;
			}

			public void PutMsg(MsgPair ss) {
				MsgQueue.add(ss);
			}

			@Override public void run() {
				while (go == 1) {
					try {
						Thread.sleep(5);// 为啥能用了？
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					if (!MsgQueue.isEmpty()) {
						// 这个改成锁
						// System.out.println("排好队！");
						MsgPair mp = MsgQueue.poll();
						try {
							s.getMsgToServer().writeInt(mp.flag);
							s.getMsgToServer().writeUTF(mp.MsgString);// 失败后写文件(吗？)
						} catch (IOException e) {
							// 写文件
							AddMsgToFile(s.getSelfName(), mp);
							e.printStackTrace();
							System.out.println("Send to " + s.getSelfName() + " error!");
						}
					}
				}
			}

		}

	}
}