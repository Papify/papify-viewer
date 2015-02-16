import javax.swing.JFrame;
import java.awt.Insets;

PFrameStats[] statsWindow;
boolean[] statsWindowCreated;
StatsWindow[] s;

void statsWindowInit() {
  statsWindow = new PFrameStats[process.actors_nb];
  s = new StatsWindow[process.actors_nb];
  statsWindowCreated = new boolean[process.actors_nb];
  for (int i=0; i<process.actors_nb; i++) {
    statsWindowCreated[i]=false;
  }
}


public class StatsWindow extends PApplet {
  PapiData data;
  boolean log = true;
  int actorIndex;
  int eventIndex=0;

  int bottomBorder = 100;
  int topBorder = 30;
  int leftBorder = 80;
  int rightBorder = 10;
  int screenMargin = 10;
  float barSize;  
  float maxValue;
  String someButtonText = "";
  boolean plotCalls = false;
  boolean plotAvgCall = false;
  String scaleButton;

  StatsWindow(PapiData data, int actorIndex) {
    this.data = data;
    this.actorIndex = actorIndex;
  }

  public void setup() {
    someButtonText = data.eventName[eventIndex];
  }

  void draw() {
    smooth(); 
    if (running && actorIndex<process.actors_nb && statsWindow!=null && statsWindow[actorIndex]!=null && statsWindow[actorIndex].isVisible()) {
      background(255);
      plotter();

      buttons();

      if (mouseX>=leftBorder && getCurrentColumn(mouseX) >=0 && getCurrentColumn(mouseX)<data.actions.size() && mouseY>topBorder && mouseY<height-bottomBorder) {
        stroke(0);
        line(leftBorder-2, mouseY, width-rightBorder, mouseY);
        Action action = data.actions.get(getActionInPos(mouseX));

        float someWidth = textWidth(action.name());
        if (textWidth("Event count: "+(int)action.eventCount[eventIndex]) > someWidth) {
          someWidth=textWidth("Event count: "+(int)action.eventCount[eventIndex]);
        } 
        if (textWidth("Called "+action.calls()+" times") > someWidth) {
          someWidth=textWidth("Called "+action.calls()+" times");
        } 
        if (plotCalls && textWidth("Average: "+action.eventCount[eventIndex]/action.calls()+"/call") > someWidth) {
          someWidth=textWidth("Average: "+action.eventCount[eventIndex]/action.calls()+"/call");
        }        

        if (mouseX+textWidth(action.name())>width) {
          textAlign(RIGHT, TOP);
          noStroke();
          fill(250); 
          rect(mouseX-someWidth, mouseY-49, someWidth, 49);
        } else {
          textAlign(LEFT, TOP);
          noStroke();
          fill(250);
          rect(mouseX, mouseY-49, someWidth, 49);
        }

        fill(0);
        text(action.name(), mouseX, mouseY-49);
        if (!plotCalls) text("Event count: "+(int)action.eventCount[eventIndex], mouseX, mouseY-37);
        text("Called "+action.calls()+" times", mouseX, mouseY-25);
        if (!plotCalls) text("Average: "+action.eventCount[eventIndex]/action.calls()+"/call", mouseX, mouseY-13);

        textAlign(RIGHT, CENTER);
        text((int)getYValue(mouseY), leftBorder-2, mouseY);
      }

      textAlign(CENTER, BOTTOM);
      fill(0);
      textSize(12);
      text("TIP: Click on an action to hide it, press U to unhide", width/2, height-1);
    }
  }


  void mouseClicked() {
    textSize(12);

    if (mouseX>leftBorder && mouseX<leftBorder+textWidth(someButtonText) && mouseY>7 && mouseY<27) {//boton evento/calls
      if (plotCalls) {
        eventIndex=0;
        plotCalls = false;
        someButtonText = data.eventName[eventIndex];
      } else if (eventIndex==data.eventsNb-1) {
        plotCalls = true;
        someButtonText = "TOTAL CALLS";
      } else 
      {
        eventIndex++;
        someButtonText = data.eventName[eventIndex];
      }
    } else if (mouseX>leftBorder+textWidth(someButtonText)+5 && mouseX<leftBorder+textWidth(someButtonText)+textWidth(scaleButton)+5 && mouseY>7 && mouseY<27) {//boton escala
      log = !log;
    } else if (mouseX>leftBorder-textWidth("Avg by call") && mouseX<leftBorder-5 && mouseY>7 && mouseY<27) {
      plotAvgCall=!plotAvgCall;
    } else if (mouseX>=leftBorder+screenMargin && getCurrentColumn(mouseX) >=0 && getCurrentColumn(mouseX)<data.actions.size() && mouseY>topBorder) {//ocultar acciones
      Action action = data.actions.get((int)getActionInPos(mouseX));
      action.hide = true;
    }
  }

  void keyPressed() {
    if (key==27) {
      key = 0;
      statsWindow[actorIndex].setVisible(false);
    } else if (key=='u' || key=='U') {
      unHideAll();
    } else {
      handleKeys(key);
    }
  }

