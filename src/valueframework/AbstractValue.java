package valueframework;

/**
* The status enum shows the abstract values of a human according to what is defined in the following paper
* @incollection{SCHWARTZ19921,
title = "Universals in the Content and Structure of Values: Theoretical Advances and Empirical Tests in 20 Countries",
editor = "Mark P. Zanna",
series = "Advances in Experimental Social Psychology",
publisher = "Academic Press",
volume = "25",
pages = "1 - 65",
year = "1992",
issn = "0065-2601",doi = "https://doi.org/10.1016/S0065-2601(08)60281-6",
url = "http://www.sciencedirect.com/science/article/pii/S0065260108602816",
author = "Shalom H. Schwartz"
}
* each of these values will be used as a root of a value tree. 
*
* @author Samaneh Heidari
* @since 2018-03-29
*/

public enum AbstractValue {
	
	SELFDIRECTION(0), UNIVERSALISM(1), BENEVOLENCE(2), CONFORMITY(3), TRADITION(4), 
	SECURITY(5), POWER(6), ACHIEVEMENT(7), HEDONISM(8), STIMULATION(9);

    int ValueIndex = 0;

    AbstractValue(int index) {
        this.ValueIndex = index;
    }

    public static AbstractValue getAbstractValueByIndex(int idx) {
        return AbstractValue.values()[idx];
    }

    public static int getIndexOfAbstractValue(String value){
    	return AbstractValue.valueOf(value).ordinal();
    	
    }
}
