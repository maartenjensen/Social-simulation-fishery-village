package valueframework;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import valueframework.common.Log;

public class ValueAssignment {
	
	private static Queue<String> queue;
	private static ArrayList<String> importanceRange;
	private static Map<String, RandomTree> valueTrees;
	private final static double multiplier = 20;

	public static void checkInitialConditions(Map<String, RandomTree> vTrees) {
		
		if (queue != null)
			queue.clear();
		queue = new LinkedList<String>();
		valueTrees = vTrees;
		if (importanceRange != null)
			importanceRange.clear();
		importanceRange = new ArrayList<String>();
		initializeImportanceRange();
		sortImportanceRange();
		initializeQueue();
		thresholdRangeCheck_buffer();
	}

	private static void thresholdRangeCheck_buffer() {
		
		String valueTtl, qItem;
		double lowerBound, upperBound;
		while (queue.size() != 0) {
			// System.out.println("\nprint queue : " + queue.toString());
			while (queue.size() != 0) {
				qItem = getImportanceRange(queue.remove());
				// System.out.println("The head of the queue : " + qItem);
				valueTtl = getValueName(qItem);
				lowerBound = getLowerBound(qItem);
				upperBound = getUpperBound(qItem);
				if (lowerBound < 0 & upperBound < 0)
					continue;
				if (lowerBound < 0 | upperBound < 0)
					Log.printError("something is wrong! bcs one the L or U bounds is negative " + lowerBound + ";" + upperBound);
				for (int irIdx = 0; irIdx < importanceRange.size(); irIdx++) {
					String irItem = importanceRange.get(irIdx);
					double originalLowerBound = getLowerBound(irItem);
					double originalUpperBound = getUpperBound(irItem);
					if (valueTtl.equals(getValueName(irItem))
							|| (originalLowerBound >= 0 & originalLowerBound == originalUpperBound))
						continue;
					// check for update
					// condition 1
					// System.out.println("value to be compared with the head of the value : "
					// + irItem);
					String addToQueue1 = checkConditionOne(qItem, irItem);
					// System.out.println("after checking the first condition : "
					// + addToQueue1);
					// condition 2
					String addToQueue2 = checkConditiontwo(qItem, irItem);
					// System.out.println("after checking the second condition "
					// + addToQueue2);

					// intersection
					String intersectionSet = intersectionConditions(
							addToQueue1, addToQueue2);
					// System.out.println("the intersection of two ranges : " +
					// intersectionSet);
					double newLowerBound = getLowerBound(intersectionSet);
					double newUpperBound = getUpperBound(intersectionSet);

					if (newLowerBound != originalLowerBound
							|| newUpperBound != originalUpperBound) {
						queue.add(getValueName(irItem));
						// System.out.println("add to queue : " +
						// intersectionSet);
						importanceRange.set(irIdx, intersectionSet);
					}
				}
			}

			String undetermindItem = findNextUndetermindItem(importanceRange);
			if (undetermindItem == null) {
				// System.out.println("all values assigned");
				break;
			} else {
				double lowerThres = getLowerBound(undetermindItem);
				double upperThres = getUpperBound(undetermindItem);
				double randThreshold = (lowerThres + (Math.random() * (upperThres - lowerThres)));
				String newItem = toString(getValueName(undetermindItem),
						randThreshold, randThreshold, randThreshold);
				importanceRange.set(importanceRange.indexOf(undetermindItem),
						newItem);
				queue.add(getValueName(undetermindItem));
			}
		}
	}

	private static String findNextUndetermindItem(
			ArrayList<String> importanceRange) {
		for (String item : importanceRange) {
			String[] items = item.split(";");
			if (!items[2].equals(items[3]))
				return item;
		}
		return null;
	}

	private static String intersectionConditions(String addToQueue1, String addToQueue2) {
		double lb1 = getLowerBound(addToQueue1), lb2 = getLowerBound(addToQueue2), newLowerBound;
		String valueTitle = getValueName(addToQueue1);
		Double valueImportance = getThreshold(addToQueue1);
		// if(lb1 < 0 || lb2 < 0)
		newLowerBound = Math.max(lb1, lb2);
		// else
		// newLowerBound = Math.max(100, Math.max(lb1, lb2));
		double ub1 = getUpperBound(addToQueue1), ub2 = getUpperBound(addToQueue2), newUpperBound;
		if (ub1 < 0 || ub2 < 0)
			newUpperBound = Math.max(ub1, ub2);
		else
			newUpperBound = Math.min(ub1, ub2);
		newLowerBound = putInRange(newLowerBound);
		newUpperBound = putInRange(newUpperBound);
		String intersectionSet;
		intersectionSet = toString(valueTitle, valueImportance, newLowerBound,
				newUpperBound);
		return intersectionSet;
	}