  int countVisibleActions() {
    int n = 0;
    for (int i=0; i<data.actions.size (); i++) {
      Action action = data.actions.get(i);
      if (!action.hide) n++;
    }
    return n;
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

    if (!plotCalls) {
      if (!plotAvgCall) fill(255);
      else fill(51);
      stroke(0);
      textSize(12);
      rect(leftBorder-5, 7, -textWidth("Avg by call"), 20);
      if (plotAvgCall) fill(255);
      else fill(51);  
      textAlign(RIGHT, CENTER);
      text("Avg by call", leftBorder-5, 15);
    }
  }

  void plotter() {
    noStroke();
    fill(230);  
    rect(leftBorder, topBorder, width-leftBorder-rightBorder, height-topBorder-bottomBorder);

    int actionsNb = data.actions.size();
    int visibleActionsNb = countVisibleActions();

    if (visibleActionsNb>0) {
      if (plotCalls) maxValue = data.getMaxCallsValue();
      else if (plotAvgCall) maxValue = data.getMaxAvgCallsValue(eventIndex);
      else maxValue = data.getMaxValue(eventIndex);//si solo vamos a dibujar un evento a la vez, esto debe ir dentro del bucle for..?
      if (log) {
        maxValue = (log(maxValue)/log(10));
      }

      int currentBar = 0;
      barSize = (width-leftBorder-rightBorder-screenMargin*2)/(float)visibleActionsNb;
      int currentAction = 0;
      for (int j=0; j<actionsNb; j++) {
        Action action = data.actions.get(j);
        if (!action.hide) {
          float y;
          float x;

          if (plotCalls) {
            if (log) {
              float result = (log(action.calls)/log(10));
              y = map(result, 0, maxValue, height-bottomBorder, topBorder+screenMargin);
            } else {
              y = map(action.calls, 0, maxValue, height-bottomBorder, topBorder+screenMargin);
            }
          } else if (plotAvgCall) {
            if (log) {
              float result = (log(action.eventCount[eventIndex]/action.calls)/log(10));
              y = map(result, 0, maxValue, height-bottomBorder, topBorder+screenMargin);
            } else {
              y = map(action.eventCount[eventIndex]/action.calls, 0, maxValue, height-bottomBorder, topBorder+screenMargin);
            }
          } else {
            if (log) {
              float result = (log(action.eventCount[eventIndex])/log(10));
              y = map(result, 0, maxValue, height-bottomBorder, topBorder+screenMargin);
            } else {
              y = map(action.eventCount[eventIndex], 0, maxValue, height-bottomBorder, topBorder+screenMargin);
            }
          }
          stroke(255);
          fill(color(101, 159, 216));
          //int x = separation*currentBar+leftBorder;
          //if (actionsNb == 1) x = lerp(leftBorder+screenMargin, width-rightBorder-screenMargin, currentBar/float(actionsNb));
          //else x = lerp(leftBorder+screenMargin, width-rightBorder-screenMargin, currentBar/float(actionsNb-1));
          x = leftBorder+screenMargin+float(currentAction)*barSize;
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
          if (action.name.length()>18) {
            text(action.name.substring(0, 10)+"..."+action.name.substring(action.name.length()-5), 0, 0);
          } else {
            text(action.name, 0, 0);
          }
          popMatrix();
          currentAction++;
        }
      }
    }
  }

  int getCurrentColumn(int x) {
    if (int(barSize)>0) return int((x-leftBorder-screenMargin)/barSize);
    return 0;
  }

  int getActionInPos(int x) {
    int actionsNb = data.actions.size();
    int currentColumn = getCurrentColumn(x);
    int currentAction = 0;

    Action action;

    for (int i = 0; i<actionsNb; i++) {
      action = data.actions.get(i);
      if (currentColumn==currentAction && !action.hide) return i;
      if (!action.hide) currentAction++;
    }
    return 0;
  }

  void unHideAll() {
    int actionsNb = data.actions.size();
    Action action;
    for (int i = 0; i<actionsNb; i++) {
      action = data.actions.get(i);
      action.hide = false;
    }
  }

  float getYValue(float y) {
    if (log) {
      return pow(10, map(y, height-bottomBorder, topBorder+screenMargin, 0, maxValue));
    }
    return map(y, height-bottomBorder, topBorder+screenMargin, 0, maxValue);
  }
}



public class PFrameStats extends JFrame {
  PFrameStats(int actorIndex) {    process.actor[actorIndex].setPapiData();


    s[actorIndex] = new StatsWindow(process.actor[actorIndex].getPapiData(), actorIndex);
    setTitle(process.actor[actorIndex].getName());
    add(s[actorIndex]);
    pack();
    setLocation(500, 200);
    setSize(500, 300);
    setTitle(process.actor[actorIndex].getName());
    setDefaultCloseOperation(HIDE_ON_CLOSE);
    //setBounds(100, 100, 500, 300);


    //Insets insets = getInsets();//crashea.. ver why TODO
    //println("insets.left: "+insets.left+" insets.top: "+insets.top);
    //setSize(insets.left + 500, insets.top + 300);
    //setBounds(insets.left, insets.top, 500, 300);
    s[actorIndex].init();

    setVisible(true);
  }
}

