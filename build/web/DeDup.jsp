<%-- 
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
        final URL xurl = new URL(url);
        final BufferedReader in = new BufferedReader(
            new InputStreamReader(xurl.openStream(), "UTF-8"));
        final StringBuilder builder = new StringBuilder();
        
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            builder.append(inputLine);
        }
        in.close();
        
        return builder.toString();
    }
    
    Set<String> getSchemas(final HttpServletRequest request) {
        final Set<String> schemas = new TreeSet<String>();
        
        try {           
            final String serverName = request.getServerName();
            final int serverPort = request.getServerPort();
            final String url = serverName.equals(SERVER_HOST) 
                               ? "http://" + serverName +  "/services/schemas"
                               : "http://" + serverName + ":" + serverPort + 
                                                     "/DeDup/services/schemas";
            final String json = readURL(url);
            final JSONObject obj = (JSONObject)JSONValue.parse(json);
            final JSONArray array = (JSONArray)obj.get("schemas");
            for (Object obj2: array) {
                schemas.add((String)obj2);
            }
            
        } catch(Exception ex) {
            final String str = ex.getMessage();
            System.out.println(str);
            schemas.clear();
        }
        return schemas;
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
    
    Set<String> getSchema(final HttpServletRequest request,
                          final String schema) {
        final Set<String> xschema = new TreeSet<String>();
        
        try {           
            final String serverName = request.getServerName();
            final int serverPort = request.getServerPort();
            final String url = serverName.equals(SERVER_HOST) 
                         ? "http://" + serverName + "/services/schema/" + schema
                         : "http://" + serverName + ":" + serverPort + 
                                             "/DeDup/services/schema/" + schema;
            final String json = readURL(url);
            final JSONObject obj = (JSONObject)JSONValue.parse(json);
            final JSONArray array = (JSONArray)obj.get("params");
            for (Object obj2: array) {
                final JSONObject obj3 = (JSONObject)obj2;
                xschema.add((String)obj3.get("name"));
            }            
        } catch(Exception ex) {
            final String str = ex.getMessage();
            System.out.println(str);
            xschema.clear();
        }
        return xschema;
    }
            
%>

<script LANGUAGE="JavaScript" TYPE="text/javascript">        
function reloadPage() {
    var database = document.getElementById("db");
    var dbValue = database.options[database.selectedIndex].value;    
    var schema = document.getElementById("sch");
    var schValue = schema.options[schema.selectedIndex].value;
    var host = "";
    var hostname = document.location.hostname;
    var path0 = "";
    if (hostname === "<%=SERVER_HOST%>") {
        host = hostname;
        path0 = "/services/";
    } else {
        host = document.location.host;
        path0 = "/DeDup/services/";
    }
    var path = "http://" + host + path0 + "?database=" 
                                              + dbValue + "&schema=" + schValue;    
    window.location=path;
}

function reloadPagePost() {
    var database = document.getElementById("db");
    var dbValue = database.options[database.selectedIndex].value;    
    var schema = document.getElementById("sch");
    var schValue = schema.options[schema.selectedIndex].value;
    var host = "";
    var hostname = document.location.hostname;
    var path0 = "";
    if (hostname === "<%=SERVER_HOST%>") {
        host = hostname;
        path0 = "/services/";
    } else {
        host = document.location.host;
        path0 = "/DeDup/services/";
    }
    var path = "http://" + host + path0;
    var form = document.createElement("form");
    var hiddenField1 = document.createElement("h1");
    var hiddenField2 = document.createElement("h2");
    
    form.setAttribute("charset", "UTF-8");
    form.setAttribute("method", "post");
    //form.setAttribute("method", "get");
    form.setAttribute("action", path);
    
    hiddenField1.setAttribute("type", "hidden");
    hiddenField1.setAttribute("name", "database");
    hiddenField1.setAttribute("value", dbValue);
    
    hiddenField2.setAttribute("type", "hidden");
    hiddenField2.setAttribute("name", "schema");
    hiddenField2.setAttribute("value", schValue);

    form.appendChild(hiddenField1);
    form.appendChild(hiddenField2);
    
    document.body.appendChild(form);
    form.submit();
}

function teste() {        
    alert("Debug da funcao teste()");
}

function putPage() {
    var database = document.getElementById("db");
    var dbValue = database.options[database.selectedIndex].value;    
    var schema = document.getElementById("sch");
    var schValue = schema.options[schema.selectedIndex].value;
    var id = "only_for_test";
    var host = "";
    var port = document.location.port;
    var hostname = document.location.hostname;
    var path0 = "";
    if (hostname === "<%=SERVER_HOST%>") {
        host = hostname;
        path0 = "/services/put/";
    } else {
        host = document.location.host;
        path0 = "/DeDup/services/put/";
    }
    var path = "http://" + host + path0 + dbValue + "/" + schValue + "/" + id;   
    var json = "{\"db\":\"" + dbValue + "\",\"schema\":\"" + schValue + "\"";
    var inputs = document.getElementsByTagName("*");
    for (var i = 0; i < inputs.length; i++) {
        if (typeof inputs[i].type !== 'undefined') {
            var typeName = inputs[i].type.toLowerCase();
            if ((typeName === 'text') && (inputs[i].name !== 'quantity')) {
                json += ",\"" + inputs[i].name + "\":\"" + inputs[i].value + "\"";
            }
        }
    }
    json += "}";
    var xhr = new XMLHttpRequest();
    xhr.open('POST', path);
    //alert("path:" + path + " json:[" + json + "]");
    xhr.setRequestHeader('Content-Type', 'application/json; charset=utf-8');
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 200) { 
            //alert("Documento adicionado.");
            return "<html><h1>OK - inserted</h1></html>";
        } else {
            alert("readyState:" + xhr.readyState + " status:" + xhr.status + 
                                        " response:[" + xhr.responseText + "]");
            return "<html><h1>ERROR - "  + xhr.readyState + " status:" + 
                    xhr.status + " response:[" + xhr.responseText + "]";
        }
    };
    xhr.send(json);
}
</script>

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
            final String port = Integer.toString(request.getLocalPort());
            final String path = (serverName.equals(SERVER_HOST))
                    ? ("http://" + serverName + "/services/duplicates")
                    : ("http://" + serverName + ":" + port + "/DeDup/services/duplicates");
        %>
        <form action="<%=path%>" method="post" >
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
            <br/><br/>
            Schema:
            <select onchange="reloadPage()" name="schema" id="sch">
                <% 
                    final Set<String> schemas = getSchemas(request);
                    final Iterator<String> it2 = schemas.iterator();
                    String sch = request.getParameter("schema");
                    if (sch == null) {
                        sch = (it2.hasNext()) ? it2.next() : null;
                    }
                    for (String schema : schemas) { %>
                    <option value="<%=schema%>"
                    <% if (sch.equals(schema)) { %>
                        selected
                    <% } %>
                    ><%=schema%></option>
                <% } %>
            </select>
            <br><br>
            <% for (String parameter : getSchema(request, sch)) { %>                   
                <% if (!parameter.equals("database") && !parameter.equals("id")) { %>
                    <p><%=parameter%>: 
                    <input type="text" name="<%=parameter%>" size="60"
                    <%
                        String param = request.getParameter(parameter);
                        if (param != null) {
                    %>     
                        value="<%=param%>"
                    <% } %>
                    > <br/>
                <% } %>                                         
            <% } %>
            <br/>
            <br/>
            Número máximo de documentos a serem retornados:
            <input type="text" name="quantity" size=2 value="10">
            <br/>
            <br/>
            <%  
                boolean first = true;
                String schs = "[";
                for (String parameter : getSchema(request, sch)) { 
                    if (first) {
                        first = false;
                    } else {
                        schs += ",";
                    }
                    schs += "\"" + parameter + "\"";                
                }
                schs += "]";
            %>   
            <!--button type="button" value="Armazenar" onclick="putPage(<%=schs%>)">Armazenar</button-->
            <!--button type="button" value="Armazenar" onclick="putPage()">Armazenar</button-->
            <button type="button" value="Armazenar" onclick="putPage()">Armazenar</button>
            <input type="submit" value="Pesquisar">
            <br/>
            <br/>
            <font size="2">&ast; Usar como separador de ocorrências a sequência: &nbsp;<i>//@//</i></font><br/>
        </form>
    </body>
</html>
