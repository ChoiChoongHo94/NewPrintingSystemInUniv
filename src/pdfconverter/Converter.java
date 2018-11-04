package pdfconverter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.xwpf.converter.core.XWPFConverterException;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

//import static org.testng.Assert.assertNotNull;
//import static org.testng.Assert.assertTrue;

import java.io.FilenameFilter;
import java.util.Set;




/* ★★★★★Converter 기능은 안드로이드 상에서 진행. ★★★★★
 *  ms-graph api 키
 * msal396e5af3-1816-4b77-9cd9-7c732bf704f4://auth
 * urn:ietf:wg:oauth:2.0:oob
 * https://login.microsoftonline.com/common/oauth2/nativeclient
 */

public class Converter {
	public static void convertWORDtoPDF(String docPath, String pdfPath) {
		// 세번째 시도(Microsoft-Graph 사용) => 안드로이드에서 시행
		
		/* 두번째 시도(openoffice, jodconverter 사용)
		OfficeManager officeManager = new DefaultOfficeManagerConfiguration()
				.setOfficeHome("C:/Program Files (x86)/OpenOffice 4").buildOfficeManager();
		OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
		DocumentFormatRegistry formatRegistry = converter.getFormatRegistry();
		officeManager.start();

		File inputFile = new File(docPath);
		//DocumentFormat inputFormat = formatRegistry.getFormatByExtension(inputExtension);
		File outputFile = new File(pdfPath);
		DocumentFormat outputFormat = formatRegistry.getFormatByExtension("pdf");
		converter.convert(inputFile, outputFile, outputFormat);
		System.out.println("Word file converted to PDF successfully");
		*/
		

		/* 첫번째 시도(Apache-poi 사용)
		 * try { InputStream doc = new FileInputStream(new File(docPath)); XWPFDocument
		 * document = new XWPFDocument(doc);
		 * 
		 * PdfOptions options = PdfOptions.create(); OutputStream out = new
		 * FileOutputStream(new File(pdfPath));
		 * PdfConverter.getInstance().convert(document, out, options); doc.close();
		 * out.close(); System.out.println("Word file converted to PDF successfully"); }
		 * catch (FileNotFoundException ex) { System.out.println(ex.getMessage()); }
		 * catch (IOException ex) { System.out.println(ex.getMessage()); }
		 */
	}

	public static void convertPPTtoPDF(String sourcepath, String destinationPath, String fileType) {
		try {
			FileInputStream inputStream = new FileInputStream(sourcepath);
			double zoom = 1;
			AffineTransform at = new AffineTransform();
			at.setToScale(zoom, zoom);
			Document pdfDocument = new Document();
			PdfWriter pdfWriter = PdfWriter.getInstance(pdfDocument, new FileOutputStream(destinationPath));
			PdfPTable table = new PdfPTable(1);
			pdfWriter.open();
			pdfDocument.open();
			Dimension pgsize = null;
			Image slideImage = null;
			BufferedImage img = null;
			if (fileType.equalsIgnoreCase("pptx")) {
				XMLSlideShow ppt = new XMLSlideShow(inputStream);
				pgsize = ppt.getPageSize();
				XSLFSlide slide[] = ppt.getSlides();
				pdfDocument.setPageSize(new Rectangle((float) pgsize.getWidth(), (float) pgsize.getHeight()));
				pdfWriter.open();
				pdfDocument.open();
				for (int i = 0; i < slide.length; i++) {
					img = new BufferedImage((int) Math.ceil(pgsize.width * zoom), (int) Math.ceil(pgsize.height * zoom),
							BufferedImage.TYPE_INT_RGB);
					Graphics2D graphics = img.createGraphics();
					graphics.setTransform(at);
					graphics.setPaint(Color.white);
					graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));
					slide[i].draw(graphics);
					graphics.getPaint();
					slideImage = Image.getInstance(img, null);
					table.addCell(new PdfPCell(slideImage, true));
				}
			}
			pdfDocument.add(table);
			pdfDocument.close();
			pdfWriter.close();
			System.out.println("Powerpoint file converted to PDF successfully");
		} catch (FileNotFoundException ex) {
			System.out.println(ex.getMessage());
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		} catch (BadElementException ex) {
			System.out.println(ex.getMessage());
		} catch (DocumentException ex) {
			System.out.println(ex.getMessage());
		}
		
		/* ppt
		 * 			if (fileType.equalsIgnoreCase("ppt")) {
				SlideShow ppt = new SlideShow(inputStream);
				pgsize = ppt.getPageSize();
				Slide slide[] = ppt.getSlides();
				pdfDocument.setPageSize(new Rectangle((float) pgsize.getWidth(), (float) pgsize.getHeight()));
				pdfWriter.open();
				pdfDocument.open();
				for (int i = 0; i < slide.length; i++) {
					img = new BufferedImage((int) Math.ceil(pgsize.width * zoom), (int) Math.ceil(pgsize.height * zoom),
							BufferedImage.TYPE_INT_RGB);
					Graphics2D graphics = img.createGraphics();
					graphics.setTransform(at);
					graphics.setPaint(Color.white);
					graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));
					slide[i].draw(graphics);
					graphics.getPaint();
					slideImage = Image.getInstance(img, null);
					table.addCell(new PdfPCell(slideImage, true));
				}
			}
		 */
	}
}
