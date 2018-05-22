package fisheryvillage.property;

public enum HouseType {
	
	HOMELESS(0.0),
	WITH_OTHERS(0.1),
	CHEAP(0.3),
	STANDARD(0.6),
	EXPENSIVE(1.0);
	
	double socialStatusHouse;
	
	HouseType(double socialStatusHouse) {
		this.socialStatusHouse = socialStatusHouse;
	}
	
	public double getSocialStatusHouse() {
		return socialStatusHouse;
	}
}