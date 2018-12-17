package ManagerUI;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.Normalizer;
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
	
	//������ TableView �κ�
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
		//System.setErr(ps);
		
		setEveryColumn();
	}
	
	public void setTableView() {
		printerStateTable.setItems(mainApp.getPrinterData());
		printerStateTable.getSelectionModel().setCellSelectionEnabled(false);
	}
	
	public void setEveryColumn() {
		//PrinterName col
		printerName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
		
		//Button col
		Callback<TableColumn<PrintService, Void>, TableCell<PrintService, Void>> cellFactory = new Callback<TableColumn<PrintService, Void>, TableCell<PrintService, Void>>() {
            @Override
            public TableCell<PrintService, Void> call(final TableColumn<PrintService, Void> param) {
                final TableCell<PrintService, Void> cell = new TableCell<PrintService, Void>() {
                	
                	TableCell<PrintService, Void> thisCell = this;
                    private Button btn = new Button("가동 중지");
                    {
                    	btn.setTextFill(Color.RED);
                    	btn.setFont(Font.font("System",FontWeight.EXTRA_BOLD, 12));
                    	btn.setPrefWidth(107.0);
                        btn.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                            	if(btn.getText().equals("가동 중지")){
                            		btn.setText("재가동");
                            		btn.setTextFill(Color.GREEN);
									handleStopBtn(thisCell.getTableRow().getItem());
                            	}
                            	else {
                            		btn.setText("가동 중지");
                            		btn.setTextFill(Color.RED);
                            		handleRestartBtn(thisCell.getTableRow().getItem());
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
	
	private void handleStopBtn(PrintService ps){
		printspooler.deletePrinter(ps);
	}

	private void handleRestartBtn(PrintService ps){
		printspooler.restartPrinter(ps);
	}
	
	public void updateQState(Integer jobqLength) {
		qLength.setText(jobqLength.toString());
	}
	
	public void setPrimaryStage(Stage primaryStage) {
		mainPrimaryStage = primaryStage;
		
		//�ݱ� �̺�Ʈ
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
			while (true) { // 1�ʸ��� jobq�� Ȯ��.
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
		final TextArea ta;
	    final byte[] txt = new byte[4096];
	    int pos = 0;
	    Object obj = new Object();

	    public Console(TextArea _ta) {
	        ta = _ta;
	    }

	    public void write(int b) {
	    	/*
	        synchronized (obj) {
	            txt[pos++] = (byte)b;
	        }
	        */
	    	txt[pos++] = (byte)b;
	    	if((char)b == '\n') {
	    		Platform.runLater(() -> {
	                //ta.appendText(new String(txt, 0, pos));
	                try {
						ta.appendText(new String(txt, 0, pos, "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                pos = 0;
	            });
	    	}	
	    }


		/*
		private TextArea ta;
		private String tmp_string="";
		
        public Console(TextArea ta) {
            this.ta = ta;
        }

        public void appendText(String valueOf) {
            Platform.runLater(() -> ta.appendText(valueOf));
        }

        public void write(int b) throws IOException {
        	if((char)b == '\n') {
        		appendText(tmp_string + String.valueOf((char)b));
        		tmp_string = "";
        	}
        	else {
        		tmp_string += String.valueOf((char)b);
        	}
        }
        */
	}
	
	public void setMainApp(MainApp mainApp) { this.mainApp = mainApp;}
}
