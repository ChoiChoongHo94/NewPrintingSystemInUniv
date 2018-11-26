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
	public final static String SERVER_ADDR = "127.0.0.1"; //���� �ʿ�
	
	public static void main(String[] args) {
		Socket socket = null;

		//�׽�Ʈ ��,    ex) java Client ./file.pdf 1 1 1
		String filename = args[0];
		String printOpt = args[1];

		try {
			// ���� ����
			socket = new Socket(SERVER_ADDR, 5000); // socket(),connect();
			System.out.println("Successfully connected to server.");
			
			FileSender fs = new FileSender(socket, filename, printOpt);
			fs.start();
			//pdf ��ȯ�߰�
			//�ε� ȭ��
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class FileSender extends Thread {	
	Socket socket;
	
	//���� ���� ����
	File f;
	DataOutputStream dos;
	FileInputStream fis;
	BufferedInputStream bis;
	int control = 0;
	
	//�μ� �ɼ�, ���� ���� ����
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
			System.out.println("printOpt ���� �Ϸ�");
			
		} catch (IOException e) {
			e.printStackTrace();
		};		
	}
	
	private void fileSending() {
		try {
			dos = new DataOutputStream(socket.getOutputStream());

			// ���� ������ �����鼭 ����
			fis = new FileInputStream(f);
			bis = new BufferedInputStream(fis);

			int len;
			int size = 4096;
			byte[] data = new byte[size];
			while ((len = bis.read(data)) != -1) {
				control++;
				if (control % 10000 == 0) {
					System.out.println("������..." + control / 10000);
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
				System.out.println("File ���� �Ϸ�");
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