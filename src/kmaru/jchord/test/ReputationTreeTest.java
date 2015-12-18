package kmaru.jchord.test;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import kmaru.jchord.ChordKey;
import kmaru.jchord.ChordNode;
import kmaru.jchord.reds.RedsChord;
import kmaru.jchord.reds.RedsChordNode;
import kmaru.jchord.reds.ReputationTree;
import kmaru.jchord.simulation.ChordProtocol;
import kmaru.jchord.simulation.Simulation;
import kmaru.jchord.simulation.Simulation.SimulationData;

public class ReputationTreeTest
{

	Simulation			simulation;
	private RedsChord	network;

	@Before
	public void berforeMethod() throws Exception
	{
		SimulationData simulationData = new SimulationData(Simulation.DEFAULT_SIMULATION_DATA);
		simulationData.setNumberOfNodes(100);
		simulationData.setBucketSize(2);
		simulationData.setRedsMinObservations(10);
		simulationData.setRedsReputationTreeDepth(35);

		simulation = new Simulation(simulationData);
		network = (RedsChord) simulation.setupNetwork(0, ChordProtocol.REDS);

	}

	@Test
	public void addScoreTest()
	{
		RedsChordNode node0 = (RedsChordNode) network.getNode(0);

		ChordNode node1 = (ChordNode) network.getNode(1);

		ReputationTree reputationTree = node0.getScoreMap().get(node0.getFingerNode(0).getNodeKey()).get(0);
		reputationTree.addScore(node1.getNodeKey(), 5);
		assertTrue(Math.abs(reputationTree.getScore(node1.getNodeKey()) - 0.5) < 0.001);

		reputationTree.addScore(node1.getNodeKey(), 10);
		assertTrue(Math.abs(reputationTree.getScore(node1.getNodeKey()) - 1) < 0.001);

		reputationTree.addScore(node1.getNodeKey(), -10);
		assertTrue(Math.abs(reputationTree.getScore(node1.getNodeKey()) - (5 / 25.0)) < 0.001);

	}

	@Test
	public void sharedReputationTest()
	{
		RedsChordNode node0 = (RedsChordNode) network.getNode(0);
		
		ChordNode node1 = (ChordNode) network.getNode(1);
		
		ReputationTree reputationTree00 = node0.getScoreMap().get(node0.getFingerNode(0).getNodeKey()).get(0);
		reputationTree00.addScore(node1.getNodeKey(), 15);
		reputationTree00.addScore(node1.getNodeKey(), -10);

		ReputationTree reputationTree01 = node0.getScoreMap().get(node0.getFingerNode(0).getNodeKey()).get(1);
		reputationTree01.addScore(node1.getNodeKey(), 12);
		reputationTree01.addScore(node1.getNodeKey(), -13);
		
		
		
	}
	
	// @Test
	public void getScoreTest()
	{
		RedsChordNode node0 = (RedsChordNode) network.getNode(0);
		ReputationTree reputationTree = node0.getScoreMap().get(node0.getFingerNode(0).getNodeKey()).get(0);

		byte[] key = new byte[simulation.getSimulationData().getKeyLength() / 8];

		for (int i = 0; i < 20; i++)
		{
			key[5] += 10;
			ChordKey nodeKey = new ChordKey(key);
			System.out.println(nodeKey);
			reputationTree.addScore(nodeKey, 1);
		}
		reputationTree.printTree(System.out);
	}

	// @Test
	public void addToTreeTest()
	{
		RedsChordNode node0 = (RedsChordNode) network.getNode(0);
		ReputationTree reputationTree = node0.getScoreMap().get(node0.getFingerNode(0).getNodeKey()).get(0);

		byte[] key = new byte[simulation.getSimulationData().getKeyLength() / 8];

		ChordKey nodeKey = new ChordKey(key);
		System.out.println(node0.getNodeKey());
		System.out.println(nodeKey);
		reputationTree.addScore(nodeKey, 1);

		reputationTree.printTree(System.out);

	}
}
