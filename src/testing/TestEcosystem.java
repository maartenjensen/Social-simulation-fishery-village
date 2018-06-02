package testing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import fisheryvillage.ecosystem.Ecosystem;
import repast.simphony.space.grid.GridPoint;

class TestEcosystem {

	@Test
	void test() {
		Ecosystem test = new Ecosystem(1000, new GridPoint(0,0));
		double output = test.getFish();
		assertEquals(1000, output);
	}

}