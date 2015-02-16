Status status;
void status() {
  int initial_position = 260;
  noStroke();
  fill(230);
  rect(initial_position, 5, screen.screen_width-initial_position+left_border-23, 20);
  status.update();
}

void status_init() {
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

  void setDrawBox(boolean b) {
    drawBox = b;
  }

  void update(String s) {
    message = s;
    print();
  }

  void update(String s, int x, int y) {
    message = s;
    pos_x = x;
    pos_y = y;
    print();
  }

  void update() {
    print();
  }

  void print() {
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

