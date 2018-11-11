package Printing;

import java.io.IOException;

import javax.print.DocFlavor;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

import com.itextpdf.text.DocumentException;

//import pdfconverter.Converter;

public class PrintInfo {
	private String pdfpath;
	private DocFlavor doc_flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
	private PrintRequestAttributeSet attr_set = new HashPrintRequestAttributeSet();
	
	public PrintInfo(String path, int pow, int copy, int border) {
		setAttrSet(copy);
		
		if(pow >= 1) {
			try {
				String newpath = path.substring(0, path.lastIndexOf("/") + 1) + "_" + path.substring(path.lastIndexOf("/") + 1);
				
				//test
				//System.out.println(newpath);
				//System.exit(1);
				
				if(border > 0)
					Tools.setPageBorder(path, newpath);
				Tools.NUp(path, newpath, pow);
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
		
		/* PDF변환은 MS-Graph를 이용하여 안드로이드 상에서 진행
		String fileType = path.substring(path.lastIndexOf(".") + 1);
		if (fileType.equalsIgnoreCase("pdf"))
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
		*/
	}

	private void setAttrSet(/*boolean isVertical, int multiple,*/ int copy) {
		attr_set.add(MediaSizeName.ISO_A4);
		/*
		if (isVertical)
			attr_set.add(OrientationRequested.PORTRAIT);
		else
			attr_set.add(OrientationRequested.LANDSCAPE);
		attr_set.add(new NumberUp(multiple));
		*/
		attr_set.add(new Copies(copy));
	}
	
	public String getPdfPath() { return pdfpath; }
	public DocFlavor getDocFlavor() { return doc_flavor; }
	public PrintRequestAttributeSet getAttrSet() { return attr_set; }
}


