import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import frame.MainFrame;
import org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper;
import org.jb2011.lnf.beautyeye.widget.N9ComponentFactory;

public class Load extends JFrame{
	static Load frame;
	static JPanel contentPane = new JPanel();
	private JTextField textField;
	private JPasswordField passwordField;

    CInstead c1 = new CInstead();
    private static final long serialVersionUID = 1L ;
    public static void main(String args[]) {
    	BeautyEyeLNFHelper.debug = true;
    	try{
    		BeautyEyeLNFHelper.launchBeautyEyeLNF();
    	}catch (Exception e1){
    		e1.printStackTrace();
    	}
    	frame = new Load();
    	frame.setVisible(true);
    }
    
    public Load() {
    	super();

    	
    	setContentPane(c1);
    	getContentPane().setFocusCycleRoot(true);
		getContentPane().setLayout(new BorderLayout(-10, 0));
		setTitle("用户登录");
		setBounds(100, 100, 485, 280);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().add(contentPane, BorderLayout.CENTER);
		
		contentPane.setOpaque(false);
		contentPane.setLayout(null);
		

    	
		JLabel label = new JLabel("取证精灵");
        label.setOpaque(true);

        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("华文彩云", Font.PLAIN, 50));
        label.setForeground(Color.WHITE);
        label.setBounds(6, 6, 422, 72);
        label.setOpaque(false);
        contentPane.add(label);
        
        JLabel label_1 = new JLabel("账号");
        label_1.setBounds(31, 90, 55, 18);
        label_1.setForeground(Color.WHITE);
        contentPane.add(label_1);
        
        JLabel label_2 = new JLabel("密码");
        label_2.setBounds(31, 134, 55, 18);
        label_2.setForeground(Color.WHITE);
        contentPane.add(label_2);
        
        textField = new JTextField("", 10);
        textField.setBounds(new Rectangle(83, 84, 184, 30));
        contentPane.add(textField);
        textField.setColumns(1);
        
        passwordField = new JPasswordField("", 10);
        passwordField.setBounds(new Rectangle(83, 128, 184, 30));
        contentPane.add(passwordField);
        passwordField.setColumns(1);
        passwordField.setEchoChar('*'); 
        passwordField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				String s = passwordField.getText();
        		if(s.length()>16)e.consume();
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
        });
        
        
        JButton button = new JButton("登录");
        button.setBounds(304, 84, 90, 30);
        contentPane.add(button);
        button.addActionListener(new LoadListener());
        
        JButton button_1 = new JButton("关闭");
        button_1.setBounds(304, 128, 90, 30);
        contentPane.add(button_1);
        setLocationRelativeTo(null);
        button_1.addActionListener(new ExitListener());
    
    }
    

    
    class CInstead extends JPanel  
    {   
 
		private static final long serialVersionUID = 1L;
	    ImageIcon icon;  
        Image img;  
        public CInstead()  
        {   
        	icon=new ImageIcon("images/loadbg.png");
            img=icon.getImage();  
            }   
        public void paintComponent(Graphics g)  
        {   
            super.paintComponent(g);  
            g.drawImage(img,0,0,null );  
        }   
    }   
    
    class LoadListener implements ActionListener {
    	@Override
    	public void actionPerformed(ActionEvent e) {
    		setVisible(false);
            /*JFrame frame = new MainFrame();
            frame.setVisible(true);*/
            try {
                Starter.main(null);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
    	}
    }
    
    class ExitListener implements ActionListener {
    	@Override
    	public void actionPerformed(ActionEvent e) {
    		setVisible(false);
    		System.exit(0);
    	}
    }
    

}
