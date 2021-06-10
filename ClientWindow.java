package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.metal.MetalIconFactory;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;

// 传进用户ID，用s连接到服务器,创建界面（做按钮、界面列表、个人信息、朋友圈入口）
// 按钮连接到聊天框、朋友圈界面
class ClientWindow extends JFrame implements Flag {

	private final HashMap<String, ChatWindow> chatWindows = new HashMap<>();// 这里改成了ChatWindow
	private final HashMap<String, ChatPanel> chatPanels = new HashMap<>();// 这里改成了ChatWindow
	private final HashMap<String, ChatPanel> chatPanels2 = new HashMap<>();// 这里改成了ChatWindow
	private final ServerConnection sc;
	private final ClientWindow cw = this;
	private final String myPath;
	public HandleASession hand;

	private final JTabbedPane tabbedPane;
	private TargetList FriendList;
	private TargetList GroupList;
	private TargetList RecentList = new TargetList();
	private final TargetList MsgList = new TargetList();
	private final UserPanel userPanel;

	ClientWindow(ServerConnection s) {
		this.setLayout(null);
		this.sc = s;
		myPath = sc.getParentFile().getAbsolutePath() + "/" + sc.getSelfName();
		// 同步消息
		if (!checkUpdate(sc)) {
			JOptionPane.showMessageDialog(cw, "服务器和客户端同步失败");
		}
		// 接受服务器消息
		hand = new HandleASession(sc);
		// 创建UI界面
		// 创建选项卡
		tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
		tabbedPane.setPreferredSize(new Dimension(400, 800));
		tabbedPane.setBackground(Color.white);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		// 创建整个好友列表
		createScrollPanel(tabbedPane, Flag.FRIENDPANE);
		createScrollPanel(tabbedPane, Flag.GROUPPANE);
		createScrollPanel(tabbedPane, Flag.MESSAGE);
		createScrollPanel(tabbedPane, Flag.RECENTPANE);
		//		tabbedPane.setEnabledAt(1, true);
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
		userPanel = new UserPanel();
		userPanel.setPreferredSize(new Dimension(400, 800));
		this.add(userPanel);
		this.add(tabbedPane);

		this.addComponentListener(new ComponentAdapter() {
			@Override public void componentResized(ComponentEvent e) {
				int width = getContentPane().getWidth();
				int height = getContentPane().getHeight();
				userPanel.setBounds(0, 0, width, 70);
				userPanel.addButton.setBounds(width - 25, 70 - 25, 20, 20);
				userPanel.textField.setBounds(width - 200, 70 - 25, 170, 20);
				tabbedPane.setBounds(0, 70, width, height - 70);
				super.componentResized(e);
			}
		});
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
				// 更新
				case Flag.LOCALUPDATE -> {
					String cachePath = myPath + "/cache/" + file;
					s.receiveFile(cachePath);
					Files.copy(new File(cachePath).toPath(), new File(myPath + "/" + file).toPath(),
							StandardCopyOption.REPLACE_EXISTING);
					new File(cachePath).delete();
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
		case FRIENDPANE -> createPane(tabbedPane, new MetalIconFactory.TreeControlIcon(true), "friendList.txt", "好友列表",
				"这里有你和所有好友聊天的信息", FRIENDPANE);
		case GROUPPANE -> createPane(tabbedPane, new MetalIconFactory.TreeLeafIcon(), "groupList.txt", "群组列表",
				"这里有你和所有群聊天的信息", GROUPPANE);
		case MESSAGE -> createMsgPane(tabbedPane, new MetalIconFactory.FolderIcon16());
		case RECENTPANE -> createPane(tabbedPane, new MetalIconFactory.FolderIcon16(), "聊天信息", "最近消息", "这里有你和所有最近聊天的信息",
				RECENTPANE);
		}
	}

