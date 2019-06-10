package nl.ordina.accelarator.jakartaee.domain;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DrinkAssortmentProvider {

	private List<String> drinks;
	
	@PostConstruct
	public void init() {
		drinks = new ArrayList<>();
		drinks.add("Fanta");
		drinks.add("Cola");
		drinks.add("Beer");
	}
	
	public List<String> getDrinksAssortment() {
		return Collections.unmodifiableList(drinks);
	}
}