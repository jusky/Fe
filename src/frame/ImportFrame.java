package frame;

import jdk.nashorn.internal.scripts.JO;
import model.*;
import org.sqlite.SQLiteConfig;
import tool.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.List;

/**
 * Created by yellowsea on 2016/7/19.
 */
public class ImportFrame extends JFrame {
    private static final int WIDTH = 320;
    private static final int HEIGHT = 150;
    private MainFrame frame;

    public ImportFrame(final MainFrame frame) {
        this.frame = frame;
        setTitle("导入");
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((dimension.width - WIDTH) / 2, (dimension.height - HEIGHT) / 2, WIDTH, HEIGHT);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        //setResizable(false);

        final JPanel optionPanel = new JPanel();
        GridBagLayout gridBagLayout = new GridBagLayout();
        JPanel infoPanel = new JPanel(gridBagLayout);
        optionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "导入选项"));
        add(optionPanel, BorderLayout.NORTH);
        add(infoPanel);

        JRadioButton importNewCaseButton = new JRadioButton("新案件");
        JRadioButton importNewMobileButton = new JRadioButton("新手机");
        JRadioButton viewButton = new JRadioButton("查看案件");

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(importNewCaseButton);
        buttonGroup.add(importNewMobileButton);
        buttonGroup.add(viewButton);

        optionPanel.add(importNewCaseButton);
        optionPanel.add(importNewMobileButton);
        optionPanel.add(viewButton);

        importNewCaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
                setBounds((dimension.width - WIDTH) / 2, (dimension.height - (HEIGHT + 350)) / 2, WIDTH, HEIGHT + 350);
                GridBagConstraints c = new GridBagConstraints();

                String[] labelNames = {"案件名称", "案件编号", "手机号码", "手机IMEI", "机主姓名", "机主证件", "操作人员", "人员编号", "数据文件"};
                infoPanel.removeAll();
                for (String labelName : labelNames) {
                    c.fill = GridBagConstraints.NONE;
                    c.insets = new Insets(10, 10, 10, 0);
                    c.anchor = GridBagConstraints.WEST;
                    c.gridwidth = 1;
                    c.weightx = 0;
                    JLabel label = new JLabel(labelName);
                    gridBagLayout.setConstraints(label, c);
                    infoPanel.add(label);

                    c.fill = GridBagConstraints.HORIZONTAL;
                    c.insets = new Insets(1, 0, 1, 5);
                    c.gridwidth = 0;
                    c.weightx = 1;
                    JTextField textField = new JTextField();
                    gridBagLayout.setConstraints(textField, c);
                    infoPanel.add(textField);
                }
                JButton browseButton = new JButton("浏览...");
                JButton importButton = new JButton("创建新案例");
                c.insets = new Insets(10, 10, 10, 10);
                c.gridwidth = 1;
                gridBagLayout.setConstraints(browseButton, c);
                c.gridwidth = 0;
                gridBagLayout.setConstraints(importButton, c);
                infoPanel.add(browseButton);
                infoPanel.add(importButton);

                browseButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser chooser = new JFileChooser();
                        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        File f = null;
                        try {
                            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                f = chooser.getSelectedFile();
                                ((JTextField) infoPanel.getComponent(labelNames.length * 2 - 1)).setText(f.getPath());
                            }
                        } catch (HeadlessException ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                importButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Connection connection = null;
                        try {
                            Class.forName("org.sqlite.JDBC");
                            String caseName = ((JTextField) infoPanel.getComponent(1)).getText();
                            String caseId = ((JTextField) infoPanel.getComponent(3)).getText();
                            String operator_id = ((JTextField) infoPanel.getComponent(15)).getText();
                            String operator_name = ((JTextField) infoPanel.getComponent(13)).getText();
                            System.out.println(caseName + "  " + caseId);
                            if (caseName.equals("") || caseId.equals("")) {
                                throw new IllegalArgumentException("case");
                            }
                            File file = new File(caseId + "_" + caseName + ".ffd");
                            if (file.isFile()) {
                                int choice = JOptionPane.showConfirmDialog(null, "案件文件已存在。是否删除此文件并新建案件？", "警告", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                                if (choice == JOptionPane.YES_OPTION) {
                                    file.delete();
                                } else {
                                    dispose();
                                }
                            }
                            connection = DriverManager.getConnection("jdbc:sqlite:" + caseId + "_" + caseName + ".ffd");
                            Statement statement = connection.createStatement();
                            statement.executeUpdate(
                                    "CREATE TABLE db_info(" +
                                            "case_id TEXT," +
                                            "case_name TEXT," +
                                            "operator_id TEXT," +
                                            "operator_name TEXT);"
                            );
                            statement.executeUpdate(
                                    "CREATE TABLE phones(" +
                                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                            "phone_number TEXT," +
                                            "imei NUMERIC(15)," +
                                            "owner_name TEXT," +
                                            "owner_id TEXT," +
                                            "default_path TEXT);"
                            );
                            statement.executeUpdate(
                                    "CREATE TABLE contacts(" +
                                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                            "phone_id INTEGER," +
                                            "name TEXT," +
                                            "number TEXT," +
                                            "others TEXT);"
                            );
                            statement.executeUpdate(
                                    "CREATE TABLE calls(" +
                                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                            "phone_id INTEGER," +
                                            "number TEXT," +
                                            "date TEXT," +
                                            "duration INTEGER," +
                                            "type INTEGER);"
                            );
                            statement.executeUpdate(
                                    "CREATE TABLE messages(" +
                                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                            "phone_id INTEGER," +
                                            "number TEXT," +
                                            "status INTEGER," +
                                            "body TEXT," +
                                            "date TEXT);"
                            );
                            statement.executeUpdate(
                            		"CREATE TABLE ucbrowser(" +
                                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            		        "phone_id INTEGER," +
                                            "name TEXT," +
                            		        "url TEXT," +
                            		        "time TEXT);"
                            );
                            statement.executeUpdate(
                            		"CREATE TABLE qqbrowser(" +
                                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            				"phone_id INTEGER," +
                                            "name TEXT," +
                            				"url TEXT," +
                                            "time TEXT);"
                            );
                            statement.executeUpdate(
                            		"CREATE TABLE qqmail(" +
                                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            				"phone_id INTEGER," +
                                            "subject TEXT," +
                            				"fromList TEXT," +
                                            "toList TEXT," +
                            				"time TEXT);"
                            );
                            statement.executeUpdate(
                            		"CREATE TABLE ctrip_hotel(" +	
                            				"id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            				"phone_id INTEGER," +
                            				"cityName CHAR," +
                            				"airportCode CHAR," +
                            				"airportName INTEGER," +
                            				"date TEXT);"
                            );
                            statement.executeUpdate(
                            		"CREATE TABLE ctrip_flight(" +
                                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            				"phone_id INTEGER," +
                                            "cityName CHAR," +
                            				"date TEXT);"
                            );
                            statement.executeUpdate(
                            		"CREATE TABLE JDBrowseHistory(" +
                                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            				"phone_id INTEGER," +
                                            "productCode LONG," +
                            				"browseTime TEXT);"
                            );
                            statement.executeUpdate(
                            		"CREATE TABLE JDSearchHistory(" +
                                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            				"phone_id INTEGER," +
                                            "word TEXT," +
                            				"search_time TEXT);"
                            );
                            PreparedStatement prepStmt = connection.prepareStatement("INSERT INTO db_info VALUES (?,?,?,?);");
                            prepStmt.setString(1, caseId);
                            prepStmt.setString(2, caseName);
                            prepStmt.setString(3, operator_id);
                            prepStmt.setString(4, operator_name);
                            prepStmt.execute();

                            Phone phone = new Phone();
                            phone.setPhoneNumber(((JTextField) infoPanel.getComponent(5)).getText());
                            phone.setPhoneImei(((JTextField) infoPanel.getComponent(7)).getText());
                            phone.setOwnerName(((JTextField) infoPanel.getComponent(9)).getText());
                            phone.setOwnerId(((JTextField) infoPanel.getComponent(11)).getText());
                            insertData(((JTextField) infoPanel.getComponent(17)).getText(), file.getPath(), phone);
                            for (int i = 2; i < 10; i++) {
                                frame.menuPanel.getComponent(i).setEnabled(true);
                                frame.dbPath = caseId + "_" + caseName + ".ffd";
                            }

                        } catch (IllegalArgumentException ex) {
                            JOptionPane.showMessageDialog(null, "案件名称和案件编号不能为空！", "非法输入", JOptionPane.ERROR_MESSAGE);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        } finally {
                            try {
                                if (connection != null)
                                    connection.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            ((JRadioButton)frame.menuPanel.getComponent(2)).doClick();
                        }
                    }
                });
                revalidate();
                repaint();
            }

        });

        importNewMobileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GridBagConstraints c = new GridBagConstraints();
                Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
                setBounds((dimension.width - WIDTH) / 2, (dimension.height - (HEIGHT + 250)) / 2, WIDTH, HEIGHT + 250);
                String[] labelNames = {"手机号码", "手机IMEI", "机主姓名", "机主证件", "案件文件", "数据文件"};
                infoPanel.removeAll();
                for (String labelName : labelNames) {
                    c.fill = GridBagConstraints.NONE;
                    c.insets = new Insets(10, 10, 10, 10);
                    c.anchor = GridBagConstraints.WEST;
                    c.gridwidth = 1;
                    c.weightx = 0;
                    JLabel label = new JLabel(labelName);
                    gridBagLayout.setConstraints(label, c);
                    infoPanel.add(label);

                    c.fill = GridBagConstraints.HORIZONTAL;
                    c.insets = new Insets(1, 0, 1, 5);
                    c.gridwidth = 0;
                    c.weightx = 1;
                    JTextField textField = new JTextField();
                    gridBagLayout.setConstraints(textField, c);
                    infoPanel.add(textField);
                }
                JButton importCaseButton = new JButton("...");
                JButton importDataButton = new JButton("...");
                c.gridwidth = 1;
                gridBagLayout.setConstraints(infoPanel.getComponent(9), c);
                gridBagLayout.setConstraints(infoPanel.getComponent(11), c);
                c.gridwidth = 0;
                c.weightx = 0;
                gridBagLayout.setConstraints(importCaseButton, c);
                gridBagLayout.setConstraints(importDataButton, c);
                infoPanel.add(importCaseButton, 10);
                infoPanel.add(importDataButton, 13);
                JButton importButton = new JButton("导入");
                c.weightx = 1;
                c.insets = new Insets(10, 10, 10, 10);
                gridBagLayout.setConstraints(importButton, c);
                infoPanel.add(importButton);
                importCaseButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser fileChooser = new JFileChooser();
                        FileNameExtensionFilter filter = new FileNameExtensionFilter("取证精灵案件数据文件(*.ffd)", "ffd");
                        fileChooser.setFileFilter(filter);
                        File f = null;
                        try {
                            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                f = fileChooser.getSelectedFile();
                                ((JTextField) infoPanel.getComponent(9)).setText(f.getPath());
                            }
                        } catch (HeadlessException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                importDataButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser chooser = new JFileChooser();
                        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        File f = null;
                        try {
                            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                f = chooser.getSelectedFile();
                                ((JTextField) infoPanel.getComponent(12)).setText(f.getPath());
                            }
                        } catch (HeadlessException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                importButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Connection connection = null;
                        String dataPath = ((JTextField) infoPanel.getComponent(12)).getText();
                        String casePath = ((JTextField) infoPanel.getComponent(9)).getText();
                        Phone phone = new Phone();
                        phone.setPhoneNumber(((JTextField) infoPanel.getComponent(1)).getText());
                        phone.setPhoneImei(((JTextField) infoPanel.getComponent(3)).getText());
                        phone.setOwnerName(((JTextField) infoPanel.getComponent(5)).getText());
                        phone.setOwnerId(((JTextField) infoPanel.getComponent(7)).getText());
                        frame.dbPath = casePath;
                        try {
                            insertData(dataPath, casePath, phone);
                            for (int i = 2; i < 10; i++) {
                                frame.menuPanel.getComponent(i).setEnabled(true);
                            }
                            ((JRadioButton)frame.menuPanel.getComponent(2)).doClick();
                        } catch (IllegalArgumentException ex) {
                            JOptionPane.showMessageDialog(null, "请选择案例文件和手机数据文件！", "警告", JOptionPane.ERROR_MESSAGE);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        } finally {
                            try {
                                if (connection != null) {
                                    connection.close();
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
                revalidate();
                repaint();
            }
        });

        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GridBagConstraints c = new GridBagConstraints();
                Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
                setBounds((dimension.width - WIDTH) / 2, (dimension.height - (HEIGHT + 100)) / 2, WIDTH, HEIGHT + 100);
                infoPanel.removeAll();
                JLabel label = new JLabel("案件文件");
                JTextField casePath = new JTextField();
                JButton browseButton  = new JButton("...");
                JButton importButton = new JButton("查看");
                c.insets = new Insets(5,10,5,5);
                c.gridwidth = 1;
                c.fill = GridBagConstraints.NONE;
                gridBagLayout.setConstraints(label, c);
                infoPanel.add(label);
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weightx = 1;
                gridBagLayout.setConstraints(casePath, c);
                infoPanel.add(casePath);
                c.fill = GridBagConstraints.NONE;
                c.weightx = 0;
                c.gridwidth = 0;
                gridBagLayout.setConstraints(browseButton, c);
                infoPanel.add(browseButton);
                c.weightx = 1;
                c.fill = GridBagConstraints.HORIZONTAL;
                gridBagLayout.setConstraints(importButton, c);
                infoPanel.add(importButton);
                browseButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser fileChooser = new JFileChooser();
                        FileNameExtensionFilter filter = new FileNameExtensionFilter("取证精灵案件数据文件(*.ffd)", "ffd");
                        fileChooser.setFileFilter(filter);
                        File f = null;
                        try {
                            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                f = fileChooser.getSelectedFile();
                                casePath.setText(f.getPath());
                            }
                        } catch (HeadlessException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                importButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        frame.dbPath = casePath.getText();
                        for (int i = 2; i < 10; i++) {
                            frame.menuPanel.getComponent(i).setEnabled(true);
                        }
                        ((JRadioButton)frame.menuPanel.getComponent(2)).doClick();
                        dispose();
                    }
                });
                revalidate();
                repaint();
            }
        });
    }

    private void insertData(String dataPath, String casePath, Phone phone)  throws IllegalArgumentException {
        if (dataPath == null || casePath == null || dataPath.trim().isEmpty() || casePath.trim().isEmpty())
            throw new IllegalArgumentException("Empty string(s)");
        Connection connection = null;
        try {
            System.out.println(dataPath + "\n" + casePath);
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + casePath);
            Statement statement = connection.createStatement();

            PreparedStatement prep = connection.prepareStatement("INSERT INTO phones(phone_number,imei,owner_name,owner_id, default_path) VALUES (?,?,?,?,?)");
            System.out.println(phone.getPhoneNumber() + phone.getPhoneImei() + phone.getOwnerName() + phone.getOwnerId());
            prep.setString(1, phone.getPhoneNumber());
            prep.setString(2, phone.getPhoneImei());
            prep.setString(3, phone.getOwnerName());
            prep.setString(4, phone.getOwnerId());
            prep.setString(5, dataPath);
            prep.executeUpdate();

            ResultSet rs = statement.executeQuery("SELECT MAX(id) FROM phones;");
            int phoneId = rs.getInt(1);

            // Import Calls
            List<Call> callList = CallsFinder.findCalls(dataPath + "/contacts2.db");
            prep = connection.prepareStatement("INSERT INTO calls(phone_id, number, date, duration, type) VALUES (?,?,?,?,?);");
            connection.setAutoCommit(false);
            for (Call call : callList) {
                prep.setInt(1, phoneId);
                prep.setString(2, call.getNumber());
                prep.setString(3, call.getTime());
                prep.setInt(4, call.getDuration());
                prep.setInt(5, call.getType());
                prep.addBatch();
            }
            prep.executeBatch();
            connection.commit();
            prep.clearBatch();
            System.out.println("Import Call Finished.");

            // Import Messages
            List<SMS> smsList = SMSFinder.findSMS(dataPath + "/mmssms.db");
            prep = connection.prepareStatement("INSERT INTO messages(phone_id, number, status, body, date) VALUES (?,?,?,?,?);");
            for (SMS sms : smsList) {
                prep.setInt(1, phoneId);
                prep.setString(2, sms.getAddress());
                prep.setInt(3, sms.getType());
                prep.setString(4, sms.getBody());
                prep.setString(5, sms.getTime());
                prep.addBatch();
            }
            prep.executeBatch();
            connection.commit();
            prep.clearBatch();
            System.out.println("Import messages finished");

            // Import contacts
            List<Contact> contactList = ContactsFinder.findContacts(dataPath + "/contacts2.db");
            prep = connection.prepareStatement("INSERT INTO contacts(phone_id, name, number) VALUES (?,?,?);");
            for (Contact contact : contactList) {
                prep.setInt(1, phoneId);
                prep.setString(2, contact.getLabel());
                prep.setString(3, contact.getNumber());
                prep.addBatch();
            }
            prep.executeBatch();
            connection.commit();
            prep.clearBatch();
            System.out.println("Import contacts finished");
            
            //Import UCBrowser
            List<Browser> browserList = UCBrowserFinder.findUCBrowser(dataPath + "/history.db");
            prep = connection.prepareStatement("INSERT INTO ucbrowser(phone_id, name, url, time) VALUES (?,?,?,?);");
            for (Browser browser : browserList) {
            	prep.setInt(1, phoneId);
            	prep.setString(2, browser.getName());
            	prep.setString(3, browser.getUrl());
            	prep.setString(4, browser.getTime());
            	prep.addBatch();
            }
            prep.executeBatch();
            connection.commit();
            prep.clearBatch();
            System.out.println("Import ucbrowser finished");
            
            //Import QQBrowser
            List<Browser> browserList2 = QQBrowserFinder.findQQBrowser(dataPath + "/database.db");
            prep = connection.prepareStatement("INSERT INTO qqbrowser(phone_id, name, url, time) VALUES (?,?,?,?);");
            for (Browser browser : browserList2){
            	prep.setInt(1, phoneId);
            	prep.setString(2, browser.getName());
            	prep.setString(3, browser.getUrl());
            	prep.setString(4, browser.getTime());
            	prep.addBatch();
            }
            prep.executeBatch();
            connection.commit();
            prep.clearBatch();
            System.out.println("Import qqbrowser finished");
            
            //Import qqmail
            List<Email> emailList = EmailFinder.findEmail(dataPath + "/EmailProvider.db");
            prep = connection.prepareStatement("INSERT INTO qqmail(phone_id, subject, fromList, toList, time) VALUES (?,?,?,?,?);");
            for (Email email : emailList){
            	prep.setInt(1, phoneId);
            	prep.setString(2, email.getSubject());
            	prep.setString(3, email.getFromList());
            	prep.setString(4, email.getToList());
            	prep.setString(5, email.getTime());
            	prep.addBatch();
            }
            prep.executeBatch();
            connection.commit();
            prep.clearBatch();
            System.out.println("Import qqmail finished");
            
            //Import ctrip_hotel
            List<Hotel> hotelList = HotelFinder.findHotel(dataPath + "/ctrip_hotel.db");
            prep = connection.prepareStatement("INSERT INTO ctrip_hotel(phone_id, cityName, airportCode, airportName, date) VALUES (?,?,?,?,?);");
            for (Hotel hotel : hotelList){
            	prep.setInt(1, phoneId);
            	prep.setString(2, hotel.getCityName());
            	prep.setString(3, hotel.getPositionCoordinates());
            	prep.setString(4, hotel.getPositionName());
            	prep.setString(5, hotel.getTime());
            	prep.addBatch();
            }
            prep.executeBatch();
            connection.commit();
            prep.clearBatch();
            System.out.println("Import hotel finished");
            
            //Import ctrip_flight
            List<Flight> flightList = FlightFinder.findFlight(dataPath + "/ctrip_flight.db");
            prep = connection.prepareStatement("INSERT INTO ctrip_flight(phone_id, cityName, date) VALUES (?,?,?);");
            for (Flight flight : flightList){
            	prep.setInt(1, phoneId);
            	prep.setString(2, flight.getCityName());
            	prep.setString(3, flight.getTime());
            	prep.addBatch();
            }
            prep.executeBatch();
            connection.commit();
            prep.clearBatch();
            System.out.println("Import flight finished");
            
            //Import JDBrowseHistory
            List<JDBrowseHistory> JDBrowseHistoryList = JDBrowseHistoryFinder.findJDBroseHistory(dataPath + "/jd.db");
            prep = connection.prepareStatement("INSERT INTO JDBrowseHistory(phone_id, productCode, browseTime) VALUES (?,?,?);"); 
            for (JDBrowseHistory JDBrowseHistory : JDBrowseHistoryList){
            	prep.setInt(1, phoneId);
            	prep.setString(2, JDBrowseHistory.getProductCode());
            	prep.setString(3, JDBrowseHistory.getBrowseTime());
            	prep.addBatch();
            }
            prep.executeBatch();
            connection.commit();
            prep.clearBatch();
            System.out.println("Import jdbrowsehistory finished");
          
            //Import JDSearchHistory
            List<JDSearchHistory> JDSearchHistoryList = JDSearchHistoryFinder.findJDSearchHistory(dataPath + "/jd.db");
            prep = connection.prepareStatement("INSERT INTO JDSearchHistory(phone_id, word, search_time) VALUES (?,?,?);");
            for (JDSearchHistory JDSearchHistory : JDSearchHistoryList){
            	prep.setInt(1, phoneId);
            	prep.setString(2, JDSearchHistory.getWord());
            	prep.setString(3, JDSearchHistory.getSearchTime());
            	prep.addBatch();
            }
            prep.executeBatch();
            connection.commit();
            prep.clearBatch();
            System.out.println("Import jdsearchhistory finished");

            //frame.setData(smsList, callList, null, null, null, null, null);

            JOptionPane.showMessageDialog(null, "导入成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
