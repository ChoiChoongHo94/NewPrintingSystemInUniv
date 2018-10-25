package Printing;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import javax.print.Doc;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;

public class PrintSpooler { // Singleton
	private Queue<PrintInfo> jobq = new LinkedList<PrintInfo>();
	private PrintService[] printerlist;
	private PrintJobWatcher jobwatcher;
	private int numofprinter;
	private boolean[] isavailable;
	
	public PrintSpooler() {
		// 프린터 목록 생성 및 초기화
		printerlist = PrintServiceLookup.lookupPrintServices(null,null);
		numofprinter = printerlist.length;
		jobwatcher = new PrintJobWatcher(numofprinter);
		isavailable = new boolean[numofprinter];
		numofprinter--;
		Arrays.fill(isavailable, true);
	}
	
	private PrintService findPrintService() {
		int i = 0 ;
		for(PrintService ps: printerlist) {
			if(isavailable[i])
				return ps;
			i++;
		}
		return null;
	}
	
	public void print() { //job setting and print, PrintSpooler 전용
		PrintInfo printinfo = dejobq();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(printinfo.getPdfPath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		if(!jobwatcher.isAvailable()) {
			jobwatcher.waitForDone();
		}
		
		PrintService printservice = findPrintService();
		if(printservice == null) {
			System.out.println("Error! Exit!");
			System.exit(1);
		}
		DocPrintJob job = printservice.createPrintJob();
		Doc doc = new SimpleDoc(fis, printinfo.getDocFlavor(), null);
		try {
			job.print(doc, printinfo.getAttrSet());
			jobwatcher.setListener(job);
		} catch (PrintException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void enjobq(PrintInfo printinfo) {jobq.offer(printinfo);}
	public PrintInfo dejobq() {return jobq.poll();}
	public boolean jobqIsEmpty() {return jobq.isEmpty();}
}

class PrintJobWatcher { //singleton
	boolean isAvailable = true;
	int max;
	int beingused = 0;

	PrintJobWatcher(int numofprinter) {
		max = numofprinter;
	}
	
	public synchronized void setListener(DocPrintJob job) {
		beingused++;
		job.addPrintJobListener(new PrintJobAdapter() {
			public void printJobCanceled(PrintJobEvent pje) {
				allDone();
			}

			public void printJobCompleted(PrintJobEvent pje) {
				allDone();
			}

			public void printJobFailed(PrintJobEvent pje) {
				allDone();
			}

			public void printJobNoMoreEvents(PrintJobEvent pje) {
				allDone();
			}

			void allDone() {
				synchronized (PrintJobWatcher.this) {
					beingused--;
					PrintJobWatcher.this.notify();
				}
			}
		});
	}
	
	public boolean isAvailable() { return max == beingused ? true : false; }

	public synchronized void waitForDone() {
		try {
			while (max == beingused) {
				wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

/*
class Pair{
	int first;
	PrintService second;
	
	Pair(int first, PrintService second){
		this.first = first;
		this.second = second;
	}
}
*/