package Printing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

public class Tools {
	// only for A4
	// a4_size = 595.4, 841.8 
	public static void NUp(String src, String dest, int pow) throws IOException, DocumentException {

		PdfReader reader = new PdfReader(src);
		Rectangle pageSize = reader.getPageSize(1);

		/*
		 * word_test = 595.0, 842.0 
		 * a4_size = 595.4, 841.8 
		 * word => only 2up(pow 1). 
		 * ppt => 2up ~ 4up(pow 1 or 2).
		 */

		// ppt2up 판별
		boolean isPPT = false;
		final float A4_WIDTH = (float) 595.4;
		final float A4_HEIGHT = (float) 841.8;
		if (pageSize.getWidth() > pageSize.getHeight()) {
			isPPT = true;
		}

		// (modified) set page size of new document
		Rectangle newSize;
		if (pow == 1 && isPPT) {
			newSize = new Rectangle(A4_WIDTH, A4_HEIGHT);
		} else {
			newSize = new Rectangle(A4_HEIGHT, A4_WIDTH);
		}

		// (modified) calculates page size of unit
		Rectangle unitSize;
		if (isPPT) {// ppt
			unitSize = new Rectangle(newSize.getWidth(), newSize.getHeight() / 2);
			if (pow == 2) // (4up)
				unitSize = new Rectangle(unitSize.getWidth() / 2, unitSize.getHeight());
		} else {// word
			// pow = 1 (2up)
			unitSize = new Rectangle(newSize.getWidth() / 2, newSize.getHeight());
		}

		int n = (int) Math.pow(2, pow); // 페이지당 unit개수
		int r = (int) Math.pow(2, pow / 2); // 행
		int c = n / r; // 열

		Document document = new Document(newSize, 0, 0, 0, 0);
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(String.format(dest, n)));
		document.open();

		PdfContentByte cb = writer.getDirectContent();
		PdfImportedPage page;
		Rectangle currentSize = reader.getPageSize(1);
		float offsetX, offsetY, factor;
		int total = reader.getNumberOfPages();
		factor = Math.min(unitSize.getWidth() / currentSize.getWidth(), unitSize.getHeight() / currentSize.getHeight())
				* (float) 0.95;

		for (int i = 0; i < total;) {
			if (i % n == 0) {
				document.newPage();
			}

			// calculates offset
			if (isPPT && pow == 1) {
				offsetX = (unitSize.getWidth() - (currentSize.getWidth() * factor)) / 2f;
				;
				offsetY = newSize.getHeight() - (unitSize.getHeight() * (((i % n) % c) + 1))
						+ (unitSize.getHeight() - (currentSize.getHeight() * factor)) / 2f;
			} else {
				offsetX = unitSize.getWidth() * ((i % n) % c)
						+ (unitSize.getWidth() - (currentSize.getWidth() * factor)) / 2f;
				offsetY = newSize.getHeight() - (unitSize.getHeight() * (((i % n) / c) + 1))
						+ (unitSize.getHeight() - (currentSize.getHeight() * factor)) / 2f;
			}
			i++;

			// scales and positions page
			page = writer.getImportedPage(reader, i);

			// cb.addTemplate(template, xScale*xs, xRote*xs, yRote*ys, yScale*ys, offsetX,
			// offsetY);
			cb.addTemplate(page, factor, 0, 0, factor, offsetX, offsetY);

		}
		document.close();
		reader.close();
		
		if((isPPT && pow == 2) || !isPPT)
			rotatePDF(dest);
	}

	public static void setPageBorder(String src, String dest) throws IOException, DocumentException {
		PdfReader reader = new PdfReader(src);
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));

		PdfContentByte canvas;
		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			canvas = stamper.getOverContent(i);
			Rectangle rect = reader.getPageSize(1);
			rect.setBorder(Rectangle.BOX);
			rect.setBorderWidth(2);
			rect.setBorderColor(BaseColor.BLACK);
			rect.setUseVariableBorders(true);
			canvas.rectangle(rect);
		}
		stamper.close();
		reader.close();

		System.gc();
		System.runFinalization();
		File file = new File(dest);
		Path original = Paths.get(src);
		Files.deleteIfExists(original);
		file.renameTo(new File(src));
	}
	
	public static void stampIDandName(String src, String studentIDandName) {
		String tempDest = src.substring(0, src.lastIndexOf("/") + 1) + "_" 
							+ src.substring(src.lastIndexOf("/") + 1);

		PdfReader reader = null;
		PdfStamper stamper = null;
		try {
			reader = new PdfReader(src);
			stamper = new PdfStamper(reader, new FileOutputStream(tempDest));
		} catch (DocumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stamper.setRotateContents(true);
		PdfContentByte canvas = stamper.getOverContent(1);
		BaseFont objBaseFont = null;
		try {
			objBaseFont = BaseFont.createFont("./font/malgun.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
		} catch (DocumentException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		 //BaseFont.IDENTITY_H  //The Unicode encoding with horizontal writing.
		Font objFont = new Font(objBaseFont, 6);
		//[학번_이름] size = (60, 7)
		// a4_size = 595.4, 841.8 
		ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT, new Phrase("["+studentIDandName+"]", objFont), 595 - 70, 841 - 8, 0);
		try {
			stamper.close();
			reader.close();
		} catch (DocumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.gc();
		System.runFinalization();
		File file = new File(tempDest);
		Path original = Paths.get(src);
		try {
			Files.deleteIfExists(original);
		} catch (IOException e) {
			e.printStackTrace();
		}
		file.renameTo(new File(src));
		//return tempDest;
	}
	
	public static void rotatePDF(String src) throws IOException, DocumentException {
		String tempDest = src.substring(0, src.lastIndexOf("/") + 1) + "_" 
				+ src.substring(src.lastIndexOf("/") + 1);
		PdfReader reader = new PdfReader(src);
		int n = reader.getNumberOfPages();
		PdfDictionary page;
		PdfNumber rotate;
		for (int p = 1; p <= n; p++) {
		    page = reader.getPageN(p);
		    rotate = page.getAsNumber(PdfName.ROTATE);
		    if (rotate == null) {
		        page.put(PdfName.ROTATE, new PdfNumber(90));
		    }
		    else {
		        page.put(PdfName.ROTATE, new PdfNumber((rotate.intValue() + 90) % 360));
		    }
		}
		
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(tempDest));
		stamper.close();
		reader.close();
		
		System.gc();
		System.runFinalization();
		File file = new File(tempDest);
		Path original = Paths.get(src);
		try {
			Files.deleteIfExists(original);
		} catch (IOException e) {
			e.printStackTrace();
		}
		file.renameTo(new File(src));
	}
}
