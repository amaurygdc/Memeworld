package com.isis.adventureISISserveur;

import generated.PallierType;
import generated.ProductType;
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

    // prend en paramètre le pseudo du joueur et le produit
    // sur lequel une action a eu lieu (lancement manuel de production ou // achat d’une certaine quantité de produit)
    // renvoie false si l’action n’a pas pu être traitée  
    public Boolean updateProduct(String username, ProductType newproduct) {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le produit équivalent à celui passé
        // en paramètre
        ProductType product = findProductById(world, newproduct.getId());
        if (product == null) {
            return false;
        }
        // calculer la variation de quantité. Si elle est positive c'est
        // que le joueur a acheté une certaine quantité de ce produit
        // sinon c’est qu’il s’agit d’un lancement de production.
        int qtchange = newproduct.getQuantite() - product.getQuantite();
        if (qtchange > 0) {
            // soustraire de l'argent du joueur le cout de la quantité
            double cout = (product.getCout() * (1 - Math.pow(product.getCroissance(), product.getQuantite()))) / (1 - product.getCroissance());
            world.setMoney(world.getMoney() - cout);
            // achetée et mettre à jour la quantité de product
            product.setQuantite(product.getQuantite() + newproduct.getQuantite());
        } else {
            // initialiser product.timeleft à product.vitesse
            product.setTimeleft(product.getVitesse());
            product.setQuantite(newproduct.getQuantite());
            // pour lancer la production
        }
        // sauvegarder les changements du monde
        saveWorldToXml(world, username);
        return true;
    }

    // prend en paramètre le pseudo du joueur et le manager acheté.
    // renvoie false si l’action n’a pas pu être traitée  
    public Boolean updateManager(String username, PallierType newmanager) {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le manager équivalent à celui passé
        // en paramètre
        PallierType manager = findManagerByName(world, newmanager.getName());
        if (manager == null) {
            return false;
        }

        // débloquer ce manager 
        // trouver le produit correspondant au manager
        ProductType product = findProductById(world, manager.getIdcible());
        if (product == null) {
            return false;
        }
        // débloquer le manager de ce produit
        manager.setUnlocked(true);
        product.setManagerUnlocked(true);
        // soustraire de l'argent du joueur le cout du manager
        world.setMoney(world.getMoney() - manager.getSeuil());
        // sauvegarder les changements au monde

        saveWorldToXml(world, username);
        return true;
    }

    private ProductType findProductById(World world, int id) {
        ProductType pt = null;
        for (ProductType p : world.getProducts().getProduct()) {
            if (p.getId() == id) {
                pt = p;
            }
        }
        return pt;
    }

    private PallierType findManagerByName(World world, String name) {
        PallierType palt = null;
        for (PallierType pal : world.getManagers().getPallier()) {
            if (pal.getName().equals(name)) {
                palt = pal;
            }
        }
        return palt;
    }

}
