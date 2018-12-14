package pdfconverter;

import com.aspose.words.Document;
import com.aspose.slides.Presentation;

public class Converter {
	public static String convertDOCXtoPDF(String src) throws Exception {
		String output = src.substring(0,src.lastIndexOf("."))
						+ ".pdf";
		
		Document document = new Document(src);
		document.save(output, com.aspose.words.SaveFormat.PDF);
		
		return output;
	}

	public static String convertPPTXtoPDF(String src) {
		String output = src.substring(0,src.lastIndexOf("."))
						+ ".pdf";
		
		Presentation presentation = new Presentation(src);
		presentation.save(output, com.aspose.slides.SaveFormat.Pdf);
		
		return output;
	}
	
	public static void setLicenses() throws Exception {
		com.aspose.words.License license1 = new com.aspose.words.License();
		license1.setLicense("./lib/Aspose.Words.lic");

		if (license1.isLicensed()) {
		    System.out.println("Words.License is Set!");
		}
		
		com.aspose.slides.License license2 = new com.aspose.slides.License();
		license2.setLicense("./lib/Aspose.Slides.lic");

		if (license2.isLicensed()) {
		    System.out.println("Slides.License is Set!");
		}
	}
}
