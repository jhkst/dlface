# ^dlFace

## What is it
**^dlFace** is helper for downloading files from various sources like **torrents**, public **file storages**, **ftps** and others.
It lays on *server side* and publishes simple *REST api* to manipulate with downloads.
There is also a simple built-in web interface which uses this *REST api*.

## Browser screenshots
![Add Download](https://cloud.githubusercontent.com/assets/7606266/22400299/33ed48e4-e5b1-11e6-92de-cb1101f5eca6.png)
![Downloads List](https://cloud.githubusercontent.com/assets/7606266/22400300/3696e03c-e5b1-11e6-9b77-fe61f5174879.png)
![Download Detail 1](https://cloud.githubusercontent.com/assets/7606266/22400301/3aee3a04-e5b1-11e6-8312-12551235944a.png)
![Download Detail 2](https://cloud.githubusercontent.com/assets/7606266/22400302/3c857e7c-e5b1-11e6-9835-a105b3dcf8e0.png)

## Build requirements
- Java 8
- Maven 2 (maybe also 3?)

## Runtime requirements
- Web container running on Java 8. Currently tested:
  - *Tomcat 8*
  - *Jetty 9*
- Internet connection (surprisingly)

## Build
To create deployable **war** do the following (where `${dlface.root}` is root of *git* clone):

```
cd ${dlface.root}
mvn clean install
cd ${dlface.root}/face
mvn war:war
```

than you can find the war in

`${dlface.root}/face/target/face-${dlface.version}.war`

## Installation/deploying

### Embedded *Jetty* maven plugin

Simply do the following
```
cd ${dlface.root}
mvn clean install
cd ${dlface.root}/face
mvn jetty:run
```
and the Jetty server will start

### *Tomcat 8*

#### Deploy from Maven
To make *Tomcat* server able to allow deploying from maven you must do following steps:

**(1)** Install tomcat admin
eg. on Ubuntu/Debian:

`sudo apt-get install tomcat8-admin`

**(2)** Add Tomcat users
Edit `tomcat-users.xml`

Add following lines:
```
  <role rolename="manager-gui"/>
  <role rolename="manager-script"/>
  <user username="admin" password="thecorepassword" roles="manager-gui,manager-script"/>
```

**(3)** Add server to Maven configuration

Edit  `~/.m2/settings.xml`
```
<settings>
  ...
  <servers>
    ...
    <server>
      <id>TomcatServer</id>
      <username>admin</username>
      <password>thecorepassword</password>
    </server>
    ...
  </servers>
  ...
</settings>
```

**(4)** Deploy to server
```
mvn install tomcat7:redeploy -Dtomcat.url=http://tomcatUrl:port/manager/text -Dtomcat.server=TomcatServer
```
Mind that `tomcat7` plugin is used also for *Tomcat 8* server

#### deploy manually
To deploy manually into *Tomcat 8* simply copy builded **war** into

`${tomcat.home}/webapps`

and the server will do the (re)deploy automatically

# Configuration
Default config folder is located in `$HOME/.dlface`, but you can override it by setting `DLFACE_CONFIG_DIR` environment property. If this folder does not exists, it's created by application if it is possible (ACL etc.).

Application then looks for `config.properties` file in this folder. If it does not exists, it's automaticaly created (check ACL) from template (with comments explaining configuration).

### Bridges configuration
Each bridge has it's own configuration located in the same folder as main configuration.

* **torrentbridge**: `torrent.properties`
* **frdbridge**: `frd.properties`

If they are not present, they are automatically created. There are no comments in these files, but the properties are almost all self-explaining.

To apply any of configuration (applies also on main configuration) you have to restart web server.



# Bridges
Bridge as the name describe is a connection between the ^dlface core services and some download implementations.

### Currently supported
* **torrentbridge** - able to take torrent file or url and start download files
     * uses third-party [ttorrent](https://github.com/mpetazzoni/ttorrent) library
* **frdbridge** - this is connection to third-party plugins of [FreeRapidDownloader](http://wordrider.net/freerapid/)
     * to run this you must run *FreeRapidDownloader* itself and let it download all supported plugins (once)
     * the same applies for plugins updates (they have rules for using their repository)
     * you can run it on different computer, then copy the `$HOME/.frd` folder into *dlface* server computer
     * support them, they do great job
* **rawbridge** - downloads file as is (usable for ftp downloads, iso images etc)

### Future plans (not yet implemented)
* **jdbridge** - connection to third-party [JDownloader](http://jdownloader.org/) (WIP)
* **jsbridge** - connection to javascript scripts for download (probably reusable by tools like [node.js](https://nodejs.org), [phantomjs](http://phantomjs.org/), [casperjs](http://casperjs.org/))

# Licensing

* frdlegacy - GNU GPL v2. // need to separate 


# Developers

### Used technologies
* [maven](https://maven.apache.org/)
* [slf4j](http://www.slf4j.org/)/[logback](http://logback.qos.ch/)
* [spring framework](https://spring.io/)
* [jersey](https://jersey.java.net/)/REST api
* [Bootstrap](http://getbootstrap.com/)
* [jQuery](https://jquery.com/)
* [DataTables](https://datatables.net/)

### Code rules
 * no redundancy (REST results etc.)
 * server side does not know anything about presentation. 
     * Built-in presetation in [Bootstrap](http://getbootstrap.com/)/JS/[jQuery](https://jquery.com/) (no JSP etc.)
 * try to reuse everything what is already written (and if the license allows it) - apache commons, datatables etc.
 * lower the amount of third party code direct inclusion in the project (sometime it's unavoidable). e.g. [webjars](http://www.webjars.org/) for js libs.
 * mind **utils** module, reuse what can be reused, add something if it's util method
  
### Logger
 The [slf4j](http://www.slf4j.org/) project is used with [Logback](http://logback.qos.ch/) implementation.

 Everything (third parties) what is possible try to redirect to this log.

 All Loggers are by class `private static final` constants. Therefore UPPERCASE.
 Example:
 ```java
    private static final Logger LOGGER = LoggerFactory.getLogger(MyClass.class);
 ```
 
### Create bridge
To create your own download bridge you have to:

* create maven module project in `bridges/${myBridge}/${myBridge}bridge`
* add other dependant modules if needed into `bridges/${myBridge}`
* add dependency on this module into main project
* add main module as parent into your module
* add dependency on `ibridge` module (this is the true bridge)
* create class `extends DefaultDownloadStatusUpdateObservable implements IBridge`
* for user actions (e.g captchas) inject `ActionHandler(Impl)`, choose one class in `dl.ibridge.action` package, create Request instance, call `actionHandler.addActionRequest()`