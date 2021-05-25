package client;

// import server.Flag;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

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
	protected JButton imageButton = new JButton("图片");
	protected JButton fileButton = new JButton("文件");

	protected ServerConnection s;
	protected String Target;

	protected ClientWindow cw;

	protected SimpleAttributeSet attr = new SimpleAttributeSet();

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
		sendButton.setMargin(new Insets(0, 0, 0, 0));
		voiceButton.setMargin(new Insets(0, 0, 0, 0));

		buttonPanel_text.setLayout(new GridLayout(2, 1));
		buttonPanel_text.add(voiceButton);
		buttonPanel_text.add(sendButton);
		buttonPanel_text.setBounds(650, 355, 35, 98);
		setIconImage(
				Toolkit.getDefaultToolkit().getImage(System.getProperty("user.dir") + "/src/client/system/icon.png"));
		this.add(buttonPanel_text);

		// this.addWindowListener(new WindowAdapter() {
		// public void windowClosing(WindowEvent e) {
		// System.exit(0);
		// }
		// });
	}

	ChatWindow(ServerConnection s, String tar, ClientWindow cw) {
		this();
		this.s = s;
		this.Target = tar;
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
		if (ss[4].equals("TEXT")) {
			StyledDocument document = (StyledDocument) MsgLabel.getDocument();
			try {
				StyleConstants.setForeground(attr, Color.black);
				MsgLabel.setParagraphAttributes(attr, false);
				document.insertString(document.getLength(), name + " " + ss[0] + "\n", null);
				if (true)
					;
				StyleConstants.setForeground(attr, Color.gray);
				MsgLabel.setParagraphAttributes(attr, false);
				String msgStr = ss[3].replaceAll("</or>", "\\|");
				msgStr = msgStr.replaceAll("<br>", "\n");
				document.insertString(document.getLength(), msgStr + "\n", null);
				MsgLabel.setCaretPosition(MsgLabel.getDocument().getLength());
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		} else if (ss[4].equals("IMG")) {
			try {
				StyledDocument document = (StyledDocument) MsgLabel.getDocument();
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
				ImageIcon img = new ImageIcon(s.getParentFile().getParent() + "/image/" + ss[3]);
				MsgLabel.insertIcon(ResizeImg(img));
				document.insertString(document.getLength(), "\n ", null);
				MsgLabel.setCaretPosition(MsgLabel.getDocument().getLength());
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
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

	abstract void sendMsg(String s);// 发信，与服务器做交互

	// 发信工具类，收信类放到用户界面内
	private class Sender implements Runnable {
		String str;

		Sender(String str1) {
			str = str1.replaceAll("\\|", "</or>");//
		}

		@Override
		public void run() {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
			str = str.replaceAll("\n", "<br>");
			str = df.format(new Date()) + "|" + s.getSelfName() + "|" + "0" + "|" + str + "|TEXT";// 0为未读
			AddMessage(str);
			sendMsg(str);
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

	// 不能删，不然就关闭全部窗口了
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {

			this.dispose();
			cw.deleteWindow(Target);// 对于好友来说，T为好友名；对于群组，T为群组ID
		} else {
			super.processWindowEvent(e);
		}
	}

	@Override
	void display() {// 从文件尾开始读文件：https://blog.csdn.net/qq_21682469/article/details/78808713
		File chatRecord = new File(s.getParentFile(), s.getSelfName() + "/friendMsg/" + Target + ".txt");// 此文件在加好友时创建,文件路径记得改
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(chatRecord));
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this, "消息记录不存在！");
			try {
				chatRecord.createNewFile();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
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
			s.getMsgToServer().writeInt(Flag.SENDTEXT);
			s.getMsgToServer().writeUTF(Target);
			s.getMsgToServer().writeUTF(str);
			int a = Flag.SUCCESS;
			// int a = s.getMsgFromServer().readInt();
			if (a != Flag.SUCCESS) {
				JOptionPane.showMessageDialog(MsgList, "发送失败");
			} else {
				File chatRecord = new File(s.getParentFile(), s.getSelfName() + "/friendMsg/" + Target + ".txt");
				PrintWriter pw = new PrintWriter(new FileOutputStream(chatRecord, true));
				pw.println(str);
				pw.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
