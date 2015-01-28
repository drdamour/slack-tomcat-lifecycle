package net.talkshowhost.slack.tomcat;

import org.apache.catalina.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;

public class LifecycleNotifier implements LifecycleListener {

    private String channel = "general";
    private String username = "tomcat";
    private String icon = ":tomcat:";
    private String url = null;
    private String serverName = null;
    private String selfResolvedServerName;

    public LifecycleNotifier(){
        //Try to self resolve the server name...if possible
        try
        {
            InetAddress addr = InetAddress.getLocalHost();
            selfResolvedServerName = addr.getHostName();
        }
        catch (UnknownHostException ex)
        {

        }

    }

    public String getServerName() {
        //If someone set it, use it
        if(serverName != null){
            return serverName;
        }

        if(selfResolvedServerName != null && selfResolvedServerName.length() > 0){
            return selfResolvedServerName;
        }

        return "unknown server";
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }



    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setUrl(String url) {
        this.url = url;
    }




    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        //We don't care about periodic events
        if("periodic".equals(event.getType())){
            return;
        }

        Object source = event.getSource();

        if(source instanceof Context) {
            this.contextEvent(event, (Context)source);
        } else if(source instanceof Server){
            this.serverEvent(event, (Server) source);
        }
        /* right now we don't care about anything else
        else {
            System.out.println("LIFECYCLE data: " + event.getData());
            System.out.println("LIFECYCLE type: " + event.getType());
            System.out.println("LIFECYCLE source: " + event.getSource());
            System.out.println("LIFECYCLE event: " + event.toString());
            System.out.println("LIFECYCLE state name: " + event.getLifecycle().getStateName());
            System.out.println("LIFECYCLE state : " + event.getLifecycle().getState());
            System.out.println("LIFECYCLE lifecycle name: " + event.getLifecycle().toString());
        }
        */
    }

    protected void contextEvent(LifecycleEvent e, Context context){
        if(LifecycleState.STARTED.equals(e.getLifecycle().getState())){
            this.sendMessage("App Context " + context.getName() + " on " + this.getServerName() + " Started");
        }

        if(LifecycleState.STOPPED.equals(e.getLifecycle().getState())){
            this.sendMessage("App Context " + context.getName() + " on " + this.getServerName() + " Stopped");
        }

        if(LifecycleState.FAILED.equals(e.getLifecycle().getState())){
            this.sendMessage(":rage: App Context " + context.getName() + " on " + this.getServerName() + " FAILED");
        }

        if(LifecycleState.DESTROYED.equals(e.getLifecycle().getState())){
            this.sendMessage("App Context " + context.getName() + " on " + this.getServerName() + " Removed");
        }
    }

    protected void serverEvent(LifecycleEvent e, Server server){
        if(LifecycleState.STARTING.equals(e.getLifecycle().getState())){
            this.sendMessage("Tomcat Server " + this.getServerName() + " Starting");
        }

        if(LifecycleState.STARTED.equals(e.getLifecycle().getState())){
            this.sendMessage("Tomcat Server " + this.getServerName() + " Started");
        }

        if(LifecycleState.STOPPING.equals(e.getLifecycle().getState())){
            this.sendMessage("Tomcat Server " + this.getServerName() + " Stopping");
        }

        if(LifecycleState.STOPPED.equals(e.getLifecycle().getState())){
            this.sendMessage("Tomcat Server " + this.getServerName() + " Stopped");
        }
    }


    protected void sendMessage(String message){
        //This code is roughly equivalent to this
        //curl -X POST --data "{\"channel\": \"#general\", \"username\": \"tomcat\", \"text\": \"message\", \"icon_emoji\": \":tomcat:\"}" https://hooks.slack.com/services/<slugs>
        if(url == null){
            System.out.println("url for slack notification not set");
            return;
        }


        StringBuilder sb = new StringBuilder();
        sb.append("{")
          .append("\"channel\": \"#").append(this.channel).append("\"")
          .append(", \"username\": \"").append(this.username).append("\"")
          .append(", \"text\": \"").append(message).append("\"")
          .append(", \"icon_emoji\": \"").append(this.icon).append("\"")
          .append("}");


        String json = sb.toString();
        String type = "application/x-www-form-urlencoded";
        try {


            URL u = new URL(this.url);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", type);
            conn.setRequestProperty("Content-Length", String.valueOf(json.length()));
            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes());
            int x = conn.getResponseCode();

            //TODO: all of this is very nasty and not too useful.  hope things go right always ;)
            if(x != 200){
                String responseMessage = conn.getResponseMessage();

                InputStream stream = conn.getErrorStream();
                InputStreamReader isReader = new InputStreamReader(stream );
                BufferedReader br = new BufferedReader(isReader );
                System.out.println("Slack response was " + br.readLine());
            }




            os.close();
            conn.disconnect();
        } catch (Exception e){
            e.printStackTrace();
        }
        //curl -X POST --data


    }

}
