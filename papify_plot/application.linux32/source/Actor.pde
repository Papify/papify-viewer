class Actor {
Table table;
  String name;
  String filePath;
  long boxNb;
  Box[] boxes;//TODO change to ArrayList
  int core = -1; //core al que pertenece (si es -1, no se encontró el core en el xcf o bien todavía no se cargó el xcf)
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
  
  void setHide(boolean b){
    hide = b;
  }
  
  void switchHide(){
    hide = !hide;
  }
  
  void setPapiData(){
    if(data == null) data = new PapiData(filePath);
  }
  
  PapiData getPapiData(){
    return data;
  }
  
  String getFilePath(){
    return filePath;
  }

  void setBoxNb(long n) {
    boxNb = n;
  }

  void setCore(int n) {
    core = n;
  }

  int getCore() {
    return core;
  }

  String getName() {
    return name;
  }  

  void draw(int line) {
    for (int i = 0; i<boxNb; i++) {
      screen.addBox(boxes[i].tini, boxes[i].tend, line, fillColor, strokeColor);
    }
  }
  
  String getBoxName(long time) {//busqueda binaria
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

