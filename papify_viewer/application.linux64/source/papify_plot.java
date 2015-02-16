import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import javax.swing.JFrame; 
import java.awt.Insets; 
import javax.swing.JFrame; 
import java.awt.Insets; 
import javax.swing.JFrame; 
import java.awt.Insets; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class papify_plot extends PApplet {

int left_border = 15;
int right_border = 180;
int top_border = 50;
int bottom_border = 50;
boolean loading = false;
String currentDir;
String loading_title = "";
String loading_msg = "";
boolean running = false;
boolean setToRedraw = false;
//TODO:
//obtener tama\u00f1o del margen derecho segun el nombre mas largo de todos..
//Mejorar pointer tool
//fix bug: when loading papi output AND xcf and then loading another papi output, display goes highwire (tip: when loading dir again, kill cores..)

Screen screen;
Process process;
Color colors;
PImage img, zoom_img;

public void setup() {
  size(1200, 768);
  background(255);
  //resizeable screen

  if (frame != null) {
    frame.setResizable(true);
    frame.setTitle("Papify plot");
  }

  screen = new Screen(0);
  screen.turnOn();
  colors = new Color();

  save("data.png");
  img = loadImage("data.png");

  process = new Process();
  buttons_init();
  pointer_init();
  status_init();
}

public void draw() {
  background(255);
  image(img, 0, 0);
  pointer();
  zoom();
  status();
  buttons();
  loading();
  cursorWatch();
  if (setToRedraw) {
    setToRedraw = false;
    screen.reset();
    process.draw();
  }
}

public void startRunning() {
  running=true;
}

public void stopRunning() {
  running = false;

  for (int i=0; i<process.actors_nb; i++) {
    if (statsWindow[i]!=null) {
      statsWindow[i].dispose();
    }
    s[i] = null;      
    statsWindow[i] = null;
    statsWindowCreated[i]=false;
  }
  statsWindow=null;
  statsWindowCreated=null;
  s=null;

  if (actorsWindow!=null) actorsWindow.dispose();
  actorsWindowCreated = false;
  a=null;
  actorsWindow=null;
}

public void keyPressed() {
  if (key==27) {
    key=0;//evitar que se cierre la app con esc?
  } else if (key==TAB) {
    if (process.mode == 1) {
      if (process.core!=null) {
        process.mode = 2;
        setToRedraw = true;
      }
    } else if (process.mode == 2) {
      if (process.actor!=null) {
        process.mode = 1;
        setToRedraw = true;
      }
    }
  } else handleKeys(key);
}



public void cursorWatch() {
  if (loading) {
    cursor(WAIT);
  } else if (false) {
    cursor(HAND);
  } else {
    cursor(ARROW);
  }
}

public void handleKeys(int key) {//Global shortcut
  if (key=='a') {
    if (running) {
      if (!actorsWindowCreated) {
        actorsWindow = new PFrameActors();
        //actorsWindow.setVisible(true);
        actorsWindowCreated = true;
      } else actorsWindow.setVisible(!actorsWindow.isVisible());
    }
  } else if (key=='A') {
    //show partitions screen.. TODO
  } else if (key=='r' || key=='R') {
    setToRedraw = true;
  } else if (key=='f' || key=='F') {
    screen.switchFullScreen();
    setToRedraw = true;
  } else if (key=='x' || key=='X') {
    pointerOn = !pointerOn;
  } else   if (key=='z') {
    if (set_zoom_ini || set_zoom_end) {
      set_zoom_ini = set_zoom_end = false;
    } else if (process.actor!=null)
      set_zoom_ini = true;
  } else if (key=='Z') {
    screen.zoomReset();
  }
}

class Actor {
Table table;
  String name;
  String filePath;
  long boxNb;
  Box[] boxes;//TODO change to ArrayList
  int core = -1; //core al que pertenece (si es -1, no se encontr\u00f3 el core en el xcf o bien todav\u00eda no se carg\u00f3 el xcf)
  int fillColor;
  int strokeColor;
  PapiData data;
  boolean hide = false;
  

  Actor(String filePath) {
    this.filePath = filePath;
    //println("reading "+filePath);
    table = loadTable(filePath, "header");
    setBoxNb(table.getRowCount());
    name = filePath.substring(filePath.indexOf("papi_output_")+12,filePath.length()-4);
    loading_msg = name;
    boxes = new Box[table.getRowCount()];
    int i = 0;
    for (TableRow row : table.rows ()) {
      boxes[i++] = new Box(row.getLong("tini"), row.getLong("tend"), row.getString("Action"));
    }
    fillColor = colors.getNext();
    strokeColor = colors.getNext();
  }
  
  public void setHide(boolean b){
    hide = b;
  }
  
  public void switchHide(){
    hide = !hide;
  }
  
  public void setPapiData(){
    if(data == null) data = new PapiData(filePath);
  }
  
  public PapiData getPapiData(){
    return data;
  }
  
  public String getFilePath(){
    return filePath;
  }

  public void setBoxNb(long n) {
    boxNb = n;
  }

  public void setCore(int n) {
    core = n;
  }

  public int getCore() {
    return core;
  }

  public String getName() {
    return name;
  }  

  public void draw(int line) {
    for (int i = 0; i<boxNb; i++) {
      screen.addBox(boxes[i].tini, boxes[i].tend, line, fillColor, strokeColor);
    }
  }
  
  public String getBoxName(long time) {//busqueda binaria
   if(boxNb>0){
    long imax = boxNb-1;
    int imin = 0;
    if (time > boxes[(int)boxNb-1].tend || time < boxes[0].tini) {
      return "";
    }
    while (imax >= imin) {
      int imid = (imin+(int)imax)/2;
      if (boxes[imid].tini <= time && boxes[imid].tend >= time)
        return boxes[imid].name;
      else if (boxes[imid].tend <= time)
        imin = imid + 1;
      else
        imax = imid - 1;
    }
    return "";
  } else return "";
  }
}

class Box {
  long tini;
  long tend;
  String name;

  Box(long tini, long tend, String name) {
    this.tini=tini;
    this.tend=tend;
    this.name=name;
  }

  Box(long tini, long tend) {
    this.tini=tini;
    this.tend=tend;
  }
}

PFrameActors actorsWindow;
boolean actorsWindowCreated;
ActorsWindow a;

public class ActorsWindow extends PApplet {
  int margin = 3;
  int iconSize = 16;
  int boxSize = 20;
  int topBorder = 26;
  int tableWidth = (int)process.longestWidth+2*boxSize+2*5+10;
  int tableHeight = boxSize*process.actors_nb;
  boolean overallHide = false;

  public void setup() {
  }

  public void draw() {
    background(255);
    if (actorsWindow!=null && running && actorsWindow.isVisible()) {
      shapeMode(CORNER);
      shape(button_reset, width-20-margin, margin, 20, 20);
      line(margin+boxSize, topBorder, margin+boxSize, topBorder+tableHeight);
      line(margin+boxSize+boxSize, topBorder, margin+boxSize+boxSize, topBorder+tableHeight);
      noFill();
      rectMode(CORNER);
      rect(margin, topBorder, tableWidth, tableHeight);
      fill(0);
      textAlign(LEFT, CENTER);
      textSize(12);
      shapeMode(CENTER);
      rectMode(CENTER);
      if (!overallHide) shape(button_unhide, margin+boxSize+boxSize/2, topBorder-boxSize+boxSize/2, iconSize, iconSize);
      else shape(button_hide, margin+boxSize+boxSize/2, topBorder-boxSize+boxSize/2, iconSize, iconSize);
      shape(button_stats_alt, margin+boxSize/2, topBorder-boxSize+boxSize/2, iconSize, iconSize);
      for (int i=0; i<process.actors_nb; i++) {
        int actorIndex = process.getActorInLineAll(currentLine(topBorder+i*boxSize+boxSize/2-topBorder));
        line(margin, topBorder+i*boxSize, margin+tableWidth, topBorder+i*boxSize);
        if (actorIndex!=-1) {
          text(process.actor[actorIndex].name, margin+boxSize+boxSize+15, topBorder+i*boxSize+boxSize/2);
          fill(process.actor[actorIndex].fillColor);
          stroke(process.actor[actorIndex].strokeColor);
          rect(margin+boxSize+boxSize+7, topBorder+i*boxSize+boxSize/2, 7, 7);
          stroke(0);
          fill(0);
          if (process.actor[actorIndex].hide) {
            shape(button_hide, margin+boxSize+boxSize/2, topBorder+i*boxSize+boxSize/2, iconSize, iconSize);
          } else {
            shape(button_unhide, margin+boxSize+boxSize/2, topBorder+i*boxSize+boxSize/2, iconSize, iconSize);
          }
          shape(button_stats_alt, margin+boxSize/2, topBorder+i*boxSize+boxSize/2, iconSize, iconSize);
        }
      }
      //println(process.actor[process.getActorInLineAll(currentLine(mouseY-topBorder))].name);
    }
  }

  public int currentLine(int y) {
    if (y>0 && y<tableHeight) {
      return (y/boxSize);
    } else return 0;
  }

  public void mouseClicked() {
    if (mouseY>topBorder && mouseY<(topBorder+tableHeight)) {
      if (mouseX>margin && mouseX<(margin+boxSize)) {//stats
        int actorIndex = process.getActorInLineAll(currentLine(mouseY-topBorder));
        if (!statsWindowCreated[actorIndex]) {
          statsWindow[actorIndex] = new PFrameStats(actorIndex);
          //statsWindow[actorIndex].setVisible(true);
          statsWindowCreated[actorIndex] = true;
        } else {
          statsWindow[actorIndex].setVisible(!statsWindow[actorIndex].isVisible());
        }
      } else if (mouseX>(margin+boxSize) && mouseX<(margin+boxSize*2)) {//hide
        process.actor[process.getActorInLineAll(currentLine(mouseY-topBorder))].switchHide();
      }
    } else if (mouseX>margin+boxSize && mouseX<margin+boxSize*2 && mouseY>topBorder-boxSize && mouseY<topBorder) {
      if (!overallHide) for (int i=0; i<process.actors_nb; i++)  process.actor[i].setHide(true);
      else for (int i=0; i<process.actors_nb; i++)  process.actor[i].setHide(false);
      overallHide = !overallHide;
    } else if (mouseX>margin && mouseX<margin+boxSize && mouseY>topBorder-boxSize && mouseY<topBorder) {
      println("overall stats button \u00bf?\u00bf?");
    } else if (mouseX>width-20 && mouseX<width && mouseY<topBorder) {
      setToRedraw = true;
    }
  }

  public void keyPressed() {
    if (key==27) {
      this.key = 0;
      actorsWindow.setVisible(false);
    } else {
      handleKeys(key);
    }
  }
}


public class PFrameActors extends JFrame {
  PFrameActors() {
    a = new ActorsWindow();
    setTitle("Actors");
    add(a);
    pack ();
    setAlwaysOnTop(true);
    setDefaultCloseOperation(HIDE_ON_CLOSE);
    setBounds(0, 0, 2*3+(int)process.longestWidth+20*2+2*5+10, 25+process.actors_nb*20+50);
    setVisible(true);

    a.init();
  }
}

java.io.FilenameFilter csvFilter = new java.io.FilenameFilter() {
  public boolean accept(File dir, String name) {
    return name.toLowerCase().endsWith(".csv");
  }
};

public void handleCSVDir(File directory) {
  if (directory == null) {
    println("Window was closed or the user hit cancel.");
    return;
  }
  stopRunning();
  File[] csvFiles = directory.listFiles(csvFilter);
  int files_nb = csvFiles.length;
  status.update("Loading, please wait.. (can take a few minutes)");
  loading=true;
  loading_title="Loading papi-output directory..";
  process.actorInit(csvFiles);
  status.update("Loading complete, click on the Actors tab to display results");
  frame.setTitle("Papify plot | "+directory.getAbsoluteFile());
  statsWindowInit();
  loading=false;
  loading_msg="";
  loading_title="";
  process.mode=1;
  startRunning();
  setToRedraw = true;
  //  loading=false;
  //  loading=true;
  //  loading_title="Drawing..";
  //  loading_msg="";
  //  statsWindowInit();
  //  screen.reset();
  //  noLoop();
  //  process.draw();
  //  loop();
  //  loading=false;
  //  startRunning();
}

public void loading() {
  if (loading) {
    rectMode(CENTER);
    stroke(0);
    fill(255);
    rect(width/2, height/2, 400, 250);
    textAlign(CENTER, CENTER);
    fill(0);
    textSize(20);
    text(loading_title, width/2, height/2-25);
    textSize(12);
    text(loading_msg, width/2, height/2+25, 400, 100);
    noStroke();
    rectMode(CORNER);
  }
}
class Color {
  int index = 0;

  int[] colors = {    
    color(120, 0, 0), //rojo
    color(200, 0, 0), 

    color(0, 120, 0), //verde
    color(0, 200, 0), 

    color(0, 0, 120), //azul
    color(0, 0, 200), 

    color(120, 120, 0), //amarillo
    color(200, 200, 0), 

    color(120, 0, 120), //fuxia
    color(200, 0, 200), 

    color(0, 120, 120), //cyan
    color(0, 200, 200), 

    color(80, 80, 80), //gris
    color(140, 140, 140), 

    color(136, 47, 192), 
    color(112, 48, 152), 

    color(255, 145, 54), 
    color(255, 116, 0), 

    color(60, 94, 249), 
    color(48, 75, 200), 

    color(255, 228, 35), 
    color(180, 164, 54), 

    color(33, 248, 240), 
    color(25, 187, 181), 

    color(34, 249, 190), 
    color(30, 156, 121), 

    color(228, 0, 200), 
    color(102, 6, 90), 

    color(74, 1, 241), 
    color(62, 4, 196),
  };

  public int getNext() {
    if (index==colors.length) index=0;
    return colors[index++];
  }

  public int get(int i) {
    if (i<colors.length) {
      return colors[i];
    } else return 0;
  }
}

class Core {
  int id;
  int actors_nb;
  String actors[];//contains the actor names in this core
  boolean hide = false;

  Core(int id, XML[] actors) {
    this.id = id;
    actors_nb = actors.length;
    this.actors = new String[actors_nb];
    for (int i = 0; i < actors_nb; i++) {
      this.actors[i] = actors[i].getString("id");
    }
  }
  
  public int getId(){
   return id; 
  }

}

class PapiData {//one per Actor..
  String actorPath;
  String actorName;
  int eventsNb = 0;
  String[] eventName;
  int[] eventColor;
  ArrayList<Action> actions = new ArrayList<Action>();
  Table table;

  PapiData(String path) {
    actorPath = path;
    actorName = actorPath.substring(actorPath.indexOf("papi_output_")+12, actorPath.length()-4);
    crunch();
  }

  public void crunch() {
    int i, j;
    println("Actor: "+actorPath);
    setEvents();
    for (i=0; i<eventName.length; i++) {
      println(eventName[i]);
    }
    setActions();

    for (i=0; i<actions.size (); i++) {
      Action action = actions.get(i);
      println("Action: "+action.name()+" called "+action.calls()+" times");
      for (j=0; j<eventName.length; j++) {
        println("  "+eventName[j]+": "+action.eventCount[j]);
      }
    }
  }

  public long getMaxValue() {
    long currentMax = 0;
    for (int i=0; i<actions.size (); i++) {
      Action action = actions.get(i);
      for (int j=0; j<eventName.length; j++) {
        if (currentMax<action.eventCount[j]) currentMax = action.eventCount[j];
      }
    }
    return currentMax;
  }

  public long getMaxValue(int eventIndex) {
    long currentMax = 0;
    for (int i=0; i<actions.size (); i++) {
      Action action = actions.get(i);
      if (currentMax<action.eventCount[eventIndex]) currentMax = action.eventCount[eventIndex];
    }
    return currentMax;
  }  


  public long getMaxCallsValue() {
    long currentMax = 0;
    for (int i=0; i<actions.size (); i++) {
      Action action = actions.get(i);
      if (currentMax<action.calls) currentMax = action.calls;
    }
    return currentMax;
  }
  
  public long getMaxAvgCallsValue(int eventIndex) {
    long currentMax = 0;
    for (int i=0; i<actions.size (); i++) {
      Action action = actions.get(i);
      if (currentMax<(action.eventCount[eventIndex]/action.calls)) currentMax = (action.eventCount[eventIndex]/action.calls);
    }
    return currentMax;
  }
  
  
  public void setEvents() {
    int j = 0;
    Table table;//usamos una table separada de la de la clase por que necesitamos la primera fila solo en este metodo (nombres de los eventos)
    table = loadTable(actorPath);
    eventsNb = table.getColumnCount()-4;
    eventName = new String[eventsNb];
    eventColor = new int[eventsNb];
    TableRow row = table.getRow(0);
    for (int i = 4; i<eventsNb+4; i++) {//empezamos en 4 para saltarnos las columnas que no son eventos (actor, accion, tini y tend)
      eventName[j] = row.getString(i);
      eventColor[j++] = colors.get(2*(i-4));
    }
  }

  public void setActions() {
    if (eventsNb > 0) {
      table = loadTable(actorPath, "header");
      for (TableRow row : table.rows ()) {
        boolean actionExists = false;
        String actionName = row.getString("Action");
        for (Action action : actions) {
          if (action.name().equals(actionName)) {
            actionExists = true;
            for (int i = 4; i<eventsNb+4; i++){
              action.sum(i-4, row.getLong(i));//WHAT? solo con dos eventos? ni mas ni menos? no me acuerdo si hice esto a posta, verificar! TODO
              //action.sum(1, row.getLong(5));
            }
            continue;
          }
        }
        if (!actionExists) {
          Action someAction = new Action(actionName, eventsNb);
            for (int i = 4; i<eventsNb+4; i++){
              someAction.sum(i-4, row.getLong(i));//WHAT? solo con dos eventos? ni mas ni menos? no me acuerdo si hice esto a posta, verificar! TODO
              //action.sum(1, row.getLong(5));
            }          
          //someAction.sum(0, row.getLong(4));//WHAT? solo con dos eventos? ni mas ni menos? no me acuerdo si hice esto a posta, verificar! TODO
          //someAction.sum(1, row.getLong(5));
          actions.add(someAction);
        }
        //for (int i = 4; i<eventsNb+4; i++) {//empezamos en 4 para saltarnos las columnas que no son eventos
        //}
      }
    }
  }

  public boolean actionExists(String name) {
    if (actions.size() == 0) {
      return false;
    }
    for (Action action : actions) {
      if (action.name().equals(name)) {
        return true;
      }
    }
    return false;
  }
  
  public int getEventIndex(String eventName){
    for(int i=0; i<eventsNb; i++){
     if(eventName.equals(this.eventName[i])) return i; 
    }
    return -1;
  }
}

class Action {
  String name;
  long[] eventCount;
  long calls = 0;
  boolean hide = false;

  Action(String name, int eventsNb) {
    this.name = name;
    eventCount = new long[eventsNb];
  }

  public String name() {
    return name;
  }

  public long calls() {
    return calls;
  }

  public void setHide (boolean b) {
    hide = b;
  }

  public void sum(int eventIndex, long value) {
    eventCount[eventIndex] += value;
    calls++;
  }
}

PFramePartitions partitionsWindow;
boolean partitionsWindowCreated;
PartitionsWindow p;

public class PartitionsWindow extends PApplet {
  int margin = 3;
  int iconSize = 16;
  int boxSize = 20;
  int topBorder = 26;
  int tableWidth = 150;
  int tableHeight = boxSize*process.cores_nb;
  boolean overallHide = false;

  public void setup() {
  }

  public void draw() {
    background(255);
    if (partitionsWindow!=null && running && partitionsWindow.isVisible()) {
      shapeMode(CORNER);
      shape(button_reset, width-20-margin, margin, 20, 20);
      line(margin+boxSize, topBorder, margin+boxSize, topBorder+tableHeight);
      line(margin+boxSize+boxSize, topBorder, margin+boxSize+boxSize, topBorder+tableHeight);
      noFill();
      rectMode(CORNER);
      rect(margin, topBorder, tableWidth, tableHeight);
      fill(0);
      textAlign(LEFT, CENTER);
      textSize(12);
      shapeMode(CENTER);
      rectMode(CENTER);
      if (!overallHide) shape(button_unhide, margin+boxSize+boxSize/2, topBorder-boxSize+boxSize/2, iconSize, iconSize);
      else shape(button_hide, margin+boxSize+boxSize/2, topBorder-boxSize+boxSize/2, iconSize, iconSize);
      shape(button_stats_alt, margin+boxSize/2, topBorder-boxSize+boxSize/2, iconSize, iconSize);
      for (int i=0; i<process.cores_nb; i++) {
        int partitionIndex = currentLine(topBorder+i*boxSize+boxSize/2-topBorder);
        line(margin, topBorder+i*boxSize, margin+tableWidth, topBorder+i*boxSize);
        if (partitionIndex>=0 && partitionIndex<process.cores_nb) {
          fill(0);
          text("Partition "+process.core[partitionIndex].id, margin+boxSize+boxSize+15, topBorder+i*boxSize+boxSize/2);
          stroke(0);
          fill(0);
          if (process.core[partitionIndex].hide) {
            shape(button_hide, margin+boxSize+boxSize/2, topBorder+i*boxSize+boxSize/2, iconSize, iconSize);
          } else {
            shape(button_unhide, margin+boxSize+boxSize/2, topBorder+i*boxSize+boxSize/2, iconSize, iconSize);
          }
          shape(button_stats_alt, margin+boxSize/2, topBorder+i*boxSize+boxSize/2, iconSize, iconSize);
        }
      }
    }
  }

  public int currentLine(int y) {
    if (y>0 && y<tableHeight) {
      return (y/boxSize);
    } else return 0;
  }

  public void mouseClicked() {
    if (mouseY>topBorder && mouseY<(topBorder+tableHeight)) {
      if (mouseX>margin && mouseX<(margin+boxSize)) {//stats
        /*
        int partitionIndex = currentLine(mouseY-topBorder);
         if (!partitionStatsWindowCreated[actorIndex]) {
         partitionStatsWindow[partitionIndex] = new PFramePartitionStats(partitionIndex);
         partitionStatsWindowCreated[partitionIndex] = true;
         } else {
         partitionStatsWindowCreated[partitionIndex].setVisible(!partitionStatsWindow[partitionIndex].isVisible());
         }*/
      } else if (mouseX>(margin+boxSize) && mouseX<(margin+boxSize*2)) {//hide
        int partitionIndex = currentLine(mouseY-topBorder);
        if (!process.core[partitionIndex].hide) process.hideCore(partitionIndex);
        else process.unHideCore(partitionIndex);
      }
    } else if (mouseX>margin+boxSize && mouseX<margin+boxSize*2 && mouseY>topBorder-boxSize && mouseY<topBorder) {
      if (!overallHide) for (int i=0; i<process.cores_nb; i++)  process.hideCore(i);
      else for (int i=0; i<process.cores_nb; i++)  process.unHideCore(i);
      overallHide = !overallHide;
    } else if (mouseX>margin && mouseX<margin+boxSize && mouseY>topBorder-boxSize && mouseY<topBorder) {
      println("overall stats button \u00bf?\u00bf?");
      if (!running || process.actor==null || process.core==null) {
        //do NOTHING
      } else if (!partitionStatsWindowCreated) {
        partitionStatsWindow = new PFramePartitionStats();
        partitionStatsWindowCreated = true;
      } else partitionStatsWindow.setVisible(!partitionStatsWindow.isVisible());
    } else if (mouseX>width-20 && mouseX<width && mouseY<topBorder) {
      setToRedraw = true;
    }
  }

  public void keyPressed() {
    if (key==27) {
      this.key = 0;
      partitionsWindow.setVisible(false);
    } else {
      handleKeys(key);
    }
  }
}


public class PFramePartitions extends JFrame {
  PFramePartitions() {
    p = new PartitionsWindow();
    setTitle("Partitions");
    add(p);
    pack ();
    setAlwaysOnTop(true);
    setDefaultCloseOperation(HIDE_ON_CLOSE);
    setBounds(0, 0, 2*3+150, 25+process.cores_nb*20+50);
    setVisible(true);

    p.init();
  }
}

class Process {
  Table table;
  Actor[] actor = null;
  Core[] core = null;
  int actors_nb;
  int cores_nb;
  int mode = 0;//1: actors, 2: cores
  float longestWidth = -1; //used to set actorsWindow size and maybe to set the right margin size in the future 

  Process() {
  }

  public Actor[] getActors() {
    return actor;
  }

  public boolean isAlive() {
    return (core!=null || actor!=null);
  }

  public void actorInit(File[] csvFiles) {
    longestWidth = -1;
    screen = new Screen(0);
    setScreenLimits(csvFiles);
    actors_nb = csvFiles.length;
    actor = new Actor[actors_nb];

    for (int i=0; i<actors_nb; i++) {
      actor[i] = new Actor(csvFiles[i].getAbsolutePath());
      if (textWidth(actor[i].name)>longestWidth) {
        if (textWidth(actor[i].name)>longestWidth) longestWidth=PApplet.parseInt(textWidth(actor[i].name));
      }
    }
    if (core!=null) {
      actorsToCores();
    }
  }

  public void coreInit(XML[] partitions) {
    cores_nb = partitions.length;
    core = new Core[cores_nb];

    for (int i = 0; i < partitions.length; i++) {
      int id = partitions[i].getInt("id");
      XML[] instances = partitions[i].getChildren("Instance");
      core[i] = new Core(id, instances);
    }
    if (actor!=null) {
      actorsToCores();
    }
  }

  public void actorsToCores() {//only run if both actors and cores != null
    int n, i, j;

    for (i = 0; i<cores_nb; i++) {
      for (j = 0; j<core[i].actors_nb; j++) {
        n = getActorIndex(core[i].actors[j]);
        if (n !=-1) {
          actor[n].setCore(core[i].id);
        }
      }
    }
  }


  public int getActorIndex(String actorName) {
    for (int i=0; i<actors_nb; i++) {
      if (actorName.equals(actor[i].name)) {
        return i;
      }
    }
    return -1;
  }
  
  public int getCoreIndex(int coreId) {
    for (int i=0; i<cores_nb; i++) {
      if (coreId == core[i].getId()) {
        return i;
      }
    }
    return -1;
  }  

  public PapiData getPapiData(String actorName) {
    return actor[getActorIndex(actorName)].getPapiData();
  }

  public void setScreenLimits(File[] csvFiles) {
    long firstValue = -1;
    long lastValue = -1;
    long read;
    for (int i=0; i<csvFiles.length; i++) {
      table = loadTable(csvFiles[i].getAbsolutePath(), "header");
      if (table.getRowCount()>0) {
        read = table.getRow(0).getLong("tini");
        if (firstValue == -1 || firstValue > read)
          firstValue = read;

        read = table.getRow(table.getRowCount()-1).getLong("tend");
        table = loadTable(csvFiles[i].getAbsolutePath(), "header");
        if (lastValue == -1 || lastValue < table.getRow(table.getRowCount()-1).getLong("tend"))
          lastValue = read;
      }
    }
    screen.setFirstValue(firstValue);
    screen.setLastValue(lastValue);
  }

  public void draw() {
    loading=true;
    loading_title="Drawing..";
    if (mode==1) {
      drawActors();
    } else if (mode==2) {
      drawCores();
    }
    loading=false;
    loading_msg="";
    loading_title="";
    status.update("Drawing complete..");
  }  

  public void drawActors() {//if mapping file is loaded, actors will be printed in order of core
    if (actor != null) {
      int currentLine = 0;
      int amountOfActors = amountOfActorsToDraw();
      mode = 1;
      screen.reset();
      screen.setLines(amountOfActors);
      if (amountOfActors>0)
        screen.lineConfig((screen.getHeight()-amountOfActors*10)/amountOfActors, 10);
      screen.reset();      
      if (core!=null) {//imprimiendo en orden de nucleo..
        int line_nb = 0;
        for (int j=0; j<core.length; j++) {
          for (int i=0; i<actor.length; i++) {
            if (!actor[i].hide && actor[i].getCore() == core[j].getId()) {
              screen.setLineName(line_nb, actor[i].getName());
              actor[i].draw(line_nb++);
            }
          }
        }
      } else {
        for (int i=0; i<actor.length; i++) {
          if (!actor[i].hide) {
            screen.setLineName(currentLine, actor[i].getName());
            actor[i].draw(currentLine++);
          }
        }
      }
      save("data.png");
      img = loadImage("data.png");
    } else {//print error?
    }
  }

  public int amountOfActorsToDraw() {
    int n = 0;
    for (int i=0; i<actor.length; i++) {
      if (!actor[i].hide) n++;
    }
    return n;
  }

  public void drawCores() {
    if (core != null) {
      mode = 2;
      screen.reset();
      screen.setLines(cores_nb);
      screen.lineConfig((screen.getHeight()-10*cores_nb)/cores_nb, 10);
      screen.reset();
      for (int i=0; i<core.length; i++) {
        screen.setLineName(i, "Partition "+core[i].getId());
        for (int j=0; j<actors_nb; j++) {
          if (!actor[j].hide && actor[j].getCore()==core[i].getId()) {
            actor[j].draw(i);
          }
        }
      }
      save("data.png");
      img = loadImage("data.png");
    } else {//print error?
    }
  }

  public String getBoxName(long time, int lineNb) {
    if (mode==1) {
      int n;
      if ((n=getActorInLine(lineNb))!=-1)
        return (lineNb < actors_nb)? actor[n].getBoxName(time):"";
      else
        return "";
    }
    if (mode==2) {
      return (lineNb < cores_nb)? getBoxNameCoreMode(core[lineNb].getId(), time):"";
    }
    return "";
  }

  public int getActorInLine(int line) {
    if (core==null && actor!=null) {
      int currentLine = 0;
      for (int i=0; i<actor.length; i++) {
        if (!actor[i].hide) {
          if (line == currentLine) return i;
          currentLine++;
        }
      }
    } else if (core!=null) {
      int currentLine = 0;
      for (int j=0; j<core.length; j++) {
        for (int i=0; i<actor.length; i++) {
          if (!actor[i].hide && actor[i].getCore() == core[j].getId()) {
            if (line == currentLine) return i;
            else currentLine++;
          }
        }
      }
    }
    return -1;
  }

  public int getActorInLineAll(int line) {//same as getActorInLine but ignores hidden actors
    if (core==null && actor!=null) {
      int currentLine = 0;
      for (int i=0; i<actor.length; i++) {
        if (line == currentLine) return i;
        currentLine++;
      }
    } else if (core!=null) {
      int currentLine = 0;
      for (int j=0; j<core.length; j++) {
        for (int i=0; i<actor.length; i++) {
          if (actor[i].getCore() == core[j].getId()) {
            if (line == currentLine) return i;
            else currentLine++;
          }
        }
      }
    }
    return -1;
  }

  public String getBoxNameCoreMode(int coreId, long time) {//busqueda binaria para cores (incluye en la busqueda todos los actores ejecutados en el core seleccionado)
    for (int i=0; i<actors_nb; i++) {
      if (!actor[i].hide && actor[i].getCore()==coreId && !actor[i].getBoxName(time).equals("")) {
        return actor[i].getName();
      }
    }
    return "";
  }

  public void unhideAllActors() {
    if (actor!=null) {
      for (int i=0; i<actor.length; i++) {
        actor[i].hide = false;
      }
    }
  }

  public void hideCore(int index) {
    for (int j=0; j<actors_nb; j++) {
      if (actor[j].getCore()==core[index].getId()) {
        actor[j].hide=true;
      }
    }
    core[index].hide=true;
  }
  
  public void unHideCore(int index) {
    for (int j=0; j<actors_nb; j++) {
      if (actor[j].getCore()==core[index].getId()) {
        actor[j].hide=false;
      }
    }
    core[index].hide=false;
  }  
}

class Screen {
  int right_border_orig = right_border;
  boolean initSet = true;
  int pos_x;
  int pos_y;
  int screen_width; //pixels
  int screen_height;
  int lines;
  long screen_span; //miliseconds
  long firstValue = 0;
  long lastValue = 0;
  long firstValue_orig = 0; //saving original values for when we want to undo zoom
  long lastValue_orig = 0; //when zooming, first and lastValue will change..
  long zoomIni;
  long zoomEnd;
  int maxLineSize = 30;
  boolean showLineNames = true;//hide line names in full screen mode
  int line_size=20;
  int line_separation=5;

  Screen(int l) {
    pos_x = left_border;
    pos_y = top_border;
    screen_width = width-left_border-right_border;
    screen_height = height-top_border-bottom_border;
    lines = l;
  }

  public void turnOn() {
    screen_width = width-left_border-right_border;
    screen_height = height-top_border-bottom_border;
    int i;
    background(255);
    noStroke();
    fill(230);
    rect(pos_x, pos_y, screen_width, screen_height);
    //draw horizontal lines:
    for (i=0; i<lines; i++) {
      stroke(190);
      line(pos_x, pos_y+line_size/2+(line_separation+line_size)*i, pos_x+screen_width, pos_y+line_size/2+(line_separation+line_size)*i);
    }
    //draw vertical lines:
    /*
    int vertLinesNb = 10;
     long vertLinesJump = 100;
     for (i=0; i<vertLinesNb; i++) {
     stroke(190);
     line(pos_x+i*vertLinesJump, pos_y, pos_x+i*vertLinesJump, pos_y+screen_height);
     }
     */
  }

  public void switchFullScreen() {
    showLineNames = !showLineNames;
    if (!showLineNames) {
      right_border = left_border;
    } else right_border = right_border_orig;
    reset();
  }

  public void lineConfig(int size, int separation) {
    if (size>maxLineSize) line_size = maxLineSize;
    else line_size = size;
    line_separation = separation;
  }

  public int getHeight() {
    return screen_height;
  }


  public void setLines(int n) {
    lines = n;
  }

  public void setFirstValue(long v) {
    firstValue = v;
    screen_span = lastValue-firstValue;
    if (initSet) {
      firstValue_orig = v;
      if (lastValue_orig != 0) {
        initSet = false;
      }
    }
  }

  public void setLastValue(long v) {
    lastValue = v;
    screen_span = lastValue-firstValue;
    if (initSet) {
      lastValue_orig = v;
      if (firstValue_orig != 0) {
        initSet = false;
      }
    }
  }

  public long getX_relative(int x) {//returns us
    return x*screen_span/screen_width;
  }

  public long getX_actual(int x) {//returns us
    return firstValue+x*screen_span/screen_width;
  }

  public long getPixel(long x) {
    return (x-firstValue)*screen_width/screen_span;
  }

  public int getLineNb(int y) {
    return y/(line_separation+line_size);
  }

  public void addBox(long start, long end, int line, int fillColor, int strokeColor) {

    fill(fillColor);
    stroke(strokeColor);

    if (start>=lastValue || end<=firstValue) {
      return;
    } else if (start>=firstValue && end<=lastValue) {
      rect(pos_x+getPixel(start), pos_y+(line_separation+line_size)*line, getPixel(end)-getPixel(start), line_size);
    } else if (start<firstValue && end<=lastValue) {//cola se sale por la izquierda
      rect(pos_x, pos_y+(line_separation+line_size)*line, getPixel(end)-getPixel(firstValue), line_size);
    } else if (start>=firstValue && end>=lastValue) {//cola se sale por la derecha
      rect(pos_x+getPixel(start), pos_y+(line_separation+line_size)*line, getPixel(lastValue)-getPixel(start), line_size);
    } else if (start<=firstValue && end>=lastValue) {//cola se sale por ambos lados
      rect(pos_x, pos_y+(line_separation+line_size)*line, getPixel(lastValue)-getPixel(firstValue), line_size);
    }
  }

  public void setLineName(int line, String name) {
    if (showLineNames) {
      fill(0);
      textAlign(LEFT, CENTER);
      text(name, pos_x+screen_width+3, pos_y+line_size/2+(line_separation+line_size)*line);
    }
  }

  public boolean isInScreen(int x, int y) {
    if (x>=pos_x && x<=pos_x+screen_width && y>=pos_y && y<=pos_y+screen_height) {
      return true;
    } else return false;
  }

  public boolean isInScreenVertical(int y) {
    if (y>=pos_y && y<=pos_y+screen_height) {
      return true;
    } else return false;
  }


  public void applyZoom() {
    if (zoomIni>zoomEnd) {
      long temp = zoomIni;
      zoomIni = zoomEnd;
      zoomEnd = temp;
    }
    setFirstValue(zoomIni);
    setLastValue(zoomEnd);
    turnOn();
    process.draw();
  }
  public void setZoomIni(long n) {
    zoomIni=n;
  }
  public void setZoomEnd(long n) {
    zoomEnd=n;
  }
  public void zoomReset() {
    if (process.isAlive()) {
      setFirstValue(firstValue_orig);
      setLastValue(lastValue_orig);
      reset();
      process.draw();
    }
  }

  public void reset() {
    background(255);
    turnOn();
    save("data.png");
    img = loadImage("data.png");
  }
}




PFrameStats[] statsWindow;
boolean[] statsWindowCreated;
StatsWindow[] s;

public void statsWindowInit() {
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

  public void draw() {
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


  public void mouseClicked() {
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

  public void keyPressed() {
    if (key==27) {
      key = 0;
      statsWindow[actorIndex].setVisible(false);
    } else if (key=='u' || key=='U') {
      unHideAll();
    } else {
      handleKeys(key);
    }
  }

  public int countVisibleActions() {
    int n = 0;
    for (int i=0; i<data.actions.size (); i++) {
      Action action = data.actions.get(i);
      if (!action.hide) n++;
    }
    return n;
  }

  public void buttons() {
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

  public void plotter() {
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
          x = leftBorder+screenMargin+PApplet.parseFloat(currentAction)*barSize;
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

  public int getCurrentColumn(int x) {
    if (PApplet.parseInt(barSize)>0) return PApplet.parseInt((x-leftBorder-screenMargin)/barSize);
    return 0;
  }

  public int getActionInPos(int x) {
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

  public void unHideAll() {
    int actionsNb = data.actions.size();
    Action action;
    for (int i = 0; i<actionsNb; i++) {
      action = data.actions.get(i);
      action.hide = false;
    }
  }

  public float getYValue(float y) {
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

Status status;
public void status() {
  int initial_position = 260;
  noStroke();
  fill(230);
  rect(initial_position, 5, screen.screen_width-initial_position+left_border-23, 20);
  status.update();
}

public void status_init() {
  int initial_position = 260;
  status = new Status("", initial_position+3, 19);
}


class Status {
  String message = "";
  int pos_x = 0;
  int pos_y = 0;
  boolean drawBox = false;

  Status (String s, int x, int y) {
    //mono = loadFont("AndaleMono-32.vlw");
    message = s;
    pos_x = x;
    pos_y = y;
  }

  Status () {
    message = "";
    pos_x = 0;
    pos_y = 0;
  }

  public void setDrawBox(boolean b) {
    drawBox = b;
  }

  public void update(String s) {
    message = s;
    print();
  }

  public void update(String s, int x, int y) {
    message = s;
    pos_x = x;
    pos_y = y;
    print();
  }

  public void update() {
    print();
  }

  public void print() {
    if (pos_x+textWidth(message)>width) {
      textAlign(RIGHT);
      if (drawBox) {
        noStroke();
        fill(250); 
        rect(pos_x-textWidth(message), pos_y-11, textWidth(message), 15);
      }
    } else {
      textAlign(LEFT);
      if (drawBox) {
        noStroke();
        fill(250); 
        rect(pos_x, pos_y-11, textWidth(message), 15);
      }
    }
    fill(0, 0, 0);
    text(message, pos_x, pos_y);
  }
} 

public void handleXCFFile(File selection) {
  XML xcf;
  if (selection == null) {
    println("Window was closed or the user hit cancel.");
    return;
  }
  xcf = loadXML(selection.getAbsolutePath());
  if (xcf == null) {
    status.update("XCF could not be parsed.");
    return;
  }
  running = false;
  XML partitioning = xcf.getChild("Partitioning");
  XML[] partitions = partitioning.getChildren("Partition");
  status.update("Loading, please wait..");
  process.coreInit(partitions);
  status.update("Loading complete, click on the Partitions tab to display results");
  process.mode=2;
  running=true;
  setToRedraw = true;
}

int icon_size = 20;

//reset
String reset_txt = "Refresh window (R)";
PShape button_reset;
int reset_X = 15, reset_Y = 5;
boolean resetOver = false;
//load papi
String loadpapi_txt = "Open papi-output directory";
PShape button_loadpapi;
int loadpapi_X = 40, loadpapi_Y = 5;
boolean loadpapiOver = false;
//load xcf
String loadxcf_txt = "Load XCF mapping file";
PShape button_loadxcf;
int loadxcf_X = 65, loadxcf_Y = 5;
boolean loadxcfOver = false;
//zoom in
String zoom_txt = "Zoom in (Z)";
PShape button_zoom;
int zoom_X = 95, zoom_Y = 5;
boolean zoomOver = false;
//zoom reset
String zoomout_txt = "Zoom reset (Shift+Z)";
PShape button_zoomout;
int zoomout_X = 120, zoomout_Y = 5;
boolean zoomoutOver = false;
//pointer
String pointer_txt = "Pointer tool (X)";
PShape button_pointer;
int pointer_X = 145, pointer_Y = 5;
boolean pointerOver = false;
//actorsWindow
String actorsWindow_txt = "Hide/unhide actors and check individual PAPI readings (A)";
PShape button_actorsWindow;
int actorsWindow_X = 170, actorsWindow_Y = 5;
boolean actorsWindowOver = false;
//partitionsWindow
String partitionsWindow_txt = "Hide/unhide partitions and check PAPI readings summarized by partition (Shift+A)";
PShape button_partitionsWindow;
int partitionsWindow_X = 195, partitionsWindow_Y = 5;
boolean partitionsWindowOver = false;
////hide
//String hide_txt = "Hide tool";
PShape button_hide;
//int hide_X = 170, hide_Y = 5;
//boolean hideOver = false;
//boolean hideOn = false;
////unhide
//String unhide_txt = "Unhide all";
PShape button_unhide;
//int unhide_X = 195, unhide_Y = 5;
//boolean unhideOver = false;
//boolean unhideOn = false;
//stats
String stats_txt = "Overall PAPI readings (P)";
PShape button_stats;
PShape button_stats_alt;
int stats_X = 225, stats_Y = 5;
boolean statsOver = false;


/***tabs***/
//drawCores
String drawCores_tab = "Partitions";
String drawCores_txt = "Draw partitions timeline";
PShape button_drawCores;
int drawCores_X = 0, drawCores_Y = top_border-20;
boolean drawCoresOver = false;
//drawActors
String drawActors_tab = "Actors";
String drawActors_txt = "Draw actors timeline";
PShape button_drawActors;
float drawActors_X = 0, drawActors_Y = top_border-20;
boolean drawActorsOver = false;
//fullscreen
String fullscreen_txt = "Show/hide right bar";
PShape button_fullscreen_r;
PShape button_fullscreen_l;
int fullscreen_X = 0, fullscreen_Y = 5;
boolean fullscreenOver = false;
/**********/
//Actor/Core names area
/***********/

boolean set_zoom_ini = false;
boolean set_zoom_end = false;

Status menu_help;
public void buttons_init() {
  load_icons();
  menu_help = new Status("", left_border, icon_size*2+5);
}

public void buttons() {
  update(mouseX, mouseY);
  shape(button_reset, reset_X, reset_Y, icon_size, icon_size);
  shape(button_loadpapi, loadpapi_X, loadpapi_Y, icon_size, icon_size);
  shape(button_loadxcf, loadxcf_X, loadxcf_Y, icon_size, icon_size);
  stroke(220);
  line((loadxcf_X+zoom_X+icon_size)/2, 5, (loadxcf_X+zoom_X+icon_size)/2, 5+icon_size);
  shape(button_zoom, zoom_X, zoom_Y, icon_size, icon_size);
  shape(button_zoomout, zoomout_X, zoomout_Y, icon_size, icon_size);
  shape(button_pointer, pointer_X, pointer_Y, icon_size, icon_size);
  //shape(button_hide, hide_X, hide_Y, icon_size, icon_size);
  //shape(button_unhide, unhide_X, unhide_Y, icon_size, icon_size);
  shape(button_actorsWindow, actorsWindow_X, actorsWindow_Y, icon_size, icon_size);
  shape(button_partitionsWindow, partitionsWindow_X, partitionsWindow_Y, icon_size, icon_size);  
  stroke(220);
  line((partitionsWindow_X+stats_X+icon_size)/2, 5, (partitionsWindow_X+stats_X+icon_size)/2, 5+icon_size);  
  shape(button_stats, stats_X, stats_Y, icon_size, icon_size);

  //tabs:
  //if (process.actor!=null) {
  noStroke();
  if (process.mode==1) fill(230); 
  else fill(245);
  rect(left_border+screen.screen_width-textWidth(drawActors_tab), drawActors_Y, textWidth(drawActors_tab), 20);
  textAlign(LEFT, TOP);
  if (process.mode==1) fill(0, 0, 0); 
  else fill(0, 0, 0, 100);
  text(drawActors_tab, left_border+screen.screen_width-textWidth(drawActors_tab), drawActors_Y);
  //}
  //if (process.core!=null) {
  noStroke();
  if (process.mode==2) fill(230); 
  else fill(245);
  rect(left_border+screen.screen_width-textWidth(drawCores_tab)-textWidth(drawActors_tab)-5, drawCores_Y, textWidth(drawCores_tab), 20);
  textAlign(LEFT, TOP);
  if (process.mode==2) fill(0, 0, 0); 
  else fill(0, 0, 0, 100);
  text(drawCores_tab, left_border+screen.screen_width-textWidth(drawCores_tab)-textWidth(drawActors_tab)-5, drawCores_Y);
  //}
  fullscreen_X = left_border+screen.screen_width-20;
  if (screen.showLineNames) {
    shape(button_fullscreen_r, fullscreen_X, fullscreen_Y, icon_size, icon_size);
  } else {
    shape(button_fullscreen_l, fullscreen_X, fullscreen_Y, icon_size, icon_size);
  }
}

public void update(int x, int y) {
  if ( overButton(zoom_X, zoom_Y, icon_size, icon_size) ) {
    zoomOver = true;
    menu_help.update(zoom_txt);
  } else {
    zoomOver = false;
  }
  if ( overButton(zoomout_X, zoomout_Y, icon_size, icon_size) ) {
    zoomoutOver = true;
    menu_help.update(zoomout_txt);
  } else {
    zoomoutOver = false;
  }
  if ( overButton(pointer_X, pointer_Y, icon_size, icon_size) ) {
    pointerOver = true;
    menu_help.update(pointer_txt);
  } else {
    pointerOver = false;
  }
  if ( overButton(loadxcf_X, loadpapi_Y, icon_size, icon_size) ) {
    loadxcfOver = true;
    menu_help.update(loadxcf_txt);
  } else {
    loadxcfOver = false;
  }
  if ( overButton(loadpapi_X, loadpapi_Y, icon_size, icon_size) ) {
    loadpapiOver = true;
    menu_help.update(loadpapi_txt);
  } else {
    loadpapiOver = false;
  }
  if ( overButton(stats_X, stats_Y, icon_size, icon_size) ) {
    statsOver = true;
    menu_help.update(stats_txt);
  } else {
    statsOver = false;
  }
  //  if ( overButton(hide_X, hide_Y, icon_size, icon_size) ) {
  //    hideOver = true;
  //    menu_help.update(hide_txt);
  //  } else {
  //    hideOver = false;
  //  }
  //  if ( overButton(unhide_X, unhide_Y, icon_size, icon_size) ) {
  //    unhideOver = true;
  //    menu_help.update(unhide_txt);
  //  } else {
  //    unhideOver = false;
  //  }
  if ( overButton(actorsWindow_X, actorsWindow_Y, icon_size, icon_size) ) {
    actorsWindowOver = true;
    menu_help.update(actorsWindow_txt);
  } else {
    actorsWindowOver = false;
  }
  if ( overButton(partitionsWindow_X, partitionsWindow_Y, icon_size, icon_size) ) {
    partitionsWindowOver = true;
    menu_help.update(partitionsWindow_txt);
  } else {
    partitionsWindowOver = false;
  }
  if ( overButton(fullscreen_X, fullscreen_Y, icon_size, icon_size) ) {
    fullscreenOver = true;
    if (screen.showLineNames) {
      menu_help.update("Hide right panel (F)");
    } else {
      menu_help.update("Show right panel (F)");
    }
  } else {
    fullscreenOver = false;
  }
  if ( overButton(reset_X, reset_Y, icon_size, icon_size) ) {
    resetOver = true;
    menu_help.update(reset_txt);
  } else {
    resetOver = false;
  }     

  if ( overTab(left_border+screen.screen_width-textWidth(drawCores_tab)-textWidth(drawActors_tab)-5, drawCores_Y, textWidth(drawCores_tab), icon_size)) {
    drawCoresOver = true;
    menu_help.update(drawCores_txt);
  } else {
    drawCoresOver = false;
  }
  if ( overTab(left_border+screen.screen_width-textWidth(drawActors_tab), drawActors_Y, textWidth(drawActors_tab), icon_size)) {
    drawActorsOver = true;
    menu_help.update(drawActors_txt);
  } else {
    drawActorsOver = false;
  }
}

public void mouseClicked() {
  if (loadpapiOver) {
    selectFolder("Select the directory containing the CSV ousput files", "handleCSVDir");
  }
  /***************/
  if (loadxcfOver) {
    selectInput("Select the mapping file (XCF)", "handleXCFFile");
  }
  /***************/
  if (drawCoresOver) {
    if (process.core!=null) {
      process.drawCores();
    } else status.update("Nothing to show here.. try loading a mapping file.");
  }
  /***************/
  if (drawActorsOver) {
    if (process.actor!=null) {
      process.drawActors();
    } else status.update("Nothing to show here.. try loading a papi-output directory.");
  }
  /***************/
  if (zoomOver) {
    if (set_zoom_ini || set_zoom_end){
      //screen.zoomReset();
      set_zoom_ini = set_zoom_end = false;
    }
    else if (process.actor!=null)
      set_zoom_ini = true;
  } else if (set_zoom_ini) {
    set_zoom_ini = false;
    set_zoom_end = true;
    screen.setZoomIni(screen.getX_actual(last_known_pointer-left_border));
  } else if (set_zoom_end) {
    set_zoom_end = false;
    screen.setZoomEnd(screen.getX_actual(last_known_pointer-left_border));
    screen.applyZoom();
  }
  /***************/
  if (zoomoutOver) {
    screen.zoomReset();
  }
  /***************/
  if (pointerOver) {
    pointerOn = (pointerOn == true)? false : true;
  }
  /***************/
  if (statsOver && running) {
//    if (!statsWindowCreated[19]) {
//      statsWindow[19] = new PFrameStats(19);
//      statsWindow[19].setVisible(true);
//      statsWindowCreated[19] = true;
//    } else {
//      statsWindow[19].setVisible(!statsWindow[19].isVisible());
//    }
  }
  /***************/
  if (resetOver) {
    screen.reset();
    process.draw();
  }
  /***************/
  if (fullscreenOver) {
    screen.switchFullScreen();
    process.draw();
  }
  /***************/
  //  if (hideOver) {
  //    hideOn = !hideOn;
  //  }
  //  if (hideOn && screen.isInScreenVertical(mouseY)) {
  //    if (process.mode == 1) {
  //      int actorIndex = process.getActorInLine(screen.getLineNb(mouseY-top_border));
  //      if (actorIndex != -1 && process.actor.length>actorIndex) {
  //        process.actor[process.getActorInLine(screen.getLineNb(mouseY-top_border))].setHide(true);
  //        process.draw();
  //      }
  //    } else if (process.mode == 2) {//partitions..
  //      //TODO
  //    }
  //    //hideOn = false;
  //  }
  /**************/
  //  if (unhideOver) {
  //    process.unhideAllActors();
  //    screen.reset();
  //    process.draw();
  //    if (!actorsWindowCreated) {
  //      actorsWindow = new PFrameActors();
  //      actorsWindow.setVisible(true);
  //      actorsWindowCreated = true;
  //    } else actorsWindow.setVisible(!actorsWindow.isVisible());
  //  }
  /**************/
  if (actorsWindowOver) {
    if(!running || process.actor==null){
      status.update("Try loading a papi-output directory before openning the actors window..");
    }
    else if (!actorsWindowCreated) {
      actorsWindow = new PFrameActors();
      //actorsWindow.setVisible(true);
      actorsWindowCreated = true;
    } else actorsWindow.setVisible(!actorsWindow.isVisible());
  }
  if (partitionsWindowOver) {
    if(!running || process.core==null){
      status.update("Try loading a XCF partitions file before openning the partitions window..");
    }
    else if (!partitionsWindowCreated) {
      partitionsWindow = new PFramePartitions();
      //actorsWindow.setVisible(true);
      partitionsWindowCreated = true;
    } else partitionsWindow.setVisible(!partitionsWindow.isVisible());
  }
  if (statsOver) {
    if(!running || process.actor==null){
      status.update("???");
    }
    else if (!globalStatsWindowCreated) {
      globalStatsWindow = new PFrameGlobalStats();
      globalStatsWindowCreated = true;
    } else globalStatsWindow.setVisible(!globalStatsWindow.isVisible());
  }   
}


public boolean overButton(int x, int y, int width, int height) {
  if (mouseX >= x && mouseX <= x+width && 
    mouseY >= y && mouseY <= y+height) {
    return true;
  } else {
    return false;
  }
}

public boolean overTab(float x, float y, float width, int height) {
  if (mouseX >= x && mouseX <= x+width && 
    mouseY >= y && mouseY <= y+height) {
    return true;
  } else {
    return false;
  }
}

public void load_icons() {
  button_loadxcf = loadShape("icons/file.svg");
  button_loadpapi = loadShape("icons/folder.svg");
  button_zoom = loadShape("icons/zoom-in.svg");
  button_zoomout = loadShape("icons/zoom-out.svg");
  button_pointer = loadShape("icons/hand-point-up.svg");
  button_stats = loadShape("icons/bar-chart.svg");
  button_stats_alt = loadShape("icons/bar-chart-alt.svg");
  button_fullscreen_r = loadShape("icons/shift-right-alt.svg");
  button_fullscreen_l = loadShape("icons/shift-left-alt.svg");
  button_hide = loadShape("icons/eye-closed2.svg");
  button_unhide = loadShape("icons/eye.svg");  
  button_reset = loadShape("icons/reload.svg");
  //button_actorsWindow = loadShape("icons/layout-list-thumb-alt.svg");
  button_partitionsWindow = loadShape("icons/partition.svg");  
  button_actorsWindow = loadShape("icons/actor.svg");
}




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

  public void draw() {
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
  public void mouseClicked() {
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

  public int getActorInPos(int x) {
    int actorsNb = stats.actorName.length;
    int currentColumn = getCurrentColumn(x);
    int currentActor = 0;

    for (int i = 0; i<actorsNb; i++) {
      if (currentColumn==currentActor && !process.actor[process.getActorIndex(stats.actorName[i])].hide) return i;
      if (!process.actor[process.getActorIndex(stats.actorName[i])].hide) currentActor++;
    }
    return -1;
  }

  public void mouseIndicator() {
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
  
  public float getYValue(float y) {
    if (log) {
      return pow(10, map(y, height-bottomBorder, topBorder+screenMargin, 0, maxValue));
    }
    return map(y, height-bottomBorder, topBorder+screenMargin, 0, maxValue);
  }  

  public int getCurrentColumn(int x) {
    if (PApplet.parseInt(barSize)>0) return PApplet.parseInt((x-leftBorder-screenMargin)/barSize);
    return 0;
  }

  public void buttons() {
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

  public void plotter() {
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
          x = leftBorder+screenMargin+PApplet.parseFloat(curretnBar)*barSize;
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

  public int countVisibleActors() {
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

  public void run() {
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

  public void setPapiData() {
    for (int i=0; i<process.actors_nb; i++) {
      process.actor[i].setPapiData();
    }
  }

  public void setGlobalData(Actor[] a) {
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

  public boolean actorHasEvent(Actor a, String eventName) {
    for (int i=0; i<a.data.eventName.length; i++) {
      if (a.data.eventName[i].equals(eventName)) return true;
    }
    return false;
  }

  public void countAndAdd(int eventIndex, int actorIndex) {
    eventCount[eventIndex][actorIndex] = 0;
    int actorProcessIndex = process.getActorIndex(actorName[actorIndex]);
    int eventDataIndex = process.actor[actorProcessIndex].data.getEventIndex(eventName.get(eventIndex));
    for (int i=0; i<process.actor[actorProcessIndex].data.actions.size (); i++) {
      eventCount[eventIndex][actorIndex] += process.actor[actorProcessIndex].data.actions.get(i).eventCount[eventDataIndex];
    }
    return;
  }

  public float getMaxValue(int eventIndex) {
    float maxValue = 0;
    for (int j=0; j< process.actors_nb; j++) {
      if (maxValue<eventCount[eventIndex][j]) maxValue = eventCount[eventIndex][j];
    }
    return maxValue;
  }
}




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

  public void draw() {
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
      //plotter();
      //buttons();
      //mouseIndicator();
    }
  }

  public void mouseClicked() {
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


  public void mouseIndicator() {
    int partitionIndex = getCurrentColumn(mouseX);
    if (partitionIndex>=0 && mouseX>=leftBorder && getCurrentColumn(mouseX) >=0 && mouseY>topBorder && mouseY<height-bottomBorder) {
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
  
  public float getYValue(float y) {
    if (log) {
      return pow(10, map(y, height-bottomBorder, topBorder+screenMargin, 0, maxValue));
    }
    return map(y, height-bottomBorder, topBorder+screenMargin, 0, maxValue);
  }  

  public int getCurrentColumn(int x) {
    if (PApplet.parseInt(barSize)>0) return PApplet.parseInt((x-leftBorder-screenMargin)/barSize);
    return 0;
  }
/*
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

  int countVisibleActors() {
    int n = 0;
    for (int i=0; i< process.actors_nb; i++) {
      if (!process.actor[i].hide) n++;
    }
    return n;
  }
  */
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

  public void run() {
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

  public void setPapiData() {
    for (int i=0; i<process.actors_nb; i++) {
      process.actor[i].setPapiData();
    }
  }

  public void setGlobalData(Core[] c, Actor[] a) {
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

  public boolean actorHasEvent(Actor a, String eventName) {
    for (int i=0; i<a.data.eventName.length; i++) {
      if (a.data.eventName[i].equals(eventName)) return true;
    }
    return false;
  }

  public void countAndAdd(int eventIndex, int actorIndex) {
    int coreIndex = process.getCoreIndex(process.actor[actorIndex].core);
    int eventDataIndex = process.actor[actorIndex].data.getEventIndex(eventName.get(eventIndex));
    for (int i=0; i<process.actor[actorIndex].data.actions.size (); i++) {
      eventCount[eventIndex][coreIndex] += process.actor[actorIndex].data.actions.get(i).eventCount[eventDataIndex];
    }
    return;
  }

  public float getMaxValue(int eventIndex) {
    float maxValue = 0;
    for (int j=0; j< process.cores_nb; j++) {
      if (maxValue<eventCount[eventIndex][j]) maxValue = eventCount[eventIndex][j];
    }
    return maxValue;
  }
}

int last_known_pointer = -1;//last known pointer location inside display (x axis) - used in several classes, don't change!

boolean pointerOn = false;
Status relative;
Status actual;
Status boxName;
Status lineIndicator;

long pointerVal = 0;

public void pointer_init() {
  relative = new Status("", left_border, height-20);
  actual = new Status("", left_border, height-5);
  lineIndicator = new Status("", width, height);
  boxName = new Status();
  boxName.setDrawBox(true);
}

static long relative_us = 0;
static long actual_us = 0;
public void pointer() {
  //update pointer location:
  if(screen.isInScreen(mouseX, mouseY)){
    last_known_pointer = mouseX;
  }
    
  String someName;
  if (pointerOn && screen.isInScreen(mouseX, mouseY)) {
    someName = process.getBoxName(screen.getX_actual(mouseX-left_border), screen.getLineNb(mouseY-top_border));
    boxName.update(someName, mouseX, mouseY);
    lineIndicator.update("\"line\": "+screen.getLineNb(mouseY-top_border), width, height);
    stroke(0);
    line(mouseX, top_border, mouseX, height-bottom_border);
  }
  
  if (pointerOn){
    stroke(0);
    line(last_known_pointer, top_border, last_known_pointer, height-bottom_border);
  }


  //always on indicators:
  if (screen.isInScreen(mouseX, mouseY)) {
    relative_us = screen.getX_relative(mouseX-left_border);
    actual_us = screen.getX_actual(mouseX-left_border);
  }
  //relative minutes and seconds:
  int miliseconds = (int) (relative_us / 1000) % 1000;
  int seconds = (int) (relative_us / 1000000) % 60;
  int minutes = (int) ((relative_us / (1000000*60)) % 60);
  int hours   = (int) ((relative_us / (1000000*60*60)) % 24);
  //
  relative.update("Relative: "+relative_us+" us ("+hours+":"+minutes+":"+seconds+":"+miliseconds+")", left_border, height-20);
  actual.update("Actual:   "+actual_us+" us", left_border, height-5);
}
public void zoom() {
  if (screen.isInScreen(mouseX, mouseY)) {
    if (set_zoom_ini) {
      stroke(204, 102, 0);
      line(screen.pos_x+screen.getPixel(screen.getX_actual(mouseX-left_border)), top_border, screen.pos_x+screen.getPixel(screen.getX_actual(mouseX-left_border)), height-bottom_border);
    }
    if (set_zoom_end) {
      fill(0, 0, 0, 20);
      stroke(204, 102, 0);
      rect(screen.pos_x+screen.getPixel(screen.zoomIni), top_border, screen.getPixel(screen.getX_actual(mouseX-left_border))-screen.getPixel(screen.zoomIni), screen.screen_height);
    }
  } else if (set_zoom_ini) {
    stroke(204, 102, 0);
    line(screen.pos_x+screen.getPixel(screen.getX_actual(last_known_pointer-left_border)), top_border, screen.pos_x+screen.getPixel(screen.getX_actual(last_known_pointer-left_border)), height-bottom_border);
  } else if (set_zoom_end) {
    fill(0, 0, 0, 20);
    stroke(204, 102, 0);
    rect(screen.pos_x+screen.getPixel(screen.zoomIni), top_border, screen.getPixel(screen.getX_actual(last_known_pointer-left_border))-screen.getPixel(screen.zoomIni), screen.screen_height);
  }
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "papify_plot" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
