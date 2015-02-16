PFrameActors actorsWindow;
boolean actorsWindowCreated;
ActorsWindow a;

public class ActorsWindow extends PApplet {
  int margin = 3;
  int iconSize = 16;
  int boxSize = 20;
  int topBorder = 26;
  int tableWidth = (int)process.longestWidth+2*boxSize+2*5+10;
  int tableHeight = boxSize*process.actors_nb;
  boolean overallHide = false;

  public void setup() {
  }

  void draw() {
    background(255);
    if (actorsWindow!=null && running && actorsWindow.isVisible()) {
      shapeMode(CORNER);
      shape(button_reset, width-20-margin, margin, 20, 20);
      line(margin+boxSize, topBorder, margin+boxSize, topBorder+tableHeight);
      line(margin+boxSize+boxSize, topBorder, margin+boxSize+boxSize, topBorder+tableHeight);
      noFill();
      rectMode(CORNER);
      rect(margin, topBorder, tableWidth, tableHeight);
      fill(0);
      textAlign(LEFT, CENTER);
      textSize(12);
      shapeMode(CENTER);
      rectMode(CENTER);
      if (!overallHide) shape(button_unhide, margin+boxSize+boxSize/2, topBorder-boxSize+boxSize/2, iconSize, iconSize);
      else shape(button_hide, margin+boxSize+boxSize/2, topBorder-boxSize+boxSize/2, iconSize, iconSize);
      shape(button_stats_alt, margin+boxSize/2, topBorder-boxSize+boxSize/2, iconSize, iconSize);
      for (int i=0; i<process.actors_nb; i++) {
        int actorIndex = process.getActorInLineAll(currentLine(topBorder+i*boxSize+boxSize/2-topBorder));
        line(margin, topBorder+i*boxSize, margin+tableWidth, topBorder+i*boxSize);
        if (actorIndex!=-1) {
          text(process.actor[actorIndex].name, margin+boxSize+boxSize+15, topBorder+i*boxSize+boxSize/2);
          fill(process.actor[actorIndex].fillColor);
          stroke(process.actor[actorIndex].strokeColor);
          rect(margin+boxSize+boxSize+7, topBorder+i*boxSize+boxSize/2, 7, 7);
          stroke(0);
          fill(0);
          if (process.actor[actorIndex].hide) {
            shape(button_hide, margin+boxSize+boxSize/2, topBorder+i*boxSize+boxSize/2, iconSize, iconSize);
          } else {
            shape(button_unhide, margin+boxSize+boxSize/2, topBorder+i*boxSize+boxSize/2, iconSize, iconSize);
          }
          shape(button_stats_alt, margin+boxSize/2, topBorder+i*boxSize+boxSize/2, iconSize, iconSize);
        }
      }
      //println(process.actor[process.getActorInLineAll(currentLine(mouseY-topBorder))].name);
    }
  }

  int currentLine(int y) {
    if (y>0 && y<tableHeight) {
      return (y/boxSize);
    } else return 0;
  }

  void mouseClicked() {
    if (mouseY>topBorder && mouseY<(topBorder+tableHeight)) {
      if (mouseX>margin && mouseX<(margin+boxSize)) {//stats
        int actorIndex = process.getActorInLineAll(currentLine(mouseY-topBorder));
        if (!statsWindowCreated[actorIndex]) {
          statsWindow[actorIndex] = new PFrameStats(actorIndex);
          //statsWindow[actorIndex].setVisible(true);
          statsWindowCreated[actorIndex] = true;
        } else {
          statsWindow[actorIndex].setVisible(!statsWindow[actorIndex].isVisible());
        }
      } else if (mouseX>(margin+boxSize) && mouseX<(margin+boxSize*2)) {//hide
        process.actor[process.getActorInLineAll(currentLine(mouseY-topBorder))].switchHide();
      }
    } else if (mouseX>margin+boxSize && mouseX<margin+boxSize*2 && mouseY>topBorder-boxSize && mouseY<topBorder) {
      if (!overallHide) for (int i=0; i<process.actors_nb; i++)  process.actor[i].setHide(true);
      else for (int i=0; i<process.actors_nb; i++)  process.actor[i].setHide(false);
      overallHide = !overallHide;
    } else if (mouseX>margin && mouseX<margin+boxSize && mouseY>topBorder-boxSize && mouseY<topBorder) {
      println("overall stats button ¿?¿?");
    } else if (mouseX>width-20 && mouseX<width && mouseY<topBorder) {
      setToRedraw = true;
    }
  }

  void keyPressed() {
    if (key==27) {
      this.key = 0;
      actorsWindow.setVisible(false);
    } else {
      handleKeys(key);
    }
  }
}


public class PFrameActors extends JFrame {
  PFrameActors() {
    a = new ActorsWindow();
    setTitle("Actors");
    add(a);
    pack ();
    setAlwaysOnTop(true);
    setDefaultCloseOperation(HIDE_ON_CLOSE);
    setBounds(0, 0, 2*3+(int)process.longestWidth+20*2+2*5+10, 25+process.actors_nb*20+50);
    setVisible(true);

    a.init();
  }
}

