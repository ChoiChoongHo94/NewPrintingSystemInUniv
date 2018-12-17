package Printing;

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
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;
import javax.print.event.PrintServiceAttributeEvent;
import javax.print.event.PrintServiceAttributeListener;

import org.jpedal.PdfDecoder;
import org.jpedal.fonts.FontMappings;
import org.jpedal.objects.PrinterOptions;
import org.jpedal.utils.PdfBook;

public class PrintSpooler { // Singleton
	
	//static private List<String> ongoingPrinters = new ArrayList<String>();
	static private HashMap<String,PrintInfo> currentJobList = new HashMap<String,PrintInfo>();
	static private HashMap<String,PrintInfo> alarmList = new HashMap<String,PrintInfo>();
	static private Queue<PrintInfo> urgentJobq = new LinkedList<PrintInfo>();
	static private Queue<PrintInfo> jobq = new LinkedList<PrintInfo>();
	static List<PrintService> printerlist = new ArrayList<PrintService>();
	
	
	static boolean isAvailable = true;
	private Alarm alarm = new Alarm();
	
	public PrintSpooler() {
		// ������ ��� ���� �� �ʱ�ȭ
		System.out.println("[SYSTEM] PrintSpooler is created.");
	}
	
	//�� �����Ϳ� �۾��Ϸ� �̺�Ʈ������ ����
	public void setJobCompleteListener() {
		for(PrintService ps : printerlist) {
			ps.addPrintServiceAttributeListener((PrintServiceAttributeListener) new PrintServiceAttributeListener() {
				@Override
				public void attributeUpdate(PrintServiceAttributeEvent psae) {
					
					PrintService ps = psae.getPrintService();
					
					//test
					/*
					System.out.println("@@@@@@@@@@ psae @@@@@@@@@");
					System.out.println(ps.getAttribute(PrinterIsAcceptingJobs.class).toString());
					System.out.println(ps.getAttribute(QueuedJobCount.class).toString());
					*/
					
					if(ps.getAttribute(QueuedJobCount.class).toString().equals("0") 
							&& currentJobList.containsKey(ps.getName())) {
						jobCompleteProcess(ps);
						currentJobList.remove(ps.getName());
						alarmList.remove(ps.getName());
					}	
				}
			});
		}
	}
	
	//�� �������� �۾��Ϸ�� ó���ؾ��� �۾�
	public synchronized void jobCompleteProcess(PrintService ps) {
		//test
		//System.out.println(alarmList.get(ps.getName()).getStudentIDandName());
		
		System.out.println("[프린트 완료] "+ps.getName()+": "+alarmList.get(ps.getName()).getStudentIDandName());
		alarm.playAlarm();
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
		for(PrintService ps : printerlist)
			System.out.println(ps.getName() + "(Printer) is connected.");
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
			System.out.println(ps.getName()+"'s prints job is enqueued into urgertJobq.");
		}
		printerlist.remove(ps);
		currentJobList.remove(ps.getName());
		alarmList.remove(ps.getName());
		System.out.println(ps.getName() + " is stopped.");
		System.out.println("Connected printers: " + printerlist.size());
	}
	
	public void restartPrinter(PrintService ps) {
		printerlist.add(ps);
		System.out.println(ps.getName() + " is reconnected.");
		System.out.println("Connected printers: " + printerlist.size());
	}
	
	public void print() { //job setting and print, PrintSpooler ����
		PrintService printservice = findPrintService();

		if(printservice == null) {
			System.out.println("Critical Error. System is exited.");
			System.exit(0);
		}

		//����Ʈ�� ���� ť���� ��������
		PrintInfo printinfo = dejobq();
		
		//���� �۾� �ӽ�����
		currentJobList.put(printservice.getName(), printinfo);

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
		job.addPrintJobListener((PrintJobListener) new PrintJobListener() {

			@Override
			public void printJobCanceled(PrintJobEvent arg0) {
				System.err.println("cancel");
				currentJobList.remove(printservice.getName());
				alarmList.remove(printservice.getName());
			}

			@Override
			public void printJobCompleted(PrintJobEvent arg0) {}

			@Override
			public void printJobFailed(PrintJobEvent arg0) {
				System.err.println("fail");
				currentJobList.remove(printservice.getName());
				alarmList.remove(printservice.getName());
			}

			@Override
			public void printJobNoMoreEvents(PrintJobEvent arg0) {}

			@Override
			public void printJobRequiresAttention(PrintJobEvent arg0) {}

			@Override
			public void printDataTransferCompleted(PrintJobEvent arg0) {
				if(alarmList.containsKey(printservice.getName())){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					jobCompleteProcess(printservice);
				}
				alarmList.put(printservice.getName(), printinfo);
			}
		});
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
	public boolean jobqIsEmpty() {return urgentJobq.isEmpty()&&jobq.isEmpty();}
	public int jobqSize() {return jobq.size() + urgentJobq.size(); }
}


