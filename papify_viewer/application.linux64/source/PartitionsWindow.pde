PFramePartitions partitionsWindow;
boolean partitionsWindowCreated;
PartitionsWindow p;

public class PartitionsWindow extends PApplet {
  int margin = 3;
  int iconSize = 16;
  int boxSize = 20;
  int topBorder = 26;
  int tableWidth = 150;
  int tableHeight = boxSize*process.cores_nb;
  boolean overallHide = false;

  public void setup() {
  }

  void draw() {
    background(255);
    if (partitionsWindow!=null && running && partitionsWindow.isVisible()) {
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
      for (int i=0; i<process.cores_nb; i++) {
        int partitionIndex = currentLine(topBorder+i*boxSize+boxSize/2-topBorder);
        line(margin, topBorder+i*boxSize, margin+tableWidth, topBorder+i*boxSize);
        if (partitionIndex>=0 && partitionIndex<process.cores_nb) {
          fill(0);
          text("Partition "+process.core[partitionIndex].id, margin+boxSize+boxSize+15, topBorder+i*boxSize+boxSize/2);
          stroke(0);
          fill(0);
          if (process.core[partitionIndex].hide) {
            shape(button_hide, margin+boxSize+boxSize/2, topBorder+i*boxSize+boxSize/2, iconSize, iconSize);
          } else {
            shape(button_unhide, margin+boxSize+boxSize/2, topBorder+i*boxSize+boxSize/2, iconSize, iconSize);
          }
          shape(button_stats_alt, margin+boxSize/2, topBorder+i*boxSize+boxSize/2, iconSize, iconSize);
        }
      }
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
        /*
        int partitionIndex = currentLine(mouseY-topBorder);
         if (!partitionStatsWindowCreated[actorIndex]) {
         partitionStatsWindow[partitionIndex] = new PFramePartitionStats(partitionIndex);
         partitionStatsWindowCreated[partitionIndex] = true;
         } else {
         partitionStatsWindowCreated[partitionIndex].setVisible(!partitionStatsWindow[partitionIndex].isVisible());
         }*/
      } else if (mouseX>(margin+boxSize) && mouseX<(margin+boxSize*2)) {//hide
        int partitionIndex = currentLine(mouseY-topBorder);
        if (!process.core[partitionIndex].hide) process.hideCore(partitionIndex);
        else process.unHideCore(partitionIndex);
      }
    } else if (mouseX>margin+boxSize && mouseX<margin+boxSize*2 && mouseY>topBorder-boxSize && mouseY<topBorder) {
      if (!overallHide) for (int i=0; i<process.cores_nb; i++)  process.hideCore(i);
      else for (int i=0; i<process.cores_nb; i++)  process.unHideCore(i);
      overallHide = !overallHide;
    } else if (mouseX>margin && mouseX<margin+boxSize && mouseY>topBorder-boxSize && mouseY<topBorder) {
      println("overall stats button ¿?¿?");
      if (!running || process.actor==null || process.core==null) {
        //do NOTHING
      } else if (!partitionStatsWindowCreated) {
        partitionStatsWindow = new PFramePartitionStats();
        partitionStatsWindowCreated = true;
      } else partitionStatsWindow.setVisible(!partitionStatsWindow.isVisible());
    } else if (mouseX>width-20 && mouseX<width && mouseY<topBorder) {
      setToRedraw = true;
    }
  }

  void keyPressed() {
    if (key==27) {
      this.key = 0;
      partitionsWindow.setVisible(false);
    } else {
      handleKeys(key);
    }
  }
}


public class PFramePartitions extends JFrame {
  PFramePartitions() {
    p = new PartitionsWindow();
    setTitle("Partitions");
    add(p);
    pack ();
    setAlwaysOnTop(true);
    setDefaultCloseOperation(HIDE_ON_CLOSE);
    setBounds(0, 0, 2*3+150, 25+process.cores_nb*20+50);
    setVisible(true);

    p.init();
  }
}

