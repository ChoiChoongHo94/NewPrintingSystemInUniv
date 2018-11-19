package Connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import Connection.Server.Printing;

public class ServerThread extends Thread{
	public final static int PORT = 5000;
	public final static String SAVEPATH = "./";
	
	public void run() {
		Socket socket = null;
		try (ServerSocket serversocket = new ServerSocket(PORT)) {// socket(), bind()
			while (true) {
				// ������ ���� ���� �� ���
				System.out.println("Waiting Client...");
				// ����Ǹ� ��ſ� ���� ����
				socket = serversocket.accept(); // listen(),accept();
				System.out.println("A client is connected.");
				long start = System.currentTimeMillis();
				
				/*�������� �׽�Ʈ
				FileReceiver fr = new FileReceiver(socket, SAVEPATH, start);
				fr.printOptReceiving();
				fr.fileReceiving();
				*/
				
				// ��� ��� ����
				Printing p = new Printing(socket, SAVEPATH, start);
				p.start();
		
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		}
}
