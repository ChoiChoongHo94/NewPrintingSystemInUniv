package Connection;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

import ManagerUI.MainApp;
//import Printing.PrintInfo;
import Printing.PrintSpooler;
import javafx.application.Application;
import pdfconverter.Converter;

public class Server{
	private static PrintSpooler printspooler = new PrintSpooler();

	public static void main(String[] args) throws Exception {
		disableWarning();
		
		JobQueueMonitor jqm = new JobQueueMonitor();
		jqm.start();
		
		Converter.setLicenses();
		
		Application.launch(MainApp.class, args);
		
		//test
		//Converter.convertPPTtoPDF("./test_file/ppt_test.pptx", "./test_file/ppt_test.pdf", "pptx");
		//System.exit(1);
		//PrintInfo testpi = new PrintInfo("./test_file/word_test.pdf", 0, 1);
		
		//String => "���ϸ�.Ȯ���� pow copy border �й�_�̸�"
		//PrintInfo testpi2 = new PrintInfo("./test_file2/ppt_test.pdf", 2, 1, 1, "201320210_����ȣ"); // (path, pow, copies, border)
		//PrintInfo testpi3 = new PrintInfo("./test_file2/ppt_test.pdf", 2, 1, 0, "201320210_����ȣ");
		//test
		/*
		printspooler.enjobq(testpi2);
		printspooler.enjobq(testpi3);
		printspooler.print();
		printspooler.print();
		*/
	}
	
	public static PrintSpooler getPrintSpooler() {
		return printspooler;
	}
	
	/*
	static class ServerThread extends Thread{		@Override
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
	/*
				Printing p = new Printing(socket, SAVEPATH, start);
				p.start();
		
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		}
	}
	*/
	
	static class JobQueueMonitor extends Thread {
		@Override
		public void run() {
			System.out.println("[SYSTEM] JobQueueMonitor is created.");
			while (true) { // 1�ʸ��� ����
				//test
				//System.out.println(printspooler.jobqSize());
				
				//jobq Ȯ��
				if (!printspooler.jobqIsEmpty()) {
					//test
					//System.out.println("Print request is detected.");
					
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
	
	/*
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
	*/
	
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

	// ���� ���� ����
	File f;
	DataInputStream dis;
	FileOutputStream fos;
	BufferedOutputStream bos;

	// �μ� �ɼ� ���۰���
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
		//���� ���ϸ��� �̹� �ִ� �� Ȯ���ϰ� ó��.
		if(Files.exists(Paths.get(filepath))) {
			processDup();
		};
		
		printOpt = data.substring(data.indexOf(" ") + 1);
	}

	public synchronized void printOptReceiving() {
		try {
			isr = new InputStreamReader(socket.getInputStream());
			br = new BufferedReader(isr);

			String data = br.readLine();
			setFilepathAndPrintOpt(savepath, data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void fileReceiving() {
		try {
			dis = new DataInputStream(socket.getInputStream());

			// ������ �����ϰ� ���Ͽ� ���� ��� ��Ʈ�� ����
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
			
			fos = new FileOutputStream(f);
			bos = new BufferedOutputStream(fos);

			// ����Ʈ �����͸� ���۹����鼭 ���
			int len;
			int size = 4096;
			byte[] data = new byte[size];
			while ((len = dis.read(data)) != -1) {
				control++;
				if (control % 10000 == 0) {
					System.out.println("������..." + control / 10000);
				}
				bos.write(data, 0, len);
			}
			long end = System.currentTimeMillis();
			//System.out.println(Thread.currentThread().getName() + "'s file is transfered!");
			//System.out.println(Thread.currentThread().getName() + "'s file receiving is complete! [Elapse time(seconds) : " + (end - start) / 1000.0 + "]");
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
	
	private synchronized void processDup() { //�ߺ����� ó��
		int dup = 1;
		while (true) {
			String beforeExtension = filepath.substring(0, filepath.lastIndexOf("."));
			String extension = filepath.substring(filepath.lastIndexOf("."));
			
			if (Files.notExists(Paths.get(beforeExtension + "(" + dup + ")" + extension))) {
				filepath = beforeExtension + "(" + dup + ")" + extension;
				return;
			}
			dup++;
		}
	}

	public String getFilePath() {return filepath;}

	public String[] getPrintOpt() {return printOpt.split(" ");}
}
