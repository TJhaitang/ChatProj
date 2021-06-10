package client;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.io.*;

// 私聊 创建好友聊天界面，将本地聊天数据读取到这里，按照时间顺序制作窗口
abstract class ChatWindow extends JFrame implements Flag {
	protected JTextPane MsgLabel = new JTextPane();
	protected HTMLDocument text_html;
	protected HTMLEditorKit htmledit = new HTMLEditorKit();
	protected JScrollPane MsgList;

	protected JScrollPane TextBox;
	protected JTextArea Text = new JTextArea();// 写消息的地方
	protected JButton sendButton = new JButton("发送");
	protected JButton voiceButton = new JButton("语音");
	protected JPanel buttonPanel_text = new JPanel();

	protected JPanel buttonPanel_side = new JPanel();
	protected JButton imageButton = new JButton("图\n片");
	protected JButton fileButton = new JButton("文\n件");
	JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView());

	protected ServerConnection s;
	protected String TargetId;

	protected ClientWindow cw;

	protected SimpleAttributeSet attr = new SimpleAttributeSet();

	ChatWindow() {
		this.setLayout(null);
		this.setSize(700, 500);
		// this.setResizable(false);// 懒得解决问题，就解决问题的起因
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((dim.width - this.getWidth()) / 2, (dim.height - this.getHeight()) / 2);
		// 侧边栏部分
		buttonPanel_side.setLayout(new GridLayout(2, 1));
		imageButton.setSize(30, 20);
		imageButton.setMargin(new Insets(0, 0, 0, 0));
		buttonPanel_side.add(imageButton);
		fileButton.setSize(30, 20);
		fileButton.setMargin(new Insets(0, 0, 0, 0));
		// 文件发送
		fileButton.addActionListener(e->
		{
			fileChooser.setMultiSelectionEnabled(true);
			int result = fileChooser.showOpenDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) {
				File[] selectedFiles = fileChooser.getSelectedFiles();
				for (File selectedFile : selectedFiles) {
					Sender sender;
					if (MyUtil.isImage(selectedFile)) {
						sender = new Sender(selectedFile, TargetId, true);
					} else {
						sender = new Sender(selectedFile, TargetId, false);
					}
					new Thread(sender).start();
					try {
						Thread.sleep(150);
					} catch (InterruptedException interruptedException) {
						interruptedException.printStackTrace();
					}
				}
			}
		});
		buttonPanel_side.add(fileButton);
		buttonPanel_side.setBounds(0, 0, 30, 455);

		this.add(buttonPanel_side);
		// 聊天信息展示部分
		text_html = (HTMLDocument) htmledit.createDefaultDocument();
		MsgLabel.setEditorKit(htmledit);
		MsgLabel.setContentType("text/html");
		MsgLabel.setDocument(text_html);
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

		sendButton.addActionListener(e->
		{
			String text = Text.getText();
			if (text == null || text.equals("")) {
				return;
			}
			Sender sender = new Sender(Text.getText(), TargetId);
			Text.setText("");
			new Thread(sender).start();
		});
		voiceButton.addActionListener(e->
		{
			// -//发消息
		});
		sendButton.setMargin(new Insets(0, 0, 0, 0));
		voiceButton.setMargin(new Insets(0, 0, 0, 0));

		buttonPanel_text.setLayout(new GridLayout(2, 1));
		buttonPanel_text.add(voiceButton);
		buttonPanel_text.add(sendButton);
		buttonPanel_text.setBounds(650, 355, 35, 98);
		setIconImage(
				Toolkit.getDefaultToolkit().getImage(System.getProperty("user.dir") + "/src/client/system/icon.png"));
		this.add(buttonPanel_text);

		this.addComponentListener(new ComponentAdapter() {// 动态调整窗口大小
			@Override public void componentResized(ComponentEvent e) {
				int width = getContentPane().getWidth();
				int height = getContentPane().getHeight();
				buttonPanel_side.setBounds(0, 0, 30, height);
				MsgList.setBounds(30, 0, width - 30, height - 100);
				TextBox.setBounds(30, height - 100, width - 80, 100);
				buttonPanel_text.setBounds(width - 50, height - 100, 50, 100);
				super.componentResized(e);
			}
		});
	}

	ChatWindow(ServerConnection s, String tar, ClientWindow cw) {
		this();
		this.s = s;
		this.TargetId = tar;
		this.cw = cw;
	}

	public void AddMessage(String msg)// msg为与服务器交互的标准模式，该函数将此信息打印到屏幕上
	{// 在这里实现信息的展示,0为在最上方插入，1为在最下方
		String[] ss = msg.split("\\|");
		String name = ss[1];// 记得改--？？改啥啊woc我完全不记得当时啥意思了能不能好好写注释
		if (name.equals(s.getSelfName())) {// 自己发的消息
			StyleConstants.setAlignment(attr, StyleConstants.ALIGN_RIGHT);
		} else {// 别人发的
			StyleConstants.setAlignment(attr, StyleConstants.ALIGN_LEFT);
		}
		StyledDocument document = (StyledDocument) MsgLabel.getDocument();
		switch (ss[4]) {
		case "TEXT":

			try {
				StyleConstants.setForeground(attr, Color.black);
				MsgLabel.setParagraphAttributes(attr, false);
				document.insertString(document.getLength(), name + " " + ss[0] + "\n", null);
				if (true) {

				}
				StyleConstants.setForeground(attr, Color.gray);
				MsgLabel.setParagraphAttributes(attr, false);
				String msgStr = ss[3].replaceAll("</or>", "\\|");
				msgStr = msgStr.replaceAll("<br>", "\n");
				document.insertString(document.getLength(), msgStr + "\n", null);
				MsgLabel.setCaretPosition(MsgLabel.getDocument().getLength());
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			break;
		case "IMG":
			try {
				StyleConstants.setForeground(attr, Color.gray);
				MsgLabel.setParagraphAttributes(attr, false);
				document.insertString(document.getLength(), name + " " + ss[0] + "\n", null);
				// MsgLabel.setCaretPosition(MsgLabel.getDocument().getLength());
				// htmledit.insertHTML(text_html, MsgLabel.getCaretPosition() - 1,
				// "<img src='file:///" + s.getParentFile().getParent() + "/image/" + ss[3] + "'
				// >", 0, 0,
				// HTML.Tag.HTML);
				// System.out.println(MsgLabel.getCaretPosition());
				MsgLabel.setCaretPosition(MsgLabel.getDocument().getLength());
				ImageIcon img = new ImageIcon(ss[3]);
				MsgLabel.insertIcon(ResizeImg(img));
				document.insertString(document.getLength(), "\n ", null);
				MsgLabel.setCaretPosition(MsgLabel.getDocument().getLength());
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			break;
		case "FILE":

			break;
		}
	}

	protected ImageIcon ResizeImg(ImageIcon im) {
		int width = im.getIconWidth();
		int height = im.getIconHeight();
		int sum = width + height;
		if (sum > 350) {
			width = (int) (350.0 * width / sum);
			height = (int) (350.0 * height / sum);
			Image img = im.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT);
			return new ImageIcon(img);
		} else {
			return im;
		}
	}

	abstract void display();// 从历史记录中读取

	protected void sendMsg(String s, int flag) {// 发信，与服务器做交互
		if (flag == Flag.SENDTEXT) {
			cw.hand.PutMsg(new MsgPack(Flag.SENDTEXT, TargetId, s));
		} else {
			cw.hand.PutMsg(new MsgPack(Flag.SENDFILE, TargetId, s));
		}
	}

	// 不能删，不然就关闭全部窗口了
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			// System.out.println("Close");
			this.dispose();
			cw.deleteWindow(TargetId);// 对于好友来说，T为好友名；对于群组，T为群组ID
		} else {
			super.processWindowEvent(e);
		}
	}

	// 发信工具类，收信类放到用户界面内
	private class Sender implements Runnable {
		String str;
		File file = null;
		boolean isImage;

		Sender(String str1, String Tar) {
			str = str1.replaceAll("\\|", "</or>");//
			str = str.replaceAll("\n", "<br>");
			str = MyUtil.generateTimeStamp() + "|" + s.getSelfName() + "|" + "0" + "|" + str + "|TEXT|" + Tar;
		}

		Sender(File file, String Tar, boolean flag) {
			this.file = file;
			isImage = flag;
			if (isImage) {
				str = MyUtil.generateTimeStamp() + "|" + s.getSelfName() + "|" + "0" + "|" + file.getAbsolutePath()
						+ "|IMG|" + Tar;
			} else {
				str = MyUtil.generateTimeStamp() + "|" + s.getSelfName() + "|" + "0" + "|" + file.getAbsolutePath()
						+ "|FILE|" + Tar;
			}
		}

		@Override public void run() {
			AddMessage(str);
			if (file != null) {
				sendMsg(str, Flag.SENDFILE);
			} else {
				sendMsg(str, Flag.SENDTEXT);
			}
		}

	}
}

