java.io.FilenameFilter csvFilter = new java.io.FilenameFilter() {
  boolean accept(File dir, String name) {
    return name.toLowerCase().endsWith(".csv");
  }
};

void handleCSVDir(File directory) {
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

void loading() {
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
