package client;

import javax.swing.*;
import java.net.Socket;

public class ChatClient
{
	public static void main(String[] args)
	{
		Socket s = new Socket();
		Login l = new Login(s);
	}
}

class Login extends JFrame
{
	Login(Socket s)
	{
		this.setVisible(true);

	}
}