	private void createMsgPane(JTabbedPane tabbedPane, Icon paneIcon) {
		JScrollPane scrollPane = new JScrollPane(MsgList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		// 设置滚轮速度(默认的太慢了)
		scrollPane.getVerticalScrollBar().setUnitIncrement(13);
		tabbedPane.addTab("消息列表", paneIcon, scrollPane, "这里是你的消息通知列表");
		MsgList.add(new MsgPanel(new MsgPack(0, "", "")) {
			@Override String getMsgString(MsgPack mp) {
				return "测试";
			}

			@Override void Send(String IsAccept) {
				MsgList.remove(panel);
			}
		});
		MsgList.add(new MsgPanel(new MsgPack(0, "", "")) {
			@Override String getMsgString(MsgPack mp) {
				return "测试";
			}

			@Override void Send(String IsAccept) {
				MsgList.remove(panel);
			}
		});
		MsgList.add(new MsgPanel(new MsgPack(0, "", "")) {
			@Override String getMsgString(MsgPack mp) {
				return "测试";
			}

			@Override void Send(String IsAccept) {
				MsgList.remove(panel);
			}
		});
	}

	private void createPane(JTabbedPane tabbedPane, Icon paneIcon, String list, String title, String tip, int sign) {
		TargetList targetPane = new TargetList();
		targetPane.setLayout(new FlowLayout(FlowLayout.CENTER));
		// 最近消息最后再创建
		if (sign == RECENTPANE) {
			List<Map.Entry<String, ChatPanel>> arrayList = new ArrayList<>(chatPanels2.entrySet());
			arrayList.sort((o1, o2)->o2.getValue().timeArea.getText().compareTo(o1.getValue().timeArea.getText()));
			for (Map.Entry<String, ChatPanel> stringChatPanelEntry : arrayList) {
				targetPane.add(stringChatPanelEntry.getValue());
			}
			RecentList = targetPane;
			JScrollPane scrollPane = new JScrollPane(targetPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			// 设置滚轮速度(默认的太慢了)
			scrollPane.getVerticalScrollBar().setUnitIncrement(13);
			tabbedPane.insertTab(title, paneIcon, scrollPane, tip, 0);
			return;
		} else {
			// 好友和群组的列表
			BufferedReader br;
			try {
				// 文件路径调用前面的
				br = new BufferedReader(new FileReader(new File(sc.getParentFile(), sc.getSelfName() + "/" + list)));
				String tmp;
				while ((tmp = br.readLine()) != null) {
					if (sign == FRIENDPANE) {
						ChatPanel chatPanel = new ChatPanel(tmp, tmp, sign);
						chatPanels.put(tmp, chatPanel);
						chatPanels2.put(tmp, new ChatPanel(tmp, tmp, sign));
						targetPane.add(chatPanel);
					} else if (sign == GROUPPANE) {
						String name = br.readLine();
						ChatPanel chatPanel = new ChatPanel(name, tmp, sign);
						chatPanels.put(tmp, chatPanel);
						chatPanels2.put(tmp, new ChatPanel(name, tmp, sign));
						targetPane.add(chatPanel);
					}
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (sign == FRIENDPANE) {
				FriendList = targetPane;
			} else if (sign == GROUPPANE) {
				GroupList = targetPane;
			}
		}

		JScrollPane scrollPane = new JScrollPane(targetPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		// 设置滚轮速度(默认的太慢了)
		scrollPane.getVerticalScrollBar().setUnitIncrement(13);
		tabbedPane.addTab(title, paneIcon, scrollPane, tip);
	}

	public void deleteWindow(String ID) {
		chatWindows.remove(ID);
		// System.out.println(friendWindows.size());
	}

	public void addMessage(String s) {
		MsgList.add(new MsgPanel(s) {

			@Override String getMsgString(MsgPack mp) {
				return null;
			}

			@Override void Send(String IsAccept) {
				MsgList.remove(this);
			}

		});
	}

	private class UserPanel extends JPanel {
		public JButton addButton = new JButton("十");
		public JTextField textField = new JTextField();
		private JLabel UserName;
		private JLabel pictureArea;
		private final JPanel panel = this;

		UserPanel() {
			this.setLayout(null);
			this.setSize(400, 70);
			this.setBackground(Color.white);
			UserName = new JLabel(sc.getSelfName(), JLabel.LEFT);
			BufferedImage bi = null;
			File imageFile = new File(myPath + "/image/" + sc.getSelfName() + ".jpg");
			if (!imageFile.exists()) {
				imageFile = new File(sc.getParentFile().getParent() + "/system/IconDefault.jpg");
			}
			try {
				bi = ImageIO.read(imageFile);

			} catch (IOException e) {
				e.printStackTrace();
			}
			BufferedImage newBI = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
			newBI.getGraphics().drawImage(bi.getScaledInstance(50, 50, Image.SCALE_SMOOTH), 0, 0, null);
			ImageIcon ic = new ImageIcon(newBI);
			pictureArea = new JLabel();
			pictureArea.setIcon(ic);

			pictureArea.setBounds(10, 10, 50, 50);
			UserName.setBounds(70, 10, 200, 20);
			this.add(pictureArea);
			this.add(UserName);

			textField.setColumns(10);
			addButton.setMargin(new Insets(0, 0, 0, 0));
			addButton.addActionListener(e->
			{
				JPopupMenu popMenu = new JPopupMenu();
				JMenuItem addFriend = new JMenuItem("加好友");
				JMenuItem creatGroup = new JMenuItem("创建群聊");
				popMenu.add(addFriend);
				popMenu.add(creatGroup);

				// 写一下输入检查
				addFriend.addActionListener(e1->
				{
					String str = textField.getText();
					// if (!str.contains("\\|")) {
					// JOptionPane.showMessageDialog(cw, "输入格式有误！\n输入格式为:\n好友1|好友2|......");
					// return;
					// }
					textField.setText("");
					hand.PutMsg(new MsgPack(Flag.ADDFRIEND, str, sc.getSelfName() + "|" + str));
				});

				creatGroup.addActionListener(e12->
				{
					String str = textField.getText();
					// if (!str.contains(":")) {
					// JOptionPane.showMessageDialog(cw, "输入格式有误！\n输入格式为:\n群名:群成员1|群成员2|......");
					// return;
					// }
					textField.setText("");
					String groupName = str.split(":")[0];
					str = str.split(":")[1];
					hand.PutMsg(new MsgPack(Flag.CREATEGROUP, str, sc.getSelfName() + "|" + groupName));
				});

				popMenu.show(cw, panel.getWidth() - 25, panel.getHeight() - 5);
			});
			this.add(textField);
			this.add(addButton);
		}
	}

	private class TargetList extends JPanel {// 这是列表类，里面有列表内容与相关方法
		int count = 0;

		TargetList() {
			this.setLayout(new FlowLayout(FlowLayout.CENTER));
		}

		@Override public Component add(Component comp) {
			count += 1;
			this.setPreferredSize(new Dimension(265, 70 * count));
			return super.add(comp);
		}

		@Override public void remove(Component comp) {
			count -= 1;
			this.setPreferredSize(new Dimension(265, 70 * count));
			super.remove(comp);
			this.repaint();
			this.revalidate();
		}
	}

	private abstract class MsgPanel extends JPanel {
		protected JPanel panel = this;
		protected MsgPack mp;
		protected JLabel Msg;
		protected JButton AcceptButton = new JButton("接受");
		protected JButton RefuseButton = new JButton("拒绝");

		MsgPanel() {
			this.setPreferredSize(new Dimension(265, 70));
			this.setLayout(null);
			this.setBackground(Color.white);

			AcceptButton.setBounds(65, 38, 60, 25);
			RefuseButton.setBounds(140, 38, 60, 25);

			AcceptButton.addActionListener(e->Send("Accept"));

			RefuseButton.addActionListener(e->Send("Refuse"));

			this.add(AcceptButton);
			this.add(RefuseButton);

			this.addMouseListener(new MouseListener() {

				@Override public void mouseClicked(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override public void mouseEntered(MouseEvent e) {
					panel.setBackground(Color.lightGray);
				}

				@Override public void mouseExited(MouseEvent e) {
					panel.setBackground(Color.white);
				}
			});
		}

		MsgPanel(MsgPack mpk) {
			this();
			this.mp = mpk;
			String MsgString = getMsgString(mp);
			Msg = new JLabel(MsgString, JLabel.CENTER);
			Msg.setBounds(0, 0, 265, 35);
			this.add(Msg);
		}

		MsgPanel(String str) {
			this();
			Msg = new JLabel(str, JLabel.CENTER);
			Msg.setBounds(0, 0, 265, 35);
			this.add(Msg);
		}

		abstract String getMsgString(MsgPack mp);

		abstract void Send(String IsAccept);
	}

	private class ChatPanel extends JPanel {// 这是选项卡类，在这里面实现对好友、群组、最近消息框的创建
		private final ChatPanel panel = this;
		private String TargetId = "";
		private String TargetName = "";
		private int sign;
		private JLabel recentTextArea;
		private JLabel timeArea;

		ChatPanel() {
			panel.setPreferredSize(new Dimension(265, 70));
			panel.setLayout(null);
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

		ChatPanel(String name, String id, int sign) {
			this();
			this.TargetId = id;
			this.TargetName = name;
			this.sign = sign;
			try {
				recentTextArea = new JLabel();
				timeArea = new JLabel();
				// 名字的控件
				JLabel nameArea = new JLabel(TargetName);
				nameArea.setHorizontalAlignment(JLabel.LEFT);
				// 头像的控件
				JLabel pictureArea = new JLabel();
				BufferedImage bi;
				File imageFile;
				if (sign == Flag.FRIENDPANE) {
					imageFile = new File(myPath + "/friendIcon/" + id + ".jpg");
				} else {
					imageFile = new File(myPath + "/groupIcon/" + id + ".jpg");
				}
				if (!imageFile.exists()) {
					imageFile = new File(sc.getParentFile().getParent() + "/system/IconDefault.jpg");
				}
				bi = ImageIO.read(imageFile);
				BufferedImage newBI = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
				newBI.getGraphics().drawImage(bi.getScaledInstance(50, 50, Image.SCALE_SMOOTH), 0, 0, null);
				ImageIcon ic = new ImageIcon(newBI);
				pictureArea.setIcon(ic);
				// 最近一条消息的控件

				updateRecentMsg(sign, "");

				recentTextArea.setFont(new Font("宋体", Font.PLAIN, 10));
				timeArea.setFont(new Font("宋体", Font.PLAIN, 10));
				// 添加控件
				panel.add(nameArea);
				panel.add(pictureArea);
				panel.add(recentTextArea);
				panel.add(timeArea);
				// 设置位置
				nameArea.setBounds(new Rectangle(70, 13, 85, 20));
				pictureArea.setBounds(new Rectangle(10, 10, 50, 50));
				recentTextArea.setBounds(new Rectangle(70, 35, 195, 20));
				timeArea.setBounds(new Rectangle(160, 13, 105, 20));
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("头像无法读取!");
			}
		}

		public void updateRecentMsg(int sign, String updateLine) {
			if (!updateLine.equals("")) {
				if ("IMG".equals(updateLine.split("\\|")[4])) {
					recentTextArea.setText(updateLine.split("\\|")[1] + ": " + "[图片]");
				} else if ("FILE".equals(updateLine.split("\\|")[4])) {
					recentTextArea.setText(updateLine.split("\\|")[1] + ": " + "[文件]");
				} else {
					recentTextArea.setText(updateLine.split("\\|")[1] + ": " + updateLine.split("\\|")[3]);
				}
				timeArea.setText(updateLine.split("\\|")[0]);
				return;
			}
			String lastLine;
			if (sign == Flag.FRIENDPANE) {
				lastLine = MyUtil.readLastLine(new File(myPath + "/friendMsg/" + TargetId + ".txt"), null);
			} else {
				// sign = GroupPane
				lastLine = MyUtil.readLastLine(new File(myPath + "/groupMsg/" + TargetId + ".txt"), null);
			}
			if (lastLine == null || "".equals(lastLine)) {
				recentTextArea.setText("");
				timeArea.setText("");
			} else {
				String[] split = lastLine.split("\\|");
				if ("IMG".equals(split[4])) {
					recentTextArea.setText(split[1] + ": " + "[图片]");
				} else if ("FILE".equals(split[4])) {
					recentTextArea.setText(split[1] + ": " + "[文件]");
				} else {
					recentTextArea.setText(split[1] + ": " + split[3]);
				}
				// 最近一条消息时间的控件
				// 怎么修改时间格式？
				timeArea.setText(split[0]);
			}
		}
	}

	public class HandleASession {
		private final File filePath;// 在这里实现信息交互
		ServerConnection s;
		Receiver receiver = new Receiver();
		Sender sender = new Sender();
		Queue<MsgPack> MsgQueue = new LinkedList<>();
		int go = 1;

		public void PutMsg(MsgPack ss) {
			MsgQueue.add(ss);
		}

		HandleASession(ServerConnection s) {
			this.s = s;
			new Thread(receiver).start();
			new Thread(sender).start();
			filePath = new File(System.getProperty("user.dir") + "/src/client/users/" + s.getSelfName());
		}

		// private void AddMsgToFile(String username, MsgPair mp) {//
		// 图片怎么办？，但应该差别不大-图片另说//目前仅考虑了TEXT
		// File file = new File(filePath.getParentFile(), username + "/MsgQ.txt");
		// if (!file.exists()) {
		// try {
		// file.createNewFile();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		// BufferedWriter bw;
		// try {
		// bw = new BufferedWriter(new FileWriter(file));
		// bw.write("" + mp.flag + "\n");
		// bw.write(mp.MsgString + "\n");
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }

		private class Receiver implements Runnable {
			String path = System.getProperty("user.dir") + "/src/client/users/";

			@Override public void run() {
				while (true) {// 接收到一个信息——信息格式是什么样的？——如果是图片、群聊呢
					int sign;
					String message, tar;
					try {
						sign = s.getMsgFromServer().readInt();
						tar = s.getMsgFromServer().readUTF();
						message = s.getMsgFromServer().readUTF();
						switch (sign) {
						case SENDFILE: {
							String name = message.split("\\|")[3];
							s.receiveFile(path + tar + "/cache/" + name);
							if (message.split("\\|")[1].equals(message.split("\\|")[5])) {
								break;
							}
						}
						case SENDTEXT:// 先实现这部分功能尝试一下运行
						{
							String[] split = message.split("\\|");
							if (chatWindows.containsKey(split[1]) && !split[1].equals(split[5])) {
								chatWindows.get(split[1]).AddMessage(message);
							}
							if (split[5].toCharArray()[0] != 'G') {// 若为已打开窗口则写入窗口中
								// 写入本地文件
								File chatRecord = new File(s.getParentFile(),
										s.getSelfName() + "/friendMsg/" + split[1] + ".txt");
								PrintWriter pw = new PrintWriter(new FileOutputStream(chatRecord, true));
								pw.println(message);
								cw.chatPanels.get(split[1]).updateRecentMsg(Flag.FRIENDPANE, message);
								cw.chatPanels2.get(split[1]).updateRecentMsg(Flag.FRIENDPANE, message);
								sortRecentPane(cw.chatPanels2.get(split[5]));
								pw.close();
							} else {
								// 写入本地文件
								File chatRecord = new File(s.getParentFile(),
										s.getSelfName() + "/groupMsg/" + split[5] + ".txt");
								PrintWriter pw = new PrintWriter(new FileOutputStream(chatRecord, true));
								cw.chatPanels.get(split[5]).updateRecentMsg(Flag.GROUPPANE, message);
								cw.chatPanels2.get(split[5]).updateRecentMsg(Flag.GROUPPANE, message);
								sortRecentPane(cw.chatPanels2.get(split[5]));
								pw.println(message);
								pw.close();
							}

							// 写入用户主窗口
							break;
						}
						// 收到请求加好友的信息
						case ADDFRIEND:// A加B好友：A|B
						{
							MsgList.add(new MsgPanel(new MsgPack(ADDFRIEND, message.split("\\|")[0], message)) {

								@Override String getMsgString(MsgPack mp) {
									String str = mp.TargetName + " 向你发来好友申请";
									return str;
								}

								@Override void Send(String IsAccept) {
									hand.PutMsg(new MsgPack(ACCEPTFRIEND, this.mp.TargetName,
											mp.MsgString + "|" + IsAccept));
									if (IsAccept.equals("Accept")) {
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
										FriendList.add(new ChatPanel(mp.MsgString.split("\\|")[0],
												mp.MsgString.split("\\|")[0], Flag.FRIENDPANE));
										FriendList.repaint();
										FriendList.revalidate();
									}
									MsgList.remove(panel);
								}

							});
							break;
							// 收到同意加好友的信息
						}
						case ACCEPTFRIEND: {// A加B好友：A|B|Accept/Refuse
							String[] split = message.split("\\|");
							if (split[2].equals("Accept")) {
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
									pw.println(split[1]);
									pw.close();
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								}
								addMessage(split[1] + "接受了你的好友请求");
								FriendList.add(new ChatPanel(split[1], split[1], Flag.FRIENDPANE));
							} else if (split[2].equals("Refuse")) {
								addMessage(split[1] + "拒绝了你的好友请求");
							}
							break;
						}
						case CREATEGROUP: {// A建群:A|ID|name
							String creater = message.split("\\|")[0];
							if (creater.equals(s.getSelfName())) {// 我套我这里写的时候是没睡醒么我写的是个啥啊
								addMessage("建群成功！群ID为: " + message.split("\\|")[1]);
								File file = new File(filePath, "/groupList.txt");
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
									pw.println(message.split("\\|")[1] + "\n" + message.split("\\|")[2]);
									pw.close();
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								}
								File groupHis = new File(filePath, "/groupMsg/" + message.split("\\|")[1] + ".txt");
								try {
									groupHis.createNewFile();
								} catch (IOException e) {
									e.printStackTrace();
								}
								GroupList.add(new ChatPanel(message.split("\\|")[2], message.split("\\|")[1],
										Flag.GROUPPANE));
								GroupList.repaint();
								GroupList.revalidate();
							} else {
								MsgList.add(new MsgPanel(new MsgPack(CREATEGROUP, message.split("\\|")[0], message)) {

									@Override String getMsgString(MsgPack mp) {
										String str = mp.MsgString.split("\\|")[0] + " 向你发来群聊邀请,群id为:" + mp.MsgString
												.split("\\|")[1];
										return str;
									}

									@Override void Send(String IsAccept) {
										if (IsAccept.equals("Accept")) {// 这么写的话，如果拒绝加群就不发了
											hand.PutMsg(new MsgPack(ACCEPTGROUP, this.mp.TargetName,
													mp.MsgString + "|" + IsAccept));// A|ID|NAME|ACCEPT
											File file = new File(filePath, "/groupList.txt");
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
												pw.println(mp.MsgString.split("\\|")[1] + "\n" + mp.MsgString
														.split("\\|")[2]);
												pw.close();
											} catch (FileNotFoundException e) {
												e.printStackTrace();
											}
											File groupHis = new File(filePath,
													"/groupMsg/" + mp.MsgString.split("\\|")[1] + ".txt");
											try {
												groupHis.createNewFile();
											} catch (IOException e) {
												e.printStackTrace();
											}
											GroupList.add(new ChatPanel(mp.MsgString.split("\\|")[2],
													mp.MsgString.split("\\|")[1], Flag.GROUPPANE));
											GroupList.repaint();
											GroupList.revalidate();
										}
										MsgList.remove(panel);
									}

								});
							}
							break;
						}
						case DELETEFRIEND: {// A删除自己:A
							break;
						}
						case DELETEGROUP: {// A删群：ID
							break;
						}
						case QUITGROUP: {// A退出群：ID|A
							break;
						}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private class Sender implements Runnable {

			@Override public void run() {
				while (go == 1) {
					try {
						Thread.sleep(5);// 为啥能用了？
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					if (!MsgQueue.isEmpty()) {
						MsgPack mp = MsgQueue.poll();
						switch (mp.flag) {
						case Flag.SENDTEXT -> SendText(mp);
						case Flag.SENDFILE -> sendFile(mp);
						default -> {
							try {
								s.getMsgToServer().writeInt(mp.flag);
								s.getMsgToServer().writeUTF(mp.TargetName);
								s.getMsgToServer().writeUTF(mp.MsgString);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						}
					}
				}
			}

			private void sendFile(MsgPack mp) {
				SendText(mp);
				s.uploadFile(mp.MsgString.split("\\|")[3]);
			}

			private void SendText(MsgPack mp) {
				try {
					s.getMsgToServer().writeInt(mp.flag);
					s.getMsgToServer().writeUTF(mp.TargetName);
					s.getMsgToServer().writeUTF(mp.MsgString);
					// 如果这是发信息，那么就写文件
					File chatRecord;
					if (mp.TargetName.toCharArray()[0] != 'G') {
						chatRecord = new File(s.getParentFile(),
								s.getSelfName() + "/friendMsg/" + mp.TargetName + ".txt");
					} else {
						chatRecord = new File(s.getParentFile(),
								s.getSelfName() + "/groupMsg/" + mp.TargetName + ".txt");
					}
					PrintWriter pw = new PrintWriter(new FileOutputStream(chatRecord, true));
					pw.println(mp.MsgString);
					cw.chatPanels.get(mp.TargetName).updateRecentMsg(Flag.FRIENDPANE, mp.MsgString);
					cw.chatPanels2.get(mp.TargetName).updateRecentMsg(Flag.FRIENDPANE, mp.MsgString);
					sortRecentPane(cw.chatPanels2.get(mp.TargetName));
					pw.close();
				} catch (IOException e) {
					// 写文件
					// AddMsgToFile(s.getSelfName(), mp);
					e.printStackTrace();
					System.out.println("Send to " + s.getSelfName() + " error!");
				}
			}

		}

	}

	// 最近消息重新排序
	private void sortRecentPane(ChatPanel chatPanel) {
		RecentList.remove(chatPanel);
		RecentList.add(chatPanel, 0);
	}
}

class MsgPack {
	int flag;
	String TargetName;
	String MsgString;

	MsgPack(int flag, String TarName, String Msg) {
		this.flag = flag;
		this.TargetName = TarName;
		this.MsgString = Msg;
	}
}