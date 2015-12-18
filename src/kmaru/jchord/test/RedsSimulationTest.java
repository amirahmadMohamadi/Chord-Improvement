package kmaru.jchord.test;

import org.junit.Before;

import kmaru.jchord.simulation.Simulation;
import kmaru.jchord.simulation.Simulation.SimulationData;

public class RedsSimulationTest
{
	SimulationData simulationData;
	
	@Before
	public void beforeMethod()
	{
		simulationData = new SimulationData(Simulation.DEFAULT_SIMULATION_DATA);
		
		simulationData.setRepeatingTestsNumber(1);
		simulationData.setMinFailureRate(20);
		simulationData.setMaxFailureRate(21);
		
		
	}
}
