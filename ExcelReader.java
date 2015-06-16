package article;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


class DeliverableSetEntry// implements Comparable<DeliverableSetEntry>
{
	String beaCode;
    int pubYear;
    String orgId;
    String orgType;
    long articleId;
    byte focalfirm;
    public String getBeaCode() {
		return beaCode;
	}
	public void setBeaCode(String beaCode) {
		this.beaCode = beaCode;
	}
	public int getPubYear() {
		return pubYear;
	}
	public void setPubYear(int pubYear) {
		this.pubYear = pubYear;
	}
	public String getOrgId() {
		return orgId;
	}
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	public String getOrgType() {
		return orgType;
	}
	public void setOrgType(String orgType) {
		this.orgType = orgType;
	}
	public long getArticleId() {
		return articleId;
	}
	public void setArticleId(long articleId) {
		this.articleId = articleId;
	}
	public byte getFocalfirm() {
		return focalfirm;
	}
	public void setFocalfirm(byte focalfirm) {
		this.focalfirm = focalfirm;
	}
	/*@Override
	public int compareTo(DeliverableSetEntry o) {
		String orgId = o.getOrgId(); 
		 
		//ascending order
		return ;
	}*/
	
}
class SearchMapKey
{
	int pubYear;
    long articleId;
    public int getPubYear() {
		return pubYear;
	}
	public void setPubYear(int pubYear) {
		this.pubYear = pubYear;
	}
	public long getArticleId() {
		return articleId;
	}
	public void setArticleId(long articleId) {
		this.articleId = articleId;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (articleId ^ (articleId >>> 32));
		result = prime * result + pubYear;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchMapKey other = (SearchMapKey) obj;
		if (articleId != other.articleId)
			return false;
		if (pubYear != other.pubYear)
			return false;
		return true;
	}
}
class SearchMapValue
{
    String beaCode;
    String orgId;
    byte focalfirm;
	public String getBeaCode() {
		return beaCode;
	}
	public void setBeaCode(String beaCode) {
		this.beaCode = beaCode;
	}
	public String getOrgId() {
		return orgId;
	}
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	public byte getFocalfirm() {
		return focalfirm;
	}
	public void setFocalfirm(byte focalfirm) {
		this.focalfirm = focalfirm;
	}
}
/*class LeadFirmMO
{
	String beaCode;
	String orgId;
	public String getBeaCode() {
		return beaCode;
	}
	public void setBeaCode(String beaCode) {
		this.beaCode = beaCode;
	}
	public String getOrgId() {
		return orgId;
	}
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
}*/
public class ExcelReader 
{
		String filename = "C:\\Users\\tarun\\Desktop\\GA Work\\focal firms pubs for collab counts.xlsx";
		public List<DeliverableSetEntry> processPatentInventorySheet() throws Exception {
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
			List<DeliverableSetEntry> patentInventoryList = sheetHandler.getPatentInventories();
			System.out.println("Number of rows in PatentID sheet : "+patentInventoryList.size());
			sheet1.close();
			return patentInventoryList;
		}
	/*	public List<LeadFirmMO> processLeadFirmSheet() throws Exception{
			OPCPackage pkg = OPCPackage.open(filename);
			XSSFReader r = new XSSFReader( pkg );
			SharedStringsTable sst = r.getSharedStringsTable();

			XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
			LeadFirmSheetHandler leadFirmHandler = new LeadFirmSheetHandler(sst);
			ContentHandler handler = sheetHandler;
			parser.setContentHandler(handler);

			InputStream sheet4 = r.getSheet("rId4");
			InputSource sheetSource = new InputSource(sheet4);
			parser.parse(sheetSource);
			List<LeadFirmMO> leadFirmList = sheetHandler.getPatentInventories();
			System.out.println("Number of rows in Lead Firm sheet : "+leadFirmList.size());
			sheet4.close();
			return leadFirmList;
		}*/
}	
class PatentInventorySheetHandler extends DefaultHandler {
	private SharedStringsTable sst;
	private String lastContents;
	private boolean nextIsString;
	DeliverableSetEntry patent = new DeliverableSetEntry();
	List<DeliverableSetEntry> patentInventoryList = new ArrayList<DeliverableSetEntry>();
	private int column=0;  
	private int row=0; 
     
	PatentInventorySheetHandler(SharedStringsTable sst) {
		this.sst = sst;
	}
	
	 public List<DeliverableSetEntry> getPatentInventories() {  
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
	       boolean isArticleIdEmpty = false;
	       boolean isYearEmpty = false;
	       boolean isFocalFirmEmpty = false;
	       if(name.equals("c") && row>0) {  
	         switch (column)  
	         {  
	           case 0:
	        	   patent.setBeaCode(lastContents);  
	             break;  
	           case 1:
	        	   if(lastContents.trim().equals("")){
	        		   isYearEmpty = true;
	        		   break;
	        	   }
	        	   patent.setPubYear(Integer.parseInt(lastContents));  
	             break;  
	           case 2:
	        	   patent.setOrgId(lastContents);  
	             break;  
	           case 3:
	        	   patent.setOrgType(lastContents);  
	             break; 
	           case 4:
	        	   if(lastContents.trim().equals("")){
	        		   isArticleIdEmpty = true;
	        		   break;
	        	   }
	        	   patent.setArticleId(Integer.parseInt(lastContents));  
	             break; 
	           case 5:
	        	   if(lastContents.trim().equals("")){
	        		   isFocalFirmEmpty = true;
	        		   break;
	        	   }
	        	   patent.setFocalfirm(Byte.parseByte(lastContents));  
	             break; 
	         }
	         column++;
	       }
	     //If it is the end of a row, save the current Parent Inventory object. And create a new one  
	       if(name.equals("row") && !isArticleIdEmpty && !isYearEmpty && !isFocalFirmEmpty) {
	         if(row>0 && patent.getPubYear()!= 0 && patent.getArticleId() != 0)  {
	        	 patentInventoryList.add(patent);
	         }
	         patent=new DeliverableSetEntry();  
	         row++;  
	         column=0;  
	       }  
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		
		lastContents += new String(ch, start, length);
	}
}

