package excel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AngDistance {
	
	//it will print -1 if the Inventors for the patentID is only 1, 
	//It will print zero if the Inventors are more than two but after cutoff year calculation there was only one inventor remaining.
	public static void main(String args[]) throws IOException {
		try{
		CustomExcelReader excelReader = new CustomExcelReader();	
		// Storing patentIds and related inventor ids as a list
		Map<Double, List<String>> patentIdsMap = new HashMap<Double, List<String>>();
		// Storing inventorId and the cutoff year
		//Map inventorYearsMap = new HashMap();
		Map<Double, Double> patentYearMap = new HashMap<Double, Double>();
	//	Map inventorYearFalgMap = new HashMap(); // used to track the patents for only current year
		double patentId = 0;
		String inventoryId = null;
		double inventorSubcatYear = 0;
		double patentYear = 0;
		List<String> invIds = null;
		
		//Reading first sheet
		List<PatentInventoryMO> patentInventoryList = excelReader.processPatentInventorySheet();
		for(PatentInventoryMO patentInvMO: patentInventoryList){
			patentId = patentInvMO.getPatentId();
			inventoryId = patentInvMO.getInventoryId();
			//inventoryYear = patentInvMO.getYear();
			patentYear = patentInvMO.getYear();
			patentYearMap.put(patentId, patentYear);
			if (patentIdsMap.get(patentId) != null) {
				invIds = patentIdsMap.get(patentId);
			} else {
				invIds = new ArrayList<String>();
			}
			invIds.add(inventoryId);
			patentIdsMap.put(patentId, invIds);
			//inventorYearsMap.put(patentId, inventoryYear);
			
		}
		//System.out.println("patentIDMap ----- "+patentIdsMap);
		// Storing inventorId and all subcats+year associated to it //within the cutoff year
		Map<String, List<String>> invSubcatsMap = new HashMap<String, List<String>>();
		double subcat = 0;
		//double maxInvYear = 0;
		List<String> subcats = null;
		
		//Reading second sheet
		List<InventorySubcatMO> inventorySubcatList = excelReader.processInventorySubcatSheet();
		for(InventorySubcatMO invSubcatMO: inventorySubcatList){
			inventoryId = invSubcatMO.getInventoryId();
			subcat = invSubcatMO.getSubcat();
			inventorSubcatYear = invSubcatMO.getYear();
			
			/*if (inventorYearsMap.get(inventoryId) != null)
				maxInvYear = (Double) inventorYearsMap.get(inventoryId);
			if (inventoryYear >= maxInvYear)
				continue; */
			
			if (invSubcatsMap.get(inventoryId) != null) {
				subcats = invSubcatsMap.get(inventoryId);
			} else {
				subcats = new ArrayList<String>();
			}
			subcats.add(subcat+"-"+inventorSubcatYear);
			invSubcatsMap.put(inventoryId, subcats);
		}
		//System.out.println("invSubcatsMap ----- "+invSubcatsMap);
		//Iterating second sheet again to add the subcats on current year for the inventors who dont have prior year subcats
		/*List<InventorySubcatMO> inventorySubcatList2 = excelReader.processInventorySubcatSheet();
			for(InventorySubcatMO invSubcatMO1: inventorySubcatList2){
			inventoryId = invSubcatMO1.getInventoryId();
			subcat = invSubcatMO1.getSubcat();
			inventoryYear = invSubcatMO1.getYear();
			if (inventorYearsMap.get(inventoryId) != null)
				maxInvYear = (Double) inventorYearsMap.get(inventoryId);
			if((inventoryYear == maxInvYear)&&(inventorYearFalgMap.get(inventoryId)!=null))
			{
				subcats = invSubcatsMap.get(inventoryId);
				subcats.add(subcat);
				invSubcatsMap.put(inventoryId, subcats);
			}
			else if ((inventoryYear == maxInvYear)&&(invSubcatsMap.get(inventoryId) == null))
			{
				subcats = new ArrayList<Double>();
				inventorYearFalgMap.put(inventoryId, 1);
				subcats.add(subcat);
				invSubcatsMap.put(inventoryId, subcats);
			}
		}*/
		
		// Iterating all the patentIds and getting the average angular distance
		for (Map.Entry<Double, List<String>> entry : patentIdsMap.entrySet()) {
			Double patId = entry.getKey();
			List<String> invIdsForPatId = entry.getValue(); 
			Map<String, List<Double>> invSubcatWithinCutoffYearMap = new HashMap<String, List<Double>>();
			for(String inventor: invIdsForPatId)// call a method with year as the argument which will return the list of all the required subcats for that inventor 
			{	
				List<Double> SubcatsInCutoffYear= getInventorSubcatsInCutOffYear(inventor, (Double)patentYearMap.get(patId), invSubcatsMap);
				//if(SubcatsInCutoffYear!=null)//null pointer exception here which needs to be checked
				//{
				invSubcatWithinCutoffYearMap.put(inventor, SubcatsInCutoffYear);
				//}
			}
			/*System.out.println("Average Angular Distance in radians for Patent ID "
					+ patId
					+ " : "
					+ (getAverageAngularDistance(patId, invIdsForPatId,
							invSubcatWithinCutoffYearMap)));*/
			double AvgAngDist = getAverageAngularDistance(patId, invIdsForPatId, invSubcatWithinCutoffYearMap);
			if(AvgAngDist==-1)
				System.out.println(patId + " : N/A");
			else
				System.out.println(patId + " : " + AvgAngDist);
		}
	}catch(Exception e){
		e.printStackTrace();
	}
}
	private static  List<Double> getInventorSubcatsInCutOffYear(String inventor, Double patYear, Map<String, List<String>> invSubcatsMap)
	{
		//Map<String, List<Double>> invSubcatWithinCutoffYearMap = new HashMap<String, List<Double>>();
		List<String> invSubcatAndYear = invSubcatsMap.get(inventor);
		//System.out.println("invSubcatAndYear----------- "+invSubcatAndYear);
		List<Double> invSubcatWithinCutoffYearList = null;
		if(invSubcatAndYear!=null)
		{
		for(String subcatAndYear : invSubcatAndYear)
		{
			//System.out.println(subcatAndYear.substring(subcatAndYear.length()-6, subcatAndYear.length()));
			if(Double.parseDouble(subcatAndYear.substring(subcatAndYear.length()-6, subcatAndYear.length())) >= patYear)
				continue;
			if (invSubcatWithinCutoffYearList != null) {
				invSubcatWithinCutoffYearList.add(Double.parseDouble(subcatAndYear.substring(0, 2)));
			} else {
				invSubcatWithinCutoffYearList = new ArrayList<Double>();
				invSubcatWithinCutoffYearList.add(Double.parseDouble(subcatAndYear.substring(0, 2)));
			}
		}
		//System.out.println("invSubcatWithinCutoffYearList------ "+invSubcatWithinCutoffYearList);
		//invSubcatWithinCutoffYearMap.put(inventor, invSubcatWithinCutoffYearList);
		
		if(invSubcatWithinCutoffYearList==null) // re-iterating the loop to check for the subcats in the current year
		{
			for(String subcatAndYear : invSubcatAndYear)
			{
				if(Double.parseDouble(subcatAndYear.substring(subcatAndYear.length()-6, subcatAndYear.length())) == patYear)
				{
					if (invSubcatWithinCutoffYearList != null) {
						invSubcatWithinCutoffYearList.add(Double.parseDouble(subcatAndYear.substring(0, 2)));
					} else {
					invSubcatWithinCutoffYearList = new ArrayList<Double>();
					invSubcatWithinCutoffYearList.add(Double.parseDouble(subcatAndYear.substring(0, 2)));
					}
				}
			}
			//invSubcatWithinCutoffYearMap.put(inventor, invSubcatWithinCutoffYearList);
		}
		}
		return(invSubcatWithinCutoffYearList);
	}
	private static double getAverageAngularDistance(Double patId,
			List<String> invIdsForPatId, Map<String, List<Double>> invSubcatsMap) {
		int numberOfInvIds = invIdsForPatId.size();
		double sumOfAngles = 0;
		double count = 0;
		// return -1 if there is only one vector for the patent ID
		if(numberOfInvIds==1)
		{
			return -1;
		}
		// Iteration for n inventor ids and finding the distances for nC2 combinations
		for (int i = 0; i < numberOfInvIds; i++) {
			for (int j = i + 1; j < numberOfInvIds; j++) {
				
				double angle = getThetaAngularDistance(invIdsForPatId.get(i),
						invIdsForPatId.get(j), invSubcatsMap);
				if(angle == -1) 
				{
					count++;
				}
				else 
				{
					sumOfAngles += angle;
				}
			}
		}
		// Average of all the angles
		double numberOfCombinations = (numberOfInvIds * (numberOfInvIds - 1)) / 2;
		if(count == numberOfCombinations)
		{
			return -1;
		}
		double averageOfAngles = sumOfAngles / numberOfCombinations;
		return averageOfAngles;
	}

	private static double getThetaAngularDistance(String invId1, String invId2,
			Map<String, List<Double>> invSubcatsMap) {

		// Hashmap for storing the dimensions and its number of times it occurred
		List<Double> subCatsForInvId1 = invSubcatsMap.get(invId1);
		List<Double> subCatsForInvId2 = invSubcatsMap.get(invId2);
		Map<Double, Integer> invSubcatsMap1 = new HashMap<Double, Integer>();
		Map<Double, Integer> invSubcatsMap2 = new HashMap<Double, Integer>();
		if (subCatsForInvId1 != null && subCatsForInvId2 != null && 
				subCatsForInvId1.size() > 0 && subCatsForInvId2.size() > 0) {
			// Iterating all subcats and storing the dimensions for first vector
			for (Iterator<Double> iter = subCatsForInvId1.iterator(); iter.hasNext();) 
			{
				Double subCat = iter.next();
				Integer noOfValuesForSubcat = invSubcatsMap1.get(subCat);
				if (noOfValuesForSubcat != null) {
					invSubcatsMap1.put(subCat, noOfValuesForSubcat + 1);
				} else {
					invSubcatsMap1.put(subCat, 1);
				}
			}

			// Iterating all subcats and storing the dimensions for second vector
			for (Iterator<Double> iter = subCatsForInvId2.iterator(); iter
					.hasNext();) {
				Double subCat = iter.next();
				Integer noOfValuesForSubcat = invSubcatsMap2.get(subCat);
				if (noOfValuesForSubcat != null) {
					invSubcatsMap2.put(subCat, noOfValuesForSubcat + 1);
				} else {
					invSubcatsMap2.put(subCat, 1);
				}
			}
			
			double AngularDistance = 0;
			// Dot product of two vectors
			if (invSubcatsMap1.size() <= invSubcatsMap2.size()) {
				for (Map.Entry<Double, Integer> entry : invSubcatsMap1
						.entrySet()) {
					Double key1 = entry.getKey();
					Integer value1 = entry.getValue();
					if (invSubcatsMap2.get(key1) != null) {
						AngularDistance += value1 * invSubcatsMap2.get(key1);
					}
				}
			} else {
				for (Map.Entry<Double, Integer> entry : invSubcatsMap2
						.entrySet()) {
					Double key1 = entry.getKey();
					Integer value1 = entry.getValue();
					if (invSubcatsMap1.get(key1) != null) {
						AngularDistance += value1 * invSubcatsMap1.get(key1);
					}
				}
			}

			double sqrtOfValues1 = 0;
			for (Map.Entry<Double, Integer> entry : invSubcatsMap1.entrySet()) {
				Integer value1 = entry.getValue();
				sqrtOfValues1 += value1 * value1;
			}
			sqrtOfValues1 = Math.sqrt(sqrtOfValues1);

			double sqrtOfValues2 = 0;
			for (Map.Entry<Double, Integer> entry : invSubcatsMap2.entrySet()) {
				Integer value2 = entry.getValue();
				sqrtOfValues2 += value2 * value2;
			}
			sqrtOfValues2 = Math.sqrt(sqrtOfValues2);
			//System.out.println("values**** "+AngularDistance / (sqrtOfValues1 * sqrtOfValues2));
			double Theta = Math.acos(AngularDistance
					/ (sqrtOfValues1 * sqrtOfValues2));
			return Theta;
		}
		return -1;
	}
}