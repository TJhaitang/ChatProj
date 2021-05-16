// package client;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

// 私聊 创建好友聊天界面，将本地聊天数据读取到这里，按照时间顺序制作窗口
abstract class ChatWindow extends JFrame implements Flag {
	protected JTextPane MsgLabel = new JTextPane();
	protected JScrollPane MsgList;

	protected JScrollPane TextBox;
	protected JTextArea Text = new JTextArea();// 写消息的地方
	protected JButton sendButton = new JButton("发送");
	protected JButton voiceButton = new JButton("语音");
	protected JPanel buttonPanel_text = new JPanel();

	protected JPanel buttonPanel_side = new JPanel();
	protected JButton imageButton = new JButton("图片");
	protected JButton fileButton = new JButton("文件");

	protected ServerConnection s;
	protected String Target;

	ChatWindow() {
		this.setLayout(null);
		this.setSize(700, 500);
		this.setResizable(false);// 懒得解决问题，就解决问题的起因
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((dim.width - this.getWidth()) / 2, (dim.height - this.getHeight()) / 2);
		// 侧边栏部分
		buttonPanel_side.setLayout(new GridLayout(2, 1));
		imageButton.setSize(30, 20);
		buttonPanel_side.add(imageButton);
		fileButton.setSize(30, 20);
		buttonPanel_side.add(fileButton);
		buttonPanel_side.setBounds(0, 0, 30, 455);

		this.add(buttonPanel_side);
		// 聊天信息展示部分
		MsgLabel.setSize(650, 300);

		MsgLabel.setEditable(false);
		MsgList = new JScrollPane(MsgLabel);
		MsgList.setBounds(30, 0, 655, 355);
		this.add(MsgList);
		// 聊天输出部分
		Text.setLineWrap(true);
		TextBox = new JScrollPane(Text);
		TextBox.setBounds(30, 355, 620, 100);
		this.add(TextBox);

		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = Text.getText();
				if (text == null || text.equals("")) {
					return;
				}
				Sender sender = new Sender(Text.getText());
				Text.setText("");
				new Thread(sender).start();
			}
		});
		voiceButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// -//发消息
			}
		});

		buttonPanel_text.setLayout(new GridLayout(2, 1));
		buttonPanel_text.add(voiceButton);
		buttonPanel_text.add(sendButton);
		buttonPanel_text.setBounds(650, 355, 35, 98);
		this.add(buttonPanel_text);

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	ChatWindow(ServerConnection s, String tar) {
		this();
		this.s = s;
		this.Target = tar;
	}

	public void AddMessage(String msg) {// 在这里实现信息的展示,0为在最上方插入，1为在最下方
		String[] ss = msg.split("\\|");
		String name = ss[1];// 记得改
		if (name == s.getSelfName()) {// 自己发的消息
			StyledDocument document = (StyledDocument) MsgLabel.getDocument();
			try {
				document.insertString(document.getLength(), name + "  " + ss[0] + "\n" + ss[3] + "\n", null);
				MsgLabel.setCaretPosition(MsgLabel.getDocument().getLength());
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		} else {// 别人发的
			StyledDocument document = (StyledDocument) MsgLabel.getDocument();
			try {
				document.insertString(document.getLength(), name + "  " + ss[0] + "\n" + ss[3] + "\n", null);
				MsgLabel.setCaretPosition(MsgLabel.getDocument().getLength());
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	abstract void display();// 从历史记录中读取

	abstract void sendMsg(String s);// 发信，与服务器做交互

	// 发信工具类，收信类放到用户界面内
	private class Sender implements Runnable {
		String str;

		Sender(String str) {
			this.str = str;
		}

		@Override
		public void run() {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
			str = df.format(new Date()) + "|" + s.getSelfName() + "|" + "0" + "|" + str;// 0为未读
			AddMessage(str);
			sendMsg(str);
		}
	}
}

class FriendWindow extends ChatWindow {
	String friendName;// 换一下，换到父类里面去

	public static void main(String[] args) {
		ServerConnection s = new ServerConnection();
		s.setSelfName("admin");
		FriendWindow fw = new FriendWindow(s, "secondPerson");

	}

	FriendWindow(ServerConnection s, String friendName) {// 构造函数，完成消息的展示即可，同步在上线时与用户界面完成
		super(s, friendName);
		this.friendName = friendName;
		this.setTitle(friendName);
		display();
		this.setVisible(true);
	}

	@Override
	void display() {// 从文件尾开始读文件：https://blog.csdn.net/qq_21682469/article/details/78808713
		File chatRecord = new File(s.getParentFile(), s.getSelfName() + "/friendMsg/" + friendName + ".txt");// 此文件在加好友时创建,文件路径记得改
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(chatRecord));
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this, "消息记录不存在！");
			chatRecord.mkdir();
			e.printStackTrace();
		}
		String str;
		try {
			while ((str = br.readLine()) != null) {
				AddMessage(str);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	void sendMsg(String str) {
		try {
			// s.getMsgToServer().writeInt(Flag.SENDTEXT);
			// s.getMsgToServer().writeUTF(str);
			// int a = s.getMsgFromServer().readInt();
			// if (a != Flag.SUCCESS) {
			// JOptionPane.showMessageDialog(MsgList, "发送失败");
			// } else {
			File chatRecord = new File(s.getParentFile(), s.getSelfName() + "/friendMsg/" + Target + ".txt");
			PrintWriter pw = new PrintWriter(new FileOutputStream(chatRecord, true));
			pw.println(str);
			pw.close();
			// }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
