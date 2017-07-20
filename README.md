# manik-hot-deploy

Maven Incremental hot deploy - a plugin for the Eclipse IDE


There are different ways how to deploy a Java EE application into an application server 
like Glassfish or JBoss. You can deploy your application using a command line tool or a 
web interface provided by your sever. Or you can use the autodeploy feature which means 
that you simply copy your application into a specific folder of your server.

The Eclipse IDE provides a plugin called "Web Tools Plattform" (WTP) which supports the 
hot deployment functionality for some application and web servers. But if you are using 
the Maven build tool the Ecipse WTP feature did not integrate smoothly with the maven 
project structure. So in most cases the hot deployment feature will get lost and the 
development will become time intensive and frustrating. This is the moment 
where manik-hot-deploy comes into play. manik-hot-deploy is an Eclipse plugin which supports 
hotdeployment functionality for your maven java enterprise project. It is easy to configure 
and speed-up the development of web and enterprise applications.



## Autodeployment vs. Hotdeployment (Incremental Deployment)

Most application servers like GlassFish or JBoss/WildFly are supporting two different modes of 
automatic deployment. The autodeployment and the hotdeplyoment (also called incremental 
deployment).

### Autodeployment

Autodeployment means that you simply copy a web- (.war) or enterprise application (.ear) 
into a specific directory of our application server. When you copy an application artifact 
into that directory the application server will automatically detect the 
new artefact and starts a deployment process. If the application was already deployed before 
a redeployment will be started.

### Hot-Deployment

In different to autodeplyoment the hotdeplyoment (or incremental deployment) will not deploy 
the whole application but parts of your application which are updated or changed during 
development. This is a powerful feature which can save you a lot of time during development. 
Incremental deployment means, that in the moment when you are changing a web resource 
(like a .xhtml, .jsf or .css files) this change is immediately transferred into your deployed 
and running application. So there is no need to build and redeploy the application.


# How to use
[See the Wiki Page for more information](https://github.com/rsoika/manik-hot-deploy/wiki)

