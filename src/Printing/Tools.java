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
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
//import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

public class Tools {
		// only for A4
		public static void NUp(String src, String dest, int pow) throws IOException, DocumentException {
			// reader for the src file
			PdfReader reader = new PdfReader(src);
			// initializations
			Rectangle pageSize = reader.getPageSize(1);

			/*
			 * word_test = 595.0, 842.0 
			 * a4_size = 595.4, 841.8 
			 * word => only 2up(pow 1). 
			 * ppt => 2up ~ 4up(pow 1 or 2).
			 */
			/* size test
			size test System.out.println(pageSize.getWidth()+", "+pageSize.getHeight());
			System.exit(0);
			*/

			// ppt2up 판별
			boolean isPPT = false;
			final float A4_WIDTH = (float) 595.4;
			final float A4_HEIGHT = (float) 841.8;
			if (pageSize.getWidth() > pageSize.getHeight()) {
				isPPT = true;
			}

			/*(original) set page size of new document Rectangle newSize = (pow % 2) == 0 ?
			new Rectangle(pageSize.getWidth(), pageSize.getHeight()) : new
			Rectangle(pageSize.getHeight(), pageSize.getWidth());
			*/

			// (modified) set page size of new document
			Rectangle newSize;
			if (pow == 1 && isPPT) {
				newSize = new Rectangle(A4_WIDTH, A4_HEIGHT);
			} else {
				newSize = new Rectangle(A4_HEIGHT, A4_WIDTH);
			}

			/*(original) calculates page size of unit 
			Rectangle unitSize = new
			Rectangle(pageSize.getWidth(), pageSize.getHeight()); 
			for (int i = 0; i < pow; i++) {
			 unitSize = new Rectangle(unitSize.getHeight() / 2,
									  unitSize.getWidth()); }
			*/

			// (modified) calculates page size of unit
			Rectangle unitSize;
			if (isPPT) {//ppt
				//pow = 1 (2up)
				//System.out.println(newSize.getWidth()+", "+ newSize.getHeight());
				unitSize = new Rectangle(newSize.getWidth(), newSize.getHeight()/2);
				//System.out.println(unitSize.getWidth()+", "+ unitSize.getHeight());
				if(pow == 2) //(4up)
					unitSize = new Rectangle(unitSize.getWidth() / 2, unitSize.getHeight());
				//System.out.println(unitSize.getWidth()+", "+ unitSize.getHeight());
			} else {//word
				//pow = 1 (2up)
				unitSize = new Rectangle(newSize.getWidth()/2, newSize.getHeight());
			}
			
			int n = (int) Math.pow(2, pow); //페이지당 unit개수
			int r = (int) Math.pow(2, pow / 2); //행
			int c = n / r; //열

			Document document = new Document(newSize, 0, 0, 0, 0);
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(String.format(dest, n)));
			document.open();

			PdfContentByte cb = writer.getDirectContent();
			PdfImportedPage page;
			Rectangle currentSize =  reader.getPageSize(1);
			float offsetX, offsetY, factor;
			int total = reader.getNumberOfPages();
			factor = Math.min( unitSize.getWidth()/ currentSize.getWidth(), 
					   unitSize.getHeight()/ currentSize.getHeight()) * (float)0.95;
			
			/*test
			System.out.println("======================================");
			System.out.println("u: " + unitSize.getWidth() + ", " + unitSize.getHeight());
			System.out.println("c: " + currentSize.getWidth() + ", " + currentSize.getHeight());
			System.out.println("f: " + factor);
			System.out.println("======================================");
			test end*/
			
			for (int i = 0; i < total;) {
				if (i % n == 0) {
					document.newPage();
				}
				//currentSize = reader.getPageSize(++i);
				/*(original) calculates scale factor 
				factor = Math.min( unitSize.getWidth()/ currentSize.getWidth(), 
								   unitSize.getHeight()/ currentSize.getHeight());
				*/

				// calculates offset
				if(isPPT && pow == 1) {
					offsetX = (unitSize.getWidth() - (currentSize.getWidth() * factor)) / 2f;;
					offsetY = newSize.getHeight() - (unitSize.getHeight() * (((i % n) % c) + 1))
							+ (unitSize.getHeight() - (currentSize.getHeight() * factor)) / 2f;
				} else {
					offsetX = unitSize.getWidth() * ((i % n) % c)
						+ (unitSize.getWidth() - (currentSize.getWidth() * factor)) / 2f;
					offsetY = newSize.getHeight() - (unitSize.getHeight() * (((i % n) / c) + 1))
						+ (unitSize.getHeight() - (currentSize.getHeight() * factor)) / 2f;
				}
				i++;

				/* test
				System.out.println(i+": "+offsetX + ", " + offsetY);
				 test end	*/
				
				// scales and positions page
				page = writer.getImportedPage(reader, i);
				//테두리 추가하려면 여기서
				//~
				//cb.addTemplate(template, xScale*xs, xRote*xs, yRote*ys, yScale*ys, offsetX, offsetY); 
				cb.addTemplate(page, factor, 0, 0, factor, offsetX, offsetY);
				

			}
			document.close();
			reader.close();
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
	        /*
	    	if(Files.deleteIfExists(original)) {
	    		System.out.println("삭제 성공");
	    	}
	    	if(file.renameTo(new File(SRC)))
	    		System.out.println("성공");
	    	*/
		}
		
		/*
		 * public void rotatePdf() {
		 * 
		 * }
		 */
		 /* public void stampID() {
		 * 
		 * }
		 */
		
		/* test
		public final static String src1 = "./word_test.pdf";
		public final static String src2 = "./ppt_test.pdf";
		public static final String RESULT = "./word_test%dup.pdf";
		public static final String RESULT2 = "./ppt_test%dup.pdf";

		public static void main(String[] args) throws IOException, DocumentException, SQLException {
			// word test
			//new Tool().NUp(src1, RESULT, 1); //OK

			// ppt test
			//new Tool().NUp(src2, RESULT2, 1); //양옆으로 나옴, 위아래로 수정필요
			new Tool().NUp(src2, RESULT2, 2); // OK
		}
		test end */
}
