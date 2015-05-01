/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package garduino;
import javax.swing.JFrame;
import java.awt.event.WindowListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 *
 * @author Giovanni
 */
public class Run extends JFrame{
    
    public Run(){//metodo costruttore del JFrame
     
        WindowListener li = new WindowAdapter(){ //creo listener per controllare la pressione del pulsante di chiusura
          @Override
          public void windowClosing(WindowEvent e){//se il pulsante "x" è premuto, esco
              
              System.exit(0);
          }  
       };
       
 GArduino g =  new GArduino();//creo il JPanel GArduino

       this.addWindowListener(li);//aggiungo il listener al JFrame
       super.getContentPane().add(g);//aggiungo il JPanel al JFrame
       this.setTitle("GArduino Controller");//imposto il titolo della finestra
       super.setVisible(true);//imposto la visbilità
       super.setResizable(false);//imposto la non possibilità di modificare le dimensioni della finestra
       super.setSize(900,600);//imposto le dimensioni della finestra
       super.setLocationRelativeTo(null);
    
    }
    public static void main(String [] args){
       new Splash();//istanzio lo splash screen
       new Run();//istanzio il JFrame, punto da cui partirà l'esecuzione del programma
    }
    
    
}
