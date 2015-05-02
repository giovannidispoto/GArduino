/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package garduino;
import java.net.*;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.*;

/**
 *
 * @author Giovanni Dispoto
 */
public class Data extends Thread{
    
    
    int port = 7777;//porta su cui si ricevono i dati
    DatagramPacket p;//pacchetto generico
    DatagramSocket sock,sockS; //socket di ricezione pacchetti e Socket di invio paccheti
    FileHandler fh; //gestione dei file di log
static final Logger  logger = Logger.getLogger("MyLog"); //logger per la scrittura dei file di log
    int portS = 8888; //porta su cui si inviano i dati
    JTextField a,b,c,d,e,g;//campi testo su cui andranno scritti i dati ricevuti
    String mess; //Stringa di testo ricevuta dall'Arduino
    boolean fStop = false,primo = true;
    boolean conn = false,stop = false;//flag per controllare se la connessione è attiva e/o se è stato richiesto l'arresto della ricezione dei dati
    InetAddress ip;//Indirizzo IP dell'Arduino
    String ipp; //IP ricevuto in input dall'utente
  
   
    boolean uv; //flag controllo se la lampada è accesa o spenta
    byte dati[]; //array di byte, utilizzato per salvare il contenuto del pacchetto
    public Data(JTextField a,JTextField b,JTextField c, JTextField d,JTextField e,JTextField g){
        //assegno la variabile locale ad una variabile globale, in modo da poter modificare i dati nei campi testo
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.g = g;
  
       logSetup(); //imposto i file di log

    }
    
    public Data(){}
    public void setStop(){ //metodo utilizzato per la richiesta di arresto della ricezione/aggiornamento dei dati
        stop = true;
    }
    public void tConn(String ipp){//metodo utilizzato per impostare l'IP dell'Arduino
        this.ipp = ipp;
        try{        
          ip = InetAddress.getByName(ipp);//inizializzo la variabile con l'IP
    }
    catch(UnknownHostException f){
       System.out.println("IP non riconosciuto");
       wLog("IP non riconosciuto");
    }
    try{
        sockS = new DatagramSocket(portS);//creo un nuovo socket utilizzato per l'invio dei dati
    }catch(SocketException f){
        System.out.println("Errore socket in uscita");
        wLog("Errore socket in uscita");
    }
    
    }
    public boolean isConnected(){ //metodo utilizzato per controllare se è stata stabilita una connessione con l'Arduino
        return conn;
    }
    public void open(){//metodo utilizzato per stabilire una connessione con l'Arduino
      if(primo){//se è la prima volta che si richiama il metodo
      try{
         sock = new DatagramSocket(port);//socket utilizzato per la ricezione dei dati
       }catch(SocketException se){
         System.out.println("Errore del socket in ingresso");
         wLog("Errore del socket in ingresso");
       }
     
   
       this.start();//avvio il Thread 
       primo = false;}
      stop = false;//imposto lo stop a false
    
    }
   
    public void onLight(){
        String s = "O";//stringa che indica l'accensione della lampada
        byte b[] = s.getBytes();//richiesti i byte della stringa e assegnati all'array utilizzato per l'invio
        DatagramPacket p = new DatagramPacket(b,b.length,ip,portS); //creo il pacchetto 
        //System.out.println(ip.toString());
     try{
        sockS.send(p); //provo ad inviare il pacchetto 
     }
    catch(IOException e){
       System.out.println("Errore Nell'inoltro del messaggio");
       wLog("Errore Nell'inoltro del messaggio");
    
    }
    }
    
     public void offLight(){
       String s = "F"; //stringa che indica lo spegnimento della lampada
        byte b[] = s.getBytes();//richiesti i byte della stringa e assegnati all'array utilizzato per l'invio
        
           DatagramPacket p = new DatagramPacket(b,b.length,ip,portS); //creo il pacchetto
     try{
        sockS.send(p);//provo ad inviare il pacchetto
     }
    catch(IOException e){
       System.out.println("Errore Nell'inoltro del messaggio");
       wLog("Errore Nell'inoltro del messaggio");
    
    }
     }
     public void irrigation(){
             String s = "I";//stringa che indica l'accensione dell'irrigazione
        byte b[] = s.getBytes();//richiesti i byte della stringa e assegnati all'array utilizzato per l'invio
        
           DatagramPacket p = new DatagramPacket(b,b.length,ip,portS);//creo il pacchetto
     try{
        sockS.send(p);//provo ad inviare il pacchetto
     }
    catch(IOException e){
       System.out.println("Errore Nell'inoltro del messaggio");
    }
         
     }
     
