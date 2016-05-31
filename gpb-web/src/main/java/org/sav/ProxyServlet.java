package org.sav;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestHandler;

public class ProxyServlet implements HttpRequestHandler {
    private final static Logger logger = Logger.getLogger(ProxyServlet.class);
    private final static Priority PRIORITY = Level.INFO;
    private HttpClientBuilder builder = HttpClientBuilder.create();

    private Set<String> headersToRemove = Collections.emptySet();
    //private String url = "http://sav-studio.by";
    private String url = "http://cli.im/news/faq";

    @Override
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.log(PRIORITY, "----------------------------- Start handling -----------------------------");

        RequestContext requestContext = new RequestContext(req, resp);

        HttpUriRequest backendRequest = buildBackendRequest(requestContext);
        requestContext.setBackendRequest(backendRequest);

        processFrontendRequestHeaders(requestContext);

        HttpClient client = builder.build();
        HttpResponse backendResponse = client.execute(backendRequest);
        requestContext.setBackendResponse(backendResponse);
        logger.log(PRIORITY, "Response : " + backendResponse);

        processBackendRequestHeaders(requestContext);

        processBackendContent(requestContext);

        resp.setStatus(backendResponse.getStatusLine().getStatusCode());

        logger.log(PRIORITY, "----------------------------- Done -----------------------------");
    }

    private void processFrontendRequestHeaders(RequestContext requestContext) {
        @SuppressWarnings("unchecked")
        Enumeration<String> headersNames = requestContext.getFrontendRequest().getHeaderNames();
        while (headersNames.hasMoreElements()) {
            String headerName = headersNames.nextElement();
            String header = requestContext.getFrontendRequest().getHeader(headerName);
            if (headersToRemove.contains(headerName.toLowerCase())) {
                logger.log(PRIORITY, String.format("Skipping header : %s : %s", headerName, header));
            } else {
                logger.log(PRIORITY, String.format("Propagating header : %s : %s", headerName, header));
                requestContext.getBackendRequest().addHeader(headerName, header);
            }
        }
    }

    private void processBackendRequestHeaders(RequestContext requestContext) {
        HttpServletResponse frontendResponse = requestContext.getFrontendResponse();
        HttpResponse backendResponse = requestContext.getBackendResponse();
        
        for (Header header : backendResponse.getAllHeaders()) {
            if (headersToRemove.contains(header.getName().toLowerCase())) {
                logger.log(PRIORITY, String.format("Skipping backend header : %s ", header));
            } else {
                logger.log(PRIORITY, "Propagating backend header : '" + header.getName() + "' : '" + header.getValue() + "'");
                frontendResponse.addHeader(header.getName(), header.getValue());
            }
        }
    }

    private void processBackendContent(RequestContext requestContext) throws IOException {
        HttpServletResponse frontendResponse = requestContext.getFrontendResponse();
        HttpResponse backendResponse = requestContext.getBackendResponse();

        Header[] ct = backendResponse.getHeaders("Content-Type");

        if (null != ct && ct.length > 0 && ct[0].getValue().startsWith("text/html")) {
            HttpEntity contentEntity = backendResponse.getEntity();
            logger.log(PRIORITY, "Processing entity " + contentEntity);

            Charset charset = Charset.forName("UTF-8");
            try {
                HeaderElement[] els = ct[0].getElements();
                NameValuePair charsetHeaderEntry = els[0].getParameterByName("charset");
                charset = Charset.forName(charsetHeaderEntry.getValue());

                logger.log(PRIORITY, "Charset overridden : " + charset);
            } catch (Exception e) {
                logger.log(Level.WARN, "Got exception", e);
            }
            
            String r = requestContext.getFrontendRequest().getRequestURI();
            String p = requestContext.getFrontendRequest().getRequestURL().toString();
            String m = p.substring(0, p.length() - r.length());

            String str = StreamUtils.copyToString(contentEntity.getContent(), charset);
            String replaced = str.replaceAll(url, m);

            logger.log(Level.TRACE, "Processed content : ------------------------- \n\n" + replaced + "\n\n----------------------------------------");

            StreamUtils.copy(replaced, charset, frontendResponse.getOutputStream());
        } else {
            HttpEntity contentEntity = backendResponse.getEntity();
            if (null != contentEntity) {
                logger.log(PRIORITY, "Propagating entity : " + contentEntity);
                InputStream is = contentEntity.getContent();
                StreamUtils.copy(is, frontendResponse.getOutputStream());
            } else {
                logger.log(PRIORITY, "No entity. Skipping...");
            }
        }
    }

    private String buildUrl(RequestContext requestContext) {
        HttpServletRequest req = requestContext.getFrontendRequest();

        String pathInfo = req.getPathInfo();
        String qStr = req.getQueryString();
        String finalUrl = StringUtils.isEmpty(pathInfo) ? url : url + pathInfo;
        if (!StringUtils.isEmpty(qStr)) {
            finalUrl += "?" + qStr;
        }
        logger.log(PRIORITY, "PathInfo '" + pathInfo + "', queryString '" + qStr + "'. Result Url : '" + finalUrl + "'");
        return finalUrl;
    }

    private HttpUriRequest buildBackendRequest(RequestContext requestContext) throws IOException {
        String finalUrl = buildUrl(requestContext);
        String method = requestContext.getFrontendRequest().getMethod();

        RequestBuilder requestBuilder = RequestBuilder.create(method).setUri(finalUrl);

        InputStream feInputStream = requestContext.getFrontendRequest().getInputStream();
        requestBuilder.setEntity(new InputStreamEntity(feInputStream));

        return requestBuilder.build();        
    }

    public void setHeadersToRemove(Set<String> headersToRemove) {
        this.headersToRemove = headersToRemove;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
