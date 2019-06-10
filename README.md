# accelarator-jakarta-ee
Build a bare-bone Jakarta EE app that can run on a Java EE 8 / Jakarta EE 8 compatible application server using Java8+.

# Maven
The pom.xml is the root from where we will build the project and the application. It's base is provided at /pom.xml and contains the bare minimum of the project to be recognized by Maven.

Create the following Maven root directories in this project to place the sources for the application:
* src/main/java
* src/main/webapp

# Java Server Pages and Servlets
To generate HTML pages with Jakarta EE, you will be using the JSP (Java Server Pages) specification. JSP's are used to generate HTML from a file that can be structured according to the JSP specification. Next to plain-old HTML, JSP supports dynamic generation of HTML by using EL (Expression Language) and Servlets (another specification, outside the scope of this workshop). To configure this, you will need to follow these steps:

## Deployment Descriptor
In order for your application to serve JSP's, you will first need to create a deployment descriptor in the form of a ``web.xml`` file. The application server will find this file and parse it as configured.

* In the src/main/webapp folder, create an additional folder: WEB-INF
* In the WEB-INF folder, create a file 'web.xml' with the following contents:

```xml
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
	version="3.1">
	<jsp-config>
		<jsp-property-group>
			<url-pattern>*.jsp</url-pattern>
			<page-encoding>UTF-8</page-encoding>
			<scripting-invalid>true</scripting-invalid>
		</jsp-property-group>
	</jsp-config>
</web-app>
```

The web.xml describes how your JSP's are to be handled by the application server. We define here that all *.jsp files are to be UTF-8 encoded and that scripting is not allowed (you'll thank me later). There is a lot more to this file, but this is all we need for now.

## HTML pages with JSP

Every JSP is contained in it's own file, ending with ``.jsp``, and will be available under that name in a URL.

* In the src/main/webapp folder, create a new file 'index.jsp' with the following contents:

```jsp
<!DOCTYPE html>
<html>
	<head>
		<title>Ordina Accelarator - Jakarta EE</title>
	</head>
	<body>
		<h1>Hello Ordina Accelarators!</h1>
	</body>
</html>
```

This page will now be available under the root of the application (/index.jsp) and show the provided HTML. Since index.jsp is a default welcome file for the application server, try ``http://localhost:8080/hello-cafe`` to access it.

# RESTful endpoints with JAX-RS

RESTful services don't use template engines to generate content. The format and structure of the generated contents is defined by the endpoints themselves, possibly modified by parameters sent by the client. JAX-RS is the specification of Jakarta EE that allows the exposure of RESTful endpoints in the application's URL's. To set-up using JAX-RS, do the following:

## Application Path
It is necessary to define the root path of where your RESTful endpoints will be available in this application. Therefore you need to create an ``ApplicationPath``:
* Create new sub folders in the src/main/java folder: nl/ordina/accelarator/jakartaee/rest
* In the rest folder, create a new file 'RestApplication.java' with the contents:

```java
package nl.ordina.accelarator.jakartaee.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/rest")
public class RestApplication extends Application {

}
```

This file marks the root of the RESTful endpoints, that will be exposed from /rest and further.

## RESTful endpoints
You can simply annotate Java classes with JAX-RS annotations that will be picked up by the application server.

* To create an endpoint to call, in the same rest folder, create a new file 'RestEndpoints.java' with the content:

```java
package nl.ordina.accelarator.jakartaee.rest;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import nl.ordina.accelarator.jakartaee.domain.DrinkAssortmentProvider;

@Path("/hello-cafe")
public class RestEndpoint

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String sayHello() {
		return "Welcome to our awesome cafe!";
	}
}
```

When you now build and deploy your application on an application server, your first RESTful endpoint should be available under ``http://localhost:8080/hello-cafe/rest/hello-cafe``

# Dependency Injection

It is undesired to place your business logic inside the RESTful endpoints. Therefore you can contain this logic inside objects that are specifically built for that purpose: enterprise beans. In Jakarta EE, these come in the forms of EJB and CDI. We will use one of the available CDI beans to contain some very basic business logic and access it in our RESTful endpoint.

## CDI Application Scoped Bean
The CDI bean that can be deployed on application scope will be made accessible everywhere that it get's injected. Make sure not to store session-specific state in it though, since it can be shared by multiple clients calling the business logic. For that purpose, another bean exists but that is outside the scope of this workshop.

* To create a bean that contains the business logic, create a folder called 'domain' in the existing nl/ordina/accelarator/jakartaee/ folder
* Create a new file in the domain folder called 'DrinkAssortmentProvider.java' with the content:

```java
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
```

This bean mimics a database that returns a list of available drinks in the virtual cafe's assortment. This bean is now available to be injected in other places due to the ``@ApplicationScoped`` annotation that will be handled by CDI on a running application server.

## Inject the bean into a RESTful service
If we want to use the DrinkAssortmentProvider bean in our RESTful service, open the RestEndpoints.java file and add the following code snippets:

* Before the existing @GET, place the snippet to inject the DrinkAssortmentProvider:

```java
@Inject
private DrinkAssortmentProvider drinkAssortmentProvider;
```

The running application server will now ensure that there will ALWAYS be a DrinkAssortmentProvider for you to call. Neither should you encounter a NullPointerException here, nor should you ever use a constructor to instantiate a DrinkAssortmentProvider yourself.

## Call the business logic
In order to access the business logic and show the assortment to the client, we need to make this busines logic available in a URL that is exposed in our RESTful service.

* Add the following code snippet in RestEndpoints.java to call the ``drinkAssortmentProvider.getDrinksAssortment()`` business method when invoking the ``/assortment`` URL.

```java
@GET
@Path("/assortment")
@Produces(MediaType.APPLICATION_JSON)
public List<String> getDrinkAssortment() {
	return drinkAssortmentProvider.getDrinksAssortment();
}
```

You will now have created your first RESTful service that calls a business method behind an API that is exposed to your clients. This URL should be available under ``http://localhost:8080/hello-cafe/rest/hello-cafe/assortment``.