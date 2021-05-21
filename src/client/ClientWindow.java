package client;

import javax.swing.*;
import javax.swing.plaf.metal.MetalIconFactory;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.util.HashMap;

// 传进用户ID，用s连接到服务器,创建界面（做按钮、界面列表、个人信息、朋友圈入口）
// 按钮连接到聊天框、朋友圈界面
class ClientWindow extends JFrame implements Flag
{

	HashMap<String, FriendWindow> friendWindows = new HashMap<>();
	private final ServerConnection sc;
	ClientWindow cw = this;

	ClientWindow(ServerConnection sc)
	{
		this.sc = sc;
		// 接受服务器消息

		// 创建选项卡
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
		tabbedPane.setPreferredSize(new Dimension(300, 300));
		tabbedPane.setBorder(null);
		tabbedPane.setBackground(Color.white);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		// 创建整个好友列表
//		createScrollPanel(tabbedPane, Flag.RECENTPANE);
		createScrollPanel(tabbedPane, Flag.FRIENDPANE);
		createScrollPanel(tabbedPane, Flag.GROUPPANE);
//		createScrollPanel(tabbedPane, Flag.PYQ);

		tabbedPane.setEnabledAt(1, true);
		tabbedPane.setSelectedIndex(0);
		tabbedPane.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				super.mouseEntered(e);
				tabbedPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				super.mouseExited(e);
				tabbedPane.setCursor(Cursor.getDefaultCursor());
			}
		});
		// 最后把选项卡放入frame
		this.add(tabbedPane, BorderLayout.CENTER);
		// new Thread(new C()).start();
		this.setBounds(100, 100, 400, 800);
		this.setVisible(true);
		this.setLayout(null);
		this.setResizable(true);
	}

	private void createScrollPanel(JTabbedPane tabbedPane, int id)
	{
		switch (id)
		{
			case FRIENDPANE -> {
				createPane(tabbedPane, new MetalIconFactory.TreeControlIcon(true),
				           "friendList.txt", "好友列表", "这里有你和所有好友聊天的信息");
			}
			case GROUPPANE -> {
				createPane(tabbedPane, new MetalIconFactory.TreeLeafIcon(),
				           "groupList.txt", "群组列表", "这里有你和所有好友聊天的信息");
			}
			case PYQ -> {
				createPane(tabbedPane, new MetalIconFactory.FolderIcon16(),
				           "friendList.txt", "朋友圈", "这里有你和所有群组的信息");
			}
			case RECENTPANE -> {
				createPane(tabbedPane, new MetalIconFactory.PaletteCloseIcon(),
				           "", "最近消息", "这里有你和所有最近聊天的信息");
			}
		}
	}

	private void createPane(JTabbedPane tabbedPane, Icon paneIcon, String list, String title, String tip)
	{
		JPanel friendPane = new JPanel();
		friendPane.setLayout(new GridLayout(20, 1));
		BufferedReader br;
		try
		{
			br = new BufferedReader(
					new FileReader(new File(sc.getParentFile(), "admin/" + list)));// 文件路径调用前面的，尽量不要重新写
			String tmp;
			while ((tmp = br.readLine()) != null)
			{
				JComponent panel = createFriendPanel(tmp);
				friendPane.add(panel);
			}
			br.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		JScrollPane scrollPane = new JScrollPane(friendPane);
		tabbedPane.addTab(title, paneIcon, scrollPane, tip);
	}

	public void deleteWindow(String ID)
	{
		friendWindows.remove(ID);
		System.out.println(friendWindows.size());
	}

	protected JComponent createFriendPanel(String text)
	{
		JPanel panel = new JPanel(false);
		JLabel filler = new JLabel(text);
		filler.setHorizontalAlignment(JLabel.CENTER);
		panel.setLayout(new GridLayout(1, 1));
		panel.add(filler);
		panel.addMouseListener(new MouseListener()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (friendWindows.containsKey(text))
				{
					friendWindows.get(text).setVisible(true);
				} else
				{
					friendWindows.put(text, new FriendWindow(sc, text, cw));
				}
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				panel.setCursor(Cursor.getDefaultCursor());
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				panel.setBackground(Color.lightGray);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				panel.setBackground(Color.white);
			}
		});
		return panel;
	}

	class HandleASession implements Runnable
	{// 在这里实现信息交互
		ServerConnection s;

		HandleASession(ServerConnection s)
		{
			this.s = s;
		}

		@Override
		public void run()
		{
			String message;
			while (true)
			{
				try
				{
					int sign = s.getMsgFromServer().readInt();
					switch (sign)
					{
						case SENDFILE -> {

//							message = s.getFileFromServer().readNBytes();
						}
						case SENDTEXT -> {
							message = s.getMsgFromServer().readUTF();
							String[] split = message.split("\\|");
							// 若为已打开窗口则写入窗口中
							if (friendWindows.containsKey(split[1]))
							{
								friendWindows.get(split[1]).AddMessage(message);
							}
							// 写入本地文件
							File chatRecord =
									new File(s.getParentFile(), s.getSelfName() + "/friendMsg/" + split[1] + ".txt");
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
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

}
