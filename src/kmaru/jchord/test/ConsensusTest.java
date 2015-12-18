package kmaru.jchord.test;

import java.io.OutputStreamWriter;
import java.util.Collection;

import org.jgrapht.ext.MatrixExporter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Before;
import org.junit.Test;

import kmaru.jchord.ChordNode;
import kmaru.jchord.reds.RedsChord;
import kmaru.jchord.reds.RedsChordNode;
import kmaru.jchord.simulation.ChordProtocol;
import kmaru.jchord.simulation.Simulation;
import kmaru.jchord.simulation.Simulation.SimulationData;

public class ConsensusTest
{

	Simulation			simulation;
	private RedsChord	network;

	@Before
	public void berforeMethod() throws Exception
	{
		SimulationData simulationData = new SimulationData(Simulation.DEFAULT_SIMULATION_DATA);
		simulationData.setNumberOfNodes(100);
		simulationData.setBucketSize(1);
		simulationData.setRedsMinObservations(10);
		simulationData.setRedsReputationTreeDepth(35);

		simulation = new Simulation(simulationData);
		network = (RedsChord) simulation.setupNetwork(0, ChordProtocol.REDS);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void consensusTest()
	{
		RedsChordNode node0 = (RedsChordNode) network.getNode(0);
		ChordNode node1 = (ChordNode) network.getNode(1);

		SimpleGraph<RedsChordNode, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

		for (RedsChordNode node : (Collection<RedsChordNode>) node0.getSharedKnucklesMap().get(0))
			graph.addVertex(node);
		graph.addVertex(node0);

		for (RedsChordNode firstVertex : graph.vertexSet())
		{
			for (RedsChordNode secondVertex : graph.vertexSet())
			{
				if (firstVertex.getNodeId().equals(secondVertex.getNodeId()))
					continue;
				if (graph.containsEdge(firstVertex, secondVertex))
					continue;
				if (firstVertex.hasFinger(secondVertex) || secondVertex.hasFinger(firstVertex))
					graph.addEdge(firstVertex, secondVertex);
			}
		}

		MatrixExporter<RedsChordNode, DefaultEdge> laplacian = new MatrixExporter<>();
		System.out.println("vertex size = " + graph.vertexSet().size());
		laplacian.exportLaplacianMatrix(new OutputStreamWriter(System.out), graph);
		System.out.println("--------------------------------");
		laplacian.exportAdjacencyMatrix(new OutputStreamWriter(System.out), graph);

		double epsilon = 0.1;
		for (ChordNode sharedKNuckle : (Collection<RedsChordNode>) node0.getSharedKnucklesMap().get(0))
		{
			
		}
	}
}
