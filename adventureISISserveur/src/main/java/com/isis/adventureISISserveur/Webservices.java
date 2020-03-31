/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.isis.adventureISISserveur;

import com.google.gson.Gson;
import generated.PallierType;
import generated.ProductType;
import generated.World;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import static javax.ws.rs.HttpMethod.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author cperrinc
 */
@Path("generic")

public class Webservices {

    Services services;

    public Webservices() {
        services = new Services();
    }

@GET
@Path("getworld")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public Response getXml(@Context HttpServletRequest request) throws JAXBException, IOException{
    String username = request.getHeader("X-user");
    return Response.ok(services.getWorld(username)).build();
}

@PUT
@Path("product")
public void putProduct(@Context HttpServletRequest request,ProductType product) throws JAXBException, IOException{
   String username = request.getHeader("X-user");
   services.updateProduct(username, product);
}

@PUT
@Path("manager")
public void putManager(@Context HttpServletRequest request,PallierType manager) throws JAXBException, IOException{
   String username = request.getHeader("X-user");
   services.updateManager(username, manager);
}
@PUT
@Path("upgrade")
public void putUpgrade(@Context HttpServletRequest request,PallierType upgrade) throws JAXBException, IOException{
    String username = request.getHeader("X-user");
    services.updateUpgrades(username, upgrade);
}
@DELETE
@Path("world")
public void deleteWorld(@Context HttpServletRequest request)throws JAXBException, IOException{
    String username = request.getHeader("X-user");
    services.deleteWorld(username);
    
}

@PUT
@Path("world")
public void putWorld(@Context HttpServletRequest request,World world) throws JAXBException, IOException{
   String username = request.getHeader("X-user");
   services.saveWorldToXml(world, username);
}

@PUT
@Path("angelupgrade")
public void angelUpgrade(@Context HttpServletRequest request,PallierType ange)throws JAXBException, IOException{
    String username = request.getHeader("X-user");
    services.angelUpgrade(username, ange);
}
}
