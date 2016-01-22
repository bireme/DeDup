<%-- 
    Document   : DeDup
    Created on : 19/01/2016, 11:01:13
    Author     : Heitor Barbieri
--%>

<%@page language="java"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.*,java.net.*,java.io.*,org.json.simple.* " %>

<%!
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
            final String json = readURL("http://localhost:" 
                          + request.getLocalPort() + "/DeDup/services/schemas");
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
            final String json = readURL("http://localhost:" 
                        + request.getLocalPort() + "/DeDup/services/databases");
            final JSONObject obj = (JSONObject)JSONValue.parse(json);
            final JSONArray array = (JSONArray)obj.get("databases");
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
            final String json = readURL("http://localhost:" 
                + request.getLocalPort() + "/DeDup/services/schema/" + schema);
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
    var path = "/DeDup/services/?database=" + dbValue + "&schema=" + schValue;
    
    window.location=path;
}

function reloadPagePost() {
    var database = document.getElementById("db");
    var dbValue = database.options[database.selectedIndex].value;    
    var schema = document.getElementById("sch");
    var schValue = schema.options[schema.selectedIndex].value;
    var path = "/DeDup/services/";
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
</script>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>DeDup</title>
    </head>
    <body style="background-color:#f7faff">
        <h1>DeDup Application</h1>
        
        <form action="/DeDup/services/duplicates" method="post" >
            Base de dados:
            <select name="database" id="db">
                <% 
                   final Set<String> databases = getDatabases(request);
                   String dbase = request.getParameter("database");
                   dbase = (dbase == null) ? databases.iterator().next() : dbase;
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
                    String sch = request.getParameter("schema");
                    sch = (sch == null) ? schemas.iterator().next() : sch;
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
            <input type="submit" value="Pesquisar">
            <br/>
            <br/>
            <font size="2">&ast; Usar como separador de ocorrências a sequência: &nbsp;<i>//@//</i></font><br/>

        </form>
    </body>
</html>
