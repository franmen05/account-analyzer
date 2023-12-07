package com.guille;

import io.quarkus.runtime.Startup;
import jakarta.inject.Singleton;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
// import java.awt.*;
// import java.io.IOException;
// import java.net.URI;
// import java.net.URISyntaxException;

@Startup
@Singleton
public class WebBrowserBean {
//    @ConfigProperty(name = "quarkus.http.port", defaultValue = "8082")
    String assignedPort="8082";

    WebBrowserBean() {
        open();
    }

    public void open()  {
        var url = "http://localhost:"+assignedPort+"/index.html";

        try {
            if(Desktop.isDesktopSupported())
                Desktop.getDesktop().browse(new URI(url));
//            else
//                Runtime.getRuntime().exec("xdg-open " + url);

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
