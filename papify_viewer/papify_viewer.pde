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
//obtener tamaño del margen derecho segun el nombre mas largo de todos..
//Mejorar pointer tool
//fix bug: when loading papi output AND xcf and then loading another papi output, display goes highwire (tip: when loading dir again, kill cores..)

Screen screen;
Process process;
Color colors;
PImage img, zoom_img;

void setup() {
  size(1200, 768);
  background(255);
  //resizeable screen

  if (frame != null) {
    frame.setResizable(true);
    frame.setTitle("Papify Viewer");
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

void draw() {
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

void startRunning() {
  running=true;
}

void stopRunning() {
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
  
  if (partitionsWindow!=null) partitionsWindow.dispose();
  partitionsWindowCreated = false;
  p=null;
  partitionsWindow=null;  
  
  if (globalStatsWindow!=null) globalStatsWindow.dispose();
  globalStatsWindowCreated = false;
  gs=null;
  globalStatsWindow=null;
  
  if (partitionStatsWindow!=null) partitionStatsWindow.dispose();
  partitionStatsWindowCreated = false;
  ps=null;
  partitionStatsWindow=null;  
}

void keyPressed() {
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



void cursorWatch() {
  if (loading) {
    cursor(WAIT);
  } else if (false) {
    cursor(HAND);
  } else {
    cursor(ARROW);
  }
}

void handleKeys(int key) {//Global shortcut
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

