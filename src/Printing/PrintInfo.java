package Printing;

import java.io.IOException;

import javax.print.DocFlavor;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

import com.itextpdf.text.DocumentException;
import pdfconverter.Converter;

public class PrintInfo {
	private String pdfpath;
	private DocFlavor doc_flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
	private PrintRequestAttributeSet attr_set = new HashPrintRequestAttributeSet();
	private String studentIDandName;
	private boolean isAddStampIDatLast = false; //copy 1 = false, copy > 1 = true;
	
	public PrintInfo(String path, int pow, int copy, int border, String idAndName) {
		studentIDandName = idAndName;
		setAttrSet(copy);
		
		if(path.substring(path.lastIndexOf(".") +1 ).equalsIgnoreCase("docx"))
			try {
				path = Converter.convertDOCXtoPDF(path);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		else if(path.substring(path.lastIndexOf(".") +1 ).equalsIgnoreCase("pptx"))
			path = Converter.convertPPTXtoPDF(path);
		
		if(pow >= 1) {
			try {
				String newpath = path.substring(0, path.lastIndexOf("/") + 1) + "_" + path.substring(path.lastIndexOf("/") + 1);
				
				if(border > 0)
					Tools.setPageBorder(path, newpath);
				Tools.NUp(path, newpath, pow);
				Tools.stampIDandName(newpath);
				pdfpath = newpath;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (DocumentException e) {
				e.printStackTrace();
			}
		}
		else {
			pdfpath = path;
		}
	}

	private void setAttrSet(/*boolean isVertical, int multiple,*/ int copy) {
		attr_set.add(MediaSizeName.ISO_A4);
		if(copy > 1) {
			attr_set.add(new Copies(copy -1));
			isAddStampIDatLast = true;
		}
	}
	
	public boolean getIsAddStampIDatLast() { return isAddStampIDatLast; }
	public String getPdfPath() { return pdfpath; }
	public DocFlavor getDocFlavor() { return doc_flavor; }
	public PrintRequestAttributeSet getAttrSet() { return attr_set; }
}