	private static String checkConditiontwo(String firstItem, String secondItem) {
		String firstItemName = getValueName(firstItem);
		String secondItemName = getValueName(secondItem);
		double secondItemThreshold = getThreshold(secondItem);
		int idx = AbstractValue.getIndexOfAbstractValue(firstItemName);
		if (idx == -1)
			return null;
		int numOfAbstractValues = AbstractValue.values().length;
		int firstIdx = (idx <= (numOfAbstractValues / 2 + 1)) ? (idx)
				: (numOfAbstractValues - idx);
		idx = AbstractValue.getIndexOfAbstractValue(secondItemName);
		if (idx == -1)
			return null;
		int secondIdx = (idx <= (numOfAbstractValues / 2 + 1)) ? (idx) : (numOfAbstractValues - idx);

		double firstThresLower = getLowerBound(firstItem);
		double firstThresUpper = getUpperBound(firstItem);
		double secondThresLower = getLowerBound(secondItem);
		double secondThresUpper = getUpperBound(secondItem);

		double maxOfSummation = 100 + multiplier / 2;
		double minOfSummation = 100 - multiplier / 2;

		if (secondIdx == (firstIdx + (numOfAbstractValues / 2))
				% numOfAbstractValues) {
			double lb, ub;

			lb = putInRange(Math.min(minOfSummation - firstThresLower,
					minOfSummation - firstThresUpper));
			ub = putInRange(Math.max(maxOfSummation - firstThresUpper,
					maxOfSummation - firstThresLower));

			String intersection = intersectionConditions(
					toString(secondItemName, secondItemThreshold,
							secondThresLower, secondThresUpper),
					toString(secondItemName, secondItemThreshold,
							Math.min(lb, ub), Math.max(lb, ub)));
			secondThresLower = getLowerBound(intersection);
			// secondThresLower = 0.0;
			secondThresUpper = getUpperBound(intersection);
		}

		if (secondThresLower > secondThresUpper) {
			// System.err.println("lowerThreshold is greater than upperThreshold"
			// + secondThresLower + " > " + secondThresUpper);
			secondThresLower = 0;
			secondThresUpper = 100;
		}
		return toString(secondItemName, secondItemThreshold, secondThresLower,
				secondThresUpper);
	}

	private static double putInRange(double d) {
		if (d < 0)
			return 0;
		if (d > 100)
			return 100;
		return d;
	}

	private static String checkConditionOne(String firstItem, String secondItem) {
		// System.out.println("\tcheckConditionOne : " + firstItem + ", " +
		// secondItem);
		// String[] firstItemPieces = firstItem.split(";");
		// String[] secondItemPieces = secondItem.split(";");

		int firstIdx = AbstractValue
				.getIndexOfAbstractValue(getValueName(firstItem));
		// System.out.println("\tfirst Index : " + firstIdx);
		if (firstIdx == -1)
			return null;
		int secondIdx = AbstractValue
				.getIndexOfAbstractValue(getValueName(secondItem));
		// System.out.println("\tsecond Index : " + secondIdx);
		if (secondIdx == -1)
			return null;
		int indexDifference = Math.abs((firstIdx - secondIdx));
		int numOfAbstractValues = AbstractValue.values().length;
		// System.out.println("\tnumOfAbstractValues " + numOfAbstractValues);
		indexDifference = indexDifference <= (numOfAbstractValues / 2) ? indexDifference
				: numOfAbstractValues - indexDifference;
		// System.out.println("\tindex difference " + indexDifference);

		double firstThresLower = Math.max(0, getLowerBound(firstItem));
		double firstThresUpper = Math.max(0, getUpperBound(firstItem));
		double secondThresLower = Math.max(0.0, firstThresLower
				- indexDifference * multiplier);
		double secondThresUpper = Math.min(100.0, firstThresUpper
				+ indexDifference * multiplier);
		// System.out.println("\t\t1-before condition check : second item lower and upper threshold : "
		// + secondThresLower + ", " + secondThresUpper);

		double lr = getLowerBound(secondItem);
		double ur = getUpperBound(secondItem);
		if (lr >= 0)
			secondThresLower = Math.max(Math.max(lr, secondThresLower), 0);
		if (ur >= 0)
			secondThresUpper = Math.min(Math.min(ur, secondThresUpper), 100);
		// System.out.println("\t\t2-after condition check : second item lower and upper threshold : "
		// + secondThresLower + ", " + secondThresUpper);

		if (secondThresLower > secondThresUpper)
			System.err.println("lowerThreshold is greater than upperThreshold "
					+ secondThresLower + " > " + secondThresUpper);
		return toString(getValueName(secondItem), getThreshold(secondItem),
				secondThresLower, secondThresUpper);
	}