class GroupWindow extends ChatWindow {
	String GroupName;

	GroupWindow(ServerConnection s, String GroupId, String targetName, ClientWindow cw) {
		super(s, GroupId, cw);
		this.GroupName = targetName;
		this.setTitle(GroupName);
		display();
		this.setVisible(true);
	}

	@Override void display() {
		File chatRecord = new File(s.getParentFile(),
				s.getSelfName() + "/groupMsg/" + TargetId + ".txt");// 此文件在加好友时创建,文件路径记得改
		if (!chatRecord.exists()) {
			try {
				chatRecord.createNewFile();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(chatRecord));
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this, "消息记录读取失败！");
		}
		String str;
		try {
			while ((str = br.readLine()) != null) {
				AddMessage(str);// 这里好像有点问题
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class FriendWindow extends ChatWindow {
	// String friendName;// 换一下，换到父类里面去

	public static void main(String[] args) {
		ServerConnection s = new ServerConnection();
		s.setSelfName("admin");
		new FriendWindow(s, "thirdPerson", null);

	}

	FriendWindow(ServerConnection s, String friendName, ClientWindow cw) {// 构造函数，完成消息的展示即可，同步在上线时与用户界面完成
		super(s, friendName, cw);
		// this.friendName = friendName;
		this.setTitle(friendName);
		display();
		this.setVisible(true);
	}

	@Override void display() {
		// 从文件尾开始读文件：https://blog.csdn.net/qq_21682469/article/details/78808713
		File chatRecord = new File(s.getParentFile(),
				s.getSelfName() + "/friendMsg/" + TargetId + ".txt");// 此文件在加好友时创建,文件路径记得改
		if (!chatRecord.exists()) {
			try {
				chatRecord.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(chatRecord));
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this, "消息记录读取失败！");
		}
		String str;
		try {
			while ((str = br.readLine()) != null) {
				AddMessage(str);// 这里好像有点问题
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
