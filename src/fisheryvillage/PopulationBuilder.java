package fisheryvillage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fisheryvillage.common.HumanUtils;
import fisheryvillage.common.Logger;
import fisheryvillage.common.SimUtils;
import fisheryvillage.ecosystem.Ecosystem;
import fisheryvillage.population.Human;
import fisheryvillage.population.Resident;
import fisheryvillage.population.Status;
import fisheryvillage.property.Boat;
import fisheryvillage.property.BoatType;
import fisheryvillage.property.municipality.Council;
import fisheryvillage.property.municipality.ElderlyCare;
import fisheryvillage.property.municipality.Factory;
import fisheryvillage.property.municipality.School;
import fisheryvillage.property.municipality.SocialCare;

public class PopulationBuilder {

	public List<String> readFile(String filePathAndName) {
		BufferedReader reader;
		List<String> data = new ArrayList<String>();
		try {
			reader = new BufferedReader(new FileReader(filePathAndName));
			String line = reader.readLine();
			while (line != null) {
				data.add(line);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	public void generatePopulation(String filePath, String fileName) {
		
		List<String> dataAll = readFile(filePath + "/" + fileName + ".txt");
		List<String> dataHumans = new ArrayList<String>();
		List<String> dataRelations = new ArrayList<String>();
		List<String> dataChildren = new ArrayList<String>();
		List<String> dataOwners = new ArrayList<String>();
		List<String> dataWaterTank = new ArrayList<String>();
		List<String> dataSocialStatus = new ArrayList<String>();
		List<String> dataInstitutes = new ArrayList<String>();
		int typeOfData = -1;
		for (String datum : dataAll) {
			if (!datum.startsWith("%")) {
				switch(typeOfData) {
				case 0:	dataHumans.add(datum); break;
				case 1: dataRelations.add(datum); break;
				case 2:	dataChildren.add(datum); break;
				case 3:	dataOwners.add(datum); break;
				case 4:	dataWaterTank.add(datum); break;
				case 5:	dataSocialStatus.add(datum); break;
				case 6:	dataInstitutes.add(datum); break;
				default: //Nothing
				}
			}
			else {
				typeOfData = Integer.parseInt(datum.substring(1,2));
			}
		}
		
		generateResidents(dataHumans);
		generateResidentsArrays(dataRelations, dataChildren, dataOwners, dataWaterTank, dataSocialStatus);
		generateInstitutes(dataInstitutes);
	}
	
	public void generateResidents(List<String> dataHumans) {
		
		int maxHumanId = -1;
		for (String humanString : dataHumans) {
			List<String> hVars = Arrays.asList(humanString.split(","));
			int id = Integer.parseInt(hVars.get(0));
			boolean gender = Boolean.parseBoolean(hVars.get(1));
			boolean foreigner = Boolean.parseBoolean(hVars.get(2));
			boolean hasBeenFisher = Boolean.parseBoolean(hVars.get(3));
			int age = Integer.parseInt(hVars.get(4));
			double money = Double.parseDouble(hVars.get(5));
			int childrenWanted = Integer.parseInt(hVars.get(6));
			double nettoIncome = Double.parseDouble(hVars.get(7));
			double necessaryCost = Double.parseDouble(hVars.get(8));
			String jobTitle = hVars.get(9);
			Status status = Status.valueOf(hVars.get(10));
			int workplaceId = Integer.parseInt(hVars.get(11));
			int notHappyTick = Integer.parseInt(hVars.get(12));
			Resident resident = new Resident(id, gender, foreigner, hasBeenFisher, age, money, childrenWanted,
									nettoIncome, necessaryCost, jobTitle, status, workplaceId, notHappyTick);
			Logger.logInfo("Initialized H" + resident.getId() + ", age: " + resident.getAge());
			if (maxHumanId < id)
				maxHumanId = id;
		}
		HumanUtils.setHumanId(maxHumanId + 1);
	}
	
	public void generateResidentsArrays(List<String> dataRelations, List<String> dataChildren, List<String> dataOwners, List<String> dataWaterTank, List<String> dataSocialStatus) {
		
		for (String relationString : dataRelations) {
			List<String> rVars = Arrays.asList(relationString.split(","));
			HumanUtils.getResidentById(Integer.parseInt(rVars.get(0))).setPartner(HumanUtils.getResidentById(Integer.parseInt(rVars.get(1))));
		}
		
		for (String childrenString : dataChildren) {
			List<String> cVars = Arrays.asList(childrenString.split(","));
			Human parent = HumanUtils.getHumanById(Integer.parseInt(cVars.get(0)));
			for (int i = 1; i < cVars.size(); i ++) {
				int childId = Integer.parseInt(cVars.get(i));
				parent.addChild(childId);
				HumanUtils.getHumanById(childId).addParent(parent.getId());
			}
		}
		
		for (String propertyString : dataOwners) {
			List<String> pVars = Arrays.asList(propertyString.split(","));
			Human owner = HumanUtils.getHumanById(Integer.parseInt(pVars.get(0)));
			for (int i = 1; i < pVars.size(); i ++) {
				owner.connectProperty(Integer.parseInt(pVars.get(i)));
			}
		}
		/*
		for (String waterTankString : dataWaterTank) {
			List<String> wVars = Arrays.asList(waterTankString.split(","));
			Resident resident = HumanUtils.getResidentById(Integer.parseInt(wVars.get(0)));
			resident.setImportantWaterTankFromData(wVars);
		}
		*/
		for (String socialStatusString : dataSocialStatus) {
			List<String> sVars = Arrays.asList(socialStatusString.split(","));
			Resident resident = HumanUtils.getResidentById(Integer.parseInt(sVars.get(0)));
			resident.setSocialStatusFromData(sVars);
		}
	}

	public void generateInstitutes(List<String> dataInstitutes) {
		
		//"%propertyType,id,savings,ownerId,extra-variables"
		for (String propertyVarsString : dataInstitutes) {
			List<String> pVars = Arrays.asList(propertyVarsString.split(","));
			switch (pVars.get(0)) {
			case "Council":
				SimUtils.getCouncil().setSavings(Double.parseDouble(pVars.get(2)));
				break;
			case "ElderlyCare":
				SimUtils.getElderlyCare().setSavings(Double.parseDouble(pVars.get(2)));
				break;
			case "Factory":
				SimUtils.getFactory().setSavings(Double.parseDouble(pVars.get(2)));
				SimUtils.getFactory().setVariables(Integer.parseInt(pVars.get(4)), Integer.parseInt(pVars.get(5)));
				break;
			case "School":
				SimUtils.getSchool().setSavings(Double.parseDouble(pVars.get(1)));
				break;
			case "SocialCare":
				SimUtils.getSocialCare().setSavings(Double.parseDouble(pVars.get(1)));
				break;
			case "Boat":
				Boat boat = (Boat) SimUtils.getPropertyById(Integer.parseInt(pVars.get(1)));
				boat.setSavings(Double.parseDouble(pVars.get(2)));
				boat.setBoatType(BoatType.getEnumByString(pVars.get(4)));
				break;
			case "Ecosystem":
				SimUtils.getEcosystem().setParameters(Double.parseDouble(pVars.get(1)));
				break;
			}
		}
	}

	public void savePopulation(String filePath, String fileName) {
		// Data humans
		List<String> dataHumans = new ArrayList<String>();
		dataHumans.add("%0:id,gender,foreigner,hasBeenFisher,age,money,childrenWanted,homelessTick,nettoIncome,necessaryCost,jobTitle,status,workplaceId");
		ArrayList<Resident> residents = SimUtils.getObjectsAll(Resident.class);
		for (Resident resident : residents) {
			dataHumans.add(resident.getHumanVarsAsString());
		}
		
		// Data network
		List<String> dataRelations = getDataRelations();
		List<String> dataChildren = getDataChildren();
		List<String> dataProperty = getDataProperty();
		List<String> dataWaterTank = getDataWaterTank();
		List<String> dataSocialStatus = getDataSocialStatus();
		
		List<String> dataInstitutes = new ArrayList<String>();
		dataInstitutes.add("%6:propertyType,id,savings,ownerId,extra-variables");
		Council council = SimUtils.getCouncil();
		dataInstitutes.add("Council," + council.getId() + "," + council.getSavings() + "," + council.getOwnerId());
		ElderlyCare elderlyCare = SimUtils.getElderlyCare();
		dataInstitutes.add("ElderlyCare," + elderlyCare.getId() + "," + elderlyCare.getSavings() + "," + elderlyCare.getOwnerId());
		Factory factory = SimUtils.getFactory();
		dataInstitutes.add("Factory," + factory.getId() + "," + factory.getSavings() + "," + elderlyCare.getOwnerId() + "," + factory.getMaxEmployees() + "," + factory.getFishUnprocessed());
		School school = SimUtils.getSchool();
		dataInstitutes.add("School," + school.getId() + "," + school.getSavings() + "," + school.getOwnerId());
		SocialCare socialCare = SimUtils.getSocialCare();
		dataInstitutes.add("SocialCare," + socialCare.getId() + "," + socialCare.getSavings() + "," + socialCare.getOwnerId());
		for (Boat boat : SimUtils.getObjectsAll(Boat.class)) {
			dataInstitutes.add("Boat," + boat.getId() + "," + boat.getSavings() + "," + boat.getOwnerId() + "," + boat.getBoatType().name());
		}
		Ecosystem ecosystem = SimUtils.getEcosystem();
		dataInstitutes.add("Ecosystem," + ecosystem.getParametersString());
		
		List<String> data = new ArrayList<String>();
		data.addAll(dataHumans);
		data.addAll(dataRelations);
		data.addAll(dataChildren);
		data.addAll(dataProperty);
		data.addAll(dataWaterTank);
		data.addAll(dataSocialStatus);
		data.addAll(dataInstitutes);
		
		writeToFile(filePath + "/" + fileName + ".txt", data);
	}

	public List<String> getDataRelations() {
		
		List<Integer> humansContained = new ArrayList<Integer>();
		List<String> data = new ArrayList<String>();
		data.add("%1:human1,human2");
		for (Human human : SimUtils.getObjectsAll(Human.class)) {
			if (!humansContained.contains(human.getId()) && human.getPartnerId() >= 0) {
				data.add(human.getId() + "," + human.getPartnerId());
			}
		}
		return data;
	}

	public List<String> getDataChildren() {
		
		List<String> data = new ArrayList<String>();
		data.add("%2:parent,child1,child2,child3,etc.");
		for (Human human : SimUtils.getObjectsAll(Human.class)) {
			ArrayList<Integer> childrenIds = human.getChildrenIds();
			if (childrenIds.size() >= 1) {
				String datum = Integer.toString(human.getId());
				for (int childId : childrenIds) {
					datum += "," + Integer.toString(childId);
				}
				data.add(datum);
			}
		}
		return data;
	}
	
	public List<String> getDataProperty() {
		
		List<String> data = new ArrayList<String>();
		data.add("%3:owner,property1,property2,property3,property4,etc.");
		for (Human human : SimUtils.getObjectsAll(Human.class)) {
			ArrayList<Integer> propertyIds = human.getPropertyIds();
			if (propertyIds.size() >= 1) {
				String datum = Integer.toString(human.getId());
				for (int propertyId : propertyIds) {
					datum += "," + Integer.toString(propertyId);
				}
				data.add(datum);
			}
		}
		return data;
	}

	public List<String> getDataWaterTank() {
		
		List<String> data = new ArrayList<String>();
		data.add("%4:value1,level1,threshold1,value2,level2,threshold2,value etc.");
		for (Resident resident : SimUtils.getObjectsAll(Resident.class)) {
			data.add(resident.getId() + "," + resident.importantWaterTankData());
		}
		return data;
	}
	
	public List<String> getDataSocialStatus() {
		
		List<String> data = new ArrayList<String>();
		data.add("%5:job,house,ecology,economy,etc.");
		for (Resident resident : SimUtils.getObjectsAll(Resident.class)) {
			data.add(resident.getId() + "," + resident.socialStatusString());
		}
		return data;
	}

	public void writeToFile(String filePathAndName, List<String> data) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(filePathAndName, "UTF-8");
			for (String datum : data) {
				writer.println(datum);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
