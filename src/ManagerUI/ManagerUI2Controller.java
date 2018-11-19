package ManagerUI;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

import Printing.PrintSpooler;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ManagerUI2Controller implements Initializable {
	private Stage mainPrimaryStage;
	private LabelUpdator lu;
	private PrintSpooler printspooler;
	@FXML
	private TextArea ta;
	@FXML
	private Label qLength;
	private PrintStream ps;
		
	public void updateQState(Integer jobqLength) {
		qLength.setText(jobqLength.toString());
	}
	
	public void setPrimaryStage(Stage primaryStage) {
		mainPrimaryStage = primaryStage;
		
		//닫기 이벤트
		mainPrimaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent we) {
				System.exit(0);
			}
		});
	}
	
	public void setPrinterSpooler(PrintSpooler printspooler) {
		this.printspooler = printspooler;
		lu = new LabelUpdator();
		qLength.textProperty().bind(lu.valueProperty());
		Thread thread = new Thread(lu);
		thread.setDaemon(true);
		thread.start();
	}

	class LabelUpdator extends Task<String> {
		@Override
		protected String call() throws Exception {
			// TODO Auto-generated method stub
			while (true) { // 1초마다 jobq를 확인.
				updateValue(((Integer) printspooler.jobqSize()).toString());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		//this.qLength.textProperty().bind(new SimpleStringProperty("0"));
		ps = new PrintStream(new Console(ta));
		System.setOut(ps);
		System.setErr(ps);
	}
	
	public class Console extends OutputStream{
		private TextArea ta;

        public Console(TextArea ta) {
            this.ta = ta;
        }

        public void appendText(String valueOf) {
            Platform.runLater(() -> ta.appendText(valueOf));
        }

        public void write(int b) throws IOException {
            appendText(String.valueOf((char)b));
        }
	}
	
}
