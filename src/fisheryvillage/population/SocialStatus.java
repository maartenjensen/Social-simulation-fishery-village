package fisheryvillage.population;


/**
* Social status class that deals with a human's social status
*
* @author Maarten Jensen
* @since 2018-02-20
*/
public class SocialStatus {

	double level;
	
	public SocialStatus(int level) {
		this.level = level;
	}
	
	public double getSocialLevel() {
		return level;
	}
	
	public void setSocialLevel(double level) {
		this.level = level;
	}
	
	public void addSocialLevel(int amount) {
		this.level += amount;
	}
}