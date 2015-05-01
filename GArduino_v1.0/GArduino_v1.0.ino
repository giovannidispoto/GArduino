/*
  @Name: GArduino Software
  @Version: v0.9(beta)
  @Authors:Nica Claudiu, Giuseppe Perrupato, Giovanni Dispoto
  @Description: Questo software ha il compito di leggere tutti i valori dai sensori collegati alle porte.
                Una volta che vengono raccolti tutti i valori, il software deve costruire un Datagramma (protocollo UDP)
                e inviarlo tramite la Ethernet Shield, sulla porta 7777.
                Il softaware è collegato ad un controller, da cui si può scegliere se accendere o meno la luce UV oppure gestire
                in modo automatico, vedendo la quantità di luce disponibile. Oltre a poter controllare la luce UV, si può anche irrigare.
*/

#include<Ethernet.h>
#include<SPI.h>
#include<EthernetUDP.h>
#include<DHT.h>

#define THSensor A1
#define VENT 3
#define IRR 6
#define DHTPIN 5
#define DHTTYPE DHT11
#define LAMP 2
#define FR A2
#define waterLevelS A0

byte mac[] = {0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED }; //indirizzo mac dell'arduino
byte dnss[] = {8,8,8,8}; //dns della rete
byte gateway[] = {10,0,1,138}; //gateway della rete
byte mask[] = {255,255,252,0}; //subnet mask
IPAddress ip(10,0,1,138);// IP dell'Arduino
IPAddress ipS;//conterrà l'IP della macchina su cui è stato lanciato il controller


String packet; //stringa che utilizzeremo per concatenare i dati
char wl; //livello di acqua nel serbatoio
char p[15];//array utilizzato per riempire il pacchetto UDP
boolean light = false; //flag che indica se la luce UV è accesa o spenta
boolean tSend; //flag che indica se bisogna mandare il pacchetto
int pLight; //variabile che conterrà la quantità di luce
unsigned int tp;
boolean conn = false,aut = false;//conn indica se la connessione con il controller è attiva, aut serve a determinare se la gestione della luce viene effettuata in automatico
const String v = ","; //la costante v indica la virgola, utilizzata per la costruzione della stringa che sarà inviata nel pacchetto
int ps = 0; //ps indica a che stato di lettura del sensore si trova il programma
unsigned int localPort = 8888; //porta su cui si ricevono i dati
unsigned int sendPort = 7777; //porta su cui si inviano i dati
char packetBuffer[UDP_TX_PACKET_MAX_SIZE]; //array che conterrà il pacchetto ricevuto 
DHT dht(DHTPIN,DHTTYPE); //sensore DHT che misura umidità dell'aria e la temperatura
EthernetUDP udp; //istanza UDP, utilizzata per inviare e ricevere dati
float h,t; //h sarà utilizzato per la lettura dell'umidità dell'aria, t per la tempratura
int th; //th sarà utilizzato per la lettura della umidità del terreno
unsigned long time; // viene utilizzato per determinare il tempo passato dalla lettura precedente dei sensori
unsigned long timeI; //viene utilizzato per determinare quanto tempo è passato dall'inizio dell'irrigazione
int waterValue;//utilizzata per la lettura del sensore del livello dell'acqua

void setup() {
  Ethernet.begin(mac,ip,dnss,gateway,mask); //inizio Ethernet
  udp.begin(localPort);//inizio udp
  Serial.begin(9600);//apertura del monitor seriale
  pinMode(LAMP,OUTPUT);//imposto LAMP come output 
  pinMode(IRR,OUTPUT); //imposto IRR come output
  dht.begin();//inizio sensore DHT
  time = millis();//fotografia al tempo attuale
  light = false;//imposto la luce a false
}