      public void auto(boolean val){//metodo utilizzato per attivare/disattivare la gestione automatica della lampada in base alla quantità di luce pervenuta
        if(val == true){ //se è stato richisto la gestione automatica della lampada
               String s = "a";//stringa che indica la gestione automatica della lampada 
        byte b[] = s.getBytes();//richiesti i byte della stringa e assegnati all'array utilizzato per l'invio
        
           DatagramPacket p = new DatagramPacket(b,b.length,ip,portS);//creo il pacchetto
     try{
        sockS.send(p);//provo ad inviare il pacchetto
     }
    catch(IOException e){
     System.out.println("Errore Nell'inoltro del messaggio");
    }
        }else{//se invece il valore è false
               String s2 = "b";//stringa che indica la gestione manuale della lampada 
        byte b2[] = s2.getBytes();//richiesti i byte della stringa e assegnati all'array utilizzato per l'invio
        
           DatagramPacket p2 = new DatagramPacket(b2,b2.length,ip,portS);//creo il pacchetto
     try{
        sockS.send(p2);//provo ad inviare il pacchetto
     }
    catch(IOException e){
       System.out.println("Errore Nell'inoltro del messaggio");
    }
            }     
        }
     
     
    @Override
    public void run(){
     while(true){
     while(!conn){ //se la connessionnessione non è abilitata
        try{
            this.sleep(100);
        }catch(InterruptedException e){
            wLog("I Dati in ingresso sono stati interrotti");
        }
    if(!stop){ //e non è stato richiesto lo stop
         String c = "u";//stringa che indica la richiesta connessione con l'Arduino
      byte tc[] = c.getBytes();//richiesti i byte della stringa e assegnati all'array utilizzato per l'invio
      DatagramPacket p = new DatagramPacket(tc,tc.length,ip,portS);//creo il pacchetto
    try{
      sockS.send(p);//provo ad inviare il pacchetto
    }catch(IOException e){
    System.out.println("Errore invio connessione");
    wLog("Errore invio connessione");
    }
  //  sock.connect(ip,port);
    byte br[] = new byte[1];
    DatagramPacket b = new DatagramPacket(br,br.length,ip,port);
    try{
    sock.receive(b);//attendo la ricezione del pacchetto
    }catch(IOException e){
      System.out.println("Errore ricezione connessione");
      wLog("Errore ricezione connessione");
    }
    catch(Exception e){
    wLog("Errore generico");
    }
    String di = new String(b.getData()); //ottengo la stringa di risposta
    if(di.equalsIgnoreCase(c)){//se la stringa ottenuta è uguale alla stringa inviata
        g.setText("CONNESSO");//scrivo sulla textbox
        conn = true;//imposto il flag di connessione a true
        }
    }
      
      }
     if(conn && stop){//se la connessione è attiva ed è stata richiesta lo stop
         
         String c = "d";//stringa che indica la richiesta di disconnessione
      byte tc[] = c.getBytes();//richiesti i byte della stringa e assegnati all'array utilizzato per l'invio
      DatagramPacket p = new DatagramPacket(tc,tc.length,ip,portS);//creo il pacchetto
    try{
      sockS.send(p);//provo ad inviare il pacchetto
    }catch(IOException e){
        wLog("Prova invio disconnessione");
    }

    try{
    sock.receive(p);//attendo la ricezione del pacchetto
    }catch(IOException e){
    System.out.println("I Dati in ingresso sono stati interrotti");
    wLog("I dati in ingresso sono stati interrotti");
    }
   // System.out.println(p1.getDa());
    String di = new String(p.getData());//ottengo la stringa di risposta
    if(di.equalsIgnoreCase(c)){//se la stringa ottenuta è uguale a quella inviata
        conn= false;//imposto il flag di connessione a false
        g.setText("DISCONNESSO");//scrivo sulla textbox 
    }
     }
     if(conn){//se la connessione è abilitata
         dati = new byte[80];//array che conterra i byte del pacchetto
         p = new DatagramPacket(dati,dati.length,ip,port);//creo il pacchetto
        try{
         sock.receive(p);//attendo la ricezione del pacchetto
        }
        catch(IOException e){
       System.out.println("Errore nella ricezione dei dati");
       wLog("Errore nella ricezione dei Dati");
        }
        catch(NullPointerException e){
            //System.out.println("Null pointer alla ricezione del pacchetto da parte di Arduino");
            wLog("Null pointer Exception");
        }
        catch(Exception e){
        wLog("Errore generico");
        }
        mess = new String(p.getData());//ottengo la stringa di risposta
        wLog(mess);
       mess = mess.trim();//cancello i byte non scritti
       wLog(mess);
      if(mess.length() != 0){//se la stringa non è vuota
          impostaDati();//analisi della stringa per ottenere i dati
        mess="";//svuoto la stringa
      }
      }
     }
    }

    
    public void impostaDati(){//metodo analisi della stringa 
         int term = 0;//indice si stato, utilizzato per differenziare il punto della stringa da anallizare
        //int co = 0;
       String temp = "",ua = "",ut ="";//dato temperatura,dato umidità dell'aria, dato umidità del terreno
      // System.out.println(mess.length());
       for(int i = 0; i < mess.length(); i++){
          switch(term){
              case 0:
                     if(mess.charAt(i) == ',' && term == 0){ //se il carattere è una virgola(utilizzato come terminatore), passo allo stato successivo del dato
                            term = 1;//passo allo stato successivo
                            a.setText(temp);//imposto la textbox con il dato
                           // System.out.println(temp);
                     }

                      if(mess.charAt(i) != ',' && term == 0){//se non è una virgola(utilizzato come terminatore), addizziono il carattere alla stringa
                        temp += mess.charAt(i);//addiziono il carattere alla stringa
                       //co++;
                        }
                    
                  break;
              case 1:
                        if(mess.charAt(i) == ',' && term == 1){//se il carattere è una virgola(utilizzato come terminatore), passo allo stato successivo del dato
                            term = 2;//passo allo stato successivo
                            b.setText(ua);//imposto la textbox con il dato
//                            System.out.println(ua);
                        }
                        if(mess.charAt(i) != ',' && term == 1){//se non è una virgola(utilizzato come terminatore), addizziono il carattere alla stringa
                                ua += mess.charAt(i);//addiziono il carattere alla stringa
                               // co++;
                            }
                  break;
              case 2:
                      if(mess.charAt(i) == ',' && term == 2){//se il carattere è una virgola(utilizzato come terminatore), passo allo stato successivo del dato
                            term = 3;//passo allo stato successivo
                            c.setText(ut);//imposto la textbox con il dato
//                            System.out.println(ut);
                        }
                              if(mess.charAt(i) != ',' && term == 2){//se  non è una virgola(utilizzato come terminatore), addizziono il carattere alla stringa
                                ut += mess.charAt(i);//addiziono il carattere ad una stringa
                                //co++;
                            }
                   break;
              case 3:
                        if(mess.charAt(i) == ','){//se il carattere è una virgola(utilizzato come terminatore), passo allo stato successivo del dato
                           term = 4;
                        }
                        if(mess.charAt(i) == 'h'){//se il carattere è uguale a 'h', imposto la textbox a ALTO
                            d.setText("ALTO");
                        }
                        if(mess.charAt(i) == 'm'){//se il carattere è uguale a 'm', imposto la textbox a MEDIO
                            d.setText("MEDIO");
                        }
                        if(mess.charAt(i) == 'l'){//se il carattere è uguale a 'l', imposto la textbox a BASSo
                            d.setText("BASSO");
                        }
                        break;
              case 4:
                  if(mess.charAt(i) == 't'){//se il carattere ricevuto è 't'
                       e.setText("ACCESO");//imposto la textbox a ACCESO
                      // System.out.println("t");
                    }else{
                       e.setText("SPENTO");//altrimenti imposto la textbox a SPENTO
                      // System.out.println("f");
                    }
                   // co++;
                    break;
                  
          }
       }
    
    }
public void wLog(String s){//metodo usato per la scrittura sul file di log
 logger.info(s);//scrivo sui file di log

}

public void logSetup(){//setup del file di log
    
    try {  
        fh = new FileHandler("Error.log");  //imposto il percorso del file di log
        logger.addHandler(fh);//aggiungo il gestore al file di log
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter);  
    }catch(IOException e){
        //System.exit(0);
    }
    
}

}

