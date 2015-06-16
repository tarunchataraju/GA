package excel;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

class PatentInventoryMO{
	long patentId;
	String inventoryId;
	int year;
	public long getPatentId() {
		return patentId;
	}
	public void setPatentId(long patentId) {
		this.patentId = patentId;
	}
	public String getInventoryId() {
		return inventoryId;
	}
	public void setInventoryId(String inventoryId) {
		this.inventoryId = inventoryId;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
}
class InventorySubcatMO{
	String inventoryId;
	int subcat;
	int year;
	public String getInventoryId() {
		return inventoryId;
	}
	public void setInventoryId(String inventoryId) {
		this.inventoryId = inventoryId;
	}
	public int getSubcat() {
		return subcat;
	}
	public void setSubcat(int subcat) {
		this.subcat = subcat;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
}
public class CustomExcelReader {
	String filename = "C:\\Users\\tarun\\Desktop\\GA Work\\team_A_test.xlsx";
	public List<PatentInventoryMO> processPatentInventorySheet() throws Exception {
		OPCPackage pkg = OPCPackage.open(filename);
		XSSFReader r = new XSSFReader( pkg );
		SharedStringsTable sst = r.getSharedStringsTable();

		XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		PatentInventorySheetHandler sheetHandler = new PatentInventorySheetHandler(sst);
		ContentHandler handler = sheetHandler;
		parser.setContentHandler(handler);

		InputStream sheet1 = r.getSheet("rId1");
		InputSource sheetSource = new InputSource(sheet1);
		parser.parse(sheetSource);
		List<PatentInventoryMO> patentInventoryList = sheetHandler.getPatentInventories();
		System.out.println("Number of rows in PatentID sheet : "+patentInventoryList.size());
		sheet1.close();
		return patentInventoryList;
	}
	public List<InventorySubcatMO> processInventorySubcatSheet() throws Exception {
		OPCPackage pkg = OPCPackage.open(filename);
		XSSFReader r = new XSSFReader( pkg );
		SharedStringsTable sst = r.getSharedStringsTable();

		XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		InventorySubcatSheetHandler sheetHandler = new InventorySubcatSheetHandler(sst);
		ContentHandler handler = sheetHandler;
		parser.setContentHandler(handler);

		InputStream sheet2 = r.getSheet("rId2");
		InputSource sheetSource = new InputSource(sheet2);
		parser.parse(sheetSource);
		List<InventorySubcatMO> inventorySubcatList = sheetHandler.getInventorySubcats();
		System.out.println("Number of rows in Subcats Sheet : "+inventorySubcatList.size());
		sheet2.close();
		return inventorySubcatList;
	}
	private static class PatentInventorySheetHandler extends DefaultHandler {
		private SharedStringsTable sst;
		private String lastContents;
		private boolean nextIsString;
		PatentInventoryMO patent = new PatentInventoryMO();
		List<PatentInventoryMO> patentInventoryList = new ArrayList<PatentInventoryMO>();
		private int column=0;  
		private int row=0; 
	     
		private PatentInventorySheetHandler(SharedStringsTable sst) {
			this.sst = sst;
		}
		
		 public List<PatentInventoryMO> getPatentInventories() {  
		       return patentInventoryList;  
		 } 
		 
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			
			// c => cell
			if(name.equals("c")) {
				String cellType = attributes.getValue("t");
				if(cellType != null && cellType.equals("s")) {
					nextIsString = true;
				} else {
					nextIsString = false;
				}
			}
			// Clear contents cache
			lastContents = "";
		}
		
