package com.zyp.ssl

import org.apache.catalina.connector.Connector
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.servlet.server.ServletWebServerFactory
import org.springframework.context.annotation.Bean

@SpringBootApplication
class SpringbootSslSampleApplication {
//    @Bean
//    fun servletContainer(): ServletWebServerFactory {
//        val tomcat = TomcatServletWebServerFactory()
//        tomcat.addAdditionalTomcatConnectors(createStandardConnector())
//        return tomcat
//    }
//
//    private fun createStandardConnector(): Connector {
//        val connector = Connector("org.apache.coyote.http11.Http11NioProtocol")
//        connector.port = 8089
//        return connector
//    }
}

fun main(args: Array<String>) {
    runApplication<SpringbootSslSampleApplication>(*args)
}
