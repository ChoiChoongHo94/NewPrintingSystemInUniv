package ManagerUI;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

import javax.print.PrintService;

import Printing.PrintSpooler;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

public class ManagerUI2Controller implements Initializable {
	
	private MainApp mainApp;
	private Stage mainPrimaryStage;
	private LabelUpdator lu;
	private PrintSpooler printspooler;
	@FXML
	private TextArea ta;
	@FXML
	private Label qLength;
	private PrintStream ps;
	
	//프린터 TableView 부분
	@FXML
	private TableView<PrintService> printerStateTable;
	@FXML
	private TableColumn<PrintService,String> printerName;
	@FXML
	private TableColumn<PrintService,Void> colBtn;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		//this.qLength.textProperty().bind(new SimpleStringProperty("0"));
		ps = new PrintStream(new Console(ta));
		System.setOut(ps);
		System.setErr(ps);
		
		setEveryColumn();
	}
	
	public void setTableView() {
		printerStateTable.setItems(mainApp.getPrinterData());
	}
	
	public void setEveryColumn() {
		//PrinterName col
		printerName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
		
		//Button col
		Callback<TableColumn<PrintService, Void>, TableCell<PrintService, Void>> cellFactory = new Callback<TableColumn<PrintService, Void>, TableCell<PrintService, Void>>() {
            @Override
            public TableCell<PrintService, Void> call(final TableColumn<PrintService, Void> param) {
                final TableCell<PrintService, Void> cell = new TableCell<PrintService, Void>() {

                    private Button btn = new Button("중지");
                    {
                    	btn.setTextFill(Color.RED);
                    	btn.setFont(Font.font("System",FontWeight.BOLD, 12));
                    	btn.setPrefWidth(107.0);
                        btn.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                            	if(btn.getText().equals("중지")){
                            		btn.setText("재가동");
                            		btn.setTextFill(Color.GREEN);
                            		//handleStopBtn()
                            	}
                            	else {
                            		btn.setText("중지");
                            		btn.setTextFill(Color.RED);
                            		//handleRestartBtn()
                            	}
                            }
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
                return cell;
            }
        };

        colBtn.setCellFactory(cellFactory);
        //printerStateTable.getColumns().add(colBtn);
	}
	
	private void handleStopBtn() throws Exception{
		
	}

	private void handleRestartBtn() throws Exception{
		
	}
	
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
	
	public void setMainApp(MainApp mainApp) { this.mainApp = mainApp;}
}
