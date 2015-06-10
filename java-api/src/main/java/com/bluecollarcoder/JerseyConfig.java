/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bluecollarcoder;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author wayne
 */
@Configuration
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        this.packages("com.bluecollarcoder.api.rest");
    }
    
}