void loop() {
  if((millis() - time )>= 2000){//se il tempo passato dalla precedente lettura è maggiore di 2 secondi
      lightTest();//se la gestione della luce è automatica, controllo il sensore di presenza luce
      temperatureLevel();//leggo la temperatura
      humidityTLevel();//leggo l'umidità dell'aria
      waterLevel();//leggo il livello di acqua nel serbatoio
      terrainHumidity();//leggo il livello di umitià nel terreno
      time = millis();//fotografo l'istante di tempo
  }
  if((millis() - timeI) >= 2000){//se il tempo passato dall'inizio dell'irrigazione è maggiore di 2 secondi
        digitalWrite(IRR,LOW);//spengo l'irrigazione
      }
  int packetSize = udp.parsePacket();//controllo la presenza di pacchetti in entrata
  if(packetSize){//se ci sono pacchetti in entrata
 
  udp.read(packetBuffer,UDP_TX_PACKET_MAX_SIZE);//acquisisco il pacchetto
 for(int i = 0; i < UDP_TX_PACKET_MAX_SIZE;i++){//ciclo for per scandire il contenuto
    if(packetBuffer[i] == 'O' || packetBuffer[i] == 'o'){//se è presente il carattere 'o'
      digitalWrite(LAMP,HIGH);//accendo la lampada UV;
      light = true;//imposto il flag di lampada accesa
      break;
    }
    if(packetBuffer[i] == 'F' || packetBuffer[i] == 'f'){//se è presente il carattere 'f'
      digitalWrite(LAMP,LOW);//spengo la lampada UV
      light = false;//imposto il flag della lampada UV
      break;
    }
    if(packetBuffer[i] == 'I' || packetBuffer[i] == 'i'){//se è presente il carattere 'i'
    //if(wl != 'l'){
      digitalWrite(IRR,HIGH);//accendo l'irrigazione
      Serial.println("Irrigo");
      timeI = millis();//fotografo l'istante di tempo
   //   }
    }
    if(packetBuffer[i] == 'u' || packetBuffer[i] == 'U'){//se è presente il carattere 'u', richiesta di connessione
      Serial.print("Client connesso");//stampo sul monitor il messaggio
      ipS = udp.remoteIP();//ottengo l'IP della macchina connessa
      Serial.print(udp.remoteIP());//stampo sul monitor l'IP della macchina
      tSend = true;//imposto il flag di prova connessione
      tp = millis();//fotografo l'istante di tempo
    }
    if(packetBuffer[i] == 'a' || packetBuffer[i] == 'A'){//se è presente il carattere 'a'
      aut = true;//gestione automatica della lampada UV
    }
    if(packetBuffer[i] == 'b' || packetBuffer[i] == 'B'){
      aut = false;
    }
    if(packetBuffer[i] == 'd' || packetBuffer[i] == 'D'){////se è presente il carattere 'd', richiesta di disconnessione
      
    if(conn){ //se c'è connessione
      udp.beginPacket(ipS,sendPort);//creo il pacchetto
      udp.write("d");//scrivo all'interno il carattere d, che sta per indicare la risposta alla richiesta di disconnessione
      udp.endPacket();//chiudo il pachetto
      conn = false;//imposto il flag di connessione
        }
    }
   }
 }
   if(tSend){//se il flag di prova connessione è attivo
   if(millis() - tp <= 2000){//se il tempo passato è minore di 2 secondi
     Serial.println("Invio Connessione");//stampo a video il messaggio
     udp.beginPacket(ipS,sendPort);//creo il pacchetto
      udp.write("u");//scrivo 'u' che sta per indicare la risposta alla richiesta di connessione
      udp.endPacket();//chiudo il pacchetto
   }else{
       conn = true;//imposto il flag di connessione 
        tSend = false;//imposto il flag di prova connessione a false
   }
 }
if(ps == 4 && conn ==true){  //se sono terminate le acquisizioni dei sensori e la connessione è attiva
  //concateno ad una stringa tutti i valori, utilizzando come terminatore la costante v, che è una virgola
   packet.concat((int)t);
   packet.concat(v);
   packet.concat((int)h);
   packet.concat(v);
   packet.concat(th);
   packet.concat(v);
   packet.concat(wl);
   packet.concat(v);
   if(light){ packet.concat("t");}//se la luce è accesa concateno 't'
   else{ packet.concat("f");}//se la luce  spenta concateno 'f'
  packet.toCharArray(p,13);//trasformo la stringa in un array di caratteri
  Serial.println(p);//stampo a video l'array di caratteri
  udp.beginPacket(ipS,sendPort);//creo il pacchetto
  udp.write(p);//scrivo la stringa con i valori nel pacchetto
  udp.endPacket();//chiudo il pacchetto
  packet = "";//pulisco la strings
  ps = 0;//rinizio la lettura dei sensori
  }
  delay(500);
  
}


void humidityTLevel(){//lettura del sensore di umitià dell'aria
  
    h = dht.readHumidity();//lettura dell'umitià
    Serial.print("Humidity: ");
    Serial.print(h);
    Serial.println("%");
    if(h >= 60) digitalWrite(VENT,HIGH);//se l'umidità è maggiore o uguale al 70%, accendo la ventola
    else digitalWrite(VENT,LOW);//altrimenti, la spengo
   if(ps == 1)
      ps = 2;//passo allo stato successivo
  
}

void temperatureLevel(){//lettura del sensore di temperatura
    t = dht.readTemperature();//leggo la temperatura
    Serial.print("Temperature: ");
    Serial.print(t);
    Serial.println("*C");
 if(ps == 0){//passo allo stato successivo
    ps = 1;
    }

}

void waterLevel(){//lettura sensore livello acqua nel serbatorio
   waterValue = analogRead(waterLevelS);//lettur sensore
   Serial.print("Water level value: ");
   Serial.println(waterValue);
   //definire range livello acqua
 if(waterValue <= 340) wl = 'l';
 if(waterValue >= 341 && waterValue <= 682)wl = 'm';
 if(waterValue >= 683)wl = 'h';
 if(ps == 3){//passo allo stato successivo
   ps = 4;
   }
}

void terrainHumidity(){//lettura sensore umidità del terreno
  th = map(analogRead(THSensor),0,1023,100,0);//leggo il sensore e lo mappo da 0 a 100 per la percentuale
   Serial.print("Terrain Humidity:");
    Serial.println(th);
 /*if(th < 40){
   if(wl != 'l'){
      digitalWrite(IRR,HIGH);//accendo l'irrigazione
      timeI = millis();//fotografo l'istante di tempo
      }
 }*/
 if(ps == 2){//passo allo stato successivo
    ps = 3;
    }
    
}

void lightTest(){//gestione automatica della lampada UV
   pLight = analogRead(FR);//leggo la fotoresistenza
   Serial.print("Livello Luce: ");
   Serial.println(pLight);
   if(pLight > 500 && aut){digitalWrite(LAMP,HIGH);light = true;}//se il valore è maggiore di 500( con gestione automatica attiva), accendo la lampada
   if(pLight < 500 && aut) {digitalWrite(LAMP,LOW);light = false;}//se il valore è minore di 500 ( con gestione automatica attiva) , spengo la lampada
   
}
