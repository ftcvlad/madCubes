
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.*;
import java.util.ArrayList;
import java.math.*;
import java.util.Random;//if we do both util.* and swing.* Timers may fail. BEWARE mCduff
import java.util.*;


public class MapPane extends JPanel implements KeyListener{

            private BufferedImage  foreground, foreforeground, magicBoxGround, infoGround;
            static Image background;
             
            int scissorsSize=10;
            Area visible;
            int placeToDrawX=-scissorsSize;
            int placeToDrawY=-scissorsSize;
            Rectangle2D.Float scissors = new Rectangle2D.Float(placeToDrawX, placeToDrawY,scissorsSize-1,scissorsSize-1);//random coordinates, random size. just create
            static Area blackArea, blackAreaAdjustedForOffset;
            Area greenArea=new Area();
            static Area redArea=new Area();
            
            int direction;
            
            Point offset = new Point(1,1);//just initialize offset for some random point
            Point to4kaOts4eta=new Point(1,1);
            ArrayList<Point> pointsArr = new ArrayList<Point>();
            
    
            boolean currentlyCrossBlack=false; 
            boolean firstTimeIntersectsBlack=true;
            
            Dimension finalDimension;
           
            ArrayList<magicBox> magicBoxesArray =new  ArrayList<magicBox>();
           static Graphics2D g2inMagic;
           
           ArrayList<Integer> hashCodesForAreas = new ArrayList<Integer>();
           int initialAreasArea=0;
           int totalSubtractedArea=0;
           int areaCutByGreen=0;
           static Graphics2D g2info; 
           int nextLevel=0;
           static int messWidth;
           double target;//% of area to subtract
           
           Timer t;
           
