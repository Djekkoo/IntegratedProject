package application;
import javax.swing.ImageIcon;
import javax.swing.ListModel;

import java.awt.event.KeyEvent;

/** 
 * @author      Florian Fikkert <f.a.j.fikkert@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-07
 */
public class GUI extends javax.swing.JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Client client;
	
	/**
	 * Creates new form GUI
	 */
	public GUI(Client client) {
		this.client = client;
		this.setVisible(true);
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings({ "unchecked", "serial", "rawtypes" })
	private void initComponents() {
		//Eigen vars
		final ImageIcon plaatje = new ImageIcon(getClass().getResource("intproject.jpg")); // user added
		final java.awt.Font standardFont = new java.awt.Font("Comic Sans", 0, 14);
		//Generated vars
		jPasswordField1 = new javax.swing.JPasswordField();
		MainPanel = new javax.swing.JPanel();
		UserPanel = new javax.swing.JPanel();
		userScrollpane = new javax.swing.JScrollPane();
		userList = new javax.swing.JList<String>();
		ChatPanel = new javax.swing.JPanel();
		chatScrollpane = new javax.swing.JScrollPane();
		chatTextPane = new javax.swing.JTextPane();
		SendPanel = new javax.swing.JPanel();
		inputText = new javax.swing.JTextField();
		sendBtn = new javax.swing.JButton();
		Titel = new javax.swing.JLabel();
		backgroundLabel = new javax.swing.JLabel();

		jPasswordField1.setText("jPasswordField1");

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setMaximumSize(new java.awt.Dimension(800, 450));
		setMinimumSize(new java.awt.Dimension(815, 450));
		setPreferredSize(new java.awt.Dimension(800, 450));
		getContentPane().setLayout(null);

		MainPanel.setBorder(javax.swing.BorderFactory.createLineBorder(
				new java.awt.Color(50, 50, 50), 5));
		MainPanel.setOpaque(false);

		userList.setFont(standardFont); // NOI18N
		userList.setModel(new javax.swing.AbstractListModel() {
			String[] strings = { "Jacco", "Florian", "Joey", "Sander" };

			public int getSize() {
				return strings.length;
			}

			public Object getElementAt(int i) {
				return strings[i];
			}
		});
		userScrollpane.setViewportView(userList);

		javax.swing.GroupLayout UserPanelLayout = new javax.swing.GroupLayout(
				UserPanel);
		UserPanel.setLayout(UserPanelLayout);
		UserPanelLayout.setHorizontalGroup(UserPanelLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addComponent(
				userScrollpane, javax.swing.GroupLayout.DEFAULT_SIZE, 158,
				Short.MAX_VALUE));
		UserPanelLayout.setVerticalGroup(UserPanelLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addComponent(
				userScrollpane, javax.swing.GroupLayout.DEFAULT_SIZE, 328,
				Short.MAX_VALUE));

		chatScrollpane.setAutoscrolls(true);

		chatTextPane.setFont(standardFont); // NOI18N
		chatTextPane
				.setText("                                                               Chat Venster");
		chatScrollpane.setViewportView(chatTextPane);

		javax.swing.GroupLayout ChatPanelLayout = new javax.swing.GroupLayout(
				ChatPanel);
		ChatPanel.setLayout(ChatPanelLayout);
		ChatPanelLayout.setHorizontalGroup(ChatPanelLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addComponent(
				chatScrollpane));
		ChatPanelLayout.setVerticalGroup(ChatPanelLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addComponent(
				chatScrollpane));

		SendPanel.setOpaque(false);

		inputText.setFont(standardFont); // NOI18N
		inputText.setText("");
		inputText.setFocusable(true);
		inputText.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				inputTextActionPerformed(evt);
			}
		});
		inputText.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				SendPanelKeyPressed(evt);
			}
		});

		sendBtn.setFont(standardFont); // NOI18N
		sendBtn.setText("Send!");
		sendBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				sendBtnActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout SendPanelLayout = new javax.swing.GroupLayout(
				SendPanel);
		SendPanel.setLayout(SendPanelLayout);
		SendPanelLayout
				.setHorizontalGroup(SendPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								SendPanelLayout
										.createSequentialGroup()
										.addComponent(
												inputText,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												486,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												sendBtn,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												90, Short.MAX_VALUE)));
		SendPanelLayout.setVerticalGroup(SendPanelLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				SendPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.BASELINE)
						.addComponent(inputText,
								javax.swing.GroupLayout.PREFERRED_SIZE, 31,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addComponent(sendBtn,
								javax.swing.GroupLayout.PREFERRED_SIZE, 31,
								javax.swing.GroupLayout.PREFERRED_SIZE)));

		javax.swing.GroupLayout MainPanelLayout = new javax.swing.GroupLayout(
				MainPanel);
		MainPanel.setLayout(MainPanelLayout);
		MainPanelLayout
				.setHorizontalGroup(MainPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								MainPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(
												UserPanel,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addGroup(
												MainPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																SendPanel,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																ChatPanel,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE))
										.addContainerGap()));
		MainPanelLayout
				.setVerticalGroup(MainPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								MainPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												MainPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.TRAILING)
														.addComponent(
																UserPanel,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addGroup(
																MainPanelLayout
																		.createSequentialGroup()
																		.addComponent(
																				ChatPanel,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				Short.MAX_VALUE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				SendPanel,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.PREFERRED_SIZE)))
										.addContainerGap()));

		getContentPane().add(MainPanel);
		MainPanel.setBounds(10, 40, 780, 360);

		Titel.setFont(new java.awt.Font("Comic Sans", 1, 26)); // NOI18N
		Titel.setForeground(new java.awt.Color(255,255,230));
		Titel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		Titel.setText("Ad-Hoc Wifi Chat");
		getContentPane().add(Titel);
		Titel.setBounds(0, 0, 800, 40);

		backgroundLabel.setIcon(plaatje); // useradded
		getContentPane().add(backgroundLabel);
		backgroundLabel.setBounds(0, -40, 920, 530);
		inputText.requestFocusInWindow();
		pack();
	}// </editor-fold>

	private void inputTextActionPerformed(java.awt.event.ActionEvent evt) {
		// TODO add your handling code here:
	}

	private void sendBtnActionPerformed(java.awt.event.ActionEvent evt) {
		sendChat(inputText.getText());
	}

	private void SendPanelKeyPressed(java.awt.event.KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
			sendChat(inputText.getText());
		}
	}
	
	public void updateUserlist(String[] users) {
		userList.setListData(users);
	}
	
	private void sendChat(String text) {
		inputText.setText("");
		client.sendChat(text);
	}
	
	public void updateChat(String lastmsg) {
		String[] chatArr = chatTextPane.getText().split("\n");
		String result = "";
		for (int i=0;i<chatArr.length;i++) {
			result = result + chatArr[i] + '\n';
		}
		chatTextPane.setText(result + lastmsg);
	}
	
	public String[] getChat() {
		return chatTextPane.getText().split("\n");
	}

	public String getUsers() {
		String plainusers = "";
		ListModel<String> list = userList.getModel();
		for(int i = 0; i < list.getSize(); i++){
		    plainusers = plainusers + " " + (list.getElementAt(i));
		}
		return plainusers;
	}
	
	// Variables declaration - do not modify
	private javax.swing.JPanel ChatPanel;
	private javax.swing.JPanel MainPanel;
	private javax.swing.JPanel SendPanel;
	private javax.swing.JLabel Titel;
	private javax.swing.JPanel UserPanel;
	private javax.swing.JLabel backgroundLabel;
	private javax.swing.JScrollPane chatScrollpane;
	private javax.swing.JTextPane chatTextPane;
	private javax.swing.JTextField inputText;
	private javax.swing.JPasswordField jPasswordField1;
	@SuppressWarnings("unused")
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JButton sendBtn;
	private javax.swing.JList<String> userList;
	private javax.swing.JScrollPane userScrollpane;
	// End of variables declaration
}
