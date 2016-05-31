package org.sav;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.HttpRequestHandler;

public class HelloHttpRequestHandler implements HttpRequestHandler {
    private static final Logger logger = Logger.getLogger(HelloHttpRequestHandler.class);

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("Hello servlet is invoked.");
        logger.debug("Hello servlet is invoked. DEBUG");
        logger.trace("Hello servlet is invoked. TRACE");
        response.getWriter().write("Hello World!");
    }
}
