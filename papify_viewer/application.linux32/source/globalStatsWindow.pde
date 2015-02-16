import javax.swing.JFrame;
import java.awt.Insets;

PFrameGlobalStats globalStatsWindow;
boolean globalStatsWindowCreated = false;
GlobalStatsWindow gs;

public class GlobalStatsWindow extends PApplet {
  GlobalStats stats;
  int bottomBorder = 100;
  int topBorder = 30;
  int leftBorder = 80;
  int rightBorder = 10;
  int screenMargin = 10;
  float barSize;
  float maxValue;
  int eventIndex=0;
  boolean log = true;
  boolean loaded = false;
  String scaleButton;
  String someButtonText = "";

  float loadingAng = 0;

  GlobalStatsWindow() {
  }

  public void setup() {
    stats = new GlobalStats();
    stats.start();
  }

  void draw() {
    smooth();
    if (!loaded) {
      background(255);
      fill(0);
      noStroke();
      textAlign(CENTER, CENTER);
      text("Loading...", width/2, height/2);
      ellipse (width/2+cos(radians(loadingAng))*10, height/2+30+sin(radians(loadingAng))*10, 5, 5);
      ellipse (width/2+cos(radians(loadingAng-120))*10, height/2+30+sin(radians(loadingAng-120))*10, 5, 5);
      ellipse (width/2+cos(radians(loadingAng-240))*10, height/2+30+sin(radians(loadingAng-240))*10, 5, 5);
      loadingAng +=15;
    } else {
      background(255);
      plotter();
      buttons();
      mouseIndicator();
    }
  }

  /****/
  void mouseClicked() {
    textSize(12);
    if (mouseX>leftBorder && mouseX<leftBorder+textWidth(someButtonText) && mouseY>7 && mouseY<27) {//boton evento
      if (eventIndex==stats.eventName.size()-1) {
        eventIndex=0;
      } else {
        eventIndex++;
      }
    } else if (mouseX>leftBorder+textWidth(someButtonText)+5 && mouseX<leftBorder+textWidth(someButtonText)+textWidth(scaleButton)+5 && mouseY>7 && mouseY<27) {//boton escala
      log = !log;
    }
  }

  int getActorInPos(int x) {
    int actorsNb = stats.actorName.length;
    int currentColumn = getCurrentColumn(x);
    int currentActor = 0;

    for (int i = 0; i<actorsNb; i++) {
      if (currentColumn==currentActor && !process.actor[process.getActorIndex(stats.actorName[i])].hide) return i;
      if (!process.actor[process.getActorIndex(stats.actorName[i])].hide) currentActor++;
    }
    return -1;
  }

  void mouseIndicator() {
    int actorIndex = getActorInPos(mouseX);
    if (actorIndex>=0 && mouseX>=leftBorder && getCurrentColumn(mouseX) >=0 && mouseY>topBorder && mouseY<height-bottomBorder) {
      stroke(0);
      line(leftBorder-2, mouseY, width-rightBorder, mouseY);

      float someWidth = textWidth(stats.actorName[actorIndex]);
      if (textWidth("Event count: "+(int)stats.eventCount[eventIndex][actorIndex]) > someWidth) {
        someWidth=textWidth("Event count: "+(int)stats.eventCount[eventIndex][actorIndex]);
      } 


      if (mouseX+textWidth(stats.actorName[actorIndex])>width) {
        textAlign(RIGHT, TOP);
        noStroke();
        fill(250); 
        rect(mouseX-someWidth, mouseY-25, someWidth, 25);
      } else {
        textAlign(LEFT, TOP);
        noStroke();
        fill(250);
        rect(mouseX, mouseY-25, someWidth, 25);
      }

      fill(0);
      text(stats.actorName[actorIndex], mouseX, mouseY-25);
      text("Event count: "+(int)stats.eventCount[eventIndex][actorIndex], mouseX, mouseY-13);

      textAlign(RIGHT, CENTER);
      text((int)getYValue(mouseY), leftBorder-2, mouseY);
    }
  }
  
  float getYValue(float y) {
    if (log) {
      return pow(10, map(y, height-bottomBorder, topBorder+screenMargin, 0, maxValue));
    }
    return map(y, height-bottomBorder, topBorder+screenMargin, 0, maxValue);
  }  

  int getCurrentColumn(int x) {
    if (int(barSize)>0) return int((x-leftBorder-screenMargin)/barSize);
    return 0;
  }

  void buttons() {
    textSize(12);
    if (log) scaleButton = "Log scale";
    else scaleButton = "Linear scale";

    fill(51);
    stroke(0);
    rect(leftBorder, 7, textWidth(someButtonText), 20);
    rect(leftBorder+textWidth(someButtonText)+5, 7, textWidth(scaleButton), 20);

    fill(255);    
    textAlign(LEFT, CENTER);
    //text(data.actorPath, width/2, 0);
    text(someButtonText, leftBorder, 15);
    text(scaleButton, leftBorder+textWidth(someButtonText)+5, 15);

    //    if (!plotCalls) {
    //      if (!plotAvgCall) fill(255);
    //      else fill(51);
    //      stroke(0);
    //      textSize(12);
    //      rect(leftBorder-5, 7, -textWidth("Avg by call"), 20);
    //      if (plotAvgCall) fill(255);
    //      else fill(51);  
    //      textAlign(RIGHT, CENTER);
    //      text("Avg by call", leftBorder-5, 15);
    //    }
  }

