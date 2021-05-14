package client;

import javax.swing.*;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.net.Socket;

// 传进用户ID，用s连接到服务器,创建界面（做按钮、界面列表、个人信息、朋友圈入口）
// 按钮连接到聊天框、朋友圈界面
class ClientWindow extends JFrame implements Flag
{
	private ChatWindow w;

//	public static void main(String[] args)
//	{
//		ClientWindow clientWindow = new ClientWindow(1, null, null);
//
//		clientWindow.addWindowListener(new WindowAdapter()
//		{
//			@Override
//			public void windowClosing(WindowEvent e)
//			{
//				System.exit(0);
//			}
//
//		});
//
//		clientWindow.setBounds(1500, 100, 400, 800);
//		clientWindow.setVisible(true);
//		clientWindow.setLayout(null);
//		clientWindow.setResizable(true);
//	}

	ClientWindow(String username, Socket s)
	{
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
		tabbedPane.setPreferredSize(new Dimension(300, 300));
		tabbedPane.setBorder(null);
		tabbedPane.setBackground(Color.white);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		Icon friendPaneIcon = new MetalIconFactory.TreeControlIcon(true);
		Icon groupPaneIcon = new MetalIconFactory.TreeLeafIcon();
		JScrollPane friendScrollPane = new JScrollPane();
		JScrollPane groupScrollPane = new JScrollPane();

		String[] columnNames = {"First Name", "Last Name", "Sport"};
//创建显示数据
		Object[][] data = {{"Kathy", "Smith", "Snowboarding"},
		                   {"John", "Doe", "Rowing"},
		                   {"Sue", "Black", "Knitting"},
		                   {"Jane", "White", "Speed reading"},
		                   {"Joe", "Brown", "Pool"}};
		JTable table = new JTable(data, columnNames);
		table.setDefaultRenderer(String.class, new MainUserCellRender());
		table.addMouseListener(new MouseListener()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{

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

			}

			@Override
			public void mouseExited(MouseEvent e)
			{

			}
		});
		table.addMouseMotionListener(new MouseMotionAdapter()
		{
			@Override
			public void mouseMoved(MouseEvent e)
			{
				int r = table.rowAtPoint(e.getPoint());
				if (r != MainUserCellRender.cover_r)
				{
					MainUserCellRender.cover_r = r;
					table.repaint();
				}
			}
		});

		friendScrollPane.setViewportView(table);
		tabbedPane.addTab("好友列表", friendPaneIcon, friendScrollPane, "这里有你和所有好友聊天的信息");
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


class MainUserCellRender extends JPanel implements TableCellRenderer
{
	static int cover_r = -1;
	static int select_r = -1;
//	static Font userNameFont;
//	static Font userAutoGraphFont;
//
//	static
//	{
//		userNameFont = LoadFont.loadFont(UImport.DefaultFont, 18);
//		userAutoGraphFont = LoadFont.loadFont(UImport.DefaultFont, 14);
//	}

	JLabel userName;
	JLabel userAutoGraph;

	public MainUserCellRender()
	{
		super();
		this.setLayout(null);
		JLabel userPic = new JLabel(new MetalIconFactory.TreeControlIcon(true));
		userPic.setBounds(1600, 100, 50, 50);
		userName = new JLabel();
		userAutoGraph = new JLabel();
		userName.setBounds(1600, 100, 200, 20);
//		username.setFont(userNameFont);
		userAutoGraph.setBounds(1600, 100, 250, 20);
//		userAutoGraph.setForeground(UColor.MainMidPanelUserLabelAutographColor);
//		userAutoGraph.setFont(userAutoGraphFont);
		this.add(userPic);
		this.add(userName);
		this.add(userAutoGraph);

	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
	                                               int row, int column)
	{
		userName.setText("friend1");
		userAutoGraph.setText("test");
		if (row == select_r)
		{
			this.setBackground(Color.white);
		} else if (row == cover_r)
		{
			this.setBackground(Color.black);
		} else
		{
			this.setBackground(Color.blue);
		}
		return this;
	}
}