package testing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import fisheryvillage.common.Constants;
import fisheryvillage.property.BoatType;

class TestBoatType {

	@Test
	void test() {
		BoatType boatType = BoatType.SMALL;
		int output = boatType.getPrice();
		assertEquals(Constants.BOAT_SMALL_PRICE + 10, output);
	}
}