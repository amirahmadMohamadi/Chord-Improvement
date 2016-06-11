package kmaru.jchord.simulation;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import kmaru.jchord.simulation.SimulationSettingsFrame.SimulationLogHandler;

public class LogDialog extends JDialog
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 7444301982714179575L;
	private final JPanel		contentPanel		= new JPanel();
	private JTable				table;
	public SimulationLogHandler	buffer;

	/**
	 * Create the dialog.
	 */
	public LogDialog(SimulationLogHandler handler)
	{
		initComponents();
		this.buffer = handler;
		initTable();
		handler.addListener(table);
	}

	private void initTable()
	{
		table.setModel(new LogTableModel());
		table.getColumnModel().getColumn(0).setMaxWidth(90);
		table.getColumnModel().getColumn(0).setMinWidth(90);
		table.getColumnModel().getColumn(0).setPreferredWidth(90);
	}

	private void initComponents()
	{
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		JScrollPane scrollPane = new JScrollPane();
		contentPanel.add(scrollPane, BorderLayout.CENTER);
		table = new JTable();
		scrollPane.setViewportView(table);
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		JButton cancelButton = new JButton("Clear");
		cancelButton.setActionCommand("Clear");
		cancelButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				buffer.flush();
			}
		});
		buttonPane.add(cancelButton);

	}

	class LogTableModel extends DefaultTableModel
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = -2485772723106209567L;

		@Override
		public int getRowCount()
		{
			return buffer.getBuffer().size();
		}

		@Override
		public boolean isCellEditable(int row, int column)
		{
			return false;
		}

		@Override
		public Object getValueAt(int row, int column)
		{
			switch (column)
			{
			case 0:
				return row + 1;
			case 1:
				return buffer.getBuffer().get(row);
			}
			return null;
		}

		@Override
		public int getColumnCount()
		{
			return 2;
		}

		@Override
		public String getColumnName(int column)
		{
			switch (column)
			{
			case 0:
				return "Record Number";
			case 1:
				return "Log";
			}
			return null;
		}

	}

}
