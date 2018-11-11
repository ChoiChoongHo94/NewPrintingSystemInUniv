package Printing;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.standard.PrinterState;
import javax.print.attribute.standard.PrinterStateReason;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;

import org.jpedal.PdfDecoder;
import org.jpedal.fonts.FontMappings;
import org.jpedal.objects.PrinterOptions;
import org.jpedal.utils.PdfBook;

public class PrintSpooler { // Singleton
	
	static private Queue<PrintInfo> jobq = new LinkedList<PrintInfo>();
	static List<PrintService> printerlist = new ArrayList<PrintService>();
	
	static boolean isAvailable = true;
	
	public PrintSpooler() {
		// 프린터 목록 생성 및 초기화
		setPrinterList();
		System.out.println("Connected printers: " + printerlist.size());
	}
	
	private void setPrinterList() { // 진행중
		PrintService[] pl = PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PAGEABLE,null);
		List<String> virtualPrinters = new ArrayList<String>();
		
		/*
		 * 가상 프린터 추가하기
		 */
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
	}
	
	private synchronized PrintService findPrintService() {
		while(true) {
			for (PrintService ps : printerlist) {
				for (Attribute a : ps.getAttributes().toArray()) {
					if (a.getName().equals("queued-job-count") && a.toString().equals("0")) {
						return ps;
					}
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

	public synchronized void enjobq(PrintInfo printinfo) {jobq.offer(printinfo);}
	public PrintInfo dejobq() {return jobq.poll();}
	public boolean jobqIsEmpty() {return jobq.isEmpty();}
}

class Pair{
	boolean first; //현재 사용 가능 여부
	PrintService second;
	
	Pair(boolean isAvailable, PrintService second){
		this.first = isAvailable;
		this.second = second;
	}
}
