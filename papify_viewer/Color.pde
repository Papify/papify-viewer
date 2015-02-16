class Color {
  int index = 0;

  int[] colors = {    
    color(120, 0, 0), //rojo
    color(200, 0, 0), 

    color(0, 120, 0), //verde
    color(0, 200, 0), 

    color(0, 0, 120), //azul
    color(0, 0, 200), 

    color(120, 120, 0), //amarillo
    color(200, 200, 0), 

    color(120, 0, 120), //fuxia
    color(200, 0, 200), 

    color(0, 120, 120), //cyan
    color(0, 200, 200), 

    color(80, 80, 80), //gris
    color(140, 140, 140), 

    color(136, 47, 192), 
    color(112, 48, 152), 

    color(255, 145, 54), 
    color(255, 116, 0), 

    color(60, 94, 249), 
    color(48, 75, 200), 

    color(255, 228, 35), 
    color(180, 164, 54), 

    color(33, 248, 240), 
    color(25, 187, 181), 

    color(34, 249, 190), 
    color(30, 156, 121), 

    color(228, 0, 200), 
    color(102, 6, 90), 

    color(74, 1, 241), 
    color(62, 4, 196),
  };

  int getNext() {
    if (index==colors.length) index=0;
    return colors[index++];
  }

  int get(int i) {
    if (i<colors.length) {
      return colors[i];
    } else return 0;
  }
}

