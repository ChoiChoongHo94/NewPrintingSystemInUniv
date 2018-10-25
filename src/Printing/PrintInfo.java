package Printing;

import javax.print.DocFlavor;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

import pdfconverter.Converter;

public class PrintInfo {
	private String pdfpath;
	private DocFlavor doc_flavor = DocFlavor.INPUT_STREAM.PDF;
	private PrintRequestAttributeSet attr_set = new HashPrintRequestAttributeSet();
	
	public PrintInfo(String path, boolean isVertical, int multiple, int copy) {
		setAttrSet(isVertical, multiple, copy);

		String fileType = path.substring(path.lastIndexOf(".") + 1);
		if (fileType.equals("pdf"))
			// pdf = new PDFPrint(path, null);
			pdfpath = path;
		else {
			String newPath = path.substring(0, path.lastIndexOf(".")) + ".pdf";
			// word to pdf 변환
			if (fileType.equals("docx")) {
				Converter.convertWORDtoPDF(path, newPath);
			}
			// ppt to pdf 변환=
			else if (fileType.equals("pptx") || fileType.equals("ppt")) {
				Converter.convertPPTtoPDF(path, newPath, fileType);
			} else
				System.out.println("Not supporting file type!");
			// pdf = new PDFPrint(newPath, null);
			pdfpath = newPath;
		}
	}

	private void setAttrSet(boolean isVertical, int multiple, int copy) {
		attr_set.add(MediaSize.ISO.A4);
		if (isVertical)
			attr_set.add(OrientationRequested.PORTRAIT);
		else
			attr_set.add(OrientationRequested.LANDSCAPE);
		attr_set.add(new NumberUp(multiple));
		attr_set.add(new Copies(copy));
	}
	
	public String getPdfPath() { return pdfpath; }
	public DocFlavor getDocFlavor() { return doc_flavor; }
	public PrintRequestAttributeSet getAttrSet() { return attr_set; }
}


