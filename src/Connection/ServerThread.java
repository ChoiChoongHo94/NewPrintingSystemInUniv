package Connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

//import Connection.Server.Printing;
import Printing.PrintInfo;
import Printing.PrintSpooler;

public class ServerThread extends Thread{
	public final static int PORT = 5000;
	public final static String SAVEPATH = "./save_path/";
	public static PrintSpooler printspooler;
	
	public ServerThread(PrintSpooler printspooler) {
		this.printspooler = printspooler;
	}

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
	
	class Printing extends Thread {
		private FileReceiver fr;
		private PrintInfo pi;

		public Printing(Socket socket, String savepath, long starttime) {
			fr = new FileReceiver(socket, savepath, starttime);
		}

		@Override
		public void run() {
			fr.printOptReceiving();
			fr.fileReceiving();
			String[] printopt = fr.getPrintOpt();
			
			pi = new PrintInfo(fr.getFilePath(), Integer.parseInt(printopt[0]), 
							   Integer.parseInt(printopt[1]), Integer.parseInt(printopt[2]), printopt[3]);
			
			printspooler.enjobq(pi);
			System.out.println(printopt[3] + " requests print!");
		}
	}
}

