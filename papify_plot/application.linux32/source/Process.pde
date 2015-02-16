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

  Actor[] getActors() {
    return actor;
  }

  boolean isAlive() {
    return (core!=null || actor!=null);
  }

  void actorInit(File[] csvFiles) {
    longestWidth = -1;
    screen = new Screen(0);
    setScreenLimits(csvFiles);
    actors_nb = csvFiles.length;
    actor = new Actor[actors_nb];

    for (int i=0; i<actors_nb; i++) {
      actor[i] = new Actor(csvFiles[i].getAbsolutePath());
      if (textWidth(actor[i].name)>longestWidth) {
        if (textWidth(actor[i].name)>longestWidth) longestWidth=int(textWidth(actor[i].name));
      }
    }
    if (core!=null) {
      actorsToCores();
    }
  }

  void coreInit(XML[] partitions) {
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

  void actorsToCores() {//only run if both actors and cores != null
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


  int getActorIndex(String actorName) {
    for (int i=0; i<actors_nb; i++) {
      if (actorName.equals(actor[i].name)) {
        return i;
      }
    }
    return -1;
  }
  
  int getCoreIndex(int coreId) {
    for (int i=0; i<cores_nb; i++) {
      if (coreId == core[i].getId()) {
        return i;
      }
    }
    return -1;
  }  

  PapiData getPapiData(String actorName) {
    return actor[getActorIndex(actorName)].getPapiData();
  }

  void setScreenLimits(File[] csvFiles) {
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

  void draw() {
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

  void drawActors() {//if mapping file is loaded, actors will be printed in order of core
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

  int amountOfActorsToDraw() {
    int n = 0;
    for (int i=0; i<actor.length; i++) {
      if (!actor[i].hide) n++;
    }
    return n;
  }

  void drawCores() {
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

  String getBoxName(long time, int lineNb) {
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

  int getActorInLine(int line) {
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

  int getActorInLineAll(int line) {//same as getActorInLine but ignores hidden actors
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

  String getBoxNameCoreMode(int coreId, long time) {//busqueda binaria para cores (incluye en la busqueda todos los actores ejecutados en el core seleccionado)
    for (int i=0; i<actors_nb; i++) {
      if (!actor[i].hide && actor[i].getCore()==coreId && !actor[i].getBoxName(time).equals("")) {
        return actor[i].getName();
      }
    }
    return "";
  }

  void unhideAllActors() {
    if (actor!=null) {
      for (int i=0; i<actor.length; i++) {
        actor[i].hide = false;
      }
    }
  }

  void hideCore(int index) {
    for (int j=0; j<actors_nb; j++) {
      if (actor[j].getCore()==core[index].getId()) {
        actor[j].hide=true;
      }
    }
    core[index].hide=true;
  }
  
  void unHideCore(int index) {
    for (int j=0; j<actors_nb; j++) {
      if (actor[j].getCore()==core[index].getId()) {
        actor[j].hide=false;
      }
    }
    core[index].hide=false;
  }  
}

