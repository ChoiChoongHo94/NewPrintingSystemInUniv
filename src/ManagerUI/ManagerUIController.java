package ManagerUI;

import java.util.ArrayList;
import java.util.List;

import javax.print.PrintService;

import Connection.ServerThread;
import Printing.PrintSpooler;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ManagerUIController {
	@FXML
	private TableView<PrintService> printerTable;
	@FXML
	private TableColumn<PrintService, String> column;
	
	private MainApp mainApp;
	private PrintSpooler printspooler;
	
	//생성자
	public ManagerUIController() {
	}
	
	@FXML
	private void initialize() {
		column.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
		printerTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}
	
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
		//set TableView
		printerTable.setItems(mainApp.getPrinterData());
	}
	
	public void setPrintSpooler(PrintSpooler printspooler) {
		this.printspooler = printspooler;
	}
	
	@FXML
	private void handleStartBtn() throws Exception {
		ObservableList<PrintService> selectedPS = printerTable.getSelectionModel().getSelectedItems();
		List<PrintService> tmppl = new ArrayList<PrintService>();
		for(PrintService ps : selectedPS) {
			tmppl.add(ps);
		}
		
		//다음 화면으로 넘어가기
		mainApp.setPrinterData(selectedPS);
		mainApp.gotoManageUI2();
		//printspooler.setPreviousState(tmppl);
		printspooler.setPrinterList(tmppl);
		printspooler.setJobCompleteListener();
		//Thread.sleep(2000);
		ServerThread server = new ServerThread(printspooler);
		server.start();
	}
}
