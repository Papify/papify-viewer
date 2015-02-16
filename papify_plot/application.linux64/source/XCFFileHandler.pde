void handleXCFFile(File selection) {
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