		public void endElement(String uri, String localName, String name)
				throws SAXException {
			// v => index of the content of a cell.  
		       if(name.equals("v") && nextIsString) {  
		         try {  
		           int idx = Integer.parseInt(lastContents); //Catch the ID in int  
		           lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString(); // Get the value referenced by index ()
		           nextIsString = false;
		         } catch (NumberFormatException e) {  
		        	 e.printStackTrace();
		         }  
		       }  
		       //If we are reading a cell and columns is not the first  
		       boolean isPatentEmpty = false;
		       boolean isYearEmpty = false;
		       if(name.equals("c") && row>0) {  
		         switch (column)  
		         {  
		           case 0:
		        	   if(lastContents.trim().equals("")){
		        		   isPatentEmpty = true;
		        		   break;
		        	   }
		        	   patent.setPatentId(Long.parseLong(lastContents));  
		             break;  
		           case 1:
		        	   patent.setInventoryId(lastContents);  
		             break;  
		           case 2:
		        	   if(lastContents.trim().equals("")){
		        		   isYearEmpty = true;
		        		   break;
		        	   }
		        	   patent.setYear(Integer.parseInt(lastContents));  
		             break;  
		         }
		         column++;
		       }
		     //If it is the end of a row, save the current Parent Inventory object. And create a new one  
		       if(name.equals("row") && !isPatentEmpty && !isYearEmpty) {
		         if(row>0 && patent.getPatentId()!= 0 && patent.getYear() != 0)  {
		        	 patentInventoryList.add(patent);
		         }
		         patent=new PatentInventoryMO();  
		         row++;  
		         column=0;  
		       }  
		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			
			lastContents += new String(ch, start, length);
		}
	}
	
	private static class InventorySubcatSheetHandler extends DefaultHandler {
		private SharedStringsTable sst;
		private String lastContents;
		private boolean nextIsString;
		InventorySubcatMO inventorySubcat = new InventorySubcatMO();
		List<InventorySubcatMO> inventorySubcatList = new ArrayList<InventorySubcatMO>();
		private int column=0;  
		private int row=0; 
	     
		private InventorySubcatSheetHandler(SharedStringsTable sst) {
			this.sst = sst;
		}
		
		 public List<InventorySubcatMO> getInventorySubcats() {  
		       return inventorySubcatList;  
		 } 
		 
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			
			// c => cell
			if(name.equals("c")) {
				String cellType = attributes.getValue("t");
				if(cellType != null && cellType.equals("s")) {
					nextIsString = true;
				} else {
					nextIsString = false;
				}
			}
			// Clear contents cache
			lastContents = "";
		}
		
		public void endElement(String uri, String localName, String name)
				throws SAXException {
			// v => index of the content of a cell.  
		       if(name.equals("v") && nextIsString) {  
		         try {  
		           int idx = Integer.parseInt(lastContents); //Catch the ID in int  
		           lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString(); // Get the value referenced by index ()
		           nextIsString = false;
		         } catch (NumberFormatException e) {  
		        	 e.printStackTrace();
		         }  
		       }  
		       //If we are reading a cell and columns is not the first  
		       boolean isSubcatEmpty = false;
		       boolean isYearEmpty = false;
		       if(name.equals("c") && row>0) {  
		         switch (column)  
		         {  
		           case 0:
		        	   inventorySubcat.setInventoryId(lastContents);  
		             break;  
		           case 1:
		        	   if(lastContents.trim().equals("")){
		        		   isSubcatEmpty = true;
		        		   break;
		        	   }
		        	   inventorySubcat.setSubcat(Integer.parseInt(lastContents));  
		             break;  
		           case 2:
		        	   if(lastContents.trim().equals("")){
		        		   isYearEmpty = true;
		        		   break;
		        	   }
		        	   inventorySubcat.setYear(Integer.parseInt(lastContents));  
		             break;  
		         }
		         column++;
		       }
		     //If it is the end of a row, save the current Parent Inventory object. And create a new one  
		       if(name.equals("row") && !isSubcatEmpty && !isYearEmpty) {
		         if(row>0 && inventorySubcat.getSubcat()!= 0 && inventorySubcat.getYear() != 0)  {
		        	 inventorySubcatList.add(inventorySubcat);
		         }
		         inventorySubcat=new InventorySubcatMO();  
		         row++;  
		         column=0;  
		       }  
		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			
			lastContents += new String(ch, start, length);
		}
	}
	
}