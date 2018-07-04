package src.cal_delta;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import xoc.dta.TestMethod;
import xoc.dta.datatypes.MultiSiteBoolean;

public class CalLossJudgeGui extends TestMethod {

    public MultiSiteBoolean results = new MultiSiteBoolean(false);
    public Integer maxCount = 1;
    private static Integer flowCount = 0;


    @Override
    public void execute() {
        // TODO Auto-generated method stub
        @SuppressWarnings("static-access")
        final String prjDir = context.workspace().getActiveProjectPath()+"/";
        final int[] activeSites = context.getActiveSites();


        if(flowCount < maxCount-1)
        {
            flowCount++;
        }
        else
        {
            flowCount =0 ;

            JButtonInfo mJButtonInfo = JButtonInfo.getInstance();
            final String textInfo = mJButtonInfo.ShowAllResult();
            CreateJFrame("ADVANTEST",results,prjDir,activeSites,textInfo);
        }
    }

//    class HomePanel extends JPanel{
//        ImageIcon icon;
//        Image img;
//        String prjDir = context.workspace().getActiveProjectPath()+"/";
//        public HomePanel(){
//            icon = new ImageIcon(prjDir+"images/PASS_JPG.jpg");
//            img= icon.getImage();
//        }
//        public void paintCompent(Graphics g){
//            super.paintComponent(g);
//            g.drawImage(img,0,0,this.getWidth(),this.getHeight(),this);
//        }
//    }


    @SuppressWarnings("static-access")
    public void CreateJFrame (String Text, MultiSiteBoolean rslt,String FilePath, int[] activeSites,final String textInfo) {

        JFrame jf = new JFrame(Text);
        JTabbedPane xxk = new JTabbedPane();
        JPanel jp1 = new JPanel();

        @SuppressWarnings("unused")
        JPanel jp2 = new JPanel();


        JButton jb;
        JLabel jl;
        JTextArea wbk = new JTextArea(textInfo);



        ImageIcon icon_1 = new ImageIcon(FilePath+"images/PASS_JPG.jpg");
        ImageIcon icon_2 = new ImageIcon(FilePath+"images/FAIL_JPG.jpg");
        icon_1 = new ImageIcon(icon_1.getImage().getScaledInstance(150, 100, Image.SCALE_DEFAULT));
        icon_2 = new ImageIcon(icon_2.getImage().getScaledInstance(150, 100, Image.SCALE_DEFAULT));
        int done = 0;
        for(int site : activeSites)
        {
            if(done ==0)
            {
                jp1.setLayout(new GridLayout(activeSites.length,2));
            }
            jl = new JLabel("Site "+site+": ",JLabel.CENTER);
            jl.setFont(new Font("Dialog",1,36));
            jp1.add(jl);


            if(rslt.get(site))
            {
                jb = new JButton(icon_1);
                jb.setBackground(Color.GREEN);

            }
            else
            {
                jb= new JButton(icon_2);
                jb.setBackground(Color.RED);
            }
            jp1.add(jb);

            done ++;


        }

        JScrollPane sp = new JScrollPane(wbk);

        xxk.add("Total",jp1); xxk.add("Detail",sp);
        jf.add(xxk,BorderLayout.CENTER);
        jf.setIconImage(new ImageIcon(FilePath+"images/Icon_JPG.jpg").getImage());
        jf.setSize(600,800);
        jf.setLocation(300,280);
        jf.setResizable(false);

        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);


    }

}
