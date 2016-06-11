package kmaru.jchord.charts;

import java.awt.Color;
import java.util.List;

import javax.swing.JFrame;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.RefineryUtilities;

/**
 * A line chart with error bars.
 */
public class FailureRatioPlot
{

	private YIntervalSeriesCollection	dataset;

	public FailureRatioPlot()
	{
		dataset = new YIntervalSeriesCollection();
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

	/**
	 * Creates a chart.
	 *
	 * @param dataset
	 *            the dataset.
	 *
	 * @return The chart.
	 */
	private JFreeChart createChart(YIntervalSeriesCollection dataset)
	{
		NumberAxis xAxis = new NumberAxis("Malicious node rate");
		NumberAxis yAxis = new NumberAxis("Failure ratio");
		XYErrorRenderer renderer = new XYErrorRenderer();
		renderer.setBaseLinesVisible(true);
		renderer.setBaseShapesVisible(false);
		XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);

		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		JFreeChart chart = new JFreeChart("Failure Ratio", plot);
		chart.setBackgroundPaint(Color.white);

		return chart;
	}

	/**
	 * Creates a sample dataset.
	 */
	public IntervalXYDataset addDataset(List<Double> x, List<Double> mean, List<Double> std, String name)
	{
		YIntervalSeries s1 = new YIntervalSeries(name);
		for (int i = 0; i < x.size(); i++)
		{
			s1.add(x.get(i), mean.get(i), mean.get(i) - std.get(i), mean.get(i) + std.get(i));
		}
		dataset.addSeries(s1);
		return dataset;
	}

}