  void plotter() {
    noStroke();
    fill(230);  
    rect(leftBorder, topBorder, width-leftBorder-rightBorder, height-topBorder-bottomBorder);
    someButtonText = stats.eventName.get(eventIndex);

    int actorsNb = process.actors_nb;
    int visibleActorsNb = countVisibleActors();

    if (visibleActorsNb>0) {
      //if (plotCalls) maxValue = data.getMaxCallsValue();
      //else if (plotAvgCall) maxValue = data.getMaxAvgCallsValue(eventIndex);
      //else maxValue = data.getMaxValue(eventIndex);//si solo vamos a dibujar un evento a la vez, esto debe ir dentro del bucle for..?
      maxValue = stats.getMaxValue(eventIndex);
      if (log) {
        maxValue = (log(maxValue)/log(10));
      }

      barSize = (width-leftBorder-rightBorder-screenMargin*2)/(float)visibleActorsNb;
      int currentActor = 0;
      int curretnBar = 0;
      for (int j=0; j<actorsNb; j++) {
        if (!process.actor[process.getActorIndex(stats.actorName[currentActor])].hide) {
          float y;
          float x;

          if (log) {
            float result = (log(stats.eventCount[eventIndex][j])/log(10));
            y = map(result, 0, maxValue, height-bottomBorder, topBorder+screenMargin);
          } else {
            y = map(stats.eventCount[eventIndex][j], 0, maxValue, height-bottomBorder, topBorder+screenMargin);
          }

          stroke(255);
          fill(color(255, 147, 147));
          x = leftBorder+screenMargin+float(curretnBar)*barSize;
          rect(x, height-bottomBorder, barSize, y-(height-bottomBorder));
          //line(x, height-bottomBorder, x, y);//linea, en vez de caja

          textSize(11);
          fill(0);
          float textX = x+barSize/2;
          float textY = height-bottomBorder;
          textAlign(RIGHT, CENTER);
          pushMatrix();
          translate(textX, textY);
          rotate(-HALF_PI/2);
          if (stats.actorName[j].length()>18) {
            text(stats.actorName[j].substring(0, 10)+"..."+stats.actorName[j].substring(stats.actorName[j].length()-5), 0, 0);
          } else {
            text(stats.actorName[j], 0, 0);
          }
          popMatrix();
          curretnBar++;
        }
        currentActor++;
      }
    }
  }
  /****/

  int countVisibleActors() {
    int n = 0;
    for (int i=0; i< process.actors_nb; i++) {
      if (!process.actor[i].hide) n++;
    }
    return n;
  }
}


public class PFrameGlobalStats extends JFrame {
  PFrameGlobalStats() {
    //process.actor[actorIndex].setPapiData();
    gs = new GlobalStatsWindow();
    //setTitle(process.actor[actorIndex].getName());
    add(gs);
    pack();
    setLocation(600, 200);
    setSize(800, 400);
    setTitle("Global Stats");
    setDefaultCloseOperation(HIDE_ON_CLOSE);

    gs.init();

    setVisible(true);
  }
}

class GlobalStats extends Thread {
  String[] actorName;
  boolean[] visible;
  StringList eventName;
  long[][] eventCount;

  GlobalStats() {
  }

  void run() {
    actorName = new String[process.actors_nb];
    visible = new boolean[process.actors_nb];
    eventName = new StringList();

    setPapiData();
    setGlobalData(process.actor);

    println("/********************/");
    println("FOUND:");
    for (int k=0; k<eventName.size (); k++) {
      println(eventName.get(k));
    }
    println("/********************/");
    for (int k=0; k<eventName.size (); k++) {
      println("EVENT: "+eventName.get(k));

      for (int j=0; j<process.actors_nb; j++) {
        println("\tACTOR: "+actorName[j]+", COUNT: "+eventCount[k][j]);
      }
    }

    gs.loaded = true;
  }

  void setPapiData() {
    for (int i=0; i<process.actors_nb; i++) {
      process.actor[i].setPapiData();
    }
  }

  void setGlobalData(Actor[] a) {
    for (int i=0; i<a.length; i++) {//Figure out how many different events are available
      actorName[i] = a[i].getName();
      visible[i] = true;
      for (int j=0; j<a[i].data.eventName.length; j++) {
        if (!eventName.hasValue(a[i].data.eventName[j])) eventName.append(a[i].data.eventName[j]);
      }
    }

    eventCount = new long[eventName.size()][process.actors_nb];
    for (int i=0; i< eventName.size (); i++) {
      for (int j=0; j< a.length; j++) {
        if (actorHasEvent(a[j], eventName.get(i))) {
          countAndAdd(i, j);//i=eventIndex, j=actorIndex
        } else eventCount[i][j] = -1;
      }
    }
  }

  boolean actorHasEvent(Actor a, String eventName) {
    for (int i=0; i<a.data.eventName.length; i++) {
      if (a.data.eventName[i].equals(eventName)) return true;
    }
    return false;
  }

  void countAndAdd(int eventIndex, int actorIndex) {
    eventCount[eventIndex][actorIndex] = 0;
    int actorProcessIndex = process.getActorIndex(actorName[actorIndex]);
    int eventDataIndex = process.actor[actorProcessIndex].data.getEventIndex(eventName.get(eventIndex));
    for (int i=0; i<process.actor[actorProcessIndex].data.actions.size (); i++) {
      eventCount[eventIndex][actorIndex] += process.actor[actorProcessIndex].data.actions.get(i).eventCount[eventDataIndex];
    }
    return;
  }

  float getMaxValue(int eventIndex) {
    float maxValue = 0;
    for (int j=0; j< process.actors_nb; j++) {
      if (maxValue<eventCount[eventIndex][j]) maxValue = eventCount[eventIndex][j];
    }
    return maxValue;
  }
}

