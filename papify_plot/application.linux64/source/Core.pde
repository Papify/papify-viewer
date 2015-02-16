class Core {
  int id;
  int actors_nb;
  String actors[];//contains the actor names in this core
  boolean hide = false;

  Core(int id, XML[] actors) {
    this.id = id;
    actors_nb = actors.length;
    this.actors = new String[actors_nb];
    for (int i = 0; i < actors_nb; i++) {
      this.actors[i] = actors[i].getString("id");
    }
  }
  
  int getId(){
   return id; 
  }

}

