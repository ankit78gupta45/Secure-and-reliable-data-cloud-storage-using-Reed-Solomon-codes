
package securestorage;


import com.dropbox.core.DbxException;
import com.microsoft.azure.storage.StorageException;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SecureStorage extends JPanel {
    public SecureStorage() throws SQLException, ClassNotFoundException, DbxException {
        super(new GridLayout(1, 1));
        
        try {
            JTabbedPane tabbedPane = new JTabbedPane();
            
            JPanel panel1 = new EncryptionAndUpload();
            panel1.setBackground(Color.white);
            tabbedPane.addTab("Encode And Upload ", null, panel1,
                    "Does nothing");
            tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
            
            JComponent panel2 = new DownloadAndDecode();
            panel2.setBackground(Color.white);
            
            tabbedPane.addTab("Download And Decode", null, panel2,
                    "Does twice as much nothing");
            tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
            //Add the tabbed pane to this panel.
            add(tabbedPane);
            
            //The following line enables to use scrolling tabs.
            tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        } catch (URISyntaxException | InvalidKeyException | StorageException ex) {
            Logger.getLogger(SecureStorage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }
    
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from
     * the event dispatch thread.
     */
    private static void createAndShowGUI() throws SQLException, ClassNotFoundException, DbxException {
        //Create and set up the window.
        JFrame frame = new JFrame("Reliable and Secure cloud Data Storage");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //Add content to the window.
        frame.add(new SecureStorage(), BorderLayout.CENTER);
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
		UIManager.put("swing.boldMetal", Boolean.FALSE);
                try {
                    createAndShowGUI();
                } catch (SQLException | ClassNotFoundException ex) {
                    Logger.getLogger(SecureStorage.class.getName()).log(Level.SEVERE, null, ex);
                } catch (DbxException ex) {
                    Logger.getLogger(SecureStorage.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

    }
}

