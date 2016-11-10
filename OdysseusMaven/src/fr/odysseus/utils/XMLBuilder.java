package fr.odysseus.utils;
import java.io.FileWriter;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;


public class XMLBuilder {
	static void affiche(Document document)
	{
	   try
	   {
	      XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
	      sortie.output(document, System.out);
	   }
	   catch (java.io.IOException e){}
	}

	public void enregistre(String fichier, Document document)
	{
	   try
	   {
	      XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
	      //avec en argument le nom du fichier pour effectuer la sérialisation.
	      sortie.output(document, new FileWriter(fichier));
	   }
	   catch (java.io.IOException e){}
	}

	public static void enregistreByRoot(String fichier, Element root)
	{
	   try
	   {
		   Document document =new Document(root);
	      XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
	      //avec en argument le nom du fichier pour effectuer la sérialisation.
	      sortie.output(document, new FileWriter(fichier));
	   }
	   catch (java.io.IOException e){}
	}

}
