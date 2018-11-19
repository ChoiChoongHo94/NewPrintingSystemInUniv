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
				// 리스너 소켓 생성 후 대기
				System.out.println("Waiting Client...");
				// 연결되면 통신용 소켓 생성
				socket = serversocket.accept(); // listen(),accept();
				System.out.println("A client is connected.");
				long start = System.currentTimeMillis();
				
				/*파일전송 테스트
				FileReceiver fr = new FileReceiver(socket, SAVEPATH, start);
				fr.printOptReceiving();
				fr.fileReceiving();
				*/
				
				// 모든 기능 실행
				Printing p = new Printing(socket, SAVEPATH, start);
				p.start();
		
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		}
}
