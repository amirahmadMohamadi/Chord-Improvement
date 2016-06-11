package kmaru.jchord.reds;

import org.jgrapht.graph.DefaultEdge;

import kmaru.jchord.ChordNode;

public class ConsensusGraphEdge extends DefaultEdge
{

	public ConsensusGraphEdge()
	{
		super();
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4115486645335905743L;

	@Override
	public ChordNode getSource()
	{
		return (ChordNode) super.getSource();
	}

	@Override
	public ChordNode getTarget()
	{
		return (ChordNode) super.getTarget();
	}

}
