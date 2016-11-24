import frame.MainFrame;
import org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper;
import tool.HTTPServer;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by yellowsea on 2016/7/16.
 */
public class Starter {
    public static void main(String[] args) throws IOException {

        BeautyEyeLNFHelper.debug = true;
        try {
            BeautyEyeLNFHelper.launchBeautyEyeLNF();
            UIManager.put("RootPane.setupButtonVisible", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new MainFrame();
        frame.setVisible(true);
    }
}
