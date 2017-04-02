package fr.odysseus.utils;

import java.io.File;

public class ListageRepertoire {
  public static File[] listeRepertoire ( File repertoire ) {
    File[] list=null;
    if ( repertoire.isDirectory ( ) ) {
      list = repertoire.listFiles();
      if (list != null){
        for ( int i = 0; i < list.length; i++) {
          // Appel récursif sur les sous-répertoires
          listeRepertoire( list[i]);
        } 
      } else {
        System.err.println(repertoire + " : Erreur de lecture.");
      }
    }
    return list; 
  }
}