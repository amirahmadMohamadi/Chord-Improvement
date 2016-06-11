package kmaru.jchord.charts;

import java.awt.Color;

import javax.swing.JFrame;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.ui.RefineryUtilities;

public class ConsensusPlot
{

	private DefaultXYDataset	dataset;

	public ConsensusPlot()
	{
		dataset = new DefaultXYDataset();
	}

	public void draw()
	{
		JFrame frame = new JFrame("Result");

		ChartPanel chartPanel = new ChartPanel(createChart(dataset));
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		frame.getContentPane().add(chartPanel);
		// frame.setSize(chartPanel.getPreferredSize());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	}

	private JFreeChart createChart(DefaultXYDataset dataset)
	{
		NumberAxis xAxis = new NumberAxis("Iteration");
		NumberAxis yAxis = new NumberAxis("Score");
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setBaseLinesVisible(true);
		renderer.setBaseShapesVisible(true);
		XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);

		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		JFreeChart chart = new JFreeChart("Consensus", plot);
		chart.setBackgroundPaint(Color.white);

		return chart;
	}

	/**
	 * Creates a sample dataset.
	 */
	public DefaultXYDataset addDataset(double[][] scores, String name)
	{
		dataset.addSeries(name, scores);
		return dataset;
	}

}
