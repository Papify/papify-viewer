import javax.swing.JFrame;
import java.awt.Insets;

PFramePartitionStats partitionStatsWindow;
boolean partitionStatsWindowCreated = false;
PartitionStatsWindow ps;

public class PartitionStatsWindow extends PApplet {
  PartitionStats stats;
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

  PartitionStatsWindow() {
  }

  public void setup() {
    stats = new PartitionStats();
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


  void mouseIndicator() {
    int partitionIndex = getCurrentColumn(mouseX);
    if (partitionIndex>=0 && partitionIndex<process.cores_nb && mouseX>=leftBorder && getCurrentColumn(mouseX) >=0 && mouseY>topBorder && mouseY<height-bottomBorder) {
      stroke(0);
      line(leftBorder-2, mouseY, width-rightBorder, mouseY);

      float someWidth = textWidth("Partition n");
      if (textWidth("Event count: "+(int)stats.eventCount[eventIndex][partitionIndex]) > someWidth) {
        someWidth=textWidth("Event count: "+(int)stats.eventCount[eventIndex][partitionIndex]);
      } 


      if (mouseX+someWidth>width) {
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
      text("Partition "+stats.partitionId[partitionIndex], mouseX, mouseY-25);
      text("Event count: "+(int)stats.eventCount[eventIndex][partitionIndex], mouseX, mouseY-13);

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
    text(someButtonText, leftBorder, 15);
    text(scaleButton, leftBorder+textWidth(someButtonText)+5, 15);
  }

  void plotter() {
    noStroke();
    fill(230);  
    rect(leftBorder, topBorder, width-leftBorder-rightBorder, height-topBorder-bottomBorder);
    someButtonText = stats.eventName.get(eventIndex);

    int partitionsNb = process.cores_nb;

    maxValue = stats.getMaxValue(eventIndex);

    if (log) {
      maxValue = (log(maxValue)/log(10));
    }

    barSize = (width-leftBorder-rightBorder-screenMargin*2)/(float)partitionsNb;
    int currentPartition = 0;
    int curretnBar = 0;

    for (int j=0; j<partitionsNb; j++) {
      float y;
      float x;

      if (log) {
        float result = (log(stats.eventCount[eventIndex][j])/log(10));
        y = map(result, 0, maxValue, height-bottomBorder, topBorder+screenMargin);
      } else {
        y = map(stats.eventCount[eventIndex][j], 0, maxValue, height-bottomBorder, topBorder+screenMargin);
      }

      stroke(255);
      fill(color(50, 227, 104));
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

      text("Partition "+stats.partitionId[j], 0, 0);

      popMatrix();
      curretnBar++;
      currentPartition++;
    }
  }
}


public class PFramePartitionStats extends JFrame {
  PFramePartitionStats() {
    ps = new PartitionStatsWindow();
    add(ps);
    pack();
    setLocation(600, 200);
    setSize(800, 400);
    setTitle("Partitions Stats");
    setDefaultCloseOperation(HIDE_ON_CLOSE);

    ps.init();

    setVisible(true);
  }
}

class PartitionStats extends Thread {
  int[] partitionId;
  StringList eventName;
  long[][] eventCount;

  PartitionStats() {
  }

  void run() {
    partitionId = new int[process.cores_nb];
    eventName = new StringList();

    setPapiData();
    setGlobalData(process.core, process.actor);

    println("/********************/");
    println("FOUND:");
    for (int k=0; k<eventName.size (); k++) {
      println(eventName.get(k));
    }
    println("/********************/");
    for (int k=0; k<eventName.size (); k++) {
      println("EVENT: "+eventName.get(k));

      for (int j=0; j<process.cores_nb; j++) {
        println("\tPARTITION: "+partitionId[j]+", COUNT: "+eventCount[k][j]);
      }
    }    

    ps.loaded = true;
  }

  void setPapiData() {
    for (int i=0; i<process.actors_nb; i++) {
      process.actor[i].setPapiData();
    }
  }

  void setGlobalData(Core[] c, Actor[] a) {
    for (int i=0; i<c.length; i++) {//Figure out how many different events are available
      partitionId[i] = c[i].getId();
      for (int j=0; j<a[i].data.eventName.length; j++) {
        if (!eventName.hasValue(a[i].data.eventName[j])) eventName.append(a[i].data.eventName[j]);
      }
    }

    eventCount = new long[eventName.size()][c.length];
    for (int i=0; i< eventName.size (); i++) {
      for (int j=0; j< c.length; j++) {
        eventCount[i][j] = 0;
      }
    }

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
    int coreIndex = process.getCoreIndex(process.actor[actorIndex].core);
    int eventDataIndex = process.actor[actorIndex].data.getEventIndex(eventName.get(eventIndex));
    for (int i=0; i<process.actor[actorIndex].data.actions.size (); i++) {
      eventCount[eventIndex][coreIndex] += process.actor[actorIndex].data.actions.get(i).eventCount[eventDataIndex];
    }
    return;
  }

  float getMaxValue(int eventIndex) {
    float maxValue = 0;
    for (int j=0; j< process.cores_nb; j++) {
      if (maxValue<eventCount[eventIndex][j]) maxValue = eventCount[eventIndex][j];
    }
    return maxValue;
  }
}

