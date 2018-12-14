package Printing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.standard.QueuedJobCount;
import javax.print.event.PrintServiceAttributeEvent;
import javax.print.event.PrintServiceAttributeListener;

import org.jpedal.PdfDecoder;
import org.jpedal.fonts.FontMappings;
import org.jpedal.objects.PrinterOptions;
import org.jpedal.utils.PdfBook;

public class PrintSpooler { // Singleton
	
	//static private List<String> ongoingPrinters = new ArrayList<String>();
	static private HashMap<String,PrintInfo> currentJobList = new HashMap<String,PrintInfo>();
	static private Queue<PrintInfo> urgentJobq = new LinkedList<PrintInfo>();
	static private Queue<PrintInfo> jobq = new LinkedList<PrintInfo>();
	static List<PrintService> printerlist = new ArrayList<PrintService>();
	
	static boolean isAvailable = true;
	private Alarm alarm = new Alarm();
	
	public PrintSpooler() {
		// 프린터 목록 생성 및 초기화	
	}
	
	//각 프린터에 작업완료 이벤트리스터 설정
	public void setJobCompleteListener() {
		for(PrintService ps : printerlist) {
			ps.addPrintServiceAttributeListener((PrintServiceAttributeListener) new PrintServiceAttributeListener() {
				@Override
				public void attributeUpdate(PrintServiceAttributeEvent psae) {
					PrintService ps = psae.getPrintService();
					if(ps.getAttribute(QueuedJobCount.class).toString().equals("0") 
							&& currentJobList.containsKey(ps.getName())) {
						jobCompleteProcess(ps);
					}
				}
			});
		}
	}
	
	//한 프린터의 작업완료시 처리해야할 작업
	public void jobCompleteProcess(PrintService ps) {
		System.out.println("[완료] "+currentJobList.get(ps.getName()).getStudentIDandName()
							+": " + ps.getName());
		alarm.playAlarm();
		currentJobList.remove(ps.getName());
	}
	
	public void setPrinterList(List<PrintService> tmppl) { 
		/*
		List<String> virtualPrinters = new ArrayList<String>();
		virtualPrinters.add("Send To OneNote 2016");
		virtualPrinters.add("Fax");
		virtualPrinters.add("Microsoft Print to PDF");
		virtualPrinters.add("Microsoft XPS Document Writer");
		virtualPrinters.add("Canon MG2900 series Printer");
		
		for(PrintService ps : pl) {
			if(!virtualPrinters.contains(ps.getName())) {
				printerlist.add(ps);
			}
		}
		*/
		printerlist = tmppl;
		System.out.println("Connected printers: " + printerlist.size());
	}
	
	private synchronized PrintService findPrintService() {
		while(true) {
			for (PrintService ps : printerlist) {
				if (ps.getAttribute(QueuedJobCount.class).toString().equals("0")) {
					return ps;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		//return null;
	}
	
	public void deletePrinter(PrintService ps) {
		if(!ps.getAttribute(QueuedJobCount.class).toString().equals("0")) {
			enUrgentJobq(currentJobList.get(ps.getName()));
			System.out.println(ps.getName()+"'s print job is enqueued into urgertJobq.");
		}
		printerlist.remove(ps);
		System.out.println(ps.getName() + " is removed.");
		System.out.println("Connected printers: " + printerlist.size());
	}
	
	public void restartPrinter(PrintService ps) {
		printerlist.add(ps);
		System.out.println(ps.getName() + " is reconnected.");
		System.out.println("Connected printers: " + printerlist.size());
	}
	
	public void print() { //job setting and print, PrintSpooler 전용
		PrintService printservice = findPrintService();

		if(printservice == null) {
			System.out.println("Critical Error. System is exited.");
			System.exit(0);
		}
		
		//test
		System.out.println("before deq: " + jobq.size());
		//test end
		
		//프린트할 정보 큐에서 가져오기
		PrintInfo printinfo = dejobq();
		
		//현재 작업 임시저장
		currentJobList.put(printservice.getName(), printinfo);
		
		//test
		System.out.println("after deq: " + jobq.size());
		//test end
		
		PdfDecoder decodePdf = new PdfDecoder(true); 
		try {
			decodePdf.openPdfFile(printinfo.getPdfPath());
		    FontMappings.setFontReplacements();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		decodePdf.setPrintAutoRotateAndCenter(true);
		decodePdf.setPrintPageScalingMode(PrinterOptions.PAGE_SCALING_FIT_TO_PRINTER_MARGINS);
				
		PdfBook pdfBook = new PdfBook(decodePdf, printservice, printinfo.getAttrSet());
		SimpleDoc doc = new SimpleDoc(pdfBook, DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
		DocPrintJob job = printservice.createPrintJob();
		try {
			job.print(doc, printinfo.getAttrSet());
		} catch (PrintException e) {
			e.printStackTrace();
		} 
	}
	
	public synchronized void enUrgentJobq(PrintInfo printinfo) { urgentJobq.offer(printinfo); }
	public synchronized void enjobq(PrintInfo printinfo) {jobq.offer(printinfo);}
	public synchronized PrintInfo dejobq() {
		if(!urgentJobq.isEmpty())
			return urgentJobq.poll();
		return jobq.poll();
	}
	public boolean jobqIsEmpty() {return urgentJobq.isEmpty()||jobq.isEmpty();}
	public int jobqSize() {return jobq.size() + urgentJobq.size(); }
}


