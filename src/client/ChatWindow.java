// package client;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

// 私聊 创建好友聊天界面，将本地聊天数据读取到这里，按照时间顺序制作窗口
abstract class ChatWindow extends JFrame implements Flag {
	private JTextPane MsgLabel = new JTextPane();
	private JScrollPane MsgList;

	private JScrollPane TextBox;
	private JTextArea Text = new JTextArea();// 写消息的地方
	private JButton sendButton = new JButton("发送");
	private JButton voiceButton = new JButton("语音");
	private JPanel buttonPanel_text = new JPanel();

	private JPanel buttonPanel_side = new JPanel();
	private JButton imageButton = new JButton("图片");
	private JButton fileButton = new JButton("文件");

	private ServerConnection s;

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

	ChatWindow(ServerConnection s) {
		this();
		this.s = s;
	}

	public void AddMessage(String name, String msg) {// 在这里实现信息的展示
		if (name == s.getSelfName()) {// 自己发的消息
			StyledDocument document = (StyledDocument) MsgLabel.getDocument();
			try {
				document.insertString(document.getLength(), name + "\n" + msg + "\n", null);
				MsgLabel.setCaretPosition(MsgLabel.getDocument().getLength());
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		} else {// 别人发的
			StyledDocument document = (StyledDocument) MsgLabel.getDocument();
			try {
				document.insertString(document.getLength(), name + "\n" + msg + "\n", null);
				MsgLabel.setCaretPosition(MsgLabel.getDocument().getLength());
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	// 发信工具类，收信类放到用户界面内
	private class Sender implements Runnable {
		String str;

		Sender(String str) {
			this.str = str;
		}

		@Override
		public void run() {
			try {
				AddMessage(s.getSelfName(), str);
				s.getMsgToServer().writeUTF(str);
				s.getMsgToServer().writeInt(Flag.SENDTEXT);
				int a = s.getMsgFromServer().readInt();
				if (a != Flag.SUCCESS) {
					JOptionPane.showMessageDialog(MsgList, "发送失败");
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(MsgList, "发送失败");
				e.printStackTrace();
			}
		}
	}
}

class FriendWindow extends ChatWindow {
	String friendName;

	public static void main(String[] args) {
		ServerConnection s = new ServerConnection();
		s.setSelfName("llala");
		FriendWindow fw = new FriendWindow(s, "1");

	}

	FriendWindow(ServerConnection s, String friendName) {
		super(s);
		this.friendName = friendName;
		this.setTitle(friendName);
		this.setVisible(true);
	}
}
