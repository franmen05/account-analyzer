package com.guille;

import io.quarkus.runtime.Startup;

import javax.inject.Singleton;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Startup
@Singleton
public class WebBrowserBean {

    WebBrowserBean() {
        //open();
    }

//    public void open()  {
//        var url = "http://localhost:8080/index.html";
//        try {
//            if(Desktop.isDesktopSupported())
//                Desktop.getDesktop().browse(new URI(url));
//            else
//                Runtime.getRuntime().exec("xdg-open " + url);
//
//        } catch (IOException | URISyntaxException e) {
//            e.printStackTrace();
//        }
//    }
}
