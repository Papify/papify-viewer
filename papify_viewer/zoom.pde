void zoom() {
  if (screen.isInScreen(mouseX, mouseY)) {
    if (set_zoom_ini) {
      stroke(204, 102, 0);
      line(screen.pos_x+screen.getPixel(screen.getX_actual(mouseX-left_border)), top_border, screen.pos_x+screen.getPixel(screen.getX_actual(mouseX-left_border)), height-bottom_border);
    }
    if (set_zoom_end) {
      fill(0, 0, 0, 20);
      stroke(204, 102, 0);
      rect(screen.pos_x+screen.getPixel(screen.zoomIni), top_border, screen.getPixel(screen.getX_actual(mouseX-left_border))-screen.getPixel(screen.zoomIni), screen.screen_height);
    }
  } else if (set_zoom_ini) {
    stroke(204, 102, 0);
    line(screen.pos_x+screen.getPixel(screen.getX_actual(last_known_pointer-left_border)), top_border, screen.pos_x+screen.getPixel(screen.getX_actual(last_known_pointer-left_border)), height-bottom_border);
  } else if (set_zoom_end) {
    fill(0, 0, 0, 20);
    stroke(204, 102, 0);
    rect(screen.pos_x+screen.getPixel(screen.zoomIni), top_border, screen.getPixel(screen.getX_actual(last_known_pointer-left_border))-screen.getPixel(screen.zoomIni), screen.screen_height);
  }
}

