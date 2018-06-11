package valueframework.common;

import java.util.ArrayList;

import valueframework.WaterTank;

public final class Facility {
	
	public static ArrayList<WaterTank> sort(ArrayList<WaterTank> whole) {
		if(whole.size()==0)
		{
			Log.printDebug("the input array to sort is empty");
			return null;
		}
        return mergeSort(whole);
    }
 
	private static ArrayList<WaterTank> mergeSort(ArrayList<WaterTank> whole) {
	    ArrayList<WaterTank> left = new ArrayList<WaterTank>();
	    ArrayList<WaterTank> right = new ArrayList<WaterTank>();
	    int center;
	 
	    if (whole.size() == 1) {   
	    	/*System.out.println("IN SORT FUNCTION :  value "  + whole.get(0).getValueName() + ", priority is " + 
	    			whole.get(0).getWaterTank().getPriorityPercentage() + ", water Level is : " + whole.get(0).getWaterTank().getFilledLevel() +
	    			", threshold is : " + whole.get(0).getWaterTank().getThreshould());*/
	        return whole;
	    } else {
	        center = whole.size()/2;
	        // copy the left half of whole into the left.
	        for (int i=0; i<center; i++) {
	                left.add(whole.get(i));
	        }
	 
	        //copy the right half of whole into the new arraylist.
	        for (int i=center; i<whole.size(); i++) {
	                right.add(whole.get(i));
	        }
	 
	        // Sort the left and right halves of the arraylist.
	        left  = mergeSort(left);
	        right = mergeSort(right);
	 
	        // Merge the results back together.
	        merge(left, right, whole);
	    }
	    return whole;
	}
	private static void merge(ArrayList<WaterTank> left, ArrayList<WaterTank> right, ArrayList<WaterTank> whole) {
	    int leftIndex = 0;
	    int rightIndex = 0;
	    int wholeIndex = 0;
	 
	    // As long as neither the left nor the right ArrayList has
	    // been used up, keep taking the smaller of left.get(leftIndex)
	    // or right.get(rightIndex) and adding it at both.get(bothIndex).
	    while (leftIndex < left.size() && rightIndex < right.size()) {
//	        if ( (left.get(leftIndex).compareTo(right.get(rightIndex))) < 0) {
	    	if((left.get(leftIndex).getPriorityPercentage()) < (right.get(rightIndex).getPriorityPercentage())){
	            whole.set(wholeIndex, left.get(leftIndex));
	            leftIndex++;
	        } else {
	            whole.set(wholeIndex, right.get(rightIndex));
	            rightIndex++;
	        }
	        wholeIndex++;
	    }
	 
	    ArrayList<WaterTank> rest;
	    int restIndex;
	    if (leftIndex >= left.size()) {
	        // The left ArrayList has been use up...
	        rest = right;
	        restIndex = rightIndex;
	    } else {
	        // The right ArrayList has been used up...
	        rest = left;
	        restIndex = leftIndex;
	    }
	 
	    // Copy the rest of whichever ArrayList (left or right) was not used up.
	    for (int i=restIndex; i<rest.size(); i++) {
	        whole.set(wholeIndex, rest.get(i));
	        wholeIndex++;
	    }
	}
}
