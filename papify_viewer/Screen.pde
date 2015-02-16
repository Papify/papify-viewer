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

  void turnOn() {
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

  void switchFullScreen() {
    showLineNames = !showLineNames;
    if (!showLineNames) {
      right_border = left_border;
    } else right_border = right_border_orig;
    reset();
  }

  void lineConfig(int size, int separation) {
    if (size>maxLineSize) line_size = maxLineSize;
    else line_size = size;
    line_separation = separation;
  }

  int getHeight() {
    return screen_height;
  }


  void setLines(int n) {
    lines = n;
  }

  void setFirstValue(long v) {
    firstValue = v;
    screen_span = lastValue-firstValue;
    if (initSet) {
      firstValue_orig = v;
      if (lastValue_orig != 0) {
        initSet = false;
      }
    }
  }

  void setLastValue(long v) {
    lastValue = v;
    screen_span = lastValue-firstValue;
    if (initSet) {
      lastValue_orig = v;
      if (firstValue_orig != 0) {
        initSet = false;
      }
    }
  }

  long getX_relative(int x) {//returns us
    return x*screen_span/screen_width;
  }

  long getX_actual(int x) {//returns us
    return firstValue+x*screen_span/screen_width;
  }

  long getPixel(long x) {
    return (x-firstValue)*screen_width/screen_span;
  }

  int getLineNb(int y) {
    return y/(line_separation+line_size);
  }

  void addBox(long start, long end, int line, int fillColor, int strokeColor) {

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

  void setLineName(int line, String name) {
    if (showLineNames) {
      fill(0);
      textAlign(LEFT, CENTER);
      text(name, pos_x+screen_width+3, pos_y+line_size/2+(line_separation+line_size)*line);
    }
  }

  boolean isInScreen(int x, int y) {
    if (x>=pos_x && x<=pos_x+screen_width && y>=pos_y && y<=pos_y+screen_height) {
      return true;
    } else return false;
  }

  boolean isInScreenVertical(int y) {
    if (y>=pos_y && y<=pos_y+screen_height) {
      return true;
    } else return false;
  }


  void applyZoom() {
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
  void setZoomIni(long n) {
    zoomIni=n;
  }
  void setZoomEnd(long n) {
    zoomEnd=n;
  }
  void zoomReset() {
    if (process.isAlive()) {
      setFirstValue(firstValue_orig);
      setLastValue(lastValue_orig);
      reset();
      process.draw();
    }
  }

  void reset() {
    background(255);
    turnOn();
    save("data.png");
    img = loadImage("data.png");
  }
}

