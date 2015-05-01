/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package garduino;
import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Giovanni
 */
public class Splash extends JWindow {
    
    Image img = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("Garduino.png"));//richiamo l'immagine utilizzata nello splash screen
    ImageIcon imgicon = new ImageIcon(img);//imposto l'immagine come splash screen
    
    public Splash(){
    try{
        setSize(800,300);//imposto le dimensioni della finestra
        setLocationRelativeTo(null);//centro la finestra
        setVisible(true);//mostro la finestra
        Thread.sleep(2000);//attendo 2 secondi
        dispose();//chiudo la finestra
    }catch(Exception e){
        JOptionPane.showMessageDialog(null,"Error","Error When Application Started",JOptionPane.DEFAULT_OPTION);
    }
    }
    
  public void paint(Graphics g)//metodo utilizzato per disegnare l'immagine
{
g.drawImage(img,0,0,this);
}
    
}
