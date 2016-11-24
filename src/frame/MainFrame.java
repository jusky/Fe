package frame;

import callPic.GetData;
import com.github.lgooddatepicker.components.*;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import model.*;
import tool.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tool.HTTPServer.addContentTypes;

/**
 * Created by yellowsea on 2016/7/16.
 */
public class MainFrame extends JFrame {
    private static final int WIDTH = 1560;
    private static final int HEIGHT = 1000;
    private static final int PORT = 1234;
    boolean flag = false;
    JPanel testPanel = new JPanel(new BorderLayout());
    JPanel infoPanel = new JPanel(new BorderLayout());
    JPanel menuPanel = new JPanel();
    final JTabbedPane tabbedPane = new JTabbedPane();
    final JTabbedPane tabbedPane2 = new JTabbedPane();
    ButtonGroup buttonGroup = new ButtonGroup();
    ImageIcon smsIcon = new ImageIcon("images/sms.jpg");
    ImageIcon wxIcon = new ImageIcon("images/weixin.jpg");
    ImageIcon qqIcon = new ImageIcon("images/qq.jpg");
    ImageIcon weiboIcon = new ImageIcon("images/weibo.jpg");
    ImageIcon mailIcon = new ImageIcon("images/mail.jpg");
    ImageIcon taobaoIcon = new ImageIcon("images/taobao.jpg");
    ImageIcon jdIcon = new ImageIcon("images/jd.jpg");
    ImageIcon ucIcon = new ImageIcon("images/ucbrowser.jpg");
    ImageIcon qqBrowserIcon = new ImageIcon("images/qqbrowser.jpg");
    ImageIcon xiechengIcon = new ImageIcon("images/xiecheng.jpg");
    ImageIcon callIcon = new ImageIcon("images/call.jpg");
    Logger logger = Logger.getLogger("MainFrame");
    HTTPServer server = null;
    String dbPath = null;
    List<SMS> smsList = new ArrayList<>();