	public static double getUpperBound(String intersectionSet) {
		return Double.parseDouble(intersectionSet.split(";")[3]);
	}

	public static double getLowerBound(String intersectionSet) {
		return Double.parseDouble(intersectionSet.split(";")[2]);
	}

	public static Double getThreshold(String item) {

		return Double.valueOf(item.split(";")[1]);
	}

	public static String getValueName(String item) {
		return item.split(";")[0];
	}

	private static String getImportanceRange(String qItem) {
		for (String s : importanceRange) {
			if (s.contains(qItem))
				return s;
		}
		return null;
	}
/*
	private static void printImportanceRange() {

		for (String s : importanceRange) {
			Log.printLog(getValueName(s) + " " + getThreshold(s) + ";\t");
		}
	}*/

	private static void initializeQueue() {
		double threhshold;
		boolean first = true;
		String valueTitle;
		for (String item : importanceRange) {
			threhshold = getThreshold(item);
			valueTitle = getValueName(item);
			if (first) {
				queue.add(valueTitle);
				String addToQ = toString(valueTitle, threhshold, threhshold,
						threhshold);
				int idx = importanceRange.indexOf(toString(valueTitle,
						threhshold, -1, -1));
				if (idx < 0 | idx > importanceRange.size())
					System.err.println("index out of bound : " + idx + " "
							+ toString(valueTitle, threhshold, -1, -1) + ", "
							+ importanceRange.size());
				importanceRange.set(importanceRange.indexOf(toString(
						valueTitle, threhshold, -1, -1)), addToQ);
			} else
				queue.add(valueTitle);
			first = false;
		}
	}

	private static void sortImportanceRange() {
		importanceRange = shuffleArray(importanceRange);
		String item, itemTemp;
		String[] items;
		double biggestImportance = -1;
		double importance;
		for (int i = 0; i < importanceRange.size(); i++) {
			biggestImportance = Double.parseDouble(importanceRange.get(i).split(";")[1]);
			for (int j = i + 1; j < importanceRange.size(); j++) {
				item = importanceRange.get(j);
				items = item.split(";");
				importance = Double.parseDouble(items[1]);
				if (importance > biggestImportance) {
					itemTemp = importanceRange.get(i);
					importanceRange.set(i, item);
					importanceRange.set(j, itemTemp);
					biggestImportance = importance;
				}
			}
		}		
	}

	private static ArrayList<String> shuffleArray(ArrayList<String> ar) {
		// If running on Java 6 or older, use `new Random()` on RHS here
		Random rnd = ThreadLocalRandom.current();
		for (int i = ar.size() - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			// Simple swap
			String a = ar.get(index);
			ar.set(index, ar.get(i));
			ar.set(i, a);
		}
		return ar;
	}

	private static String toString(String valueTitle, double threhshold,
			double lowerBound, double upperBound) {
		return valueTitle + ";" + threhshold + ";" + lowerBound + ";"
				+ upperBound;
	}

	private static void initializeImportanceRange() {
//		 System.out.println("\t\tnum of elements in valueTrees " + valueTrees.size());
		for (String valueName : valueTrees.keySet()) {
//			 System.out.println("valueName" + valueName + ", " +
//			 valueTrees.get(valueName).getWaterTank().getThreshold());
			importanceRange.add(toString(valueName, valueTrees.get(valueName)
					.getWaterTank().getThreshold(), -1, -1));
		}

	}

	public static ArrayList<String> getImportanceRange() {
		return importanceRange;
	}

}
