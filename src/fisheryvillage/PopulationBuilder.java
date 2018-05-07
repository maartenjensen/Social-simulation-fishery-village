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
import fisheryvillage.population.Human;
import fisheryvillage.population.Resident;
import fisheryvillage.population.Status;
import fisheryvillage.property.municipality.Council;
import fisheryvillage.property.municipality.ElderlyCare;
import fisheryvillage.property.municipality.Factory;
import fisheryvillage.property.municipality.School;
import fisheryvillage.property.municipality.SocialCare;

public class PopulationBuilder {

	public List<String> readFile(String filePathAndName) {
		BufferedReader reader;
		List<String> humans = new ArrayList<String>();
		try {
			reader = new BufferedReader(new FileReader(filePathAndName));
			String line = reader.readLine();
			while (line != null) {
				if(!line.startsWith("%")){//means that it is not comment
					humans.add(line);
				}	
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return humans;
	}
		
	public void generatePopulation(String filePath, String fileName) {
		
		List<String> humansList = readFile(filePath + "/" + fileName + "Data.txt");
		for (String humanString : humansList) {
			List<String> hVars = Arrays.asList(humanString.split(","));
			int id = Integer.parseInt(hVars.get(0));
			int age = Integer.parseInt(hVars.get(1));
			boolean isMan = Boolean.parseBoolean(hVars.get(2));
			boolean higherEducated = Boolean.parseBoolean(hVars.get(3));
			double money = Double.parseDouble(hVars.get(4));
			int childrenWanted = Integer.parseInt(hVars.get(5));
			boolean foreigner = Boolean.parseBoolean(hVars.get(6));
			int homelessTick = Integer.parseInt(hVars.get(7));
			double nettoIncome = Double.parseDouble(hVars.get(8));
			double necessaryCost = Double.parseDouble(hVars.get(0));
			String jobTitle = hVars.get(10);
			Status status = Status.valueOf(hVars.get(11));
			int boatId = Integer.parseInt(hVars.get(12));
			Resident resident = new Resident(id, isMan, foreigner, higherEducated, age, money, childrenWanted, homelessTick,
									nettoIncome, necessaryCost, jobTitle, status, boatId);
			Logger.logInfo("Initialized H" + resident.getId() + ", age: " + resident.getAge());
		}
		
		List<String> relationsList = readFile(filePath + "/" + fileName + "Relations.txt");
		for (String relationString : relationsList) {
			List<String> rVars = Arrays.asList(relationString.split(","));
			HumanUtils.getResidentById(Integer.parseInt(rVars.get(0))).actionSetPartner(HumanUtils.getResidentById(Integer.parseInt(rVars.get(1))));
		}
		
		List<String> childrenList = readFile(filePath + "/" + fileName + "Children.txt");
		for (String childrenString : childrenList) {
			List<String> cVars = Arrays.asList(childrenString.split(","));
			HumanUtils.getHumanById(Integer.parseInt(cVars.get(0))).addChild(Integer.parseInt(cVars.get(1)));
		}
		
		List<String> propertyList = readFile(filePath + "/" + fileName + "Property.txt");
		for (String propertyString : propertyList) {
			List<String> pVars = Arrays.asList(propertyString.split(","));
			HumanUtils.getHumanById(Integer.parseInt(pVars.get(0))).connectProperty(Integer.parseInt(pVars.get(1)));
		}
		
		List<String> propertyVarsList = readFile(filePath + "/" + fileName + "PropertyVars.txt");
		for (String propertyVarsString : propertyVarsList) {
			List<String> pVars = Arrays.asList(propertyVarsString.split(","));
			switch (pVars.get(0)) {
			case "Council":
				SimUtils.getCouncil().setSavings(Double.parseDouble(pVars.get(1)));
				break;
			case "ElderlyCare":
				SimUtils.getElderlyCare().setSavings(Double.parseDouble(pVars.get(1)));
				break;
			case "Factory":
				SimUtils.getFactory().setSavings(Double.parseDouble(pVars.get(1)));
				SimUtils.getFactory().setVariables(Integer.parseInt(pVars.get(2)), Integer.parseInt(pVars.get(3)));
				break;
			case "School":
				SimUtils.getSchool().setSavings(Double.parseDouble(pVars.get(1)));
				break;
			case "SocialCare":
				SimUtils.getSocialCare().setSavings(Double.parseDouble(pVars.get(1)));
				break;
			}
		}
	}

	public void savePopulation(String filePath, String fileName) {
		// Data humans
		List<String> dataHumans = new ArrayList<String>();
		dataHumans.add("%id,gender,foreigner,higherEducated,age,money,childrenWanted,homelessTick,nettoIncome,necessaryCost,jobTitle");
		ArrayList<Resident> residents = SimUtils.getObjectsAll(Resident.class);
		for (Resident resident : residents) {
			dataHumans.add(resident.getHumanVarsAsString());
		}
		writeToFile(filePath + "/" + fileName + "Data.txt", dataHumans);
		
		// Data network
		safeRelations(filePath + "/" + fileName + "Relations.txt");
		safeChildren(filePath + "/" + fileName + "Children.txt");
		safeProperty(filePath + "/" + fileName + "Property.txt");
		
		List<String> dataProperties = new ArrayList<String>();
		dataProperties.add("%propertyType,savings,extra-variables");
		Council council = SimUtils.getCouncil();
		dataProperties.add("Council," + council.getSavings());
		ElderlyCare elderlyCare = SimUtils.getElderlyCare();
		dataProperties.add("ElderlyCare," + elderlyCare.getSavings());
		Factory factory = SimUtils.getFactory();
		dataProperties.add("Factory," + factory.getSavings() + "," + factory.getMaxEmployees() + "," + factory.getFishUnprocessed());
		School school = SimUtils.getSchool();
		dataProperties.add("School," + school.getSavings());
		SocialCare socialCare = SimUtils.getSocialCare();
		dataProperties.add("SocialCare," + socialCare.getSavings());
		writeToFile(filePath + "/" + fileName + "PropertyVars.txt", dataProperties);
	}
	
	public void safeRelations(String filePathAndName) {
		
		List<Integer> humansContained = new ArrayList<Integer>();
		List<String> data = new ArrayList<String>();
		data.add("%human1,human2");
		for (Human human : SimUtils.getObjectsAll(Human.class)) {
			if (!humansContained.contains(human.getId()) && human.getPartnerId() >= 0) {
				data.add(human.getId() + "," + human.getPartnerId());
			}
		}
		writeToFile(filePathAndName, data);
	}
	
	public void safeChildren(String filePathAndName) {
		
		List<String> data = new ArrayList<String>();
		data.add("%parent,child1,child2,child3,etc.");
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
		writeToFile(filePathAndName, data);
	}
	
	public void safeProperty(String filePathAndName) {
		
		List<String> data = new ArrayList<String>();
		data.add("%owner,property1,property2,property3,property4,etc.");
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
		writeToFile(filePathAndName, data);
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
