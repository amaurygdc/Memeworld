package com.isis.adventureISISserveur;

import generated.PallierType;
import generated.ProductType;
import generated.TyperatioType;
import static generated.TyperatioType.ANGE;
import generated.World;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.tomcat.util.http.fileupload.FileUtils;

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
    InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
    
    

public static World readWorldFromXml(String username){
        try {
            
            // On va vérifier que le monde existe
            File f = new File(username+"world.xml");
            if(f.exists()){
                JAXBContext cont = JAXBContext.newInstance(World.class);
                Unmarshaller u =cont.createUnmarshaller();
                World world =(World) u.unmarshal(f);
                System.out.print("fefe");
                
                return world;
            }
            else {
                InputStream input = FileUtils.class.getClassLoader().getResourceAsStream("world.xml");
                JAXBContext cont = JAXBContext.newInstance(World.class);
                Unmarshaller u = cont.createUnmarshaller();
                World world = (World) u.unmarshal(input);
                
                return world;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    
    public void saveWorldToXml(World world, String username) throws FileNotFoundException, JAXBException, IOException{
        
        OutputStream output = new FileOutputStream(username+"world.xml");
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Marshaller m = cont.createMarshaller();
        
        m.marshal(world, output);
        
        output.close();
    }
    
    public World getWorld(String username) throws JAXBException, FileNotFoundException, IOException{
        
        World world =  readWorldFromXml(username);
          
        saveWorldToXml(world, username);
            return world;
    }
    
    public void deleteWorld(String username)throws JAXBException, FileNotFoundException, IOException{
        World monde =  readWorldFromXml(username);
        double angeActif=monde.getActiveangels();
        double angeTotal=monde.getTotalangels();
        double aRajouter=nombreAnges(monde);
        angeActif+= angeActif + aRajouter ;
        angeTotal+= angeTotal + aRajouter;
        double score=monde.getScore();
        
        
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Unmarshaller u = cont.createUnmarshaller();
        World world = (World) u.unmarshal(input);
        world.setActiveangels(angeActif);
        world.setTotalangels(angeTotal);
        world.setScore(score);
        saveWorldToXml(world, username);
        
        
    }
    
    public double nombreAnges(World world)throws JAXBException, FileNotFoundException, IOException{
        double nombreAnges=world.getTotalangels();
        double angeToClaim=Math.round(150*Math.sqrt((world.getScore())/Math.pow(10,15)))-nombreAnges;
        return angeToClaim;
        
    }
    
    public void updateMonde(World world){
        Long derniereMaj= world.getLastupdate();
        Long maintenant = System.currentTimeMillis();
        Long delta= maintenant-derniereMaj;
        int angeBonus=world.getAngelbonus();
        List<ProductType> pt = (List<ProductType>) world.getProducts();
        for (ProductType a :pt){
            if (a.isManagerUnlocked()){
                int tempsProduit=a.getVitesse();
                int nbrePd= (int) (delta/tempsProduit);
                long restant = a.getVitesse()-delta%tempsProduit;
                double argent =a.getRevenu()*nbrePd*(1+world.getActiveangels()*angeBonus/100);
                world.setMoney(world.getMoney()+argent);
                world.setScore(world.getScore()+argent);
                a.setTimeleft(restant);
            }
            else {
                if (a.getTimeleft()!=0 && a.getTimeleft()<delta){
                    double argent =a.getRevenu();
                    world.setMoney(world.getMoney()+argent);
                    world.setScore(world.getScore()+argent);
                    
                }
                a.setTimeleft(a.getTimeleft()-delta);
            }
        }
        world.setLastupdate(System.currentTimeMillis()); 
    }
    
    public ProductType findProductByID(World world, int id ){
        ProductType pt = null;
        for (ProductType a :world.getProducts().product){
            if (id == a.id){
                pt=a;
            }
        }
        return pt;
    }
    
    public PallierType findManagerByName(World world, String name){
        PallierType manager=null;
        for (PallierType a: world.getManagers().pallier){
            if(name.equals(a.getName())){
                manager=a;
            }
        }
        return manager;
    }
    
    public Boolean updateProduct(String username, ProductType newproduct) throws JAXBException, IOException{
        World world = getWorld(username);
        ProductType product = findProductByID(world, newproduct.getId());
        if (product == null){
            return false;
        }
        
        int qtchange =newproduct.getQuantite() - product.getQuantite();
        if (qtchange>0){
            double argent =world.getMoney();
            double q =product.getCroissance();
            //double prix= newproduct.cout*qtchange;
            double prix1 = product.getCout();
            double prix2=prix1*((1-(Math.pow(q, qtchange)))/(1-q));
            double argentRestant = argent- prix2;
            world.setMoney(argentRestant);
            product.setQuantite(newproduct.getQuantite());
            
            
        }
        else {
            product.timeleft=product.vitesse;
            
        }
        List<PallierType> t= (List<PallierType>) product.getPalliers().getPallier();
        for (PallierType a : t){
            if (a.isUnlocked()==false && product.getQuantite()>=a.getSeuil()){
                a.setUnlocked(true);
                if (a.getTyperatio()==TyperatioType.VITESSE){
                    int b=product.getVitesse();
                    b=(int) (b*a.getRatio());
                    product.setVitesse(b);
                }
                else {
                    double c=product.getRevenu();
                    c=c*a.getRatio();
                    product.setRevenu(c);
                }
            }
        }
        
        saveWorldToXml(world, username);
        return true;
    }
    
    public boolean updateUpgrades(String username,PallierType upgrade)throws JAXBException, IOException {
         World world = getWorld(username);
         if(world.getMoney()>=upgrade.getSeuil()){
             if(upgrade.getIdcible()==0){
                 List<ProductType> listeProduits =world.getProducts().getProduct();
                 for (ProductType p : listeProduits){
                     majPallier(upgrade,p);
                 }
                 return true;
             }
             else {
                 ProductType p = findProductByID(world, upgrade.getIdcible());
                 majPallier(upgrade, p);
                 return true;
             }
            
             
         }
          return false;
    }
    public void majPallier(PallierType pt, ProductType p ){
        pt.setUnlocked(true);
        if(pt.getTyperatio()== TyperatioType.VITESSE){
            double v = p.getVitesse();
            v=(int) (v*pt.getRatio());
            p.setVitesse((int) v);
            
            
        }
        if (pt.getTyperatio()==TyperatioType.GAIN) {
            
            double c=p.getRevenu();
                    c=c*pt.getRatio();
                    p.setRevenu(c);
            
        }
    }
    public boolean updateManager(String username,PallierType newmanager) throws JAXBException, IOException{
        World world =getWorld(username);
        PallierType manager = findManagerByName(world, newmanager.getName());
        
        if (manager == null){
            return false;
        }
        manager.setUnlocked(true);
        
        ProductType product = findProductByID(world, manager.getIdcible());
        if (product==null){
            return false;
        }
       
        product.setManagerUnlocked(true);
        
        double argent = world.getMoney();
        double prix = manager.getSeuil();
        double cout = argent-prix;
        world.setMoney(cout);
        
        
        saveWorldToXml(world, username);
        return true;
        
    }
    
   public void angelUpgrade(String username, PallierType ange) throws JAXBException, IOException{
       int a=ange.getSeuil();
       World world =getWorld(username);
       double angeActif=world.getActiveangels();
       double newAngeActif=angeActif-a;
       if(ange.getTyperatio()==TyperatioType.ANGE){
           int angeBonus=world.getAngelbonus();
           angeBonus+=angeBonus+ange.getRatio();
           world.setAngelbonus(angeBonus);
           //demander
       }
       else {
           updateUpgrades(username,ange);
       }
       world.setActiveangels(newAngeActif);
   }
    
    
    
}