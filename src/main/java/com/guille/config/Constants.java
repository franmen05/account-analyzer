package com.guille.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "config")
public interface Constants {
    String uploadDir();
}
