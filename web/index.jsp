<%--
    Document   : index
    Created on : 28/09/2015, 14:16:35
    Author     : heitor
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>DeDup - Finding duplicated records</title>
    </head>
    <body>
        <h1>DeDup Services:</h1>

        <h2>1) Example application</h2>
        <p><i>description:</i> Example application that displays documents whose fields
          are similar/equal to those used as input.</p>
        <p><i>path:</i> http://&lt;host&gt;/services</p>

        <h2>2) Show Schemas</h2>
        <p><i>description:</i> Show all available schemas in this service.</p>
        <p><i>path:</i> http://&lt;host&gt;/services/schemas</p>
        <p><i>method:</i> GET</p>
        <p><i>consumes:</i> &lt;none&gt;</p>
        <p><i>produces:</i> application/json</p>
        <p><i>parameters:</i> &lt;none&gt;</p>

        <h2>3) Show Databases</h2>
        <p><i>description: Show all availables index sets in this service. Each
        index set is defined by the schema definition.</i></p>
        <p><i>path:</i> http://&lt;host&gt;/services/databases</p>
        <p><i>method:</i> GET</p>
        <p><i>consumes:</i> &lt;none&gt;</p>
        <p><i>produces:</i> application/json</p>
        <p><i>parameters:</i> &lt;none&gt;</p>

        <h2>4) Show One Schema</h2>
        <p><i>description:</i> Show all schema elements.</p>
        <p><i>path:</i> http://&lt;host&gt;/services/schema/&lt;schema&gt;</p>
        <p><i>method:</i> GET</p>
        <p><i>consumes:</i> &lt;none&gt;</p>
        <p><i>produces:</i> application/json</p>
        <p><i>parameters:</i> &lt;schema&gt; - the schema name as listed in 'Show Schemas'</p>

        <h2>5) Duplicates (GET)</h2>
        <p><i>description:</i> Show all duplicated or very similar documents. GET version.</p>
        <p><i>path:</i> http://&lt;host&gt;/services/get/duplicates</p>
        <p><i>method:</i> GET</p>
        <p><i>consumes:</i></p>
        <p><i>produces:</i> application/json</p>
        <p><i>parameters:</i><br/>
           &lt;database&gt; - database name. See 'Show Databases'.<br/>
           &lt;schema&gt; - schema name. See 'Show Schemas'.<br/>
           &lt;quantity&gt; - number of similar/equal douments to be showed.<br/>
           &lt;param1&gt; - document field 1 as defined in schema. See 'Show One Schema'.<br/>
           &lt;param2&gt; - document field 2 as defined in schema. See 'Show One Schema'.<br/>
           ...<br/>
           &lt;paramN&gt; - document field N as defined in schema. See 'Show One Schema'.<br/>
        </p>

        <h2>6) Duplicates (POST)</h2>
        <p><i>description:</i> Show all duplicated or very similar documents. POST version.</p>
        <p><i>path:</i> http://&lt;host&gt;/services/duplicates</p>
        <p><i>method:</i> POST</p>
        <p><i>consumes:</i> application/x-www-form-urlencoded</p>
        <p><i>produces:</i> application/json</p>
        <p><i>parameters:</i><br/>
           &lt;database&gt; - database name. See 'Show Databases'.<br/>
           &lt;schema&gt; - schema name. See 'Show Schemas'.<br/>
           &lt;quantity&gt; - number of similar/equal douments to be showed.<br/>
           &lt;param1&gt; - document field 1 as defined in schema. See 'Show One Schema'.<br/>
           &lt;param2&gt; - document field 2 as defined in schema. See 'Show One Schema'.<br/>
           ...<br/>
           &lt;paramN&gt; - document field N as defined in schema. See 'Show One Schema'.<br/>
        </p>
    </body>
</html>
