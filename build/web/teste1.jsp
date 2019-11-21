<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.*,java.net.*,java.io.*" %>

<%!
    String getResponseCode(final String url) throws MalformedURLException, IOException {
        HttpURLConnection conn;
        final URL resourceUrl = new URL(url);
            
        conn = (HttpURLConnection) resourceUrl.openConnection();
        conn.setInstanceFollowRedirects(false);   // Make the logic below easier to detect redirections
            
        return Integer.toString(conn.getResponseCode());
    }
%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>SSL Test Page</title>
    </head>
    <body>
        <h1>http://dedup.bireme.org/services/indexes</h1>
                
        <p>http://dedup.bireme.org/services/indexes <%=getResponseCode("http://dedup.bireme.org/services/indexes")%></p>
        
    </body>
</html>
