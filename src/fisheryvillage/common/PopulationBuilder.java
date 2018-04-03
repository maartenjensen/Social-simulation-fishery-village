package fisheryvillage.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fisheryvillage.municipality.Council;
import fisheryvillage.population.Human;
import fisheryvillage.population.Status;
import fisheryvillage.property.ElderlyCare;
import fisheryvillage.property.Factory;
import fisheryvillage.property.Property;
import fisheryvillage.property.School;
import fisheryvillage.property.SocialCare;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;

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
		
		List<String> humansList = readFile(filePath + "/" + fileName + ".txt");
		for (String humanString : humansList) {
			List<String> hVars = Arrays.asList(humanString.split(","));
			int id = Integer.parseInt(hVars.get(0));
			int age = Integer.parseInt(hVars.get(1));
			boolean isMan = Boolean.parseBoolean(hVars.get(2));
			double money = Double.parseDouble(hVars.get(3));
			int childrenWanted = Integer.parseInt(hVars.get(4));
			boolean foreigner = Boolean.parseBoolean(hVars.get(5));
			int homelessTick = Integer.parseInt(hVars.get(6));
			double nettoIncome = Double.parseDouble(hVars.get(7));
			double necessaryCost = Double.parseDouble(hVars.get(8));
			String jobTitle = hVars.get(9);
			Status status = Status.valueOf(hVars.get(10));
			int boatId = Integer.parseInt(hVars.get(11));
			Human human = new Human(isMan, age, id, money, foreigner, childrenWanted, homelessTick,
									nettoIncome, necessaryCost, jobTitle, status, boatId);
			Logger.logInfo("Generated H" + human.getId() + ", age: " + human.getAge());
		}
		
		List<String> relationsList = readFile(filePath + "/" + fileName + "Relations.txt");
		Network<Object> relationNetwork = SimUtils.getNetwork(Constants.ID_NETWORK_COUPLE);
		for (String relationString : relationsList) {
			List<String> rVars = Arrays.asList(relationString.split(","));
			relationNetwork.addEdge(HumanUtils.getHumanById(Integer.parseInt(rVars.get(0))), HumanUtils.getHumanById(Integer.parseInt(rVars.get(1))) );
		}
		
		List<String> childrenList = readFile(filePath + "/" + fileName + "Children.txt");
		Network<Object> childrenNetwork = SimUtils.getNetwork(Constants.ID_NETWORK_CHILDREN);
		for (String childrenString : childrenList) {
			List<String> cVars = Arrays.asList(childrenString.split(","));
			childrenNetwork.addEdge(HumanUtils.getHumanById(Integer.parseInt(cVars.get(0))), HumanUtils.getHumanById(Integer.parseInt(cVars.get(1))) );
		}
		
		List<String> propertyList = readFile(filePath + "/" + fileName + "Property.txt");
		Network<Object> propertyNetwork = SimUtils.getNetwork(Constants.ID_NETWORK_PROPERTY);
		for (String propertyString : propertyList) {
			List<String> pVars = Arrays.asList(propertyString.split(","));
			propertyNetwork.addEdge(HumanUtils.getHumanById(Integer.parseInt(pVars.get(0))), SimUtils.getPropertyById(Integer.parseInt(pVars.get(1))) );
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
		dataHumans.add("%id,age,gender,money,childrenWanted,foreigner,homelessTick,nettoIncome,necessaryCost,jobTitle");
		ArrayList<Human> humans = SimUtils.getObjectsAll(Human.class);
		for (Human human : humans) {
			dataHumans.add(human.getHumanVarsAsString());
		}
		writeToFile(filePath + "/" + fileName + ".txt", dataHumans);
		
		// Data network
		safeNetwork(Constants.ID_NETWORK_COUPLE, filePath + "/" + fileName + "Relations.txt");
		safeNetwork(Constants.ID_NETWORK_CHILDREN, filePath + "/" + fileName + "Children.txt");
		safeNetwork(Constants.ID_NETWORK_PROPERTY, filePath + "/" + fileName + "Property.txt");
		
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
	
	public void safeNetwork(String networkId, String filePathAndName) {
		
		List<String> dataNetwork = new ArrayList<String>();
		dataNetwork.add("%source_id, target_id");
		Iterable<RepastEdge<Object>> edges = SimUtils.getNetwork(networkId).getEdges();
		for (RepastEdge<Object> edge : edges) {
			if (edge.getTarget() instanceof Human) {
				dataNetwork.add( ((Human) edge.getSource()).getId() + "," + ((Human) edge.getTarget()).getId() );
			}
			else if (edge.getTarget() instanceof Property) {
				dataNetwork.add( ((Human) edge.getSource()).getId() + "," + ((Property) edge.getTarget()).getId() );
			}
		}
		writeToFile(filePathAndName, dataNetwork);
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