    public MainFrame() {
        setFocusCycleRoot(true);
        setLayout(new BorderLayout(-10, 0));
        setTitle("取证精灵");
        setBounds(0, 30, 1400, 700);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        testPanel.setBackground(new Color(75, 10, 92));
        JLabel bgLabel = new JLabel();
        Icon backIcon = new ImageIcon("images/background.png");
        bgLabel.setIcon(backIcon);
        testPanel.add(bgLabel);
        add(testPanel);

        // 图片按钮菜单
        menuPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        menuPanel.setBackground(new Color(223, 223, 223));
        addButton("home", new HomeListener());
        addButton("import", new FileItemListener());
        addButton("browse", new BrowseListener());
        addButton("search", new SearchListener());
        addButton("recovery", new RecoveryListener());
        addButton("analyse", new AnalyseListener());
        addButton("map", new MapListener());
        addButton("reproduce", new ReproduceListener());
        addButton("evidence", null);
        addButton("report", null);
        addButton("help", null);
        add(menuPanel, BorderLayout.NORTH);
        for (int i = 2; i < 8; i++) {
            menuPanel.getComponent(i).setEnabled(false);
        }
        ((JRadioButton) menuPanel.getComponent(0)).doClick();

        // 开启服务器
        try {
            File dir = new File("WebRoot");
            if (!dir.canRead())
                throw new FileNotFoundException(dir.getAbsolutePath());
            // set up server
            for (File f : Arrays.asList(new File("/etc/mime.types"), new File(dir, ".mime.types")))
                if (f.exists())
                    addContentTypes(f);
            server = new HTTPServer(PORT);
            HTTPServer.VirtualHost host = server.getVirtualHost(null); // default host
            host.setAllowGeneratedIndex(true); // with directory index pages
            host.addContext("/", new HTTPServer.FileContextHandler(dir, "/"));
            host.addContext("/api/time", new HTTPServer.ContextHandler() {
                public int serve(HTTPServer.Request req, HTTPServer.Response resp) throws IOException {
                    long now = System.currentTimeMillis();
                    resp.getHeaders().add("Content-Type", "text/plain");
                    resp.send(200, String.format("%tF %<tT", now));
                    return 0;
                }
            });
            server.start();
            logger.info("HTTPServer正在监听端口" + PORT);

        } catch (Exception e) {
            logger.severe("error: " + e);
        }

        // 关闭窗口时关闭服务器
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                server.stop();
                System.exit(0);
            }
        });
    }

    private void addButton(String iconName, ActionListener listener) {
        JRadioButton button = new JRadioButton();
        button.setContentAreaFilled(false);
        button.setIcon(new ImageIcon("images/" + iconName + "_unselected.png"));
        button.setRolloverIcon(new ImageIcon("images/" + iconName + "_hover.png"));
        button.setSelectedIcon(new ImageIcon("images/" + iconName + "_selected.png"));
        button.setDisabledIcon(new ImageIcon("images/" + iconName + "_disabled.png"));
        button.addActionListener(listener);
        buttonGroup.add(button);
        menuPanel.add(button);
    }

    private class HomeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            logger.info("回到首页");
            testPanel.removeAll();
            flag = false;
            testPanel.setBackground(new Color(75, 10, 92));
            JLabel bgLabel = new JLabel(new ImageIcon("images/background.png"));
            testPanel.add(bgLabel);
            testPanel.revalidate();
            testPanel.repaint();
        }
    }

    private class FileItemListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            logger.info("文件导入");
            flag = true;
            ImportFrame importFrame = new ImportFrame(MainFrame.this);
            importFrame.setVisible(true);
        }
    }

    private class BrowseListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            logger.info("数据浏览");
            testPanel.removeAll();
            JPanel choosePanel = new JPanel();
            JLabel label = new JLabel("请选择手机：");
            choosePanel.add(label);
            Vector<String> phones = new Vector<>();
            phones.add("全部");
            ArrayList<String> phonePath = new ArrayList<>();
            Connection connection = null;
            try {
                Class.forName("org.sqlite.JDBC");
                logger.info("dbPath:" + dbPath);
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery("SELECT id, owner_name, phone_number, default_path FROM phones");
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String ownerName = rs.getString(2);
                    String phoneNumber = rs.getString(3);
                    String path = rs.getString(4);
                    phonePath.add(path);
                    phones.add("手机" + id + " " + ownerName + " " + phoneNumber);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            JComboBox phoneCombo = new JComboBox(phones);
            choosePanel.add(phoneCombo);
            phoneCombo.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    try {
                        int index = phoneCombo.getSelectedIndex();
                        if (index != 0) {
                            logger.info(phonePath.get(index - 1));
                            logger.info(CallsFinder.findCalls(phonePath.get(index - 1) + "/contacts2.db").isEmpty() + "");
                            setData(SMSFinder.findSMS(phonePath.get(index - 1) + "/mmssms.db"),
                                    CallsFinder.findCalls(phonePath.get(index - 1) + "/contacts2.db"),
                                    EmailFinder.findEmail(phonePath.get(index - 1) + "/EmailProvider.db"),
                                    UCBrowserFinder.findUCBrowser(phonePath.get(index - 1) + "/history.db"),
                                    QQBrowserFinder.findQQBrowser(phonePath.get(index - 1) + "/database.db"),
                                    HotelFinder.findHotel(phonePath.get(index - 1) + "/ctrip_hotel.db"),
                                    FlightFinder.findFlight(phonePath.get(index - 1) + "/ctrip_flight.db"),
                                    JDBrowseHistoryFinder.findJDBroseHistory(phonePath.get(index - 1) + "/jd.db"),
                                    JDSearchHistoryFinder.findJDSearchHistory(phonePath.get(index - 1) + "/jd.db")
                            );
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            phoneCombo.setSelectedIndex(phones.size() - 1);
            tabbedPane.setSelectedIndex(0);
            testPanel.add(choosePanel, BorderLayout.NORTH);
            testPanel.revalidate();
            testPanel.repaint();
        }
    }

    private class SearchListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            logger.info("数据查询");
            testPanel.removeAll();
            JPanel searchPanel = new JPanel();
            JPanel detailPanel = new JPanel();

            // JScrollPane scrollPane = new JScrollPane();
            GridBagLayout layout = new GridBagLayout();
            searchPanel.setLayout(layout);
            detailPanel.setLayout(layout);
            JLabel hintLabel = new JLabel("请选择查询数据类型：");
            searchPanel.add(hintLabel);

            JButton smsButton = new JButton("短信");
            //smsButton.setIcon(smsIcon);
            searchPanel.add(smsButton);
            smsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    detailPanel.removeAll();

                    JCheckBox contactCheckBox = new JCheckBox();
                    JLabel contactLabel = new JLabel("联系人：    ");
                    JTextField contactTextField = new JTextField(10);
                    detailPanel.add(contactCheckBox);
                    detailPanel.add(contactLabel);
                    detailPanel.add(contactTextField);

                    JCheckBox keywordCheckBox = new JCheckBox();
                    JLabel keywordLabel = new JLabel("关键字：    ");
                    JTextField keywordTextField = new JTextField(10);
                    detailPanel.add(keywordCheckBox);
                    detailPanel.add(keywordLabel);
                    detailPanel.add(keywordTextField);

                    JCheckBox startDateCheckBox = new JCheckBox();
                    JLabel startDateLabel = new JLabel("起始日期    ");
                    DatePickerSettings datePickerSettings = new DatePickerSettings();
                    datePickerSettings.setFormatForDatesCommonEra("yyyy-MM-dd");
                    DatePicker startDatePicker = new DatePicker(datePickerSettings);
                    detailPanel.add(startDateCheckBox);
                    detailPanel.add(startDateLabel);
                    detailPanel.add(startDatePicker);

                    JCheckBox endDateCheckBox = new JCheckBox();
                    JLabel endDateLabel = new JLabel("截止日期    ");
                    DatePicker endDatePicker = new DatePicker(datePickerSettings.copySettings());
                    detailPanel.add(endDateCheckBox);
                    detailPanel.add(endDateLabel);
                    detailPanel.add(endDatePicker);

                    JCheckBox startTimeCheckBox = new JCheckBox();
                    JLabel startTimeLabel = new JLabel("开始时间    ");
                    TimePickerSettings settings = new TimePickerSettings();
                    settings.setDisplaySpinnerButtons(true);
                    settings.setFormatForDisplayTime("HH:mm");
                    TimePicker startTimePicker = new TimePicker(settings);
                    detailPanel.add(startTimeCheckBox);
                    detailPanel.add(startTimeLabel);
                    detailPanel.add(startTimePicker);

                    JCheckBox endTimeCheckBox = new JCheckBox();
                    JLabel endTimeLabel = new JLabel("截止时间    ");
                    TimePicker endTimePicker = new TimePicker(settings);
                    detailPanel.add(endTimeCheckBox);
                    detailPanel.add(endTimeLabel);
                    detailPanel.add(endTimePicker);

                    JButton searchButton = new JButton("搜索");
                    detailPanel.add(searchButton);

                    GridBagConstraints constraints = new GridBagConstraints();
                    constraints.weightx = 1;
                    constraints.gridwidth = 0;
                    constraints.fill = GridBagConstraints.HORIZONTAL;
                    layout.setConstraints(contactTextField, constraints);
                    layout.setConstraints(keywordTextField, constraints);
                    layout.setConstraints(startDatePicker, constraints);
                    layout.setConstraints(endDatePicker, constraints);
                    layout.setConstraints(startTimePicker, constraints);
                    layout.setConstraints(endTimePicker, constraints);
                    layout.setConstraints(searchButton, constraints);

                    searchButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            System.out.println("Start search");
                            String[] columnNames = {"手机", "联系人", "内容", "时间", "类型"};
                            String[][] data = null;
                            DefaultTableModel model = new DefaultTableModel(data, columnNames) {
                                @Override
                                public boolean isCellEditable(int row, int column) {
                                    return false;
                                }
                            };
                            JTable table = new JTable(model);
                            StringBuffer sql = new StringBuffer("SELECT '手机' || messages.phone_id || \" \" || phones.phone_number AS phone," +
                                    "messages.number || ' ' || ifnull(contacts.name, '') AS num," +
                                    "messages.body AS body, messages.date AS date, messages.status AS type" +
                                    "  FROM messages LEFT OUTER JOIN contacts" +
                                    " ON messages.number = contacts.number AND messages.phone_id = contacts.phone_id, phones" +
                                    " WHERE phones.id = messages.phone_id ");
                            if (!contactTextField.getText().isEmpty() && contactCheckBox.isSelected()) {
                                sql.append("AND num LIKE '%" + contactTextField.getText() + "%'");
                            }
                            if (!keywordTextField.getText().isEmpty() && keywordCheckBox.isSelected()) {
                                sql.append(" AND body LIKE '%" + keywordTextField.getText() + "%'");
                            }
                            if (!startDatePicker.getText().isEmpty() && startDateCheckBox.isSelected()) {
                                sql.append(" AND date >= '" + startDatePicker.getText() + " 00:00:00'");
                            }
                            if (!endDatePicker.getText().isEmpty() && endDateCheckBox.isSelected()) {
                                sql.append(" AND date <= '" + endDatePicker.getText() + " 23:59:59'");
                            }
                            sql.append(";");
                            Connection connection = null;
                            try {
                                connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                                Statement statement = connection.createStatement();

                                ResultSet rs = statement.executeQuery(sql.toString());
                                while (rs.next()) {
                                    String[] row = new String[5];
                                    for (int i = 0; i < 4; i++)
                                        row[i] = rs.getString(i + 1);
                                    switch (rs.getInt(5)) {
                                        case 1:
                                            row[4] = "接到";
                                            break;
                                        case 2:
                                            row[4] = "发出";
                                            break;
                                        default:
                                            row[4] = "";
                                    }
                                    model.addRow(row);
                                    System.out.println(Arrays.toString(row));
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            testPanel.removeAll();
                            testPanel.add(searchPanel, BorderLayout.WEST);
                            JScrollPane scrollPane = new JScrollPane(table);
                            testPanel.add(scrollPane);
                            testPanel.revalidate();
                            testPanel.repaint();
                        }
                    });

                    detailPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "短信查询"));
                    detailPanel.revalidate();
                    detailPanel.repaint();
                    searchButton.doClick();
                }
            });


            JButton callButton = new JButton("通话记录");
            //callButton.setIcon(callIcon);
            searchPanel.add(callButton);
            callButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    detailPanel.removeAll();

                    JCheckBox contactCheckBox = new JCheckBox();
                    JLabel contactLabel = new JLabel("联系人：");
                    JTextField contactTextField = new JTextField(10);
                    detailPanel.add(contactCheckBox);
                    detailPanel.add(contactLabel);
                    detailPanel.add(contactTextField);

                    JCheckBox startDateCheckBox = new JCheckBox();
                    JLabel startDateLabel = new JLabel("起始日期");
                    DatePickerSettings datePickerSettings = new DatePickerSettings();
                    datePickerSettings.setFormatForDatesCommonEra("yyyy-MM-dd");
                    DatePicker startDatePicker = new DatePicker(datePickerSettings);
                    detailPanel.add(startDateCheckBox);
                    detailPanel.add(startDateLabel);
                    detailPanel.add(startDatePicker);

                    JCheckBox endDateCheckBox = new JCheckBox();
                    JLabel endDateLabel = new JLabel("截止日期");
                    DatePicker endDatePicker = new DatePicker(datePickerSettings.copySettings());
                    detailPanel.add(endDateCheckBox);
                    detailPanel.add(endDateLabel);
                    detailPanel.add(endDatePicker);

                    JCheckBox startTimeCheckBox = new JCheckBox();
                    JLabel startTimeLabel = new JLabel("开始时间");
                    TimePickerSettings settings = new TimePickerSettings();
                    settings.setDisplaySpinnerButtons(true);
                    settings.setFormatForDisplayTime("HH:mm");
                    TimePicker startTimePicker = new TimePicker(settings);
                    detailPanel.add(startTimeCheckBox);
                    detailPanel.add(startTimeLabel);
                    detailPanel.add(startTimePicker);

                    JCheckBox endTimeCheckBox = new JCheckBox();
                    JLabel endTimeLabel = new JLabel("截止时间");
                    TimePicker endTimePicker = new TimePicker(settings);
                    detailPanel.add(endTimeCheckBox);
                    detailPanel.add(endTimeLabel);
                    detailPanel.add(endTimePicker);

                    JButton searchButton = new JButton("搜索");
                    detailPanel.add(searchButton);

                    GridBagConstraints constraints = new GridBagConstraints();
                    constraints.weightx = 1;
                    constraints.gridwidth = 0;
                    constraints.fill = GridBagConstraints.HORIZONTAL;
                    layout.setConstraints(contactTextField, constraints);
                    layout.setConstraints(startDatePicker, constraints);
                    layout.setConstraints(endDatePicker, constraints);
                    layout.setConstraints(startTimePicker, constraints);
                    layout.setConstraints(endTimePicker, constraints);
                    layout.setConstraints(searchButton, constraints);

                    searchButton.removeAll();
                    searchButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            System.out.println("Start search");
                            String[] columnNames = {"手机", "联系人", "时间", "通话长度", "类型"};
                            String[][] data = null;
                            DefaultTableModel model = new DefaultTableModel(data, columnNames) {
                                @Override
                                public boolean isCellEditable(int row, int column) {
                                    return false;
                                }
                            };
                            JTable table = new JTable(model);
                            StringBuffer sql = new StringBuffer("SELECT '手机' || calls.phone_id || \" \" || phones.phone_number AS phone," +
                                    "calls.number || ' ' || ifnull(contacts.name, '') AS num," +
                                    "calls.date AS date, calls.duration AS duration, calls.type AS type" +
                                    "  FROM calls LEFT OUTER JOIN contacts" +
                                    " ON calls.number = contacts.number AND calls.phone_id = contacts.phone_id, phones" +
                                    " WHERE phones.id = calls.phone_id ");
                            if (!contactTextField.getText().isEmpty() && contactCheckBox.isSelected()) {
                                sql.append("AND num LIKE '%" + contactTextField.getText() + "%'");
                            }
                            if (!startDatePicker.getText().isEmpty() && startDateCheckBox.isSelected()) {
                                sql.append(" AND date >= '" + startDatePicker.getText() + " 00:00:00'");
                            }
                            if (!endDatePicker.getText().isEmpty() && endDateCheckBox.isSelected()) {
                                sql.append(" AND date <= '" + endDatePicker.getText() + " 23:59:59'");
                            }
                            sql.append(";");
                            Connection connection = null;
                            try {
                                connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                                Statement statement = connection.createStatement();

                                ResultSet rs = statement.executeQuery(sql.toString());
                                while (rs.next()) {
                                    String[] row = new String[5];
                                    for (int i = 0; i < 4; i++)
                                        row[i] = rs.getString(i + 1);
                                    switch (rs.getInt(5)) {
                                        case 1:
                                            row[4] = "拨出";
                                            break;
                                        case 2:
                                            row[4] = "接听";
                                            break;
                                        case 3:
                                            row[4] = "未接";
                                            break;
                                        default:
                                            row[4] = "";
                                    }
                                    model.addRow(row);
                                    System.out.println(Arrays.toString(row));
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            testPanel.removeAll();
                            testPanel.add(searchPanel, BorderLayout.WEST);
                            JScrollPane scrollPane = new JScrollPane(table);
                            testPanel.add(scrollPane);
                            testPanel.revalidate();
                            testPanel.repaint();
                        }
                    });
                    detailPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "通话记录查询"));
                    detailPanel.revalidate();
                    detailPanel.repaint();
                }
            });

            JButton ucButton = new JButton("UC浏览器记录");
            //ucButton.setIcon(ucIcon);
            searchPanel.add(ucButton);
            ucButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    detailPanel.removeAll();
                    JCheckBox websiteCheckBox = new JCheckBox();
                    JLabel websiteLabel = new JLabel("网址：  ");
                    JTextField websiteTextField = new JTextField(10);
                    detailPanel.add(websiteCheckBox);
                    detailPanel.add(websiteLabel);
                    detailPanel.add(websiteTextField);

                    JCheckBox titleCheckBox = new JCheckBox();
                    JLabel titleLabel = new JLabel("网站标题");
                    JTextField titleTextField = new JTextField(10);
                    detailPanel.add(titleCheckBox);
                    detailPanel.add(titleLabel);
                    detailPanel.add(titleTextField);
                    
                    JCheckBox startDateCheckBox = new JCheckBox();
                    JLabel startDateLabel = new JLabel("起始日期");
                    DatePickerSettings datePickerSettings = new DatePickerSettings();
                    datePickerSettings.setFormatForDatesCommonEra("yyyy-MM-dd");
                    DatePicker startDatePicker = new DatePicker(datePickerSettings);
                    detailPanel.add(startDateCheckBox);
                    detailPanel.add(startDateLabel);
                    detailPanel.add(startDatePicker);

                    JCheckBox endDateCheckBox = new JCheckBox();
                    JLabel endDateLabel = new JLabel("截止日期");
                    DatePicker endDatePicker = new DatePicker(datePickerSettings.copySettings());
                    detailPanel.add(endDateCheckBox);
                    detailPanel.add(endDateLabel);
                    detailPanel.add(endDatePicker);

                    JButton searchButton = new JButton("搜索");
                    detailPanel.add(searchButton);

                    GridBagConstraints constraints = new GridBagConstraints();
                    constraints.weightx = 1;
                    constraints.gridwidth = 0;
                    constraints.fill = GridBagConstraints.HORIZONTAL;
                    layout.setConstraints(websiteTextField, constraints);
                    layout.setConstraints(titleTextField, constraints);
                    layout.setConstraints(startDatePicker, constraints);
                    layout.setConstraints(endDatePicker, constraints);
                    layout.setConstraints(searchButton, constraints);
                    
                    searchButton.removeAll();
                    searchButton.addActionListener(new ActionListener(){
                    	public void actionPerformed(ActionEvent e){
                    		System.out.println("Start search");
                    		String[] columnNames = {"手机", "网站标题", "URL", "时间"};
                    		String[][] data = null;
                    		DefaultTableModel model = new DefaultTableModel(data, columnNames){
                    			@Override
                    			public boolean isCellEditable(int row, int column) {
                    				return false;
                    			}
                    		};
                    		JTable table = new JTable(model);
                    		StringBuffer sql = new StringBuffer("SELECT '手机' || ucbrowser.phone_id || \" \" || phones.phone_number AS phone, " + 
                    				"ucbrowser.name AS name, " + "ucbrowser.url AS url, " + "ucbrowser.time AS time " +
                    			    "FROM ucbrowser LEFT OUTER JOIN phones " +
                    				" ON ucbrowser.phone_id = phones.id ");
                    		if (!websiteTextField.getText().isEmpty() && websiteCheckBox.isSelected()){
                    			sql.append(" WHERE url LIKE '%" + websiteTextField.getText() + "%'");
                    		}
                    		if (!titleTextField.getText().isEmpty() && titleCheckBox.isSelected()){
                    			sql.append(" WHERE name LIKE '%" + titleTextField.getText() + "%'");
                    		}
                    		if (!startDatePicker.getText().isEmpty() && startDateCheckBox.isSelected()) {
                                sql.append(" WHERE date >= '" + startDatePicker.getText() + " 00:00:00'");
                            }
                            if (!endDatePicker.getText().isEmpty() && endDateCheckBox.isSelected()) {
                                sql.append(" WHERE date <= '" + endDatePicker.getText() + " 23:59:59'");
                            }
                            sql.append(";");
                            Connection connection = null;
                            try {
                            	connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                            	Statement statement = connection.createStatement();
                            	ResultSet rs = statement.executeQuery(sql.toString());
                            	while(rs.next()){
                            		String[] row = new String[4];
                            		for(int i = 0; i < 4; i++)
                            			row[i] = rs.getString(i + 1);
                            		model.addRow(row);
                            		System.out.println(Arrays.toString(row));
                            	}
                            }catch(Exception ex){
                            	ex.printStackTrace();
                            }
                            testPanel.removeAll();
                            testPanel.add(searchPanel, BorderLayout.WEST);
                            JScrollPane scrollPane = new JScrollPane(table);
                            testPanel.add(scrollPane);
                            testPanel.revalidate();
                            testPanel.repaint();
                    	}
                    		
                    });
                    detailPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "搜索UC浏览器记录"));
                    detailPanel.revalidate();
                    detailPanel.repaint();
                }
            });

            JButton qbButton = new JButton("QQ浏览器记录");
            searchPanel.add(qbButton);
            qbButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    detailPanel.removeAll();
                    JCheckBox websiteCheckBox = new JCheckBox();
                    JLabel websiteLabel = new JLabel("网址：  ");
                    JTextField websiteTextField = new JTextField(10);
                    detailPanel.add(websiteCheckBox);
                    detailPanel.add(websiteLabel);
                    detailPanel.add(websiteTextField);

                    JCheckBox titleCheckBox = new JCheckBox();
                    JLabel titleLabel = new JLabel("网站标题");
                    JTextField titleTextField = new JTextField(10);
                    detailPanel.add(titleCheckBox);
                    detailPanel.add(titleLabel);
                    detailPanel.add(titleTextField);
                    
                    JCheckBox startDateCheckBox = new JCheckBox();
                    JLabel startDateLabel = new JLabel("起始日期");
                    DatePickerSettings datePickerSettings = new DatePickerSettings();
                    datePickerSettings.setFormatForDatesCommonEra("yyyy-MM-dd");
                    DatePicker startDatePicker = new DatePicker(datePickerSettings);
                    detailPanel.add(startDateCheckBox);
                    detailPanel.add(startDateLabel);
                    detailPanel.add(startDatePicker);

                    JCheckBox endDateCheckBox = new JCheckBox();
                    JLabel endDateLabel = new JLabel("截止日期");
                    DatePicker endDatePicker = new DatePicker(datePickerSettings.copySettings());
                    detailPanel.add(endDateCheckBox);
                    detailPanel.add(endDateLabel);
                    detailPanel.add(endDatePicker);

                    JButton searchButton = new JButton("搜索");
                    detailPanel.add(searchButton);

                    GridBagConstraints constraints = new GridBagConstraints();
                    constraints.weightx = 1;
                    constraints.gridwidth = 0;
                    constraints.fill = GridBagConstraints.HORIZONTAL;
                    layout.setConstraints(websiteTextField, constraints);
                    layout.setConstraints(titleTextField, constraints);
                    layout.setConstraints(startDatePicker, constraints);
                    layout.setConstraints(endDatePicker, constraints);
                    layout.setConstraints(searchButton, constraints);
                    
                    searchButton.removeAll();
                    searchButton.addActionListener(new ActionListener(){
                    	public void actionPerformed(ActionEvent e){
                    		System.out.println("Start search");
                    		String[] columnNames = {"手机", "网站标题", "URL", "时间"};
                    		String[][] data = null;
                    		DefaultTableModel model = new DefaultTableModel(data, columnNames){
                    			@Override
                    			public boolean isCellEditable(int row, int column) {
                    				return false;
                    			}
                    		};
                    		JTable table = new JTable(model);
                    		StringBuffer sql = new StringBuffer("SELECT '手机' || qqbrowser.phone_id || \" \" || phones.phone_number AS phone," + 
                    				"qqbrowser.name AS name," + "qqbrowser.url AS url," + "qqbrowser.time AS time " +
                    			    "FROM qqbrowser LEFT OUTER JOIN phones " +
                    				" ON qqbrowser.phone_id = phones.id ");
                    		if (!websiteTextField.getText().isEmpty() && websiteCheckBox.isSelected()){
                    			sql.append(" WHERE url LIKE '%" + websiteTextField.getText() + "%'");
                    		}
                    		if (!titleTextField.getText().isEmpty() && titleCheckBox.isSelected()){
                    			sql.append(" WHERE name LIKE '%" + titleTextField.getText() + "%'");
                    		}
                    		if (!startDatePicker.getText().isEmpty() && startDateCheckBox.isSelected()) {
                                sql.append(" WHERE date >= '" + startDatePicker.getText() + " 00:00:00'");
                            }
                            if (!endDatePicker.getText().isEmpty() && endDateCheckBox.isSelected()) {
                                sql.append(" WHERE date <= '" + endDatePicker.getText() + " 23:59:59'");
                            }
                            sql.append(";");
                            Connection connection = null;
                            try {
                            	connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                            	Statement statement = connection.createStatement();
                            	ResultSet rs = statement.executeQuery(sql.toString());
                            	while(rs.next()){
                            		String[] row = new String[4];
                            		for(int i = 0; i < 4; i++)
                            			row[i] = rs.getString(i + 1);
                            		model.addRow(row);
                            		System.out.println(Arrays.toString(row));
                            	}
                            }catch(Exception ex){
                            	ex.printStackTrace();
                            }
                            testPanel.removeAll();
                            testPanel.add(searchPanel, BorderLayout.WEST);
                            JScrollPane scrollPane = new JScrollPane(table);
                            testPanel.add(scrollPane);
                            testPanel.revalidate();
                            testPanel.repaint();
                    	}
                    		
                    });
                    
                    detailPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "搜索QQ浏览器记录"));
                    detailPanel.revalidate();
                    detailPanel.repaint();
                }
            });

            JButton mailButton = new JButton("邮件记录");
            //mailButton.setIcon(mailIcon);
            searchPanel.add(mailButton);
            mailButton.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent e){
            		detailPanel.removeAll();
            		
            		JCheckBox subjectCheckBox = new JCheckBox();
            		JLabel subjectLabel = new  JLabel("邮件标题    ");
            		JTextField subjectTextField = new JTextField(10);
            		detailPanel.add(subjectCheckBox);
            		detailPanel.add(subjectLabel);
            		detailPanel.add(subjectTextField);
            		
                    JCheckBox mailCheckBox = new JCheckBox();
                    JLabel mailLabel = new JLabel("发件人：    ");
                    JTextField mailTextField = new JTextField(10);
                    detailPanel.add(mailCheckBox);
                    detailPanel.add(mailLabel);
                    detailPanel.add(mailTextField);
                    
                    JCheckBox mailCheckBox2 = new JCheckBox();
                    JLabel mailLabel2 = new JLabel("收件人：    ");
                    JTextField mailTextField2 = new JTextField(10);
                    detailPanel.add(mailCheckBox2);
                    detailPanel.add(mailLabel2);
                    detailPanel.add(mailTextField2);

                    JCheckBox startDateCheckBox = new JCheckBox();
                    JLabel startDateLabel = new JLabel("起始日期    ");
                    DatePickerSettings datePickerSettings = new DatePickerSettings();
                    datePickerSettings.setFormatForDatesCommonEra("yyyy-MM-dd");
                    DatePicker startDatePicker = new DatePicker(datePickerSettings);
                    detailPanel.add(startDateCheckBox);
                    detailPanel.add(startDateLabel);
                    detailPanel.add(startDatePicker);

                    JCheckBox endDateCheckBox = new JCheckBox();
                    JLabel endDateLabel = new JLabel("截止日期    ");
                    DatePicker endDatePicker = new DatePicker(datePickerSettings.copySettings());
                    detailPanel.add(endDateCheckBox);
                    detailPanel.add(endDateLabel);
                    detailPanel.add(endDatePicker);
                    
                    JButton searchButton = new JButton("搜索");
                    detailPanel.add(searchButton);

                    GridBagConstraints constraints = new GridBagConstraints();
                    constraints.weightx = 1;
                    constraints.gridwidth = 0;
                    constraints.fill = GridBagConstraints.HORIZONTAL;
                    layout.setConstraints(subjectTextField, constraints);
                    layout.setConstraints(mailTextField, constraints);
                    layout.setConstraints(mailTextField2, constraints);
                    layout.setConstraints(startDatePicker, constraints);
                    layout.setConstraints(endDatePicker, constraints);
                    layout.setConstraints(searchButton, constraints);
                    
                    searchButton.removeAll();
                    searchButton.addActionListener(new ActionListener(){
                    	public void actionPerformed(ActionEvent e){
                    		System.out.println("Start search");
                    		String[] columnNames = {"手机", "邮件标题", "发件人", "收件人", "时间"};
                    		String[][] data = null;
                    		DefaultTableModel model = new DefaultTableModel(data, columnNames){
                    			@Override
                    			public boolean isCellEditable(int row, int column) {
                    				return false;
                    			}
                    		};
                    		JTable table = new JTable(model);
                    		StringBuffer sql = new StringBuffer("SELECT '手机' || qqmail.phone_id || \" \" || phones.phone_number AS phone," + 
                    				" qqmail.subject AS subject," + " qqmail.fromList AS fromList," + " qqmail.toList AS toList, " + "qqmail.time AS date " +
                    			    "FROM qqmail LEFT OUTER JOIN phones " +
                    				"ON qqmail.phone_id = phones.id ");
                    		if (!subjectTextField.getText().isEmpty() && subjectCheckBox.isSelected()){
                    			sql.append(" WHERE subject LIKE '%" + subjectTextField.getText() + "%'");
                    		}
                    		if (!mailTextField.getText().isEmpty() && mailCheckBox.isSelected()){
                    			sql.append(" WHERE fromList LIKE '%" + mailTextField.getText() + "%'");
                    		}
                    		if (!mailTextField2.getText().isEmpty() && mailCheckBox2.isSelected()){
                    			sql.append(" WHERE toList LIKE '%" + mailTextField.getText() + "%'");
                    		}
                    		if (!startDatePicker.getText().isEmpty() && startDateCheckBox.isSelected()) {
                                sql.append(" WHERE date >= '" + startDatePicker.getText() + " 00:00:00'");
                            }
                            if (!endDatePicker.getText().isEmpty() && endDateCheckBox.isSelected()) {
                                sql.append(" WHERE date <= '" + endDatePicker.getText() + " 23:59:59'");
                            }
                            sql.append(";");
                            Connection connection = null;
                            try {
                            	connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                            	Statement statement = connection.createStatement();
                            	ResultSet rs = statement.executeQuery(sql.toString());
                            	while(rs.next()){
                            		String[] row = new String[5];
                            		for(int i = 0; i < 5; i++)
                            			row[i] = rs.getString(i + 1);
                            		model.addRow(row);
                            		System.out.println(Arrays.toString(row));
                            	}
                            }catch(Exception ex){
                            	ex.printStackTrace();
                            }
                            testPanel.removeAll();
                            testPanel.add(searchPanel, BorderLayout.WEST);
                            JScrollPane scrollPane = new JScrollPane(table);
                            testPanel.add(scrollPane);
                            testPanel.revalidate();
                            testPanel.repaint();
                    	}

                    });
                    detailPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "QQ邮箱邮件记录"));
                    detailPanel.revalidate();
                    detailPanel.repaint();
            	}
            });

            JButton xcHotelButton = new JButton("携程酒店记录");
            //xcHotelButton.setIcon(xiechengIcon);
            searchPanel.add(xcHotelButton);
            xcHotelButton.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent e){
            		detailPanel.removeAll();
                    JCheckBox addressCheckBox = new JCheckBox();
                    JLabel addressLabel = new JLabel("酒店地址");
                    JTextField addressTextField = new JTextField(10);
                    detailPanel.add(addressCheckBox);
                    detailPanel.add(addressLabel);
                    detailPanel.add(addressTextField);

                    JCheckBox startDateCheckBox = new JCheckBox();
                    JLabel startDateLabel = new JLabel("起始日期");
                    DatePickerSettings datePickerSettings = new DatePickerSettings();
                    datePickerSettings.setFormatForDatesCommonEra("yyyy-MM-dd");
                    DatePicker startDatePicker = new DatePicker(datePickerSettings);
                    detailPanel.add(startDateCheckBox);
                    detailPanel.add(startDateLabel);
                    detailPanel.add(startDatePicker);

                    JCheckBox endDateCheckBox = new JCheckBox();
                    JLabel endDateLabel = new JLabel("截止日期");
                    DatePicker endDatePicker = new DatePicker(datePickerSettings.copySettings());
                    detailPanel.add(endDateCheckBox);
                    detailPanel.add(endDateLabel);
                    detailPanel.add(endDatePicker);
                    
                    JButton searchButton = new JButton("搜索");
                    detailPanel.add(searchButton);

                    GridBagConstraints constraints = new GridBagConstraints();
                    constraints.weightx = 1;
                    constraints.gridwidth = 0;
                    constraints.fill = GridBagConstraints.HORIZONTAL;
                    layout.setConstraints(addressTextField, constraints);
                    layout.setConstraints(startDatePicker, constraints);
                    layout.setConstraints(endDatePicker, constraints);
                    layout.setConstraints(searchButton, constraints);
                    
                    searchButton.removeAll();
                    searchButton.addActionListener(new ActionListener(){
                    	public void actionPerformed(ActionEvent e){
                    		System.out.println("Start search");
                    		String[] columnNames = {"手机", "酒店地址", "时间"};
                    		String[][] data = null;
                    		DefaultTableModel model = new DefaultTableModel(data, columnNames){
                    			@Override
                    			public boolean isCellEditable(int row, int column) {
                    				return false;
                    			}
                    		};
                    		JTable table = new JTable(model);
                    		StringBuffer sql = new StringBuffer("SELECT '手机' || ctrip_hotel.phone_id || \" \" || phones.phone_number AS phone," + 
                    				"ctrip_hotel.airportName AS address," + "ctrip_hotel.date AS date " + 
                    			    "FROM ctrip_hotel LEFT OUTER JOIN phones " +
                    				"ON ctrip_hotel.phone_id = phones.id ");
                    		if (!addressTextField.getText().isEmpty() && addressCheckBox.isSelected()){
                    			sql.append(" WHERE address LIKE '%" + addressTextField.getText() + "%'");
                    		}
                    		if (!startDatePicker.getText().isEmpty() && startDateCheckBox.isSelected()) {
                                sql.append(" WHERE date >= '" + startDatePicker.getText() + " 00:00:00'");
                            }
                            if (!endDatePicker.getText().isEmpty() && endDateCheckBox.isSelected()) {
                                sql.append(" WHERE date <= '" + endDatePicker.getText() + " 23:59:59'");
                            }
                            sql.append(";");
                            Connection connection = null;
                            try {
                            	connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                            	Statement statement = connection.createStatement();
                            	ResultSet rs = statement.executeQuery(sql.toString());
                            	while(rs.next()){
                            		String[] row = new String[3];
                            		for(int i = 0; i < 3; i++)
                            			row[i] = rs.getString(i + 1);
                            		model.addRow(row);
                            		System.out.println(Arrays.toString(row));
                            	}
                            }catch(Exception ex){
                            	ex.printStackTrace();
                            }
                            testPanel.removeAll();
                            testPanel.add(searchPanel, BorderLayout.WEST);
                            JScrollPane scrollPane = new JScrollPane(table);
                            testPanel.add(scrollPane);
                            testPanel.revalidate();
                            testPanel.repaint();
                    	}

                    });
                    
                    detailPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "携程酒店记录"));
                    detailPanel.revalidate();
                    detailPanel.repaint();
            	}
            });

            JButton xcTicketButton = new JButton("携程机票记录");
            //xcTicketButton.setIcon(xiechengIcon);
            searchPanel.add(xcTicketButton);
            xcTicketButton.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent e){
            		detailPanel.removeAll();
                    JCheckBox addressCheckBox = new JCheckBox();
                    JLabel addressLabel = new JLabel("地址：      ");
                    JTextField addressTextField = new JTextField(10);
                    detailPanel.add(addressCheckBox);
                    detailPanel.add(addressLabel);
                    detailPanel.add(addressTextField);

                    JCheckBox startDateCheckBox = new JCheckBox();
                    JLabel startDateLabel = new JLabel("起始日期    ");
                    DatePickerSettings datePickerSettings = new DatePickerSettings();
                    datePickerSettings.setFormatForDatesCommonEra("yyyy-MM-dd");
                    DatePicker startDatePicker = new DatePicker(datePickerSettings);
                    detailPanel.add(startDateCheckBox);
                    detailPanel.add(startDateLabel);
                    detailPanel.add(startDatePicker);

                    JCheckBox endDateCheckBox = new JCheckBox();
                    JLabel endDateLabel = new JLabel("截止日期    ");
                    DatePicker endDatePicker = new DatePicker(datePickerSettings.copySettings());
                    detailPanel.add(endDateCheckBox);
                    detailPanel.add(endDateLabel);
                    detailPanel.add(endDatePicker);
                    
                    JButton searchButton = new JButton("搜索");
                    detailPanel.add(searchButton);

                    GridBagConstraints constraints = new GridBagConstraints();
                    constraints.weightx = 1;
                    constraints.gridwidth = 0;
                    constraints.fill = GridBagConstraints.HORIZONTAL;
                    layout.setConstraints(addressTextField, constraints);
                    layout.setConstraints(startDatePicker, constraints);
                    layout.setConstraints(endDatePicker, constraints);
                    layout.setConstraints(searchButton, constraints);
                    
                    searchButton.removeAll();
                    searchButton.addActionListener(new ActionListener(){
                    	public void actionPerformed(ActionEvent e){
                    		System.out.println("Start search");
                    		String[] columnNames = {"手机", "地址", "时间"};
                    		String[][] data = null;
                    		DefaultTableModel model = new DefaultTableModel(data, columnNames){
                    			@Override
                    			public boolean isCellEditable(int row, int column) {
                    				return false;
                    			}
                    		};
                    		JTable table = new JTable(model);
                    		StringBuffer sql = new StringBuffer("SELECT '手机' || ctrip_flight.phone_id || \" \" || phones.phone_number AS phone," + 
                    				"ctrip_flight.cityName AS address," + "ctrip_flight.date AS date " + 
                    			    "FROM ctrip_flight LEFT OUTER JOIN phones " +
                    				"ON ctrip_flight.phone_id = phones.id ");
                    		if (!addressTextField.getText().isEmpty() && addressCheckBox.isSelected()){
                    			sql.append(" WHERE address LIKE '%" + addressTextField.getText() + "%'");
                    		}
                    		if (!startDatePicker.getText().isEmpty() && startDateCheckBox.isSelected()) {
                                sql.append(" WHERE date >= '" + startDatePicker.getText() + " 00:00:00'");
                            }
                            if (!endDatePicker.getText().isEmpty() && endDateCheckBox.isSelected()) {
                                sql.append(" WHERE date <= '" + endDatePicker.getText() + " 23:59:59'");
                            }
                            sql.append(";");
                            Connection connection = null;
                            try {
                            	connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                            	Statement statement = connection.createStatement();
                            	ResultSet rs = statement.executeQuery(sql.toString());
                            	while(rs.next()){
                            		String[] row = new String[3];
                            		for(int i = 0; i < 3; i++){
                            			row[i] = rs.getString(i + 1);
                            		}
                            		model.addRow(row);
                            		System.out.println(Arrays.toString(row));
                            	}
                            }catch(Exception ex){
                            	ex.printStackTrace();
                            }
                            testPanel.removeAll();
                            testPanel.add(searchPanel, BorderLayout.WEST);
                            JScrollPane scrollPane = new JScrollPane(table);
                            testPanel.add(scrollPane);
                            testPanel.revalidate();
                            testPanel.repaint();
                    	}

                    });
                    
                    detailPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "携程机票记录"));
                    detailPanel.revalidate();
                    detailPanel.repaint();
            	}
            });

            JButton jdbrowseButton = new JButton("京东浏览记录");
            searchPanel.add(jdbrowseButton);
            jdbrowseButton.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent e){
            		detailPanel.removeAll();
                    JCheckBox idCheckBox = new JCheckBox();
                    JLabel idLabel = new JLabel("商品代码    ");
                    JTextField idTextField = new JTextField(10);
                    detailPanel.add(idCheckBox);
                    detailPanel.add(idLabel);
                    detailPanel.add(idTextField);
                    
                    JCheckBox startDateCheckBox = new JCheckBox();
                    JLabel startDateLabel = new JLabel("起始日期    ");
                    DatePickerSettings datePickerSettings = new DatePickerSettings();
                    datePickerSettings.setFormatForDatesCommonEra("yyyy-MM-dd");
                    DatePicker startDatePicker = new DatePicker(datePickerSettings);
                    detailPanel.add(startDateCheckBox);
                    detailPanel.add(startDateLabel);
                    detailPanel.add(startDatePicker);

                    JCheckBox endDateCheckBox = new JCheckBox();
                    JLabel endDateLabel = new JLabel("截止日期    ");
                    DatePicker endDatePicker = new DatePicker(datePickerSettings.copySettings());
                    detailPanel.add(endDateCheckBox);
                    detailPanel.add(endDateLabel);
                    detailPanel.add(endDatePicker);
                    
                    JButton searchButton = new JButton("搜索");
                    detailPanel.add(searchButton);

                    GridBagConstraints constraints = new GridBagConstraints();
                    constraints.weightx = 1;
                    constraints.gridwidth = 0;
                    constraints.fill = GridBagConstraints.HORIZONTAL;
                    layout.setConstraints(idTextField, constraints);
                    layout.setConstraints(startDatePicker, constraints);
                    layout.setConstraints(endDatePicker, constraints);
                    layout.setConstraints(searchButton, constraints);
                    
                    searchButton.removeAll();
                    searchButton.addActionListener(new ActionListener(){
                    	public void actionPerformed(ActionEvent e){
                    		System.out.println("Start search");
                    		String[] columnNames = {"手机", "商品代码", "时间"};
                    		String[][] data = null;
                    		DefaultTableModel model = new DefaultTableModel(data, columnNames){
                    			@Override
                    			public boolean isCellEditable(int row, int column) {
                    				return false;
                    			}
                    		};
                    		JTable table = new JTable(model);
                    		StringBuffer sql = new StringBuffer("SELECT '手机' || JDBrowseHistory.phone_id || \" \" || phones.phone_number AS phone," + 
                    				"JDBrowseHistory.productCode AS productCode," + "JDBrowseHistory.browseTime AS date " + 
                    			    "FROM JDBrowseHistory LEFT OUTER JOIN phones " +
                    				"ON JDBrowseHistory.phone_id = phones.id ");
                    		if (!idTextField.getText().isEmpty() && idCheckBox.isSelected()){
                    			sql.append(" WHERE productCode LIKE '%" + idTextField.getText() + "%'");
                    		}
                    		if (!startDatePicker.getText().isEmpty() && startDateCheckBox.isSelected()) {
                                sql.append(" WHERE date >= '" + startDatePicker.getText() + " 00:00:00'");
                            }
                            if (!endDatePicker.getText().isEmpty() && endDateCheckBox.isSelected()) {
                                sql.append(" WHERE date <= '" + endDatePicker.getText() + " 23:59:59'");
                            }
                            sql.append(";");
                            Connection connection = null;
                            try {
                            	connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                            	Statement statement = connection.createStatement();
                            	ResultSet rs = statement.executeQuery(sql.toString());
                            	while(rs.next()){
                            		String[] row = new String[3];
                            		for(int i = 0; i < 3; i++)
                            			row[i] = rs.getString(i + 1);
                            		model.addRow(row);
                            		System.out.println(Arrays.toString(row));
                            	}
                            }catch(Exception ex){
                            	ex.printStackTrace();
                            }
                            testPanel.removeAll();
                            testPanel.add(searchPanel, BorderLayout.WEST);
                            JScrollPane scrollPane = new JScrollPane(table);
                            testPanel.add(scrollPane);
                            testPanel.revalidate();
                            testPanel.repaint();
                    	}

                    });
                    
                    detailPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "微信记录"));
                    detailPanel.revalidate();
                    detailPanel.repaint();
            	}
            });

            JButton jdsearchButton = new JButton("京东搜索记录");
            searchPanel.add(jdsearchButton);
            jdsearchButton.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent e){
            		detailPanel.removeAll();   
                    JCheckBox keywordCheckBox = new JCheckBox();
                    JLabel keywordLabel = new JLabel("关键字：    ");
                    JTextField keywordTextField = new JTextField(10);
                    detailPanel.add(keywordCheckBox);
                    detailPanel.add(keywordLabel);
                    detailPanel.add(keywordTextField);
                    
                    JCheckBox startDateCheckBox = new JCheckBox();
                    JLabel startDateLabel = new JLabel("起始日期    ");
                    DatePickerSettings datePickerSettings = new DatePickerSettings();
                    datePickerSettings.setFormatForDatesCommonEra("yyyy-MM-dd");
                    DatePicker startDatePicker = new DatePicker(datePickerSettings);
                    detailPanel.add(startDateCheckBox);
                    detailPanel.add(startDateLabel);
                    detailPanel.add(startDatePicker);

                    JCheckBox endDateCheckBox = new JCheckBox();
                    JLabel endDateLabel = new JLabel("截止日期    ");
                    DatePicker endDatePicker = new DatePicker(datePickerSettings.copySettings());
                    detailPanel.add(endDateCheckBox);
                    detailPanel.add(endDateLabel);
                    detailPanel.add(endDatePicker);
                    
                    JButton searchButton = new JButton("搜索");
                    detailPanel.add(searchButton);

                    GridBagConstraints constraints = new GridBagConstraints();
                    constraints.weightx = 1;
                    constraints.gridwidth = 0;
                    constraints.fill = GridBagConstraints.HORIZONTAL;
                    layout.setConstraints(keywordTextField, constraints);
                    layout.setConstraints(startDatePicker, constraints);
                    layout.setConstraints(endDatePicker, constraints);
                    layout.setConstraints(searchButton, constraints);
                    
                    searchButton.removeAll();
                    searchButton.addActionListener(new ActionListener(){
                    	public void actionPerformed(ActionEvent e){
                    		System.out.println("Start search");
                    		String[] columnNames = {"手机", "搜索关键字", "时间"};
                    		String[][] data = null;
                    		DefaultTableModel model = new DefaultTableModel(data, columnNames){
                    			@Override
                    			public boolean isCellEditable(int row, int column) {
                    				return false;
                    			}
                    		};
                    		JTable table = new JTable(model);
                    		StringBuffer sql = new StringBuffer("SELECT '手机' || JDSearchHistory.phone_id || \" \" || phones.phone_number AS phone," + 
                    				"JDSearchHistory.word AS word," + "JDSearchHistory.search_time AS date " + 
                    			    "FROM JDSearchHistory LEFT OUTER JOIN phones " +
                    				"ON JDSearchHistory.phone_id = phones.id ");
                    		if (!keywordTextField.getText().isEmpty() && keywordCheckBox.isSelected()){
                    			sql.append(" WHERE word LIKE '%" + keywordTextField.getText() + "%'");
                    		}
                    		if (!startDatePicker.getText().isEmpty() && startDateCheckBox.isSelected()) {
                                sql.append(" WHERE date >= '" + startDatePicker.getText() + " 00:00:00'");
                            }
                            if (!endDatePicker.getText().isEmpty() && endDateCheckBox.isSelected()) {
                                sql.append(" WHERE date <= '" + endDatePicker.getText() + " 23:59:59'");
                            }
                            sql.append(";");
                            Connection connection = null;
                            try {
                            	connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                            	Statement statement = connection.createStatement();
                            	ResultSet rs = statement.executeQuery(sql.toString());
                            	while(rs.next()){
                            		String[] row = new String[3];
                            		for(int i = 0; i < 3; i++)
                            			row[i] = rs.getString(i + 1);
                            		model.addRow(row);
                            		System.out.println(Arrays.toString(row));
                            	}
                            }catch(Exception ex){
                            	ex.printStackTrace();
                            }
                            testPanel.removeAll();
                            testPanel.add(searchPanel, BorderLayout.WEST);
                            JScrollPane scrollPane = new JScrollPane(table);
                            testPanel.add(scrollPane);
                            testPanel.revalidate();
                            testPanel.repaint();
                    	}

                    });
                    
                    detailPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "QQ记录"));
                    detailPanel.revalidate();
                    detailPanel.repaint();
            	}
            });
  

            JLabel fillLabel = new JLabel();
            searchPanel.add(fillLabel);


            searchPanel.add(detailPanel);
            GridBagConstraints s = new GridBagConstraints();
            s.fill = GridBagConstraints.HORIZONTAL;
            s.insets = new Insets(1, 5, 0, 5);
            s.gridwidth = 0;
            s.weightx = 1;
            layout.setConstraints(hintLabel, s);
            s.gridwidth = 1;
            layout.setConstraints(smsButton, s);
            layout.setConstraints(ucButton, s);
            layout.setConstraints(mailButton, s);
            layout.setConstraints(xcTicketButton, s);
            s.gridwidth = 0;
            layout.setConstraints(xcHotelButton, s);
            layout.setConstraints(jdbrowseButton, s);
            layout.setConstraints(callButton, s);
            layout.setConstraints(qbButton, s);
            s.weightx = 0.5;
            layout.setConstraints(jdsearchButton, s);
            s.fill = GridBagConstraints.BOTH;
            s.weighty = 1;
            layout.setConstraints(fillLabel, s);
            layout.setConstraints(detailPanel, s);

            searchPanel.setPreferredSize(new Dimension(300, 500));
            smsButton.doClick();
            testPanel.add(searchPanel, BorderLayout.WEST);
            testPanel.revalidate();
            testPanel.repaint();
        }
    }

    private class RecoveryListener implements ActionListener {
        JLabel bclb3 = new JLabel();

        public void actionPerformed(ActionEvent e) {
            System.out.println("数据恢复");
            testPanel.removeAll();
            testPanel.setBackground(new Color(245, 245, 245));
            testPanel.setLayout(null);
            Icon backIcon3 = new ImageIcon("images/recoverylogo.png");
            bclb3.setIcon(backIcon3);
            final JButton fileButton = new JButton("浏览");
            final JButton submitButton = new JButton("恢复数据库");
            bclb3.setBounds(400, 50, 600, 140);
            testPanel.add(bclb3);
            fileButton.setBounds(800, 250, 70, 30);
            submitButton.setBounds(880, 250, 100, 30);
            final JTextArea t1 = new JTextArea(10, 50);
            t1.setLineWrap(true);// 激活自动换行功能
            t1.setWrapStyleWord(true);// 激活断行不断字功能
            t1.setFont(new Font("宋体", Font.PLAIN, 18));
            t1.setEditable(false);
            final JTextArea t2 = new JTextArea(10, 50);
            t2.setLineWrap(true);
            t2.setWrapStyleWord(true);
            t2.setFont(new Font("宋体", Font.PLAIN, 18));
            t2.setEditable(false);

            final JTextField fileTextField = new JTextField(60);
            fileTextField.setBounds(400, 250, 380, 30);


            fileButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {

                    JFileChooser fileChooser = new JFileChooser();
                    //fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);// 只能选择目录
                    String path = null;
                    java.io.File f = null;
                    int flag = 0;
                    try {
                        flag = fileChooser.showOpenDialog(null);
                    } catch (HeadlessException head) {
                        System.out.println("Open File Dialog ERROR!");
                    }

                    if (flag == JFileChooser.APPROVE_OPTION) {
                        // 获得该文件
                        f = fileChooser.getSelectedFile();
                        path = f.getPath();

                        fileTextField.setText(path);
                    }

                }
            });
            submitButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    System.out.println("数据恢复过程");
                    testPanel.remove(bclb3);
                    testPanel.removeAll();
                    testPanel.setLayout(null);
                    testPanel.add(fileTextField);
                    testPanel.add(fileButton);
                    testPanel.add(submitButton);

                    fileButton.setBounds(800, 25, 70, 30);
                    submitButton.setBounds(880, 25, 100, 30);
                    fileTextField.setBounds(400, 25, 380, 30);

                    Runtime r = Runtime.getRuntime();
                    Process p = null;

                    try {
                        System.out.println("Starting the process..");
                        String pythonString = new String();
                        pythonString = "D:\\桌面\\python\\sqlparse.py -f " + fileTextField.getText() + " -o D:\\桌面\\python\\output.txt ";
                        //System.out.println(pythonString);
                        p = r.exec("cmd.exe /c start python " + pythonString);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    FileReader fr;
                    String s = new String();
                    String s1 = new String();
                    String s2 = new String();
                    List<SMS> list = new ArrayList<>();
                    try {
                        InputStreamReader isr = new InputStreamReader(new FileInputStream("D:\\桌面\\python\\output.txt"), "UTF-8");
                        BufferedReader br = new BufferedReader(isr);
                        while (br.readLine() != null) {
                            s = br.readLine();
                            s2 = checkNum(s);
                            t1.append(s + "\r\n");
                            if (!s2.isEmpty()) {
                                SMS sms = new SMS();
                                sms.setAddress(s2);
                                s1 = s;
                                s1 = s1.replaceAll(s2, "");
                                s1 = s1.replaceAll("[^\u0020-\u9FA5]", "");
                                sms.setBody(s1);

                                list.add(sms);
                                System.out.println(s2);
                                System.out.println(s);
                                for (int i = 0; i < s.length(); i++) {
                                    if ((s.charAt(i) + "").getBytes().length > 1) {
                                        t2.append(s.charAt(i) + "");
                                    }
                                }
                                t2.append("\n\r");
                            }
                        }
                        br.close();
                        String[] columnNames = {"联系人", "短信内容", "数据状态"}; // 定义表格列名数组
                        // 定义表格数据数组
                        String[][] tableValues = new String[list.size() + 1][5];
                        for (int i = 0; i < list.size(); i++) {
                            tableValues[i][0] = list.get(i).getAddress();
                            tableValues[i][1] = list.get(i).getBody();
                            //if (smsList.get(i).getType() == 1)
                            //	tableValues[i][3] = "接收";
                            //if (smsList.get(i).getType() == 2)
                            //	tableValues[i][3] = "发送";
                            tableValues[i][2] = "已删除数据";
                        }
                        JTable smsTable = new JTable(tableValues, columnNames) {
                            private static final long serialVersionUID = 1L;
                        };
                        JScrollPane scrollPane = new JScrollPane(smsTable);
                        tabbedPane2.addTab("短信", smsIcon, scrollPane, "短信");
                        tabbedPane2.setSelectedIndex(0);
                        tabbedPane2.setFont(new Font("宋体", Font.PLAIN, 18));
                    } catch (FileNotFoundException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }


                    JPanel test2Pane = new JPanel(new GridLayout(1, 3));
                    test2Pane.setBounds(30, 150, 1290, 440);

                    JPanel test1Pane = new JPanel(new GridLayout(1, 3));
                    test1Pane.setBounds(30, 70, 1290, 80);
                    JButton gl = new JButton("概览");
                    gl.setIcon(new ImageIcon("images/gl.png"));
                    gl.setRolloverEnabled(false);
                    test1Pane.add(gl);
                    JButton ym = new JButton("原码");
                    ym.setIcon(new ImageIcon("images/ym.png"));
                    ym.setRolloverEnabled(false);
                    test1Pane.add(ym);
                    JButton zl = new JButton("整理");
                    zl.setIcon(new ImageIcon("images/zl.png"));
                    zl.setRolloverEnabled(false);
                    test1Pane.add(zl);

                    gl.setFont(new Font("宋体", Font.PLAIN, 24));
                    ym.setFont(new Font("宋体", Font.PLAIN, 24));
                    zl.setFont(new Font("宋体", Font.PLAIN, 24));

                    testPanel.add(test1Pane);
                    testPanel.add(test2Pane);
                    test2Pane.add(tabbedPane2);
                    final JScrollPane scroll = new JScrollPane(t1);
                    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                    test2Pane.add(scroll);
                    final JScrollPane scroll2 = new JScrollPane(t2);
                    scroll2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                    test2Pane.add(scroll2);
                    //test2Pane.add(t1);
                    //test2Pane.add(t2);


                }

            });

            testPanel.add(fileTextField);
            testPanel.add(fileButton);
            testPanel.add(submitButton);

            testPanel.repaint();
            testPanel.doLayout();
            testPanel.setLayout(new BorderLayout());

        }

    }

    private class ReproduceListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            System.out.println("情景重现");
            testPanel.removeAll();
            testPanel.setLayout(null);
            testPanel.setBackground(new Color(245, 245, 245));
            JLabel bclb4 = new JLabel();
            Icon backIcon4 = new ImageIcon("images/reproducelogo.png");
            Icon launchIcon1 = new ImageIcon("images/lauch-1.png");
            Icon launchIcon2 = new ImageIcon("images/lauch-2.png");
            bclb4.setIcon(backIcon4);
            bclb4.setBounds(300, 50, 800, 140);
            testPanel.add(bclb4);
            JRadioButton lauchRadio = new JRadioButton(launchIcon1);
            lauchRadio.setContentAreaFilled(false);
            lauchRadio.setRolloverIcon(launchIcon2);
            lauchRadio.setSelectedIcon(launchIcon2);
            lauchRadio.addActionListener(new LauchListener());
            lauchRadio.setBounds(1030, 210, 200, 200);
            testPanel.add(lauchRadio);


            final JLabel appLabel = new JLabel("请选择装载的app：");
            appLabel.setFont(new Font("宋体", Font.PLAIN, 20));
            appLabel.setForeground(Color.BLACK);
            appLabel.setBounds(250, 200, 200, 30);

            final JButton fileButton = new JButton("浏览");
            final JButton submitButton = new JButton("装载");

            fileButton.setBounds(750, 230, 70, 30);
            submitButton.setBounds(830, 230, 70, 30);
            final JLabel dataLabel = new JLabel("请选择导入的用户数据：");
            dataLabel.setFont(new Font("宋体", Font.PLAIN, 20));
            dataLabel.setForeground(Color.BLACK);
            dataLabel.setBounds(250, 330, 250, 30);
            final JButton fileButton2 = new JButton("浏览");
            final JButton submitButton2 = new JButton("导入");

            fileButton2.setBounds(750, 360, 70, 30);
            submitButton2.setBounds(830, 360, 70, 30);
            final JTextField fileTextField = new JTextField(60);
            fileTextField.setBounds(350, 230, 380, 30);
            final JTextField fileTextField2 = new JTextField(60);
            fileTextField2.setBounds(350, 360, 380, 30);

            final JButton removeButton = new JButton("重新选择");
            removeButton.setBounds(910, 230, 70, 30);
            final JButton removeButton2 = new JButton("重新选择");
            removeButton2.setBounds(910, 360, 70, 30);

            fileButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {

                    JFileChooser fileChooser = new JFileChooser();
                    //fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);// 只能选择目录
                    String path = null;
                    java.io.File f = null;
                    int flag = 0;
                    try {
                        flag = fileChooser.showOpenDialog(null);
                    } catch (HeadlessException head) {
                        System.out.println("Open File Dialog ERROR!");
                    }

                    if (flag == JFileChooser.APPROVE_OPTION) {
                        // 获得该文件
                        f = fileChooser.getSelectedFile();
                        path = f.getPath();

                        fileTextField.setText(path);
                    }

                }
            });

            submitButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Runtime r1 = Runtime.getRuntime();
                    Process p1 = null;
                    try {
                        System.out.println("Starting the process..");
                        p1 = r1.exec("cmd.exe /c start adb install -r " + fileTextField.getText());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });
            fileButton2.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {

                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);// 只能选择目录
                    String path = null;
                    java.io.File f = null;
                    int flag = 0;
                    try {
                        flag = fileChooser.showOpenDialog(null);
                    } catch (HeadlessException head) {
                        System.out.println("Open File Dialog ERROR!");
                    }

                    if (flag == JFileChooser.APPROVE_OPTION) {
                        // 获得该文件
                        f = fileChooser.getSelectedFile();
                        path = f.getPath();

                        fileTextField2.setText(path);
                    }

                }
            });

            submitButton2.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Runtime r1 = Runtime.getRuntime();
                    Process p1 = null;
                    try {
                        System.out.println("Starting the process..");
                        p1 = r1.exec("cmd.exe /c start adb push " + fileTextField2.getText() + " /data/data/com.zhihu.android");
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });
            removeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fileTextField.setText("");
                }
            });
            removeButton2.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fileTextField2.setText("");
                }
            });
            testPanel.add(appLabel);
            testPanel.add(dataLabel);
            testPanel.add(fileButton);
            testPanel.add(submitButton);
            testPanel.add(removeButton);
            testPanel.add(fileButton2);
            testPanel.add(submitButton2);
            testPanel.add(removeButton2);
            testPanel.add(fileTextField);
            testPanel.add(fileTextField2);
            testPanel.repaint();
            testPanel.doLayout();
            testPanel.setLayout(new BorderLayout());
        }

        class LauchListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                Runtime r = Runtime.getRuntime();
                Process p = null;
                try {
                    System.out.println("Starting the process..");
                    p = r.exec("cmd.exe /c start emulator -avd ForensicsElf");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }


    }


    private class AnalyseListener implements ActionListener {
        JMenuBar menuBar = new JMenuBar();
        JFXPanel webBrowser = new JFXPanel();
        ImageIcon singleIcon1 = new ImageIcon("images/single-1.png");
        ImageIcon singleIcon2 = new ImageIcon("images/single-2.png");
        ImageIcon singleIcon3 = new ImageIcon("images/single-2.png");
        ImageIcon multiIcon1 = new ImageIcon("images/multi-1.png");
        ImageIcon multiIcon2 = new ImageIcon("images/multi-2.png");
        ImageIcon multiIcon3 = new ImageIcon("images/multi-2.png");

        public void actionPerformed(ActionEvent e) {
            System.out.println("数据分析界面");
            testPanel.removeAll();
            testPanel.setLayout(null);
            testPanel.setBackground(new Color(245, 245, 245));
            JLabel bclb = new JLabel();
            Icon backIcon = new ImageIcon("images/analyselogo.png");
            bclb.setIcon(backIcon);
            bclb.setBounds(420, 50, 500, 140);
            testPanel.add(bclb);

            JRadioButton singleRadio = new JRadioButton(singleIcon1);
            singleRadio.setContentAreaFilled(false);
            singleRadio.setRolloverIcon(singleIcon2);
            singleRadio.setSelectedIcon(singleIcon3);
            singleRadio.addActionListener(new SingleListener());
            singleRadio.setBounds(350, 210, 200, 200);

            JRadioButton multiRadio = new JRadioButton(multiIcon1);
            multiRadio.setContentAreaFilled(false);
            multiRadio.setRolloverIcon(multiIcon2);
            multiRadio.setSelectedIcon(multiIcon3);
            multiRadio.addActionListener(new MultiListener());
            multiRadio.setBounds(800, 210, 200, 200);

            testPanel.add(singleRadio);
            testPanel.add(multiRadio);

            testPanel.revalidate();
            testPanel.repaint();
            testPanel.setLayout(new BorderLayout());

        }

        class SingleListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                try {
                    testPanel.removeAll();
                    JPanel choosePanel = new JPanel();
                    JPanel mainPanel = new JPanel(new FlowLayout());
                    JScrollPane scrollPane = new JScrollPane();
                    JButton back = new JButton("返回");
                    JLabel label = new JLabel("请选择手机：");
                    choosePanel.add(back);
                    choosePanel.add(label);
                    back.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            ((JRadioButton) menuPanel.getComponent(5)).doClick();
                        }
                    });
                    Vector<String> phones = new Vector<>();
                    ArrayList<String> phonePath = new ArrayList<>();
                    Connection connection = null;
                    try {
                        Class.forName("org.sqlite.JDBC");
                        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                        Statement statement = connection.createStatement();
                        ResultSet rs = statement.executeQuery("SELECT id, owner_name, phone_number, default_path FROM phones");
                        while (rs.next()) {
                            int id = rs.getInt(1);
                            String ownerName = rs.getString(2);
                            String phoneNumber = rs.getString(3);
                            String path = rs.getString(4);
                            phonePath.add(path);
                            phones.add("手机" + id + " " + ownerName + " " + phoneNumber);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    JComboBox phoneCombo = new JComboBox(phones);
                    choosePanel.add(phoneCombo);
                    phoneCombo.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            try {
                                int index = phoneCombo.getSelectedIndex();
                                logger.info(phonePath.get(index));
                                String[] columns = {"联系人", "亲密度(通过人工智能算法算出，数值越高联系越密)", "通话次数", "通话总时长", "短信条数", "最近一次联系时间"};
                                String[][] data = null;
                                DefaultTableModel model = new DefaultTableModel(data, columns) {
                                    @Override
                                    public boolean isCellEditable(int row, int column) {
                                        return false;
                                    }

                                    @Override
                                    public Class<?> getColumnClass(int columnIndex) {
                                        Class returnValue;
                                        if ((columnIndex >= 0 && columnIndex < getColumnCount())) {
                                            returnValue = getValueAt(0, columnIndex).getClass();
                                        } else {
                                            returnValue = Object.class;
                                        }
                                        return returnValue;
                                    }
                                };
                                JTable table = new JTable(model);
                                table.setAutoCreateRowSorter(true);
                                String sql = "SELECT * FROM (SELECT number FROM calls WHERE phone_id = " + (index + 1) +
                                        " UNION SELECT number FROM messages WHERE phone_id = " + (index + 1) + ");";
                                Connection connection = null;
                                try {
                                    connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                                    Statement statement = connection.createStatement();

                                    ResultSet numberSet = statement.executeQuery(sql);
                                    while (numberSet.next()) {
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                        Date latestDate = null;
                                        Object[] row = new Object[6];
                                        row[0] = numberSet.getString(1);
                                        Statement statement1 = connection.createStatement();
                                        ResultSet callCountSet = statement1.executeQuery(
                                                "SELECT COUNT(*), SUM(duration), MAX(date) FROM calls WHERE duration > 0 AND phone_id = " + (index + 1) + " AND number = '" + row[0] + "';"
                                        );
                                        if (callCountSet.next() && callCountSet.getInt(1) > 0) {
                                            row[2] = callCountSet.getInt(1);
                                            row[3] = callCountSet.getInt(2);
                                            latestDate = sdf.parse(callCountSet.getString(3).substring(0, 10));
                                        } else {
                                            row[2] = 0;
                                            row[3] = 0;
                                        }
                                        ResultSet messagesCountSet = statement1.executeQuery(
                                                "SELECT COUNT(*), MAX(date) FROM messages WHERE phone_id = " + (index + 1) + " AND number = '" + row[0] + "';"
                                        );


                                        if (messagesCountSet.next() && messagesCountSet.getInt(1) > 0) {
                                            row[4] = messagesCountSet.getInt(1);
                                            Date date = sdf.parse(messagesCountSet.getString(2).substring(0, 10));
                                            if (latestDate == null || date.getTime() - latestDate.getTime() > 0) {
                                                latestDate = date;
                                            }
                                        }
                                        if (latestDate == null)
                                            continue;
                                        row[4] = messagesCountSet.getInt(1);
                                        row[5] = latestDate;

                                        // 计算亲密度，亲密度的算法为(通话次数+短信条数+ln(通话总时长/通话次数))/(上次时间距今的天数/30)
                                        int tonghuacishu = (Integer) row[2];
                                        int duanxintiaoshu = (Integer) row[4];
                                        int tonghuashichang = (Integer) row[3];
                                        int jujintianshu = (int) ((System.currentTimeMillis() - latestDate.getTime()) / (24 * 60 * 60 * 1000));
                                        double pingjun = (tonghuashichang == 0) ? 0 : Math.log((double) tonghuashichang / tonghuacishu);
                                        double qinmidu = (tonghuacishu + duanxintiaoshu / 5.0 + pingjun) / ((double) (jujintianshu + 2) / 30);
                                        row[1] = qinmidu;
                                        model.addRow(row);
                                    }
                                    if (testPanel.getComponentCount() > 1)
                                        testPanel.remove(1);
                                    testPanel.add(new JScrollPane(table));
                                    testPanel.revalidate();
                                    testPanel.repaint();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });

                    //testPanel.add(mainPanel, BorderLayout.CENTER);
                    testPanel.add(choosePanel, BorderLayout.NORTH);
                    phoneCombo.setSelectedIndex(phones.size() - 1);
                    testPanel.revalidate();
                    testPanel.repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                testPanel.revalidate();
                testPanel.repaint();
            }
        }

        class MultiListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                JPanel choosePanel = new JPanel();

                System.out.println("run " + e.getActionCommand());
                if (true) {
                    System.out.println("ok");
                    testPanel.removeAll();
                    testPanel.add(webBrowser);
                    webBrowser.setBounds(0, 0, 1300, 1200);
                    Platform.setImplicitExit(false);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            GetData.generate(dbPath);
                            Group root = new Group();
                            Scene scene = new Scene(root, 1300, 1000);
                            webBrowser.setScene(scene);
                            Double widthDouble = new Integer(1050).doubleValue();
                            Double heightDouble = new Integer(700).doubleValue();

                            VBox box = new VBox(10);
                            HBox urlBox = new HBox(10);
                            WebView view = new WebView();
                            view.setMinSize(widthDouble, heightDouble);
                            view.setPrefSize(widthDouble, heightDouble);
                            final WebEngine eng = view.getEngine();
                            System.out.println("ttttt");
                            eng.load("http://localhost:1234/frame.html");

                            root.getChildren().add(view);
                            box.getChildren().add(urlBox);
                            box.getChildren().add(view);
                            root.getChildren().add(box);
                            System.out.println("t");

                        }
                    });
                    GridBagLayout layout = new GridBagLayout();
                    JPanel newPanel = new JPanel(layout);
                    newPanel.setMaximumSize(new Dimension(300, HEIGHT));
                    JButton button = new JButton("返回");

                    JLabel label = new JLabel("请选择时间区间");
                    JLabel startTime = new JLabel("开始时间");
                    DateTimePicker startPicker = new DateTimePicker();
                    startPicker.getDatePicker().setDate(LocalDate.of(2016, Month.JANUARY, 1));
                    startPicker.getTimePicker().setTime(LocalTime.of(0,0,0));

                    JLabel endTime = new JLabel("结束时间");
                    DateTimePicker endPicker = new DateTimePicker();
                    endPicker.getDatePicker().setDateToToday();
                    endPicker.getTimePicker().setTime(LocalTime.of(23,59,59));
                    JButton okButton = new JButton("重新生成");
                    JLabel emptyLabel = new JLabel();

                    GridBagConstraints constraints = new GridBagConstraints();
                    newPanel.add(button);
                    newPanel.add(label);
                    newPanel.add(startTime);
                    newPanel.add(startPicker);
                    newPanel.add(endTime);
                    newPanel.add(endPicker);
                    newPanel.add(okButton);
                    newPanel.add(emptyLabel);

                    constraints.gridwidth = 0;
                    constraints.anchor = GridBagConstraints.WEST;
                    constraints.fill = GridBagConstraints.NONE;
                    constraints.insets = new Insets(5,10,5,10);
                    layout.setConstraints(label, constraints);
                    layout.setConstraints(startTime, constraints);
                    layout.setConstraints(endTime, constraints);
                    constraints.gridwidth = 0;
                    constraints.weightx = 1;
                    layout.setConstraints(button, constraints);
                    layout.setConstraints(startPicker, constraints);
                    layout.setConstraints(endPicker, constraints);
                    layout.setConstraints(okButton, constraints);
                    constraints.weighty = 1;
                    layout.setConstraints(emptyLabel, constraints);
                    testPanel.add(newPanel, BorderLayout.WEST);

                    button.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            ((JRadioButton) menuPanel.getComponent(5)).doClick();
                        }
                    });

                    okButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            webBrowser.setVisible(false);
                            try {
                                Thread.sleep(1300);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                            webBrowser.setVisible(true);
                        }
                    });

                    testPanel.repaint();
                    testPanel.doLayout();
                }
            }
        }
    }

    private class MapListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            logger.info("轨迹图");
            testPanel.removeAll();
            testPanel.setBackground(new Color(245,245,245));
            JFXPanel webBrowser = new JFXPanel();
            WebEngine engine;
            JPanel panel = new JPanel(new BorderLayout());
            JTextField imageUrl = new JTextField(80);
            JButton chooseButton = new JButton("选择文件夹");
            JButton goButton = new JButton("开始分析");

            JPanel topBar = new JPanel();
            topBar.add(imageUrl);
            topBar.add(chooseButton);
            topBar.add(goButton);
            testPanel.add(topBar, BorderLayout.NORTH);
            testPanel.add(webBrowser, BorderLayout.CENTER);

            Platform.setImplicitExit(false);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        //PhotoAnalyzer.write(imageUrl.getText());
                        Group root = new Group();
                        Scene scene = new Scene(root, WIDTH, HEIGHT);
                        webBrowser.setScene(scene);
                        VBox box = new VBox(10);
                        WebView view = new WebView();
                        view.setMinSize(WIDTH, HEIGHT);
                        logger.info("Loading Track HTML...");
                        String url = "http://map.baidu.com";
                        view.getEngine().load(url);
                        root.getChildren().add(view);
                        box.getChildren().add(view);
                        root.getChildren().add(box);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            chooseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    File f = null;
                    try {
                        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                            f = chooser.getSelectedFile();
                            imageUrl.setText(f.getPath());
                        }
                    } catch (HeadlessException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            goButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Platform.setImplicitExit(false);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                PhotoAnalyzer.write(imageUrl.getText());
                                Group root = new Group();
                                Scene scene = new Scene(root, WIDTH, HEIGHT);
                                webBrowser.setScene(scene);
                                VBox box = new VBox(10);
                                WebView view = new WebView();
                                view.setMinSize(WIDTH, HEIGHT);
                                logger.info("Loading Track HTML...");
                                String url = "file:///" + System.getProperty("user.dir").replace("\\\\", "/") + "/WebRoot/track.html";
                                view.getEngine().load(url);
                                root.getChildren().add(view);
                                box.getChildren().add(view);
                                root.getChildren().add(box);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                }
            });
            testPanel.add(webBrowser);
            testPanel.revalidate();
            testPanel.repaint();
        }
    }

    public void setData(List<SMS> SMSList, List<Call> callsList, List<Email> emailList, List<Browser> UCBrowser,
                        List<Browser> QQBrowser, List<Hotel> hotelList, List<Flight> flightList, List<JDBrowseHistory> jdBrowseHistories,
                        List<JDSearchHistory> jdSearchHistories) {
        this.smsList = SMSList;
        testPanel.add(infoPanel);
        infoPanel.setLayout(new BorderLayout());
        infoPanel.setBackground(new Color(223, 223, 223));
        // 设置选项卡标签的布局方式
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        /*tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                // 获得被选中选项卡的索引
                int selectedIndex = tabbedPane.getSelectedIndex();
                // 获得指定索引的选项卡标签
                String title = tabbedPane.getTitleAt(selectedIndex);
                System.out.println(title);
            }
        });*/

        infoPanel.add(tabbedPane, BorderLayout.CENTER);
        tabbedPane.removeAll();
        if (!SMSList.isEmpty())
            updatesms(SMSList);
        if (!callsList.isEmpty())
            updatecalls(callsList);
        if (!UCBrowser.isEmpty())
            updateUCBrowser(UCBrowser);
        if (!QQBrowser.isEmpty())
            updateQQBrowser(QQBrowser);
        if (!emailList.isEmpty())
            updateEmail(emailList);
        if (!hotelList.isEmpty())
            updateHotel(hotelList);
        if (!flightList.isEmpty())
            updateFlight(flightList);
        if (!jdBrowseHistories.isEmpty() || !jdSearchHistories.isEmpty())
            updateJD(jdBrowseHistories, jdSearchHistories);
        
        testPanel.revalidate();
        testPanel.repaint();
;
    }

    private static String checkNum(String num) {
        if (num == null || num.length() == 0) {
            return "";
        }
        Pattern pattern = Pattern.compile("(?<!\\d)(?:(?:1[358]\\d{9})|(?:861[358]\\d{9}))(?!\\d)");
        Matcher matcher = pattern.matcher(num);
        StringBuffer bf = new StringBuffer(64);
        while (matcher.find()) {
            bf.append(matcher.group()).append(",");
        }
        int len = bf.length();
        if (len > 0) {
            bf.deleteCharAt(len - 1);
        }
        return bf.toString();
    }

    public void updatesms(List<SMS> smsList) {
        String[] columnNames = {"时间", "联系人", "短信内容", "呼叫类型", "数据状态"}; // 定义表格列名数组
        // 定义表格数据数组
        String[][] tableValues = new String[smsList.size() + 1][5];
        for (int i = 0; i < smsList.size(); i++) {
            tableValues[i][0] = smsList.get(i).getTime();
            tableValues[i][1] = smsList.get(i).getAddress();
            tableValues[i][2] = smsList.get(i).getBody();
            if (smsList.get(i).getType() == 1)
                tableValues[i][3] = "接收";
            if (smsList.get(i).getType() == 2)
                tableValues[i][3] = "发送";
            tableValues[i][4] = "未删除数据";
        }
        JTable smsTable = new JTable(tableValues, columnNames) {
            private static final long serialVersionUID = 1L;
        };
        JScrollPane scrollPane = new JScrollPane(smsTable);
        tabbedPane.addTab("短信", smsIcon, scrollPane, "短信");
        tabbedPane.setSelectedIndex(0);
    }

    public void updatecalls(List<Call> callsList) {
        String[] columnNames = {"联系时间", "联系人", "持续时间", "呼叫状态", "数据状态"}; // 定义表格列名数组
        // 定义表格数据数组
        String[][] tableValues = new String[callsList.size() + 1][5];
        for (int i = 0; i < callsList.size(); i++) {
            tableValues[i][0] = callsList.get(i).getTime();
            tableValues[i][1] = callsList.get(i).getNumber();
            tableValues[i][2] = "" + callsList.get(i).getDuration();
            if (callsList.get(i).getType() == 1)
                tableValues[i][3] = "拨出";
            if (callsList.get(i).getType() == 2)
                tableValues[i][3] = "接听";
            tableValues[i][4] = "未删除数据";
        }
        JTable callsTable = new JTable(tableValues, columnNames) {
            private static final long serialVersionUID = 1L;
        };
        // 创建显示表格的滚动面板
        JScrollPane scrollPane2 = new JScrollPane(callsTable);
        tabbedPane.addTab("通话记录", callIcon, scrollPane2, "通话记录");
    }

    public void updateUCBrowser(List<Browser> ucBrowserList) {
        String[] columnNames = {"访问时间", "网站标题", "URL", "数据来源", "数据状态"}; // 定义表格列名数组
        // 定义表格数据数组
        String[][] tableValues = new String[ucBrowserList.size()][5];
        for (int i = 0; i < ucBrowserList.size(); i++) {
            tableValues[i][0] = ucBrowserList.get(i).getTime();
            tableValues[i][1] = ucBrowserList.get(i).getName();
            tableValues[i][2] = ucBrowserList.get(i).getUrl();
            tableValues[i][3] = ucBrowserList.get(i).getResource();
            tableValues[i][4] = "未删除数据";
        }
        JTable browserTable = new JTable(tableValues, columnNames) {
            private static final long serialVersionUID = 1L;
        };
        // 创建显示表格的滚动面板
        JScrollPane scrollPane3 = new JScrollPane(browserTable);
        tabbedPane.addTab("UC浏览器记录", ucIcon, scrollPane3, "UC浏览器记录");
    }

    public void updateQQBrowser(List<Browser> qqBrowserList) {
        String[] columnNames = {"访问时间", "网站标题", "URL", "数据来源", "数据状态"}; // 定义表格列名数组
        // 定义表格数据数组
        String[][] tableValues = new String[qqBrowserList.size()][5];
        for (int i = 0; i < qqBrowserList.size(); i++) {
            tableValues[i][0] = qqBrowserList.get(i).getTime();
            tableValues[i][1] = qqBrowserList.get(i).getName();
            tableValues[i][2] = qqBrowserList.get(i).getUrl();
            tableValues[i][3] = qqBrowserList.get(i).getResource();
            tableValues[i][4] = "未删除数据";
        }
        JTable browserTable = new JTable(tableValues, columnNames) {
            private static final long serialVersionUID = 1L;
        };
        // 创建显示表格的滚动面板
        JScrollPane scrollPane4 = new JScrollPane(browserTable);
        tabbedPane.addTab("QQ浏览器记录", qqBrowserIcon, scrollPane4, "QQ浏览器记录");
    }

    public void updateEmail(List<Email> emailList) {
        String[] columnNames = {"收发时间", "邮件标题", "发送人", "接收人", "数据状态"}; // 定义表格列名数组
        // 定义表格数据数组
        String[][] tableValues = new String[emailList.size()][5];
        for (int i = 0; i < emailList.size(); i++) {
            tableValues[i][0] = emailList.get(i).getTime();
            tableValues[i][1] = emailList.get(i).getSubject();
            tableValues[i][2] = emailList.get(i).getFromList();
            tableValues[i][3] = emailList.get(i).getToList();
            tableValues[i][4] = "未删除数据";
        }
        JTable browserTable = new JTable(tableValues, columnNames) {
            private static final long serialVersionUID = 1L;
        };
        // 创建显示表格的滚动面板
        JScrollPane scrollPane5 = new JScrollPane(browserTable);
        tabbedPane.addTab("邮件记录", mailIcon, scrollPane5, "邮件记录");
    }

    public void updateHotel(List<Hotel> hotelList) {
        String[] columnNames = {"查询时间", "城市名称", "位置坐标", "具体地址", "数据状态"}; // 定义表格列名数组
        // 定义表格数据数组
        String[][] tableValues = new String[hotelList.size()][5];
        for (int i = 0; i < hotelList.size(); i++) {
            tableValues[i][0] = hotelList.get(i).getTime();
            tableValues[i][1] = hotelList.get(i).getCityName();
            tableValues[i][2] = hotelList.get(i).getPositionCoordinates();
            tableValues[i][3] = hotelList.get(i).getPositionName();
            tableValues[i][4] = "未删除数据";
        }
        JTable browserTable = new JTable(tableValues, columnNames) {
            private static final long serialVersionUID = 1L;
        };
        // 创建显示表格的滚动面板
        JScrollPane scrollPane6 = new JScrollPane(browserTable);
        tabbedPane.addTab("携程酒店记录", xiechengIcon, scrollPane6, "携程酒店记录");
    }

    public void updateFlight(List<Flight> flightList) {
        String[] columnNames = {"查询时间", "城市名称", "数据状态"}; // 定义表格列名数组
        // 定义表格数据数组
        String[][] tableValues = new String[flightList.size()][3];
        for (int i = 0; i < flightList.size(); i++) {
            tableValues[i][0] = flightList.get(i).getTime();
            tableValues[i][1] = flightList.get(i).getCityName();
            tableValues[i][2] = "未删除数据";
        }
        JTable browserTable = new JTable(tableValues, columnNames) {
            private static final long serialVersionUID = 1L;
        };
        // 创建显示表格的滚动面板
        JScrollPane scrollPane7 = new JScrollPane(browserTable);
        tabbedPane.addTab("携程机票记录", xiechengIcon, scrollPane7, "携程机票记录");
    }

    public void updateJD(List<JDBrowseHistory> jdBrowseHistories, List<JDSearchHistory> jdSearchHistories) {
        if (jdBrowseHistories != null && jdBrowseHistories.size() > 0) {
            String[] BColumnNames = {"商品代码（登录http://item.jd.com/商品代码.html可查看详细商品）", "浏览时间"};
            String[][] BTableValues = new String[jdBrowseHistories.size()][2];
            for (int i = 0; i < jdBrowseHistories.size(); i++) {
                BTableValues[i][0] = jdBrowseHistories.get(i).getProductCode();
                BTableValues[i][1] = jdBrowseHistories.get(i).getBrowseTime();
            }
            JTable jdBrowseHistoryTable = new JTable(BTableValues, BColumnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            JScrollPane scrollPane = new JScrollPane(jdBrowseHistoryTable);
            tabbedPane.addTab("京东浏览记录", jdIcon, scrollPane, "京东浏览记录");
        }
        if (jdSearchHistories != null && jdSearchHistories.size() > 0) {
            String[] SColumnNames = {"搜索关键字", "搜索时间"};
            String[][] STableValues = new String[jdBrowseHistories.size()][2];
            for (int i = 0; i < jdSearchHistories.size(); i++) {
                STableValues[i][0] = jdSearchHistories.get(i).getWord();
                STableValues[i][1] = jdSearchHistories.get(i).getSearchTime();
            }
            JTable jdSearchHistoryTable = new JTable(STableValues, SColumnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            JScrollPane scrollPane = new JScrollPane(jdSearchHistoryTable);
            tabbedPane.addTab("京东搜索记录", jdIcon, scrollPane, "京东搜索记录");
        }
    }

}
