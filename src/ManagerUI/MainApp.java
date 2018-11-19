package ManagerUI;

import java.io.IOException;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import Connection.Server;
import Printing.PrintSpooler;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainApp extends Application {
	
	private Stage primaryStage;
	private ObservableList<PrintService> printerData = FXCollections.observableArrayList();
	private PrintSpooler printspooler;

	public MainApp() {
		PrintService[] pl = PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PAGEABLE,null);
		for(PrintService ps : pl) {
			printerData.add(ps);
		}
		
		printspooler = Server.getPrintSpooler();
	}
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Management UI");
		this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent we) {
				System.exit(0);
			}
		});
		showManagerUI();
	}
	
	public void showManagerUI() {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(MainApp.class.getResource("managerUI.fxml"));
		try {
			AnchorPane managerUI = (AnchorPane) loader.load();
			Scene scene = new Scene(managerUI);
			primaryStage.setScene(scene);
			primaryStage.show();
			
			ManagerUIController controller = loader.getController();
			controller.setMainApp(this);
			controller.setPrintSpooler(printspooler);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//UI2 ฐüทร
	public void gotoManageUI2() {
		ManagerUI2Controller controller2 = (ManagerUI2Controller) replaceSceneContent("managerUI2.fxml");
		controller2.setPrimaryStage(primaryStage);
		controller2.setPrinterSpooler(printspooler);
	}
	
	public Initializable replaceSceneContent(String fxml) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
			Parent root = loader.load();
			
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			return (Initializable) loader.getController();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ObservableList<PrintService> getPrinterData(){
		return printerData;
	}
	
	

	/*
	public static void main(String[] args) {
		launch(args);
	}
	*/
}
