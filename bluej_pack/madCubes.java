



import javax.swing.*;
import java.awt.EventQueue;

public class madCubes {
    static JFrame frame; 
    static int level;
    
    public static void main(String[] args) {
         new madCubes(0);
    }
    //просто создание GUI выносится в eventDispatchThread:  (EventQueue.invokeLater(Runnable))
    public madCubes(int level) {
        madCubes.level=level;
        frame = new JFrame("Test");
        //you know  new Thread(class implementing runnable).start()
        //just new thing in the event dispatch thread standing in the queue. WE have no queue, just make sure everything is in EDT
        EventQueue.invokeLater(new Runnable() {//anonymous class implementing runnable, so  it is executed by  a thread
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ex) {
                }

                
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(new MapPane());
                frame.pack();
                frame.setLocationRelativeTo(null);//makes frame be at screen center if arg==null
                frame.setVisible(true);

                
                //return;//does it give smth?
            }
        });
    }

    

}

