# manik-hot-deploy

Manik-Hot-Deploy is a plugin for the Eclipse IDE which brings hot-deploy to the development of web applications. It supports Glassfish, Payara, JBoss and Wildfly application servers. 

There are different ways how to deploy a Web Application into an application server. You can deploy your application using a command line tool or a web interface provided by your sever. This is called Cold-Deployment. But this takes time as you need to redeploy your application each time you have done a change. You can automate this by using the auto-deploy and hot-deploy features supported by most application servers. This means, changes of the artefact or web content will automatically be deployed to your server in the background.

If you are using the Eclipse IDE and Maven, Manik-Hot-Deploy allows you to setup hot-deploy for most application servers in a easy way. 

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
[See the Projekt Home for more information](https://manik.imixs.org/)



# Development

Background:

 - https://maven.apache.org/plugin-developers/index.html
 - https://www.baeldung.com/maven-plugin
 - https://github.com/fizzed/maven-plugins/blob/master/watcher/src/main/java/com/fizzed/maven/watcher/RunMojo.java

## Testing the Plugin

	$ mvn clean install

## Executing Our Plugin

	$ mvn org.imixs:hotdeploy-plugin:0.0.1-SNAPSHOT:dependency-counter
	$ mvn org.imixs:hotdeploy-plugin:0.0.1-SNAPSHOT:hotdeploy