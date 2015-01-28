# slack-tomcat-lifecycle
A way to have tomcat lifecycle events broadcast to a slack chat

# Setting It Up
You'll need to put the JAR in your tomcat/catalina class path. For 7 this is /lib
Then you'll need to add a listener for server and/or context.  I add the following for both conf/server.xml and conf/context.xml
```
<Listener className="net.talkshowhost.slack.tomcat.LifecycleNotifier"
    channel="some-channel"
    url="https://hooks.slack.com/services/<slug from incoming webhooks>"
  />
```
Other settings include
* username - The name of the user the message should be sent by.  Defaults to tomcat
* icon - The emoji to use as the avatar.  Defaults to :tomcat:
* serverName - Overrides the name of the server (Defaults to using self DNS lookup).
