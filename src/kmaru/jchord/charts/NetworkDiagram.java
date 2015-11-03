package kmaru.jchord.charts;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingWorker;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

import kmaru.jchord.Chord;
import kmaru.jchord.ChordNode;

public class NetworkDiagram
{
	public static void drawNetwork(final Chord chord, String name)
	{
		final JFrame frame = new JFrame(name);
		frame.setLayout(new BorderLayout(5, 5));

		final mxGraph graph = new mxGraph();
		graph.setCellsMovable(false);
		graph.setPortsEnabled(false);
		graph.setCellsResizable(false);
		graph.setCellsEditable(false);
		graph.setConnectableEdges(false);
		graph.setAllowDanglingEdges(false);
		graph.setAllowNegativeCoordinates(true);
		graph.setAutoOrigin(true);
		graph.setAutoSizeCells(true);

		final Object parent = graph.getDefaultParent();

		new SwingWorker<Void, Void>()
		{

			@Override
			protected Void doInBackground() throws Exception
			{
				graph.getModel().beginUpdate();
				try
				{
					// get stylesheet
					mxStylesheet stylesheet = graph.getStylesheet();

					// define stylename
					String reliableStyleName = "Good";
					String maliciousStyleName = "Malicious";

					// create image style
					Hashtable<String, Object> reliableStyle = new Hashtable<String, Object>();
					reliableStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_IMAGE);
					reliableStyle.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_TOP);
					reliableStyle.put(mxConstants.STYLE_IMAGE, "/kmaru/jchord/charts/green_dot.png");

					Hashtable<String, Object> maliciousStyle = new Hashtable<String, Object>();
					maliciousStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_IMAGE);
					maliciousStyle.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_TOP);
					maliciousStyle.put(mxConstants.STYLE_IMAGE, "/kmaru/jchord/charts/red_dot.png");

					stylesheet.putCellStyle(reliableStyleName, reliableStyle);
					stylesheet.putCellStyle(maliciousStyleName, maliciousStyle);

					int size = chord.getSortedNodeMap().size();
					double r = 300;
					double x = 0;
					double y = 0;
					ChordNode node = null;
					List<Object> vertices = new ArrayList<>();

					for (int i = 0; i < size; i++)
					{
						node = chord.getSortedNode(i);

						x = r * Math.sin(
								(double) i / size * 2 * Math.PI) /* + 2 * r */;
						y = -1* r * Math.cos(
								(double) i / size * 2 * Math.PI) /* + 2 * r */;
						vertices.add(graph.insertVertex(parent, node.toString(), i, x, y, 20, 20,
								node.isMalicious() ? maliciousStyleName : reliableStyleName));
					}

					for (int j = 0; j < vertices.size() - 1; j++)
					{
						Object v1 = vertices.get(j);
						Object v2 = vertices.get(j + 1);
						graph.insertEdge(parent, null, null, v2, v1);
					}
					graph.insertEdge(parent, null, null, vertices.get(0), vertices.get(vertices.size() - 1));
				}
				finally
				{
					graph.getModel().endUpdate();
				}

				return null;
			}

			@Override
			protected void done()
			{
				mxGraphComponent graphComponent = new mxGraphComponent(graph);
				graphComponent.setCenterPage(true);

				frame.add(graphComponent, BorderLayout.CENTER);
				frame.pack();

				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setVisible(true);
			}
			
			
		}.execute();

	}

}
