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

            
         class magicBox {
               
                
                double x;
                double y;
                int width=5;
                int height=5;
                
                double dx;
                double dy;
                Rectangle2D magicBoxRect;
                
                Color c;
               
                
                
                double xdobavl;//for functions
                int znak=1;
                int znak2=1;
                double yVersina;
                
                int counter=1;
                int parabolaMode;
                Area greenArea;
                
                public magicBox(Area greenArea, double x, double y, double dx, double dy, int red, int green, int blue, int alpha, int parabolaMode){
                    this.greenArea = greenArea;
                    c=new Color(red, green, blue, alpha);
                    
                    this.x=x;
                    this.y=y;
                    this.dx=dx;
                    this.dy=dy;
                    
                    
                    magicBoxRect = new Rectangle2D.Double(x,y,width,height);
                    this.parabolaMode=parabolaMode;
                    
                    yVersina=y;
                    
                    
                    
                }
                
                
               public void doMagicBoxStuff () {
                       
                        if (greenArea.intersects(this.x,this.y, this.width, this.height)){
                            MapPane.g2info.setColor(Color.RED);
                            MapPane.g2info.drawString("YOU LOOSE",MapPane.background.getWidth(null)/2-MapPane.messWidth/2, MapPane.background.getHeight(null)/2);
                        }
                        
                        
                        
                        
                        MapPane.g2inMagic.setBackground(c);
                        MapPane.g2inMagic.clearRect((int)this.x,(int)this.y, this.width,this.height);
            
                        
                     if (parabolaMode==1){
                        if (!MapPane.blackArea.contains(this.x+dx,this.y+dy, this.width, this.height)){
                        
                            
                             
                            dy=dy*-1;
                                if (!MapPane.blackArea.contains(this.x+dx,this.y+dy, this.width, this.height)){
                                    dy=dy*-1;
                                    dx=dx*-1;
                                         if (!MapPane.blackArea.contains(this.x+dx,this.y+dy, this.width, this.height)){
                                        dy=dy*-1;
                                        

                                    
                                }
                                
                                
                                
                            }
                            

                        }
                        //this if block for fun with red area
                        if (MapPane.redArea.intersects(this.x+dx,this.y+dy, this.width, this.height)){
                        
                            
                             
                            dy=dy*-1;
                                if (MapPane.redArea.intersects(this.x+dx,this.y+dy, this.width, this.height)){
                                    dy=dy*-1;
                                    dx=dx*-1;
                                         if (MapPane.redArea.intersects(this.x+dx,this.y+dy, this.width, this.height)){
                                        dy=dy*-1;
                                        

                                    
                                }
                                
                                
                                
                            }
                            

                        }
                        x+=dx;
                        y+=dy;

                        MapPane.g2inMagic.setColor(Color.WHITE);
                    }
                    else if (parabolaMode==2){
                       
                        
                        
                        
                        if (!MapPane.blackArea.contains(this.x+dx, y+znak*0.005*(xdobavl+dx)*2, this.width, this.height)){
                            dx=dx*-1;
                                if (!MapPane.blackArea.contains(this.x+dx, y+znak*0.005*(xdobavl+dx)*2, this.width, this.height)){
                                    
                                    dx=dx*-1;
                                    znak*=-1;
                                    xdobavl=0;
                                    if (!MapPane.blackArea.contains(this.x+dx, y+znak*0.005*(xdobavl+dx)*2, this.width, this.height)){
                                        dx=dx*-1;
                                        xdobavl=0;
                                        
                                    }
                                    
                                    
                                    
                                }
                                
                                
                                
                            }

                        xdobavl = xdobavl+dx;
                        x=x+dx;
                        
                        y=y+znak*0.005*xdobavl*2; // psevdo parabola :))))))))))))))))))))))))
                        
                        MapPane.g2inMagic.setColor(Color.RED);
                    } 
 
                    else if (parabolaMode==3){
                            //to bring some randomness :) *************
                            /*
                                counter++;
                                if (counter%50==0){
                                znak=znak*-1;
                                xdobavl=0;
                                yVersina=this.y;
                                
                                
                            }*/
                            //***********
                            
                            
                           
                             // this also makes it parabola mode, not sinus mode 
                            if (!MapPane.blackArea.contains(this.x+dx, yVersina+znak*0.009*(xdobavl+dx*znak2)*(xdobavl+dx*znak2), this.width, this.height)){
                                // this also makes it parabola mode, not sinus mode
                                 magicBoxRect.setRect(this.x+dx,yVersina+znak*0.009*(xdobavl+dx*znak2)*(xdobavl+dx*znak2),this.width,this.height);
                                 
                                 Area magicBoxArea = new Area(magicBoxRect);
                                 Area intersectingArea = new Area(magicBoxRect);
                                
                                intersectingArea.intersect(MapPane.blackArea);//intersection
                                magicBoxArea.subtract(intersectingArea);
                                Rectangle2D rec = magicBoxArea.getBounds2D();
                                
                                xdobavl=0;
                                yVersina=this.y;
                                //for this parabolaMode can happen that changing sign or dx BOTH will result in ok next point, so we base direction
                                //on intersect width/height as well
                                if (rec.getWidth()>rec.getHeight()){
                                    znak=znak*-1;
                                    if (!MapPane.blackArea.contains(this.x+dx, yVersina+znak*0.009*(xdobavl+dx*znak2)*(xdobavl+dx*znak2), this.width, this.height)){
                                        
                                        dx=dx*-1;
                                        znak*=-1;
                                        
                                        if (!MapPane.blackArea.contains(this.x+dx, yVersina+znak*0.009*(xdobavl+dx*znak2)*(xdobavl+dx*znak2), this.width, this.height)){
                                            znak=znak*-1;
                                            
                                            
                                        }
                                        
                                        
                                        
                                    }
                                    
                                }
                                
                                else{
                                      dx=dx*-1;
                                    if (!MapPane.blackArea.contains(this.x+dx, yVersina+znak*0.009*(xdobavl+dx*znak2)*(xdobavl+dx*znak2), this.width, this.height)){
                                        
                                        dx=dx*-1;
                                        znak*=-1;
                                        
                                        if (!MapPane.blackArea.contains(this.x+dx, yVersina+znak*0.009*(xdobavl+dx*znak2)*(xdobavl+dx*znak2), this.width, this.height)){
                                            dx=dx*-1;
                                            
                                            
                                        }
                                        
                                        
                                        
                                    }
 
                                }

                        }
                           
                        xdobavl = xdobavl+dx*znak2;
                        x=x+dx;
                            

                        // this also makes it parabola mode, not sinus mode
                        y=yVersina+znak*0.009*xdobavl*xdobavl;
                        
                        MapPane.g2inMagic.setColor(Color.GREEN);
                    }
                      else if (parabolaMode==4){//this not done
                          
                          if (!MapPane.blackArea.contains(this.x+dx, y+znak*0.005*(xdobavl+dx)*2, this.width, this.height)){
                            dx=dx*-1;
                                if (!MapPane.blackArea.contains(this.x+dx, y+znak*0.005*(xdobavl+dx)*2, this.width, this.height)){
                                    
                                    dx=dx*-1;
                                    znak*=-1;
                                    xdobavl=0;
                                    if (!MapPane.blackArea.contains(this.x+dx, y+znak*0.005*(xdobavl+dx)*2, this.width, this.height)){
                                        dx=dx*-1;
                                        xdobavl=0;
                                        
                                    }
                                    
                                    
                                    
                                }
                                
                                
                                
                            }

                        xdobavl = xdobavl+dx;
                        x=x+dx;
                        
                        y=y+znak*0.005*xdobavl*2; // psevdo parabola :))))))))))))))))))))))))
                        
                        MapPane.g2inMagic.setColor(Color.RED);
                           
                        //x=0.3y;
                        //y=4x*(1-x);
                    }  
                    
                    MapPane.g2inMagic.fillRect((int)this.x,(int)this.y,this.width,this.height); 
                            

            }
        }