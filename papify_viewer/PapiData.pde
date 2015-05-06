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

  void crunch() {
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

  long getMaxValue() {
    long currentMax = 0;
    for (int i=0; i<actions.size (); i++) {
      Action action = actions.get(i);
      for (int j=0; j<eventName.length; j++) {
        if (currentMax<action.eventCount[j]) currentMax = action.eventCount[j];
      }
    }
    return currentMax;
  }

  long getMaxValue(int eventIndex) {
    long currentMax = 0;
    for (int i=0; i<actions.size (); i++) {
      Action action = actions.get(i);
      if (currentMax<action.eventCount[eventIndex]) currentMax = action.eventCount[eventIndex];
    }
    return currentMax;
  }  


  long getMaxCallsValue() {
    long currentMax = 0;
    for (int i=0; i<actions.size (); i++) {
      Action action = actions.get(i);
      if (currentMax<action.calls) currentMax = action.calls;
    }
    return currentMax;
  }
  
  long getMaxAvgCallsValue(int eventIndex) {
    long currentMax = 0;
    for (int i=0; i<actions.size (); i++) {
      Action action = actions.get(i);
      if (currentMax<(action.eventCount[eventIndex]/action.calls)) currentMax = (action.eventCount[eventIndex]/action.calls);
    }
    return currentMax;
  }
  
  
  void setEvents() {
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

  void setActions() {
    if (eventsNb > 0) {
      table = loadTable(actorPath, "header");
      for (TableRow row : table.rows ()) {
        boolean actionExists = false;
        String actionName = row.getString("Action");
        for (Action action : actions) {
          if (action.name().equals(actionName)) {
            actionExists = true;
            for (int i = 4; i<eventsNb+4; i++){
              action.sumEvent(i-4, row.getLong(i));//WHAT? solo con dos eventos? ni mas ni menos? no me acuerdo si hice esto a posta, verificar! TODO
              //action.sum(1, row.getLong(5));
            }
            action.sumCalls();
            continue;
          }
        }
        if (!actionExists) {
          Action someAction = new Action(actionName, eventsNb);
            for (int i = 4; i<eventsNb+4; i++){
              someAction.sumEvent(i-4, row.getLong(i));//WHAT? solo con dos eventos? ni mas ni menos? no me acuerdo si hice esto a posta, verificar! TODO
              //action.sum(1, row.getLong(5));
            }
            someAction.sumCalls();     
          //someAction.sum(0, row.getLong(4));//WHAT? solo con dos eventos? ni mas ni menos? no me acuerdo si hice esto a posta, verificar! TODO
          //someAction.sum(1, row.getLong(5));
          actions.add(someAction);
        }
        //for (int i = 4; i<eventsNb+4; i++) {//empezamos en 4 para saltarnos las columnas que no son eventos
        //}
      }
    }
  }

  boolean actionExists(String name) {
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
  
  int getEventIndex(String eventName){
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

  String name() {
    return name;
  }

  long calls() {
    return calls;
  }

  void setHide (boolean b) {
    hide = b;
  }

  void sumEvent(int eventIndex, long value) {
    eventCount[eventIndex] += value;
  }
  void sumCalls(){
    calls++;
  }
}

