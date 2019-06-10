package nl.ordina.accelarator.jakartaee.rest;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import nl.ordina.accelarator.jakartaee.domain.DrinkAssortmentProvider;

@Path("/hello-cafe")
public class RestEndpoints {

	@Inject
	private DrinkAssortmentProvider drinkAssortmentProvider;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String sayHello() {
		return "Welcome to our awesome cafe!";
	}
	
	@GET
	@Path("/assortment")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getDrinkAssortment() {
		return drinkAssortmentProvider.getDrinksAssortment();
	}
}