package client;

import javax.swing.*;
import javax.swing.plaf.metal.MetalIconFactory;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

// 传进用户ID，用s连接到服务器,创建界面（做按钮、界面列表、个人信息、朋友圈入口）
// 按钮连接到聊天框、朋友圈界面
class ClientWindow extends JFrame implements Flag
{
	private ChatWindow w;
	private ServerConnection sc;

	public static void main(String[] args)
	{
		ClientWindow clientWindow = new ClientWindow(null);

		clientWindow.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}

		});

		clientWindow.setBounds(1500, 100, 400, 800);
		clientWindow.setVisible(true);
		clientWindow.setLayout(null);
		clientWindow.setResizable(true);
	}

	ClientWindow(ServerConnection sc)
	{
		this.sc = sc;
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
		tabbedPane.setPreferredSize(new Dimension(300, 300));
		tabbedPane.setBorder(null);
		tabbedPane.setBackground(Color.white);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		createFriendsScrollPanel(tabbedPane);
		JScrollPane groupScrollPane = new JScrollPane();
		Icon groupPaneIcon = new MetalIconFactory.TreeLeafIcon();
		tabbedPane.addTab("群组列表", groupPaneIcon, groupScrollPane, "这里有你所有群组的信息");
		tabbedPane.addTab("空间", null, null, "朋友圈");

		tabbedPane.setEnabledAt(2, true);
		tabbedPane.setSelectedIndex(0);
		this.add(tabbedPane, BorderLayout.CENTER);
//		new Thread(new C()).start();
		this.setBounds(100, 100, 400, 800);
		this.setVisible(true);
		this.setLayout(null);
		this.setResizable(true);
	}

	private void createFriendsScrollPanel(JTabbedPane tabbedPane)
	{
		Icon friendPaneIcon = new MetalIconFactory.TreeControlIcon(true);
		JPanel friendPane = new JPanel();
		friendPane.setLayout(new GridLayout(20, 1));
		BufferedReader br;
		try
		{
			br = new BufferedReader(
					new FileReader(System.getProperty("user.dir") + "/src/client/users/admin/friendList.txt"));
			String tmp = br.readLine();
			while (tmp != null)
			{
				JComponent panel = createFriendPanel(tmp);
				friendPane.add(panel);
				tmp = br.readLine();
			}
			br.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		JScrollPane friendScrollPane = new JScrollPane(friendPane);
		tabbedPane.addTab("好友列表", friendPaneIcon, friendScrollPane, "这里有你和所有好友聊天的信息");
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
				new FriendWindow(text, sc);
			}

			@Override
			public void mousePressed(MouseEvent e)
			{

			}

			@Override
			public void mouseReleased(MouseEvent e)
			{

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
//	private class C implements Runnable
//	{
//
//		@Override
//		public void run()
//		{
//
//		}
//	}
}
