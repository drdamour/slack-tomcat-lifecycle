package net.talkshowhost.slack.tomcat;

import org.apache.catalina.*;
import org.junit.Test;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LifecycleNotifierIntegration {

    @Test
    public void testSendMessage(){
        //System.setProperty("http.proxyHost", "localhost");
        //System.setProperty("http.proxyPort", "8888");

        LifecycleNotifier notifier = new LifecycleNotifier();
        notifier.sendMessage("a test message");
    }

}
