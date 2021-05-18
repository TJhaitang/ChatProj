package client;

public interface Flag
{
	final int LOGIN = 1;
	final int SIGNUP = 2;

	final int FAIL = 0;
	final int SUCCESS = 10;

	final int SENDFILE = 3;
	final int SENDTEXT = 4;

	final int ADDFRIEND = 5;
	final int CREATEGROUP = 6;
	final int DELETEFRIEND = 7;
	final int DELETEGROUP = 8;
	final int QUITGROUP = 9;

	final int FRIENDPANE = 10;
	final int GROUPPANE = 11;
	final int PYQ = 12;
	final int RECENTPANE = 13;
}
