# manik-hot-deploy

Manik-Hot-Deploy is a Maven Plugin which brings hot-deploy to the development of web applications. It supports Glassfish, Payara, JBoss and Wildfly application servers. 

There are different ways how to deploy a Web Application into an application server. You can deploy your application using a command line tool or a web interface provided by your sever. This is called Cold-Deployment. But this takes time as you need to redeploy your application each time you have done a change. You can automate this by using the auto-deploy and hot-deploy features supported by most application servers. This means, changes of the artifact or web content will automatically be deployed to your server in the background.

Using the Maven Plugin Manik-Hot-Deploy allows you to setup hot-deploy for most application servers in a easy way. 

See the [project home](https://manik.imixs.org/) for more information. 

## Autodeployment vs. Hotdeployment (Incremental Deployment)

Most application servers like GlassFish or JBoss/WildFly are supporting two different modes of 
automatic deployment. The auto-deployment and the hot-deplyoment (also called incremental 
deployment).

### Auto-Deployment

Auto-Deployment means that you simply copy a web- (.war) or enterprise application (.ear) 
into a specific directory of your application server.The application server will automatically detect the 
new artefact and starts a deployment process. If the application was already deployed before 
a redeployment will be started.

### Hot-Deployment

In different to auto-deplyoment a hot-deplyoment (or incremental deployment) deploys parts of your application which are updated or changed during development. This is a powerful feature which can save you a lot of time during development. 
Incremental deployment means, that in the moment when you are changing a web resource (like a .xhtml, .jsf or .css files) this change is immediately transferred into your deployed and running application. So there is no need to build and redeploy the application.


# How to use

To add the Manik-Hotdeploy Plugin to your maven project you just need to add plugin section into your pom.xml:

```xml
	....
	<build>
		<plugins>
		  .....
			<!-- Manik Hotdploy -->
			<plugin>
				<groupId>org.imixs.maven</groupId>
				<artifactId>manik-hotdeploy-maven-plugin</artifactId>
				<version>2.0.0-SNAPSHOT</version>
				<executions>
				  <execution>
				    <phase>install</phase>
				    <goals>
				      <goal>deploy</goal>
				    </goals>
				  </execution>
            	</executions>
				<configuration>
					<!-- List Source and Target folders for Autodeploy and Hotdeploy -->
					<autodeployments>
						<deployment>
							<!-- wildcard deployment -->
							<source>target/*.{war,ear,jar}</source>
							<target>docker/deployments/</target>
							<unpack>true</unpack>						
						</deployment>
					</autodeployments>
					<hotdeployments>
						<deployment>
							<source>src/main/webapp</source>
							<target>docker/deployments/my-app.war</target>
						</deployment>						
					</hotdeployments>
				</configuration>
			</plugin>
			.....
		</plugins>
	</build>
	....
```



In this example the Autodepolyment is configured for your application named "my app.war" into the target folder `docker/deployments/`
This means, if you run the maven standard goal `install` on your web project, the Manik-Hotdeploy-Plugin will deploy the resulting web artifact my-app.war into your application servers autodeploy location specified in the plugin configuration.

In addition a hotdeployment is configured for all source code files located under `/src/main/webapp`

To start the hotdeployment mode jus run:

	$ mvn manik-hotdeploy:hotdeploy

This will start a service automatically watching your `/src/main/webapp/` folder for any changes. So if you change a HTML, XHTML, JavaScritp, CSS or JSF file, the manik-hotdeploy feature will push your changes immediately into the application server on the specified location. 

[See the Projekt Home for more information](https://manik.imixs.org/)



# Development

## Testing the Plugin

	$ mvn clean install

## Executing Our Plugin

	$ mvn manik-hotdeploy:hotdeploy

To run a specific version run:

	$ mvn org.imixs.maven:manik-hotdeploy-maven-plugin:2.0.0-SNAPSHOT:hotdeploy
	


Background:

For more background how to develop a plugin read:

 - https://maven.apache.org/plugin-developers/index.html
 - https://www.baeldung.com/maven-plugin
 - https://github.com/fizzed/maven-plugins/blob/master/watcher/src/main/java/com/fizzed/maven/watcher/RunMojo.java
	