            public MapPane() {
                String name="img/pic"+madCubes.level+".jpg";
                System.out.println(javax.swing.SwingUtilities.isEventDispatchThread());
                try {
                    
                    background = ImageIO.read(getClass().getResource(name));//URL object as an argument. Can be file or inputstream
                    } catch (Exception e) {
                    e.printStackTrace();
                }
                   
                /*
                           class PrimeRun implements Runnable {
                     
                     PrimeRun() {
                         
                     }
            
                     public void run() {
                         System.out.println(javax.swing.SwingUtilities.isEventDispatchThread());
                     }
                 }
                PrimeRun p = new PrimeRun();
                new Thread(p).start();*/
                 
                 
                
                    //get smaller image with the same aspectRatio if it is too big to fit 0.8x0.8 of screen
                    Dimension imgSize = new Dimension(background.getWidth(null), background.getHeight(null));
                    Dimension screenDimension=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
                    Dimension boundary=new Dimension ((int)(screenDimension.getWidth()*0.8),(int)(screenDimension.getHeight()*0.8));
                    finalDimension = getMaxRightDimension(imgSize,boundary);
                    background =  background.getScaledInstance((int)finalDimension.getWidth(),(int)finalDimension.getHeight(),Image.SCALE_SMOOTH);
                    
                    initialAreasArea=background.getWidth(null)*background.getHeight(null);
                    
                    
                    foreground = new BufferedImage(background.getWidth(null), background.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                    int rgb=Color.BLACK.getRGB();
                    for (int i=0;i<background.getWidth(null);i++){
                        for (int j=0;j<background.getHeight(null);j++){
                            foreground.setRGB(i,j,rgb);
                        }
                    }
                    Rectangle2D.Float rrr=new Rectangle2D.Float(0,0,background.getWidth(null),background.getHeight(null));
                    blackArea=new Area(rrr);
                    
                   /* polygon 5x5 , but area 4x4 
                    
                        int[] xpoints={50,54,54,50};
                        int[] ypoints={50,50,54,54};
                        int npoints=4;
                        Polygon p=new Polygon(xpoints,ypoints,npoints);
                        
                        
                        visible= new Area(p);
                       
                        blackArea.subtract(visible);
                    
                    */
                    
                    foreforeground = new BufferedImage(background.getWidth(null), background.getHeight(null), BufferedImage.TYPE_INT_ARGB);
    
                    magicBoxGround = new BufferedImage(background.getWidth(null), background.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                    g2inMagic = magicBoxGround.createGraphics();
                    
                   
                   //create magicBoxes with parameters
                   initializeMagicBoxes(madCubes.level);
                   
             
                   
                   infoGround = new BufferedImage(background.getWidth(null), background.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                   g2info = infoGround.createGraphics();
                  // g2info.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                   
                   Font f=new Font("Arial",Font.BOLD,15); 
                   g2info.setFont(f);
                   FontMetrics fm = g2info.getFontMetrics(f);
                   messWidth = fm.stringWidth("YOU LOOSE");//width of message when you loose, captain vlad

                   target=(madCubes.level==0)?0.05:0.1;
                    
                   g2info.setBackground(new Color(255,255,255,0));
                   g2info.setColor(new Color(255,128,64));
                   
                   drawPercentCut();
                    
                    
                 //th=new timerHandler();
                 ActionListener mainTimerListener=new ActionListener(){
                                
                                public void actionPerformed(ActionEvent actionEvent) {
                                   for (int j=0;j<magicBoxesArray.size();j++){
                                       magicBoxesArray.get(j).doMagicBoxStuff();
                                    }
                                    repaint();
                          }

                    };
                     t = new Timer(1, mainTimerListener);
                     t.setInitialDelay(0);
                     t.start();
                 
                 
                 
                 
                    
                
    
                MouseAdapter mouseHandler = new MouseAdapter() {//we EXTEND MouseAdapter here
                    //After NEW The name of an interface to implement or a class to extend. In this example,
                    //the anonymous class is extending abstract class MouseAdapter.
                    //Because an anonymous class definition is an expression, it must be part of a statement.
                    //This explains why there is a semicolon after the closing brace.
                    private Point startPoint;
                    int buttonPressed;
                    
                    //these variables are for button3
                    int signX=1;
                    double yCoord;
                    double xDiff;
                    double slope;
                    Ellipse2D.Double elli=new Ellipse2D.Double(0,0, 20 ,20  );
                    
                    
                    @Override
                    public void mousePressed(MouseEvent e) {
                        startPoint = e.getPoint();
                        buttonPressed=e.getButton();
                    }
    
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        startPoint = null;
                    }
    
                    @Override
                    public void mouseDragged(MouseEvent e) {
                        Point endPoint = e.getPoint();
                        Graphics2D g2d = foreground.createGraphics();//Creates a Graphics2D, which can be used to draw into this BufferedImage.
                        
                         offset = getOffset();
    
                        
                        Point from = new Point(startPoint);
                        //mouse coordinates are counted basing on JPanel, not image size, BUT draw() is called basing on foreground
                        //because g2d was obtained from it, so we should subtract offset from mouse coordinates. ЧЕ ТО в этом роде 
                        from.translate(-offset.x, -offset.y);
                        Point to = new Point(endPoint);
                        to.translate(-offset.x, -offset.y);
                        
                        g2d.setColor(Color.RED);
                        
                      
                        //g2d.setStroke(new ShapeStroke(new Shape[] {new Ellipse2D.Float( 0, 0, 60, 2),new Ellipse2D.Float(0, 0, 4, 4)},15.0f));
                        //g2d.setStroke( new CompositeStroke( new BasicStroke( 10f ), new BasicStroke( 0.5f ) ) );
                        
                        //with stroke it freezes much more
                        //g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                          int[] Xs=new int[4];
                          int[] Ys=new int[4];
                        
                         if (buttonPressed == MouseEvent.BUTTON1) {
                             Line2D.Float lineToAdd=new Line2D.Float(from,to);
                             g2d.draw(lineToAdd);
                             
                             
                            }
                         else if(buttonPressed == MouseEvent.BUTTON3){
                            //Ellipse2D.Float elli=new Ellipse2D.Float(from.x,from.y,Math.abs(from.x-to.x)  ,Math.abs(from.y-to.y)  );
                            //g2d.draw(elli);
                           //redArea.add(new Area(elli));
                           
                         
                           Xs[0]=startPoint.x;
                           Ys[0]=startPoint.y;
                           Xs[1]=startPoint.x-1;
                           Ys[1]=startPoint.y-1;
                           
                           Xs[2]=endPoint.x;
                           Ys[2]=endPoint.y;
                           Xs[3]=endPoint.x-1;
                           Ys[3]=endPoint.y-1;
                           
                           
                           Polygon p=new Polygon(Xs,Ys,4);
                           g2d.draw(p);
                           redArea.add(new Area(p));
                                
                         }
                         else if (buttonPressed==MouseEvent.BUTTON2){
                              xDiff=startPoint.x-endPoint.x;
                              slope = ((startPoint.y-endPoint.y)*1.0)/(startPoint.x-endPoint.x);
                             if (startPoint.x>endPoint.x){
                                 signX=-1;
                                }
                                
                             for (double xCoord=0;xCoord<=Math.abs(xDiff);xCoord=xCoord+20*signX){
                                   if (Math.abs(xCoord)>Math.abs(xDiff)){
                                     break;
                                    }
                                 
                                 yCoord=xCoord*slope;
                                 
                                 
                                 //elli=new Ellipse2D.Double(xCoord+startPoint.x,yCoord+startPoint.y, 20 ,20  );
                                 elli.setFrameFromCenter(xCoord+startPoint.x,yCoord+startPoint.y,xCoord+startPoint.x-10  ,yCoord+startPoint.y-10);
                                 g2d.draw(elli);
                                 redArea.add(new Area(elli));
                                }
                            }
                        g2d.dispose();//g2d are created too often, so to optimise garbage collection
                        startPoint = endPoint;
                        
                    }
                };
                setFocusable(true);
                requestFocusInWindow(); 
                addMouseListener(mouseHandler);
                addMouseMotionListener(mouseHandler);
                addKeyListener(this);
            }
    
            
            public void initializeMagicBoxes(int level){
                
                // magicBox(double x, double y, double dx, double dy,  int red, int green, int blue, int alpha, int parabolaMode){
                   //последние 4 числа--цвет которым будем закрашивать старые прямоугольники. альфа 0=безцветным
                if (level==0){
                   magicBox magicBox1=new magicBox(greenArea,300,300,0.2,1,200, 154, 125, 0, 1);
                   magicBox magicBox2=new magicBox(greenArea,50,50,1,1, 142, 1, 12, 200,2);
                   
                   magicBox magicBox3=new magicBox(greenArea,500,100,1,1, 128, 128, 255,0,3);
                   magicBox magicBox4=new magicBox(greenArea,400,20,-0.5,-0.9, 128, 128, 255,0,1);
                   magicBox magicBox5=new magicBox(greenArea,10,10,-0.2,1, 128, 128, 255,0,1);
                  
                   magicBoxesArray.add(magicBox1);
                   magicBoxesArray.add(magicBox2);
                   magicBoxesArray.add(magicBox3);
                   magicBoxesArray.add(magicBox4);
                   magicBoxesArray.add(magicBox5);
                }
                else if(level==1){
                   magicBox magicBox1=new magicBox(greenArea,300,300,0.2,1,200, 154, 125, 0, 1);
                   magicBox magicBox2=new magicBox(greenArea,50,50,10,1, 142, 1, 12, 120,2);
                   magicBox magicBox3=new magicBox(greenArea,500,100,1,1, 128, 128, 255,0,3);
                   magicBox magicBox4=new magicBox(greenArea,400,20,-0.5,-0.9, 128, 128, 255,0,1);
                   magicBox magicBox5=new magicBox(greenArea,10,10,-0.2,1, 128, 128, 255,0,1);
                   magicBox magicBox6=new magicBox(greenArea,155,200,2,-0.5, 128, 128, 255,0,1);
                   magicBox magicBox7=new magicBox(greenArea,200,155,-3,3, 128, 128, 255,0,1);
                   magicBox magicBox8=new magicBox(greenArea,400,400,1,2, 128, 128, 255,0,1);
                   
                   magicBoxesArray.add(magicBox1);
                   magicBoxesArray.add(magicBox2);
                   magicBoxesArray.add(magicBox3);
                   magicBoxesArray.add(magicBox4);
                   magicBoxesArray.add(magicBox5);
                   magicBoxesArray.add(magicBox6);
                   magicBoxesArray.add(magicBox7);
                   magicBoxesArray.add(magicBox8);
                   
                    
                }
                else if (level==2){
                    
                   
                    for (int b=0;b<60;b++){
                       magicBox bbb=new magicBox(greenArea,5+(b)*7,140,1,1, 128, 128, 255,0,1);
                       magicBoxesArray.add(bbb);
                       
                       
                       
                    }
                    
                    
                }
                else if(level==3){
                    
                    
                     for (int b=0;b<50;b++){
                       magicBox bbb=new magicBox(greenArea,5+(b)*7,140,-2,1, 128, 128, 255,0,3);
                       magicBoxesArray.add(bbb);
                       
                       
                       
                    }
                    
                    
                    
                    
                }
                
                
                
                
            }
            
            
            
        public Dimension getMaxRightDimension(Dimension imgSize, Dimension boundary){
            int original_width = imgSize.width;
            int original_height = imgSize.height;
            
            int bound_width = boundary.width;
            int bound_height = boundary.height;
            
            int new_width = original_width;
            int new_height = original_height;
        
            if (original_width > bound_width) {
                
                new_width = bound_width;
                new_height = (new_width * original_height) / original_width;
            }
        
            if (new_height > bound_height) {
                new_height = bound_height;
                new_width = (new_height * original_width) / original_height;
            }
            return new Dimension(new_width, new_height);
        }

        public void keyReleased(KeyEvent event){
        }
        
        public void keyPressed(KeyEvent event){
            ////0==vpravo, 1== vverh, 2==vlevo, 3==vniz  !!!!!!!!!!!!!!!!
            //System.out.println("bebe");
            if (event.getKeyCode() == KeyEvent.VK_RIGHT){
                placeToDrawX+=7;
                direction=0;
            }
            else if (event.getKeyCode() == KeyEvent.VK_LEFT){
                placeToDrawX-=7;
                direction=2;
            }
             else if (event.getKeyCode() == KeyEvent.VK_UP){
                  placeToDrawY-=7;
                  direction=1;
            }
             else if (event.getKeyCode() == KeyEvent.VK_DOWN){
                 placeToDrawY+=7;
                 direction=3;
            }
            
 
            Graphics2D g2d = foreforeground.createGraphics();
            Area rectangleArea;
            
            if (blackAreaAdjustedForOffset.intersects(offset.x+placeToDrawX,offset.y+placeToDrawY,scissorsSize,scissorsSize)){
                
                currentlyCrossBlack=true;
                
                //here we change foreforeground. in paintComponent we just draw it.
                //Graphics2D g2d = foreforeground.createGraphics();//Creates a Graphics2D, which can be used to draw into this BufferedImage.
                g2d.setColor(Color.GREEN);
                g2d.fillRect(placeToDrawX,placeToDrawY, scissorsSize,scissorsSize);//no offset as this is just drawing IN forefore
                
                
                Rectangle2D.Float zzz=new Rectangle2D.Float(placeToDrawX,placeToDrawY, scissorsSize,scissorsSize);
                rectangleArea = new Area(zzz);
                
                //only that part of square that intersects black is left
                rectangleArea.intersect(blackArea);
                
                //calculate area cut by green. this is close, but NOT exact
                Area newGreenArea=new Area(rectangleArea);
                newGreenArea.subtract(greenArea);
                Rectangle2D newCutRectangle =  newGreenArea.getBounds2D();
                areaCutByGreen=areaCutByGreen+((int)(newCutRectangle.getWidth()*newCutRectangle.getHeight()));
                
                
                greenArea.add(rectangleArea);
                //we intersect blackArea for the 1st time 
                    if (firstTimeIntersectsBlack==true){
                        firstTimeIntersectsBlack=false;
                        //we find middle dot in the area of intersection
    
                       Rectangle2D rec = rectangleArea.getBounds2D();
                       int to4kaX = ((int)rec.getX())+((int)rec.getWidth())/2;
                       int to4kaY = ((int)rec.getY())+((int)rec.getHeight())/2;
                       
                       
                       to4kaOts4eta=new Point(to4kaX, to4kaY); 
                        
    
                }
            }
            else{
                 
                int subtractedAreasArea=0;
                //this is when scissors first leave black area. currentlyCrossBlack was still true 
                if (currentlyCrossBlack==true){
                     currentlyCrossBlack=false;
                    //we don't need this as we draw only green in it, but these regions are invisible anyway
                    //foreforeground = new BufferedImage(background.getWidth(null), background.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                    
                   
                    firstTimeIntersectsBlack=true;
                    blackArea.subtract(greenArea);
                    
                    
                      
                    //we find all borderpoints for all resulting areas
                    findStartPoints(to4kaOts4eta);
                   
                    //now go around border and determine vertices (robotJack is a rectangle 1x1 that travels so to have 2 or 3 pixels belonging
                    //to blackArea around him. He travels until finds start pixel
                    //we look for black area around only if there is any black area, that is if pointsArr.size()>0
                        for (int anotherStartPoint=0; anotherStartPoint<pointsArr.size();anotherStartPoint++){
                          
                            //we calculate vertices of black area and return them in array
                           ArrayList<Point> newVertices= calculateAdditionalArea(anotherStartPoint);
                           
                    
                        
                        
                        
                        
                        int[] Xs=new int[newVertices.size()];
                        int[] Ys=new int[newVertices.size()];
                        int hashCodeForArea=0;
                        for (int j=0;j<newVertices.size();j++){
                            Xs[j]=newVertices.get(j).x;
                            Ys[j]=newVertices.get(j).y;
                            //we sum all vertices' x and y to get UNIQUE about area. if 2 areas are same, disregard 1 
                            hashCodeForArea=hashCodeForArea+newVertices.get(j).x+newVertices.get(j).y;
                        }
                        boolean oldHashCode = chechIfOldHashCode(hashCodeForArea);
                        //if same area || same, but already deleted area
                        if(oldHashCode==true || hashCodeForArea==0){
                            continue;
                        }
                        else{
                            hashCodesForAreas.add(hashCodeForArea);
                        }
                        
                        
                        int currPolygonArea = calculatePolygonArea(Xs,Ys,newVertices.size());

                        Polygon p=new Polygon(Xs,Ys,newVertices.size());
                        Area noPixel=new Area(p);
                        //извращение. Добавляем недостающий пиксель слепливая смещенные площади. Почему его недостает хз. :(
                        Area hasAllPixels = removeONEpixelAtBorder(noPixel);
                        
                        boolean dontDeleteMe=false;
                            for (int v=0;v<magicBoxesArray.size();v++){
                            if (hasAllPixels.contains(magicBoxesArray.get(v).x,magicBoxesArray.get(v).y, magicBoxesArray.get(v).width,magicBoxesArray.get(v).height)){
                                //if area contains at least 1 box
                                dontDeleteMe=true;
                                break;
                            }
                            else{
                                
                            }
                             
                        }
                        
                        
                        if (!dontDeleteMe==true){
                            blackArea.subtract(hasAllPixels);
                            subtractedAreasArea+=currPolygonArea;
                        }
                        
                    }
                    
                    
                    
                    totalSubtractedArea+=areaCutByGreen+subtractedAreasArea;
                   
                    drawPercentCut();
                    //if cut area is big enough, wait some sec to enjoy pic and go to new level
                     if (totalSubtractedArea/(initialAreasArea*1.0)>target){//>target
                
                         
                         blackArea=new Area();
                        
                          ActionListener taskPerformer = new ActionListener() {
                              public void actionPerformed(ActionEvent evt) {
                                  t.stop();
                                  
                                  
                                 /*
                                  Set<Thread> threadSet = Thread.getAllStackTraces().keySet();


                                  Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
                                  for (int j=0;j<threadArray.length;j++){
                                      System.out.println(threadArray[j]);
                                      
                                      
                                    }
                                    System.out.println();*/
                                  
                                  
                                   madCubes.frame.dispose();
                                  new madCubes((madCubes.level+1)%4);
                                  
                                 
                              }
                          };
                          Timer tim = new Timer(3000, taskPerformer);
                          tim.setRepeats(false);
                          tim.start();

                   }
                    
                    
                    
                    //add it back if we want green not to be deleted from black. this block happens only if there are black areas around green
                    /*
                    if (pointsArr.size()>0)  {
                        
                            blackArea.add(greenArea); 
                            totalSubtractedArea-=areaCutByGreen;
                    }*/
                             
                   //we don't need old pointsArr, we have newVertices. HURRAY
                    pointsArr=new ArrayList<Point>(); 
                     
                   
                    subtractedAreasArea=0;
                    areaCutByGreen=0;
                    
                    greenArea.reset();
                }
                
                
            }
            
            //every 10 millisecs secs it is repainted by timer, so mb no need
            //repaint();
        }
        //этот метод просьбы убрать как только пойму какого черта Area не включает все вершины of polygon (нет 1 пикс справа и сниз
        public Area removeONEpixelAtBorder(Area noPixel){
            AffineTransform vladko=new AffineTransform();
                        
            vladko.setToTranslation(0,1);
            noPixel.add(noPixel.createTransformedArea(vladko));
            
            vladko.setToTranslation(1,0);
            noPixel.add(noPixel.createTransformedArea(vladko));
            
            return noPixel;
            
        }
        
        public void drawPercentCut(){
            BigDecimal bd = new BigDecimal(totalSubtractedArea/(initialAreasArea*1.0));
            bd = bd.setScale(6, RoundingMode.HALF_UP);
            //String str = bd.doubleValue()+"";
            g2info.clearRect(0,0,200,80);
            g2info.drawString(new String("Target: "+target),5,13);
            g2info.drawString( new String("Cut     : "+bd.doubleValue()+""), 5, 28);
        }
        
        public boolean chechIfOldHashCode(int hashCodeForArea){
            for (int j=0;j<hashCodesForAreas.size();j++){
                            if (hashCodesForAreas.get(j)==hashCodeForArea){
                                return true;
                            }
                        }
            return false;
        }
        
        public int calculatePolygonArea(int[] Xs,int[] Ys,int npoints){
            int sum = 0;
            for (int i = 0; i < npoints ; i++)
            {
                sum = sum + Xs[i]*Ys[(i+1)%npoints] - Ys[i]*Xs[(i+1)%npoints];
            }
            return Math.abs(sum / 2);
        }
        
        
        public void keyTyped(KeyEvent event){ 
        }
        
          
        public void findStartPoints(Point to4kaOts4eta){
            int x=to4kaOts4eta.x;
            int y=to4kaOts4eta.y;
            
            
            do{
                x++;
            }while( greenArea.contains(x,y));
            if (blackArea.contains(x,y)){
                pointsArr.add(new Point(x,y));
            }
            x=to4kaOts4eta.x;
            
            
            do{
                x--;
            }while( greenArea.contains(x,y));
            if (blackArea.contains(x,y)){
                pointsArr.add(new Point(x,y));
            }
            x=to4kaOts4eta.x;
            
            do{
                y++;
            }while( greenArea.contains(x,y));
            if (blackArea.contains(x,y)){
                pointsArr.add(new Point(x,y));
            }
            y=to4kaOts4eta.y;
            
            do{
                y--;
            }while( greenArea.contains(x,y));
            if (blackArea.contains(x,y)){
                pointsArr.add(new Point(x,y));
            }
            
            
        }
        
        public ArrayList<Point> calculateAdditionalArea(int anotherStartPoint){
            
            
            ArrayList<Point> newVertices = new ArrayList<Point>();
            Point startPoint=new Point(pointsArr.get(anotherStartPoint));
            
            Point currPoint=new Point(pointsArr.get(anotherStartPoint));
            int ind=0;
            boolean badBadAngle=false;
            int nomerSmeni=0; //1st time change direction to 1st side, 2nd time to 2nd, but NOT to the opposite
            
            int currDirection=-1;//make sure at first curr!=next
            int nextDirection=0;//rnd direction. here to the right
            boolean success;
            int shitfuck=0;
                    do{
                     
                    ind=0;
                    
                    //0==right, 1== up, 2==left, 3==down  !!!!!!!!!!!!!!!!
                    success=false;
                    nomerSmeni=0;
                    while(success!=true){
                        
                        int x=currPoint.x;
                        int y=currPoint.y;
                        if (nextDirection==0){
                            x++;
                        }
                        else if (nextDirection==1){
                            y--;
                        }
                        else if (nextDirection==2){
                            x--;
                        }
                        else if (nextDirection==3){
                            y++;
                        }
                        else{
                        }
                    
                        if (blackArea.contains(x+1,y)){
                                ind++;
                            }
                        
                        if (blackArea.contains(x-1,y)){
                            ind++;
                        }
                        if (blackArea.contains(x,y+1)){
                            ind++;
                        }
                        if (blackArea.contains(x,y-1)){
                            ind++;
                        }
                    
                        
                        
                        //to avoid problems with badbad angles
                        if ((ind==4 && nextDirection==currDirection)||(ind==4 && shitfuck==1)){
                            //we can make it 0 here, as if we crossed green, we will encounter ind==4 ALWAYS
                            shitfuck=0;
                            if (badBadAngle==false){
                                badBadAngle=true;
                                ind--;
                                
                            }
                            else{
                               badBadAngle=false; 
    
                            }
                        }
                        
                        
                        if (ind==3 || ind==2){
                              
                            //чтобы случайно не залезть в зеленое на углах
                            if (!greenArea.contains(x,y)){
                                //jesli smenili napravlenije. i mi NE MOZHEM smenit napravlenije na protivopolozhnoje
                                
                                        if (currDirection!=nextDirection){
                                           
                                              
                                            newVertices.add(new Point (currPoint.x,currPoint.y));
                                           
                                            currDirection=nextDirection;
                                            
                                        }
                                        currPoint.setLocation(x, y);
 
                                       success=true;
                                       
                                       break;
                                }
                                else{
                                    //this means we are in shitty angle (and when we intersected just 1 or 2 pixels of black),
                                    //where we should accept ind==4, but we still choosing direction, so currDir!=nextDir
                                    // WE will GET HERE VERY VERY RARELY)
                                    shitfuck=1;
      
                                }
                            }
                        
                        //try another direction if current is unsuccessful
                        ind=0;   
                        nomerSmeni++;
                        
                        if (nomerSmeni==1){
                            nextDirection++;
                        
                        }
                        else if(nomerSmeni==2){
                             nextDirection=nextDirection+2;
                        }
                        //if nomerSmeni>2, this startPoint is not needed (blackArea was deleted), so we return
                        else {
                            break;
                        }
                        nextDirection=nextDirection%4;
                        
                        
                    }
                    //we return empty newVertices array, there construct empty area and subtract it with no consequences
                     if (nomerSmeni>2){
                       break;
                     }
                        
                    
                }while(!currPoint.equals(startPoint));
                
                //odd number of vertices only if we have 1 unneeded 1st startPoint "vertice"
                if (newVertices.size()%2==1){
                    newVertices.remove(0);
                }
                
                return newVertices;
        }
      
        
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (background != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                     offset = getOffset();
                    
                    //we make sure that after any changes in size(после растягиваний) scissors are in the right place
                    scissors.setRect(placeToDrawX+offset.x, placeToDrawY+offset.y, scissorsSize-1,scissorsSize-1);
                    // ректангл 19х19==fillRect 20x20. у ректангла есть outline 1 pixel
                    
                    g2d.drawImage(background, offset.x, offset.y ,this);//this==imageObserver as JPanel implements imageObserver
                    
    
                   
                    // сначала везде рисуем синюю границу ножниц. потом черное ее закроет
                    g2d.setColor(Color.BLUE);
                    g2d.draw(scissors);
                    
                    //blackArea relative to 0 doesn't include offset!!! 
                    AffineTransform at=new AffineTransform();
                    at.setToTranslation(offset.x,offset.y);
                    blackAreaAdjustedForOffset = blackArea.createTransformedArea(at);
                   
                    //this is for checking if boxes fly out of black
                    //g2d.drawImage(magicBoxGround,offset.x+ 0, offset.y+0, this); 
                    
                    g2d.drawImage(infoGround,offset.x+ 0, offset.y+0, this); 
                    
                    //площадь на которой черный прям видно
                    g2d.setClip(blackAreaAdjustedForOffset);
        
                   
                  
    
                    //AlphaComposite determines how background and foreground interact when they intersect
                    g2d.setComposite(AlphaComposite.SrcOver.derive(1.0f));
                   
                    
                    
                    g2d.drawImage(foreground, offset.x+0,offset.y+ 0, this);
                   
                    g2d.drawImage(foreforeground,offset.x+ 0, offset.y+0, this); 
                    
                    g2d.drawImage(magicBoxGround,offset.x+ 0, offset.y+0, this);
                    g2d.drawImage(infoGround,offset.x+ 0, offset.y+0, this); 
                     
                    //draw filled rectangle over visible black areas
                    g2d.setColor(Color.YELLOW);
                    g2d.fillRect(offset.x+placeToDrawX, offset.y+placeToDrawY, scissorsSize,scissorsSize);
                    
                  
                    //draw border of the rectangle 
                    g2d.setColor(Color.RED);
                    //g2d.draw(scissors);
                  
                     
                    g2d.dispose();
                }
            }
            
            
            
            

              //some SHIT METHOD is calling getPreferredSize (after frame.pack() method is called) and we override it here
            @Override
            public Dimension getPreferredSize() {
                return background == null ? super.getPreferredSize() : new Dimension(background.getWidth(null)+scissorsSize*2, background.getHeight(null)+scissorsSize*2);
            }
    
            //we get first x and y OVER (top left corner?) the background
            protected Point getOffset() {
                Point p = new Point();
                if (background != null) {
                    p.x = (getWidth() - background.getWidth(null)) / 2;
                    p.y = (getHeight() - background.getHeight(null)) / 2;
                }
                return p;
            }
            
 
            
            

    }