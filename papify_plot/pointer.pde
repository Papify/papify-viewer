int last_known_pointer = -1;//last known pointer location inside display (x axis) - used in several classes, don't change!

boolean pointerOn = false;
Status relative;
Status actual;
Status boxName;
Status lineIndicator;

long pointerVal = 0;

void pointer_init() {
  relative = new Status("", left_border, height-20);
  actual = new Status("", left_border, height-5);
  lineIndicator = new Status("", width, height);
  boxName = new Status();
  boxName.setDrawBox(true);
}

static long relative_us = 0;
static long actual_us = 0;
void pointer() {
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
