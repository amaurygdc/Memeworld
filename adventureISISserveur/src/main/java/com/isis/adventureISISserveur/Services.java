package com.isis.adventureISISserveur;

import generated.World;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author cperrinc
 */
public class Services {

    static World world = new World();
    String path = "c:/temp";

    public World readWorldFromXml(String pseudo) {
        JAXBContext jaxbContext;
        InputStream input;
        try {
            try {
                File f = new File(pseudo + "world.xml");
                input = new FileInputStream(f);
            } catch (Exception e) {

                input = getClass().getClassLoader().getResourceAsStream("world.xml");
                System.out.println("pas de monde associé au joueur" + e.getMessage());
            }
            jaxbContext = JAXBContext.newInstance(World.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            world = (World) jaxbUnmarshaller.unmarshal(input);

        } catch (JAXBException ex) {
            System.out.println("Erreur lecture du fichier:" + ex.getMessage());
            ex.printStackTrace();
        }
        return world;
    }

    public void saveWorldToXml(World world, String pseudo) {
        JAXBContext jaxbContext;
        try {
            OutputStream output = new FileOutputStream(pseudo + "world.xml");
            jaxbContext = JAXBContext.newInstance(World.class);
            Marshaller march = jaxbContext.createMarshaller();
            march.marshal(world, output);
        } catch (Exception ex) {
            System.out.println("Erreur écriture du fichier:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public World getWorld(String pseudo) {
        return readWorldFromXml(pseudo);
    }

}
