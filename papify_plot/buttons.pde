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
void buttons_init() {
  load_icons();
  menu_help = new Status("", left_border, icon_size*2+5);
}

void buttons() {
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

void update(int x, int y) {
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

void mouseClicked() {
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


boolean overButton(int x, int y, int width, int height) {
  if (mouseX >= x && mouseX <= x+width && 
    mouseY >= y && mouseY <= y+height) {
    return true;
  } else {
    return false;
  }
}

boolean overTab(float x, float y, float width, int height) {
  if (mouseX >= x && mouseX <= x+width && 
    mouseY >= y && mouseY <= y+height) {
    return true;
  } else {
    return false;
  }
}

void load_icons() {
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

