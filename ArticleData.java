package article;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class ArticleData 
{
	public static void main(String args[]) throws IOException {
		try{
		ExcelReader excelReader = new ExcelReader();	
		List<DeliverableSetEntry> articlesList = excelReader.processPatentInventorySheet();
		Map<SearchMapKey,List<SearchMapValue>> searchMap = new HashMap<SearchMapKey,List<SearchMapValue>>();
		//Set<DeliverableSetEntry> deliverableSet = new TreeSet<DeliverableSetEntry>();
		List<DeliverableSetEntry> deliverableSet = new ArrayList<DeliverableSetEntry>();
		
		//reading the lead firm sheet
		InputStream ExcelFileToRead = new FileInputStream("C:/Users/tarun/Desktop/GA Work/focal firms pubs for collab counts.xlsx");
		XSSFWorkbook wb = new XSSFWorkbook(ExcelFileToRead);
		XSSFSheet leadFirmsSheet = wb.getSheetAt(3);
		XSSFRow row;
		Iterator leadFirmsSheetRows = leadFirmsSheet.rowIterator();
		Map<Double, List<String>> leadFirmMap = new HashMap<Double, List<String>>();
		Double beaCode = null;
		String leadFirmId = null;
		List<String> leadFirmsList= null;
		// For ignoring the header record
				if (leadFirmsSheetRows.hasNext()) {
					row = (XSSFRow) leadFirmsSheetRows.next();
				}
				//Reading first sheet
				while (leadFirmsSheetRows.hasNext()) {
					row = (XSSFRow) leadFirmsSheetRows.next();
					if (row.getCell(0) == null && row.getCell(1) == null)
						break;
					beaCode = (Double) row.getCell(0).getNumericCellValue();
					leadFirmId = (String) row.getCell(1).getStringCellValue();
					if (leadFirmMap.get(beaCode) != null) {
						leadFirmsList = leadFirmMap.get(beaCode);
					} else {
						leadFirmsList = new ArrayList<String>();
					}
					leadFirmsList.add(leadFirmId);
					leadFirmMap.put(beaCode, leadFirmsList);
			}
		
		for(DeliverableSetEntry dset : articlesList)
		{
			SearchMapKey key= new SearchMapKey();
			SearchMapValue value= new SearchMapValue();
			key.setArticleId(dset.getArticleId());
			key.setPubYear(dset.getPubYear());
			value.setBeaCode(dset.getBeaCode());
			value.setFocalfirm(dset.getFocalfirm());
			value.setOrgId(dset.getOrgId());
			//searchMap.put(key, value);
			if(searchMap.get(key)!=null)
			{
				List<SearchMapValue> searchMapValueList = searchMap.get(key);
				searchMapValueList.add(value);
				searchMap.put(key, searchMapValueList);
			}
			else
			{
				List<SearchMapValue> searchMapValueList = new ArrayList<SearchMapValue>();
				searchMapValueList.add(value);
				searchMap.put(key, searchMapValueList);
			}
/*			for(SearchMapValue listentry: searchMap.get(key))
			System.out.println(key.getPubYear()+"   "+key.getArticleId()+"   "+listentry.getOrgId()+"   "+listentry.getBeaCode()+"   "+listentry.getFocalfirm());
			System.out.println(" ");*/
			if(dset.getFocalfirm()==1)
				deliverableSet.add(dset);
		}
		SearchMapKey srchKey = new SearchMapKey();
		for(DeliverableSetEntry setEntry: deliverableSet)
		{
			int localLeadCount=0,distantLeadCount=0;
			//int localFirmCount=0,localProCount=0,distantFirmCount=0,distantProCount=0;
			//int firmCount=0,prosCount=0;
			Set<Long> articleIdSet = new HashSet<Long>();
			for(DeliverableSetEntry setEntry2: deliverableSet)
			{
				if(setEntry2.getPubYear()==setEntry.getPubYear() && setEntry2.getOrgId().equals(setEntry.getOrgId()))
				{
					articleIdSet.add(setEntry2.getArticleId());
				}
			}
			List<SearchMapValue> srchValList = new ArrayList<SearchMapValue>();
			srchKey.setPubYear(setEntry.getPubYear());
			for(Long articleId: articleIdSet)
			{
			srchKey.setArticleId(articleId);
			srchValList.addAll(searchMap.get(srchKey));// form key with setEntry.pubYear,setEntry.articleId
			}
			//System.out.println(srchValList);
			if(srchValList!=null)
			{
			for(SearchMapValue srchVal: srchValList)
			{
				
			//logic to find the local and distant lead firm counts
				if(setEntry.beaCode.equals(srchVal.beaCode))
				{
					if(srchVal.focalfirm==1 && !srchVal.orgId.equals(setEntry.orgId))
					{
						if(leadFirmMap.get(Double.parseDouble(setEntry.getBeaCode()))!=null && leadFirmMap.get(Double.parseDouble(setEntry.getBeaCode())).contains(setEntry.getOrgId()))
						{
							localLeadCount++;
						}
					}
				}
				else
				{
				    if(srchVal.focalfirm==1 && !srchVal.orgId.equals(setEntry.orgId))
				    {
				    if(leadFirmMap.get(Double.parseDouble(setEntry.getBeaCode()))!=null && leadFirmMap.get(Double.parseDouble(setEntry.getBeaCode())).contains(setEntry.orgId))
					{
				        distantLeadCount++;
				    }
				    }
				}
				
				//logic to find the Firm and Pro counts
				/*if(srchVal.focalfirm==1 && !srchVal.orgId.equals(setEntry.orgId))
			    {
					firmCount++;
			    }else if(srchVal.focalfirm==0){
			    	prosCount++;
			    }*/
			
				//System.out.println(srchVal.getOrgId()+" "+srchVal.getBeaCode()+ "  "+ srchVal.getFocalfirm());
		
			//logic to find the Local and distant Firm and Pro counts.	
			/*if(setEntry.beaCode.equals(srchVal.beaCode))
			{
			//	System.out.println("####"+setEntry.beaCode);
			    if(srchVal.focalfirm==1 && !srchVal.orgId.equals(setEntry.orgId))
			    {
			        localFirmCount++;
			    }else if(srchVal.focalfirm==0){
			        localProCount++;
			    }
			}
			else
			{
			    if(srchVal.focalfirm==1 && !srchVal.orgId.equals(setEntry.orgId))
			    {
			        distantFirmCount++;
			    }else if(srchVal.focalfirm==0){
			        distantProCount++;
			    }
			}*/
			
			}
			}
			System.out.println(setEntry.getBeaCode()+ "#" +setEntry.getPubYear()+ "#" + setEntry.getOrgId()+ "#" + localLeadCount + "#"+ distantLeadCount );
			//System.out.println(setEntry.getBeaCode()+ "#" +setEntry.getPubYear()+ "#" + setEntry.getOrgId()+ "#" + localFirmCount + "#"+ localProCount  + "#"+ distantFirmCount + "#"+ distantProCount);
			//System.out.println(setEntry.getPubYear()+"#"+setEntry.getOrgId()+ "#" +firmCount + "#"+prosCount);
			//print values to excel sheet as a row
			}//end of loop
		}catch(Exception e){
			e.printStackTrace();
		}
	}	
}