package Connection;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import Printing.PrintInfo;
import Printing.PrintSpooler;

public class Server {
	public final static int PORT = 5000;
	public final static String SAVEPATH = "./";
	static PrintSpooler printspooler = new PrintSpooler();

	public static void main(String[] args) {
		disableWarning();
		
		//jobq 체크 쓰레드
		WorkingPrintSpooler wps = new WorkingPrintSpooler();
		wps.start();
		
		Socket socket = null;
		try (ServerSocket serversocket = new ServerSocket(PORT)) {// socket(), bind()
			while (true) {
				// 리스너 소켓 생성 후 대기
				System.out.println("Waiting Client...");
				// 연결되면 통신용 소켓 생성
				socket = serversocket.accept(); // listen(),accept();
				System.out.println("A client is connected.");
				long start = System.currentTimeMillis();
				
				//파일전송 테스트
				FileReceiver fr = new FileReceiver(socket, SAVEPATH, start);
				fr.printOptReceiving();
				fr.fileReceiving();
				
				/*
				Printing p = new Printing(socket, start);
				p.start();
				*/
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static class WorkingPrintSpooler extends Thread {
		@Override
		public void run() {
			while (true) {
				if (!printspooler.jobqIsEmpty()) {
					printspooler.print();
				} else {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static class Printing extends Thread {
		private FileReceiver fr;
		private PrintInfo pi;

		public Printing(Socket socket,String savepath, long starttime) {
			fr = new FileReceiver(socket, savepath, starttime);
		}

		@Override
		public void run() {
			fr.printOptReceiving();
			fr.fileReceiving();
			String[] printopt = fr.getPrintOpt();
			pi = new PrintInfo(fr.getFilePath(), Boolean.valueOf(printopt[0]), Integer.getInteger(printopt[1]),
					Integer.getInteger(printopt[2]));
			printspooler.enjobq(pi);
		}
	}

	public static void disableWarning() {
		System.err.close();
		System.setErr(System.out);
	}
}

class FileReceiver {
	Socket socket;
	long start;
	int control = 0;
	String savepath;

	// 파일 전송 관련
	File f;
	DataInputStream dis;
	FileOutputStream fos;
	BufferedOutputStream bos;

	// 인쇄 옵션 전송관련
	InputStreamReader isr;
	BufferedReader br;
	String filepath;
	String printOpt;

	public FileReceiver(Socket socket, String savepath, long starttime) {
		this.socket = socket;
		this.savepath = savepath;
		this.start = starttime;
	}

	private void setFilepathAndPrintOpt(String savepath, String data) {
		filepath = savepath + data.substring(0, data.indexOf(" "));
		printOpt = data.substring(data.indexOf(" ") + 1);
	}

	public void printOptReceiving() {
		try {
			isr = new InputStreamReader(socket.getInputStream());
			br = new BufferedReader(isr);

			String data = br.readLine();
			setFilepathAndPrintOpt(savepath, data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		;
	}

	public void fileReceiving() {
		try {
			dis = new DataInputStream(socket.getInputStream());

			// 파일을 생성하고 파일에 대한 출력 스트림 생성
			f = new File(filepath);

			int sleepcount = 0;
			while (!f.createNewFile()) {
				try {
					System.out.println(Thread.currentThread().getName() + "'s sleepcount: " + sleepcount);
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println(Thread.currentThread().getName() + "'s file is created!");

			fos = new FileOutputStream(f);
			bos = new BufferedOutputStream(fos);

			// 바이트 데이터를 전송받으면서 기록
			int len;
			int size = 4096;
			byte[] data = new byte[size];
			while ((len = dis.read(data)) != -1) {
				control++;
				if (control % 10000 == 0) {
					System.out.println("수신중..." + control / 10000);
				}
				bos.write(data, 0, len);
			}
			long end = System.currentTimeMillis();
			System.out.println("File 받기 완료! [경과시간(seconds) : " + (end - start) / 1000.0 + "]");
			bos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				isr.close();
				bos.close();
				fos.close();
				dis.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String getFilePath() {return filepath;}

	public String[] getPrintOpt() {return printOpt.split(" ");}
}
