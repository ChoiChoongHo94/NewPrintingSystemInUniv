package Printing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.Attribute;
import javax.print.attribute.standard.QueuedJobCount;

import org.jpedal.PdfDecoder;
import org.jpedal.fonts.FontMappings;
import org.jpedal.objects.PrinterOptions;
import org.jpedal.utils.PdfBook;

public class PrintSpooler { // Singleton
	
	static private HashMap<String,PrintInfo> currentJobList = new HashMap<String,PrintInfo>();
	static private Queue<PrintInfo> urgentJobq = new LinkedList<PrintInfo>();
	static private Queue<PrintInfo> jobq = new LinkedList<PrintInfo>();
	static List<PrintService> printerlist = new ArrayList<PrintService>();
	
	static boolean isAvailable = true;
	
	public PrintSpooler() {
		// 프린터 목록 생성 및 초기화		
	}
	
	public void setPrinterList(List<PrintService> pl) { 
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
		printerlist = pl;
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
		System.out.println(ps.getName() + " is connected.");
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
		
		PrintInfo printinfo = dejobq();
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

