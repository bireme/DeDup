<%-- 
  =========================================================================

    DeDup © Pan American Health Organization, 2018.
    See License at: https://github.com/bireme/DeDup/blob/master/LICENSE.txt

  ==========================================================================

    Document   : DeDup
    Created on : 19/01/2016, 11:01:13
    Author     : Heitor Barbieri
--%>

<%@page language="java"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.*,java.net.*,java.io.*,org.json.simple.* " %>

<%!
    final static String SERVER_HOST = "dedup.bireme.org";

    String readURL(final String url) throws MalformedURLException, IOException {
        HttpURLConnection conn;
        String url1 = url;
        int times = 0;
        
        while (true) {
            if (times++ > 3) throw new IOException("Stuck in redirect loop");
            
            final URL resourceUrl = new URL(url1);
            
            conn = (HttpURLConnection) resourceUrl.openConnection();
            conn.setInstanceFollowRedirects(false);   // Make the logic below easier to detect redirections
            
            final int respCode = conn.getResponseCode();
            if ((respCode == HttpURLConnection.HTTP_MOVED_PERM) ||
                (respCode == HttpURLConnection.HTTP_MOVED_TEMP)) {
                    final String location = URLDecoder.decode(
                                      conn.getHeaderField("Location"), "UTF-8");                    
                    final URL next = new URL(resourceUrl, location);  // Deal with relative URLs                    
                    url1 = next.toExternalForm();
            } else if (respCode == HttpURLConnection.HTTP_OK) break;
            else throw new IOException("Connection ERROR:" + respCode);
        }
    
        final BufferedReader in = new BufferedReader(
            new InputStreamReader(conn.getInputStream(), "UTF-8"));
        final StringBuilder builder = new StringBuilder();

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            builder.append(inputLine);
        }
        in.close();
        conn.disconnect();

        return builder.toString();
    }
    
    String getResponseCode(final String url) throws MalformedURLException, IOException {
        HttpURLConnection conn;
        final URL resourceUrl = new URL(url);
            
        conn = (HttpURLConnection) resourceUrl.openConnection();
        conn.setInstanceFollowRedirects(false);   // Make the logic below easier to detect redirections
            
        return Integer.toString(conn.getResponseCode());
    }

    Set<String> getDatabases(final HttpServletRequest request) {
        final Set<String> databases = new TreeSet<String>();

        try {
            final String serverName = request.getServerName();
            final int serverPort = request.getServerPort();
            final String url = serverName.equals(SERVER_HOST)
                               ? "http://" + serverName + "/services/indexes"
                               : "http://" + serverName + ":" + serverPort +
                                               "/DeDup/services/indexes";
            final String json = readURL(url);
            final JSONObject obj = (JSONObject)JSONValue.parse(json);
            final JSONArray array = (JSONArray)obj.get("indexes");
            for (Object obj2: array) {
                databases.add((String)obj2);
            }
        } catch(Exception ex) {
            final String str = ex.getMessage();
            System.out.println(str);
            databases.clear();
        }
        return databases;
    }
%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>DeDup</title>
    </head>
    <body style="background-color:#f7faff">
        <h1>DeDup Application</h1>

        <%
            final String serverName = request.getServerName();
            final int serverPort = request.getServerPort();
            final String path = serverName.equals(SERVER_HOST)
                               ? "https://" + serverName + "/services/indexes"
                               : "http://" + serverName + ":" + serverPort +
                                               "/DeDup/services/indexes";
        %>  
            Base de dados:
            <select name="database" id="db">
                <%
                   final Set<String> databases = getDatabases(request);
                   final Iterator<String> it = databases.iterator();
                   String dbase = request.getParameter("database");
                   if (dbase == null) {
                       dbase = (it.hasNext()) ? it.next() : null;
                   }
                   for (String database : databases) { %>
                    <option value="<%=database%>"
                    <% if (dbase.equals(database)) { %>
                        selected
                    <% } %>
                    ><%=database%></option>
                <% } %>
            </select>
            <br/>
            <br/>
            <font size="2">&ast; Usar como separador de ocorrências a sequência: &nbsp;<i>//@//</i></font><br/>
            
            <br/>
            path = <%= path%>
            <br/>
            <br/>
            
    </body>
</html>
