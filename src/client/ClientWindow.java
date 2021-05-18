package client;

// import server.Flag;

import javax.swing.*;
import javax.swing.plaf.metal.MetalIconFactory;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.rmi.server.ServerCloneException;
import java.util.Vector;
import java.util.function.Predicate;

// 传进用户ID，用s连接到服务器,创建界面（做按钮、界面列表、个人信息、朋友圈入口）
// 按钮连接到聊天框、朋友圈界面
class ClientWindow extends JFrame implements Flag
{

	Vector<FriendWindow> friendWindows = new Vector<>();
	private final ServerConnection sc;
	ClientWindow cw = this;

	ClientWindow(ServerConnection sc)
	{
		this.sc = sc;

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
		tabbedPane.setPreferredSize(new Dimension(300, 300));
		tabbedPane.setBorder(null);
		tabbedPane.setBackground(Color.white);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		// 创建整个好友列表
		createFriendsScrollPanel(tabbedPane);
		JScrollPane groupScrollPane = new JScrollPane();
		Icon groupPaneIcon = new MetalIconFactory.TreeLeafIcon();
		tabbedPane.addTab("群组列表", groupPaneIcon, groupScrollPane, "这里有你所有群组的信息");
		tabbedPane.addTab("空间", null, null, "朋友圈");
		tabbedPane.setEnabledAt(2, true);
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

	private void createFriendsScrollPanel(JTabbedPane tabbedPane)
	{
		Icon friendPaneIcon = new MetalIconFactory.TreeControlIcon(true);
		JPanel friendPane = new JPanel();
		friendPane.setLayout(new GridLayout(20, 1));
		BufferedReader br;
		try
		{
			br = new BufferedReader(new FileReader(new File(sc.getParentFile(), "admin/friendList.txt")));// 文件路径调用前面的，尽量不要重新写
			String tmp;
			while ((tmp = br.readLine()) != null)
			{
				JComponent panel = createFriendPanel(tmp);
				friendPane.add(panel);
			}
			br.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		JScrollPane friendScrollPane = new JScrollPane(friendPane);
		tabbedPane.addTab("好友列表", friendPaneIcon, friendScrollPane, "这里有你和所有好友聊天的信息");
	}

	public void deleteWindow(String ID)
	{

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

				friendWindows.add(new FriendWindow(sc, text, cw));
				// TODO 处理不够完善；可考虑使用定时器定期清理vector
				friendWindows.removeIf(friendWindow -> !friendWindow.isVisible());
				// System.out.println(friendWindows.size());
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
	// private class C implements Runnable
	// {
	//
	// @Override
	// public void run()
	// {
	//
	// }
	// }
}
