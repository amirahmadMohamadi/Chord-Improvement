package kmaru.jchord.ui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import kmaru.jchord.test.ChordProtocol;
import kmaru.jchord.test.Simulation;

public class SimulationSettingsFrame extends JFrame
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 4029167558803322908L;
	private JPanel				contentPane;

	private JComboBox<String>	hashFunctionComboBox;
	private JFormattedTextField	keyLengthField;
	private JFormattedTextField	nodesField;
	private JFormattedTextField	repeatingTestField;
	private JFormattedTextField	failureRateField;
	private JFormattedTextField	haloRedundancyField;
	private JCheckBox			drawNetworkCheckBox;
	private JCheckBox			drawResultCheckBox;
	private JCheckBox			saveToFileCheckBox;
	private JButton				startSimulationButton;
	private JProgressBar		progressBar;
	private JLabel				lblNumberOfLookups;
	private JFormattedTextField	lookupsField;
	private JLabel				lblRedsBucketSize;
	private JFormattedTextField	bucketSizeField;

	private Simulation	simulation	= new Simulation();
	private JCheckBox	chordCheckBox;
	private JCheckBox	haloCheckBox;
	private JCheckBox	redsCheckBox;
	private JFormattedTextField minObservationsField;
	private JFormattedTextField reputationTreeDepthField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					SimulationSettingsFrame frame = new SimulationSettingsFrame();
					frame.setVisible(true);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public SimulationSettingsFrame()
	{
		initComponents();
		initValues();
	}

	private void initValues()
	{
		hashFunctionComboBox.setSelectedItem(Simulation.DEFAULT_SIMULATION_SETTINGS.HASH_FUNCTION);
		keyLengthField.setValue(Simulation.DEFAULT_SIMULATION_SETTINGS.KEY_LENGTH);
		nodesField.setValue(Simulation.DEFAULT_SIMULATION_SETTINGS.NUM_OF_NODES);
		lookupsField.setValue(Simulation.DEFAULT_SIMULATION_SETTINGS.NUM_OF_LOOKUPS);
		failureRateField.setValue(Simulation.DEFAULT_SIMULATION_SETTINGS.MAX_MALICIOUS_PROBABILITY);
		repeatingTestField.setValue(Simulation.DEFAULT_SIMULATION_SETTINGS.NUM_OF_REPEATING_TESTS);
		haloRedundancyField.setValue(Simulation.DEFAULT_SIMULATION_SETTINGS.NUM_OF_HALO_REDUNDANCY);
		bucketSizeField.setValue(Simulation.DEFAULT_SIMULATION_SETTINGS.NUM_OF_BUCKET_SIZE);
		minObservationsField.setValue(Simulation.DEFAULT_SIMULATION_SETTINGS.REDS_MINIMUM_OBSERVATIONS);
		reputationTreeDepthField.setValue(Simulation.DEFAULT_SIMULATION_SETTINGS.REDS_REPUTATION_TREE_DEPTH);
		
		for (ChordProtocol chordProtocol : simulation.getRunningSimulations())
		{
			switch (chordProtocol)
			{
			case Chord:
				chordCheckBox.setSelected(true);
				break;
			case HALO:
				haloCheckBox.setSelected(true);
				break;
			case REDS:
				redsCheckBox.setSelected(true);
				break;

			}
		}

		drawNetworkCheckBox.setSelected(simulation.isNetworkRingDrawn());
		drawResultCheckBox.setSelected(simulation.isResultDrawn());
		saveToFileCheckBox.setSelected(simulation.isResultSaved());

		progressBar.setValue(0);
	}

	private void initComponents()
	{
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 658, 492);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Key Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(
				new TitledBorder(null, "Network Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(
				new TitledBorder(null, "Output Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		JPanel panel_3 = new JPanel();
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addComponent(panel_3, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 622, Short.MAX_VALUE)
						.addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 622, Short.MAX_VALUE)
						.addComponent(panel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
						.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addGap(21))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(14)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 215, GroupLayout.PREFERRED_SIZE)
					.addGap(12)
					.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(panel_3, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(16, Short.MAX_VALUE))
		);

		progressBar = new JProgressBar();

		startSimulationButton = new JButton("Start Simulation");
		startSimulationButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					startSimulation();
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}

		});
		GroupLayout gl_panel_3 = new GroupLayout(panel_3);
		gl_panel_3.setHorizontalGroup(
			gl_panel_3.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_3.createSequentialGroup()
					.addContainerGap()
					.addComponent(startSimulationButton)
					.addGap(27)
					.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, 444, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(17, Short.MAX_VALUE))
		);
		gl_panel_3.setVerticalGroup(
			gl_panel_3.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_3.createSequentialGroup()
					.addGap(20)
					.addGroup(gl_panel_3.createParallelGroup(Alignment.TRAILING)
						.addComponent(progressBar, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
						.addComponent(startSimulationButton, Alignment.LEADING))
					.addContainerGap(11, Short.MAX_VALUE))
		);
		panel_3.setLayout(gl_panel_3);

		drawNetworkCheckBox = new JCheckBox("Draw Network Ring");
		drawNetworkCheckBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				simulation.setNetworkRingDrawn(drawNetworkCheckBox.isSelected());
			}
		});

		drawResultCheckBox = new JCheckBox("Draw result diagram");
		drawResultCheckBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				simulation.setResultDrawn(drawResultCheckBox.isSelected());
			}
		});

		saveToFileCheckBox = new JCheckBox("Save results to file");
		saveToFileCheckBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				simulation.setResultSaved(saveToFileCheckBox.isSelected());
			}
		});
		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
		gl_panel_2.setHorizontalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addContainerGap()
					.addComponent(drawNetworkCheckBox)
					.addPreferredGap(ComponentPlacement.RELATED, 73, Short.MAX_VALUE)
					.addComponent(drawResultCheckBox)
					.addGap(64)
					.addComponent(saveToFileCheckBox)
					.addGap(23))
		);
		gl_panel_2.setVerticalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
						.addComponent(drawNetworkCheckBox)
						.addComponent(saveToFileCheckBox)
						.addComponent(drawResultCheckBox))
					.addContainerGap(9, Short.MAX_VALUE))
		);
		panel_2.setLayout(gl_panel_2);

		JLabel lblNodes = new JLabel("Nodes");

		nodesField = new JFormattedTextField();
		nodesField.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				simulation.setNumberOfNodes(Integer.parseInt((String) nodesField.getValue()));
			}
		});

		JLabel lblMaximumFailureRate = new JLabel("Maximum Failure Rate");

		failureRateField = new JFormattedTextField();
		failureRateField.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				simulation.setMaxFailureRate(Integer.parseInt((String) failureRateField.getValue()));
			}
		});

		JLabel lblNumberOfRepeating = new JLabel("Repeating Tests");

		repeatingTestField = new JFormattedTextField();
		repeatingTestField.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				simulation.setRepeatingTestsNumber(Integer.parseInt((String) repeatingTestField.getValue()));
			}
		});

		JLabel lblHaloRedundancy = new JLabel("Halo Redundancy");

		haloRedundancyField = new JFormattedTextField();
		haloRedundancyField.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				simulation.setHaloRedundancy(Integer.parseInt((String) haloRedundancyField.getValue()));
			}
		});

		lblNumberOfLookups = new JLabel("Number of Lookups");

		lookupsField = new JFormattedTextField();
		lookupsField.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				simulation.setNumberOfLookups(Integer.parseInt((String) lookupsField.getValue()));
			}
		});

		lblRedsBucketSize = new JLabel("REDS Bucket Size");

		bucketSizeField = new JFormattedTextField();
		bucketSizeField.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				simulation.setBucketSize(Integer.parseInt((String) bucketSizeField.getValue()));
			}
		});

		JLabel lblProtocols = new JLabel("Protocols");

		chordCheckBox = new JCheckBox("Chord");
		chordCheckBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (chordCheckBox.isSelected())
					simulation.getRunningSimulations().add(ChordProtocol.Chord);
				else
					simulation.getRunningSimulations().remove(ChordProtocol.Chord);
			}
		});

		haloCheckBox = new JCheckBox("HALO");
		haloCheckBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (haloCheckBox.isSelected())
					simulation.getRunningSimulations().add(ChordProtocol.HALO);
				else
					simulation.getRunningSimulations().remove(ChordProtocol.HALO);
			}
		});

		redsCheckBox = new JCheckBox("REDS");
		redsCheckBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (redsCheckBox.isSelected())
					simulation.getRunningSimulations().add(ChordProtocol.REDS);
				else
					simulation.getRunningSimulations().remove(ChordProtocol.REDS);

			}
		});
		
		JLabel lblRedsBucketSize_1 = new JLabel("REDS Min Obsevations");
		
		minObservationsField = new JFormattedTextField();
		minObservationsField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				simulation.setRedsMinObservations(Integer.parseInt((String) minObservationsField.getValue()));
			}
		});
		
		JLabel reputationTreeDepthLabel = new JLabel("REDS ReputatioTree Depth");
		
		reputationTreeDepthField = new JFormattedTextField();
		reputationTreeDepthField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				simulation.setRedsReputationTreeDepth(Integer.parseInt((String) reputationTreeDepthField.getValue()));
			}
		});
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addGap(15)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
						.addComponent(lblNumberOfRepeating)
						.addComponent(lblNodes)
						.addComponent(lblNumberOfLookups)
						.addComponent(lblProtocols)
						.addComponent(lblRedsBucketSize_1))
					.addGap(7)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_1.createSequentialGroup()
							.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
								.addComponent(minObservationsField, GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
								.addComponent(lookupsField, GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
								.addComponent(nodesField, GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
								.addComponent(repeatingTestField, GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE))
							.addGap(38))
						.addGroup(gl_panel_1.createSequentialGroup()
							.addComponent(chordCheckBox)
							.addGap(95)))
					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
						.addComponent(lblMaximumFailureRate)
						.addComponent(lblHaloRedundancy)
						.addComponent(reputationTreeDepthLabel)
						.addComponent(lblRedsBucketSize)
						.addGroup(gl_panel_1.createSequentialGroup()
							.addGap(27)
							.addComponent(haloCheckBox)))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING, false)
						.addComponent(reputationTreeDepthField)
						.addGroup(gl_panel_1.createParallelGroup(Alignment.TRAILING)
							.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING, false)
								.addComponent(bucketSizeField)
								.addComponent(haloRedundancyField)
								.addComponent(failureRateField, GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE))
							.addComponent(redsCheckBox)))
					.addGap(17))
		);
		gl_panel_1.setVerticalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblMaximumFailureRate)
						.addComponent(failureRateField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNodes)
						.addComponent(nodesField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNumberOfRepeating)
						.addComponent(lblHaloRedundancy)
						.addComponent(haloRedundancyField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(repeatingTestField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNumberOfLookups)
						.addComponent(lookupsField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblRedsBucketSize)
						.addComponent(bucketSizeField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(13)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblRedsBucketSize_1)
						.addComponent(minObservationsField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(reputationTreeDepthLabel)
						.addComponent(reputationTreeDepthField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(18)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblProtocols)
						.addComponent(chordCheckBox)
						.addComponent(redsCheckBox)
						.addComponent(haloCheckBox))
					.addGap(20))
		);
		panel_1.setLayout(gl_panel_1);

		hashFunctionComboBox = new JComboBox<>();
		hashFunctionComboBox.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				if (e.getStateChange() == ItemEvent.SELECTED)
				{
					simulation.setHashFunction((String) hashFunctionComboBox.getSelectedItem());
				}
			}
		});
		hashFunctionComboBox.setModel(new DefaultComboBoxModel<String>(new String[] { "SHA-1", "CRC32", "Java" }));

		JLabel lblHashFunction = new JLabel("Hash Function");

		JLabel lblKeyLength = new JLabel("Key Length");

		keyLengthField = new JFormattedTextField();
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(23)
					.addComponent(lblHashFunction)
					.addGap(51)
					.addComponent(hashFunctionComboBox, GroupLayout.PREFERRED_SIZE, 121, GroupLayout.PREFERRED_SIZE)
					.addGap(42)
					.addComponent(lblKeyLength)
					.addPreferredGap(ComponentPlacement.RELATED, 107, Short.MAX_VALUE)
					.addComponent(keyLengthField, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)
					.addGap(19))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(5)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblHashFunction)
						.addComponent(keyLengthField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblKeyLength)
						.addComponent(hashFunctionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		panel.setLayout(gl_panel);
		contentPane.setLayout(gl_contentPane);
	}

	private void startSimulation() throws Exception
	{
		startSimulationButton.setEnabled(false);
		SwingWorker<Void, Void> simulationWorker = new SwingWorker<Void, Void>()
		{

			@Override
			protected Void doInBackground() throws Exception
			{
				simulation.simulate();
				return null;
			}

			@Override
			protected void done()
			{
				try
				{
					get();
				}
				catch (InterruptedException | ExecutionException e)
				{
					e.printStackTrace();
				}
				finally
				{
					startSimulationButton.setEnabled(true);
				}
			}

		};
		simulationWorker.execute();

		progressBar.setMinimum(0);
		progressBar.setMaximum(simulation.getMaxFailureRate() * simulation.getRepeatingTestsNumber()
				* simulation.getRunningSimulations().size());
		progressBar.setValue(0);
		SwingWorker<Void, Void> progressWorker = new SwingWorker<Void, Void>()
		{

			@Override
			protected Void doInBackground() throws Exception
			{
				int progress = simulation.getProgress();
				while (progress < simulation.getMaxFailureRate() * simulation.getRepeatingTestsNumber()
						* simulation.getRunningSimulations().size())
				{
					if (progress != simulation.getProgress())
					{
						progress = simulation.getProgress();
						publish();
					}
				}
				return null;
			}

			@Override
			protected void process(List<Void> chunks)
			{
				progressBar.setValue(simulation.getProgress());
			}

		};
		progressWorker.execute();

	}
}
