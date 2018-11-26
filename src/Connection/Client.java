package Connection;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Client {
	public final static String SERVER_ADDR = "127.0.0.1"; //변경 필요
	
	public static void main(String[] args) {
		Socket socket = null;

		//테스트 용,    ex) java Client ./file.pdf 1 1 1
		String filename = args[0];
		String printOpt = args[1];

		try {
			// 서버 연결
			socket = new Socket(SERVER_ADDR, 5000); // socket(),connect();
			System.out.println("Successfully connected to server.");
			
			FileSender fs = new FileSender(socket, filename, printOpt);
			fs.start();
			//pdf 변환추가
			//로딩 화면
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class FileSender extends Thread {	
	Socket socket;
	
	//파일 전송 관련
	File f;
	DataOutputStream dos;
	FileInputStream fis;
	BufferedInputStream bis;
	int control = 0;
	
	//인쇄 옵션, 파일 정보 관련
	OutputStreamWriter osw;
	BufferedWriter bw;
	String printOpt; // ex: "true 1 5"

	public FileSender(Socket socket, String filepath, String printOpt) {
		this.socket = socket;
		f = new File(filepath);
		this.printOpt = f.getName() + " " + printOpt;
	}
	
	private void printOptSending() {
		try {
			osw = new OutputStreamWriter(socket.getOutputStream());
			bw = new BufferedWriter(osw);
			
			bw.write(printOpt);
			bw.newLine();
			bw.flush();
			System.out.println("printOpt 전송 완료");
			
		} catch (IOException e) {
			e.printStackTrace();
		};		
	}
	
	private void fileSending() {
		try {
			dos = new DataOutputStream(socket.getOutputStream());

			// 파일 내용을 읽으면서 전송
			fis = new FileInputStream(f);
			bis = new BufferedInputStream(fis);

			int len;
			int size = 4096;
			byte[] data = new byte[size];
			while ((len = bis.read(data)) != -1) {
				control++;
				if (control % 10000 == 0) {
					System.out.println("전송중..." + control / 10000);
				}
				dos.write(data, 0, len);
			}
			dos.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
				osw.close();
				dos.close();
				bis.close();
				fis.close();
				System.out.println("File 전송 완료");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		printOptSending();
		fileSending();
	}
}