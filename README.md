详细说明见我的博客  
[http://wengyingjian.github.io/blog/2015/12/17/java-deploy](http://wengyingjian.github.io/blog/2015/12/17/java-deploy)




~/.base_profile:

	#alias for deploy
	alias deploy='java -jar ~/deploy/deploy.jar'
	alias deploy-log='java -jar -Dmode=log ~/deploy/deploy.jar'
	alias deploy-shell='java -jar -Dmode=shell  ~/deploy/deploy.jar'
	alias deploy-manual='java -jar -Dmode=manual ~/deploy/deploy.jar'
	
	
cmds:

	deploy a.properties 
	deploy-shell a.properties
	deploy-log a.properties
	deploy-manual a.properties old/new
	


properties file:

	host=192.168.40.43
	user=root
	password=admin
	sourceWarFile=/Users/wyj/Documents/g_new/demo-api/target/demo-api-0.0.1-SNAPSHOT.war
	catalinaHome=/usr/local/apache-tomcat-8.0.29
	loggerFile=/usr/local/apache-tomcat-8.0.29/logs/catalina.out
