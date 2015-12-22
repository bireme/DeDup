/*=========================================================================

    Copyright © 2015 BIREME/PAHO/WHO

    This file is part of DeDup.

    DeDup is free software: you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation, either version 2.1 of
    the License, or (at your option) any later version.

    DeDup is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with DeDup. If not, see <http://www.gnu.org/licenses/>.

=========================================================================*/

package br.bireme.ddp;

import br.bireme.ngrams.NGIndex;
import br.bireme.ngrams.NGSchema;
import br.bireme.ngrams.NGrams;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

/**
 * REST Web Service - Find Duplicated Records
 *
 * @author Heitor Barbieri
 * date: 20150928
 */

@Path("")
public class DeDup {
    private static final String ID = "id";
    private static final String DBASE = "database";
    private static final String TOKEN = "token";
    private static final String QUANTITY = "quantity";
    private static final String SCHEMA = "schema";
    private static final String OCC_SEP = "//@//";

    private final boolean PROCESS_TOKEN = false;

    @Context
    private ServletContext context;

    public DeDup() {
    }

    private Instances getInstances() {
        Instances instances;
        final Instances inst = (Instances)context.getAttribute("INSTANCES");

        if (inst == null) {
            final String confFile = context.getInitParameter("DEDUP_CONF_FILE");
            if (confFile == null) {
                throw new NullPointerException(
                                  "Init parameter 'DEDUP_CONF_FILE is missing");
            }
            try {
                instances = new Instances(confFile);
            } catch(Exception ex) {
                instances = null;
            }
            context.setAttribute("INSTANCES", instances);
        } else {
            instances = inst;
        }

        return instances;
    }

    /**
     * Retrieves representation of an instance of xxx.GenericResource
     * http://localhost:8084/DeDup/?database=lilacs&database=medline
     * @param request
     * @param response
     * @return 
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
   /* @GET
    @Produces("application/json") @Path("/")
    public void DeDupApp(@Context final HttpServletRequest request,
                         @Context final HttpServletResponse response)
                                          throws ServletException, IOException {
        final String nextJSP = "posthtml.html";
        System.out.println("path=" + context.getContextPath());
        final RequestDispatcher dispatcher =
                                          context.getRequestDispatcher(nextJSP);
        dispatcher.forward(request,response);
    }*/

    @GET
    @Produces("application/json") @Path("/schema/{schema}")
    public String showSchema(@PathParam("schema") String schema) {
        final Instances instances = getInstances();
        final String json;

        if (instances == null) {
            json = "{}";
        } else {
            final NGSchema nschema = instances.getSchemas().get(schema);
            if (nschema == null) {
                json = "{\"ERROR\":\"Schema not found: " + schema + "\"}";
            } else {                
                json = nschema.getSchemaJson();
            }
        }
        return json;
    }

    @GET
    @Produces("application/json") @Path("/schemas")
    public String showSchemas() {
        final Instances instances = getInstances();
        final String json;

        if (instances == null) {
            json = "{}";
        } else {
            final StringBuilder builder = new StringBuilder("{\"schemas\":[");
            boolean first = true;
            
            for (String schema : instances.getSchemas().keySet()) {
                if (first) {
                    first = false;
                } else {
                    builder.append(",");
                }
                builder.append("\"");
                builder.append(schema);
                builder.append("\"");
            }
            builder.append("]}");
            json = builder.toString();
        }
        return json;
    }
    
    @GET
    @Produces("application/json") @Path("/databases")
    public String showDatabases() {
        final Instances instances = getInstances();
        final String json;

        if (instances == null) {
            json = "{}";
        } else {
            final StringBuilder builder = new StringBuilder("{\"databases\":[");
            boolean first = true;
        
            for (String db : instances.getIndexes().keySet()) {
                if (first) {
                    first = false;
                } else {
                    builder.append(",");
                }
                builder.append("\"");
                builder.append(db);
                builder.append("\"");
            }
            builder.append("]}");
            json = builder.toString();
        }
        return json;
    }

    /**
     * * http://localhost:8084/DeDup/get/duplicates/?database=lilacs&database=medline
     * @param dbList
     * @param uriInfo
     * @param schema
     * @param token
     * @return
     */
    @GET
    @Produces("application/json") @Path("/get/duplicates")
    public String duplicatesGet(@Context final UriInfo uriInfo,
                              @QueryParam("database") final List<String> dbList,
                              @QueryParam("schema") final String schema,
                              @QueryParam("token") final String token) {
        String json;

        if ((dbList == null) || dbList.isEmpty()) {
            json = "{\"ERROR\":\"missing 'database' parameter\"}";
        } else if ((schema == null) || schema.isEmpty()) {
            json = "{\"ERROR\":\"missing 'schema' parameter\"}";   
        } else if (PROCESS_TOKEN) {
            if ((token == null) || token.isEmpty()) {
                json = "{\"ERROR\":\"invalid token value\"}";
            }
            // Check token here
        } else {
            try {
                final MultivaluedMap<String, String> queryParams =
                                                   uriInfo.getQueryParameters();
                final Instances instances = getInstances();
                final NGSchema nschema = instances.getSchemas().get(schema);
                if (nschema == null) {
                    throw new IllegalArgumentException(
                                       "invalid 'schema' parameter: " + schema);
                }
                final String expr = 
                             getPipedExpression(instances, schema, queryParams);
                final String squant = queryParams.getFirst("quantity");
                final MultivaluedMap<String,String> results =
                                    new MultivaluedHashMap<String,String>();
                int quantity = (squant == null) ? 10 : Integer.parseInt(squant);
                quantity = (quantity <= 0) ? 10 : quantity;

                for (String dbs : dbList) {
                    final String[] split = dbs.split(" *" + OCC_SEP + " *");
                    for (String dbn : split) {
                        final Set<NGIndex> indexes = 
                                            instances.getIndexes().get(dbn);
                        if (indexes == null) {
                            throw new IllegalArgumentException(
                                    "invalid 'database' parameter: " + dbn);
                        }
                        for (NGIndex nindex: indexes) {
                            /*final Set<String> srcRes = 
                                    NGrams.search(nindex, nschema, expr, false);*/
                            final Set<String> srcRes = 
                                    NGrams.search(nindex, nschema, expr, true);
                            for (String res : srcRes) {
                                results.add(dbn, res);
                            }
                        }
                    }
                }
                final List<String> grouped = groupResults(results, 0, 1, quantity);
                json = showJsonResults(0, 1, nschema, queryParams, grouped);
            } catch(Exception ex) {
                String msg = ex.getMessage();
                msg = (msg == null) ? "" : msg.replace('"', '\'');
                json = "{\"ERROR\":\"" + msg + "\"}";
            }
        }
        return json;
    }

    @POST
    @Path("/duplicates")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public String duplicatesPost(@FormParam("database") final List<String> dbList,
                                 @FormParam("schema") final String schema,
                                 @FormParam("token") final String token,
                                 MultivaluedMap<String, String> formParams) {
        String json;

        if ((dbList == null) || dbList.isEmpty()) {
            json = "{\"ERROR\":\"missing 'database' parameter\"}";
        } else if ((schema == null) || schema.isEmpty()) {
            json = "{\"ERROR\":\"missing 'schema' parameter\"}";    
        } else if (PROCESS_TOKEN) {
            if ((token == null) || token.isEmpty()) {
                json = "{\"ERROR\":\"invalid token value\"}";
            }
            // Check token here
        } else {
            try {
                final Instances instances = getInstances();
                final NGSchema nschema = instances.getSchemas().get(schema);
                if (nschema == null) {
                    throw new IllegalArgumentException(
                                       "invalid 'schema' parameter: " + schema);
                }
                final String expr = 
                             getPipedExpression(instances, schema, formParams);
                final String squant = formParams.getFirst("quantity");
                final MultivaluedMap<String,String> results =
                                    new MultivaluedHashMap<String,String>();
                int quantity = (squant == null) ? 10 : Integer.parseInt(squant);
                quantity = (quantity <= 0) ? 10 : quantity;

                for (String dbs : dbList) {
                    final String[] split = dbs.split(" *" + OCC_SEP + " *");
                    for (String dbn : split) {
                        final Set<NGIndex> indexes = 
                                            instances.getIndexes().get(dbn);
                        if (indexes == null) {
                            throw new IllegalArgumentException(
                                    "invalid 'database' parameter: " + dbn);
                        }
                        for (NGIndex nindex: indexes) {
                            final Set<String> srcRes = 
                                     NGrams.search(nindex, nschema, expr, true);
                            for (String res : srcRes) {
                                results.add(dbn, res);
                            }
                        }
                    }
                }
                final List<String> grouped = groupResults(results, 0, 1, quantity);
                json = showJsonResults(0, 1, nschema, formParams, grouped);
            } catch(Exception ex) {
                String msg = ex.getMessage();
                msg = (msg == null) ? "" : msg.replace('"', '\'');
                json = "{\"ERROR\":\"" + msg + "\"}";
            }
        }
        return json;
    }

    private String getPipedExpression(final Instances instances,
                                      final String schema,
                             final MultivaluedMap<String, String> queryParams) {
        assert instances != null;
        assert schema != null;
        assert queryParams != null;

        final NGSchema nschema = instances.getSchemas().get(schema);
        if (nschema == null) {
            throw new IllegalArgumentException("invalid schema: " + schema);
        }
        final String indexedFldName = nschema.getIndexedFldName();
        if (!queryParams.containsKey(indexedFldName)) {
            throw new IllegalArgumentException("'" + indexedFldName
                                                    + "' parameter is missing");
        }        
        final StringBuilder builder = new StringBuilder();
        final Map<String,Integer> namesPos = nschema.getNamesPos();
        final TreeMap<Integer,String> posNames = (TreeMap<Integer,String>)
                                                         nschema.getPosNames();
        final String[] array = new String[posNames.lastKey() + 1];               

        for (Map.Entry<String,List<String>> entry : queryParams.entrySet()) {
            final String key = entry.getKey();
            final List<String> value = entry.getValue();
            builder.setLength(0);

            /*if (key.equals("id") || key.equals(indexedFldName)) {
                array[namesPos.get(key)] = value.get(0);
            } else {}*/
                
            if ((!key.equals(TOKEN)) && (!key.equals(QUANTITY) && 
                                                       (!key.equals(SCHEMA)))) {
                if (!namesPos.containsKey(key)) {
                    throw new IllegalArgumentException("'" + key + "' parameter"
                                           + " is not into configuration file");
                }
                boolean first = true;
                for (String val : value) {
                    if (first) {
                        first = false;
                    } else {
                        builder.append(OCC_SEP);
                    }
                    builder.append(val);
                }
                array[namesPos.get(key)] = builder.toString();
            }
        }
        return getPipedExpression(array);
    }

    private String getPipedExpression(final String[] array) {
        assert array != null;

        final StringBuilder builder = new StringBuilder();

        for (int idx = 0; idx < array.length; idx++) {
            if (idx > 0) {
                builder.append('|');
            }
            final String val = array[idx];
            if (val != null) {
                builder.append(val);
            }
        }
        return builder.toString();
    }

    private List<String> groupResults(final MultivaluedMap<String,String> results,
                                      final int scorePos,
                                      final int similarityPos,
                                      final int quantity) {
        assert results != null;
        assert scorePos >= 0;
        assert similarityPos >= 0;
        assert quantity > 0;

        final TreeMap<String,String> tree = new TreeMap<String,String>();
        final List<String> ret = new ArrayList<String>();

        for (Map.Entry<String,List<String>> entry : results.entrySet()) {
            final String key = entry.getKey();
            final List<String> values = entry.getValue();

            for (String val : values) {
                final String[] split = val.split("\\|");
                if (split.length <= similarityPos) {
                    throw new IllegalArgumentException("bad result:" + val);
                }
                tree.put(Float.parseFloat(split[similarityPos]) + "_" +
                         Float.parseFloat(split[scorePos]) + "_" + key, val);
            }
        }

        int tot = 0;
        for (String result : tree.descendingMap().values()) {
            if (++tot > quantity) {
                break;
            }
            ret.add(result);
        }
        return ret;
    }

    private void getParameters(final MultivaluedMap<String, String> queryParams,
                               final StringBuilder builder) {
        assert queryParams != null;
        assert builder != null;

        boolean first1 = true;
        boolean first2 = true;

        builder.append("\"params\":{");
        if (PROCESS_TOKEN) {
            builder.append("\"token\":\"");
            builder.append(queryParams.get(TOKEN).get(0));
        }
        final TreeMap<String,List<String>> qmap =
                                  new TreeMap<String,List<String>>(queryParams);
        for (Map.Entry<String,List<String>> entry : qmap.entrySet()) {
            final String key = entry.getKey();
            final List<String> value = entry.getValue();
            if (first1) {
                first1 = false;
            } else {
                builder.append(",");
            }
            if(!key.equals(TOKEN)) {
                builder.append("\"").append(key).append("\":");
                if (value.size() > 1) {
                    builder.append("[");
                    if (first2) {
                        first2 = false;
                    } else {
                        builder.append(",");
                    }
                    for (String val : value) {
                        builder.append("\"").append(val).append("\"");
                    }
                    builder.append("]");
                } else {
                    final String val = entry.getValue().get(0);
                    if (val.contains(OCC_SEP)) {
                        boolean first3 = true;
                        final String[] spl = val.split(" *" + OCC_SEP + " *");

                        builder.append("[");
                        for (String s : spl) {
                            if (first3) {
                                first3 = false;
                            } else {
                                builder.append(",");
                            }
                            builder.append("\"").append(s).append("\"");
                        }
                        builder.append("]");
                    } else {
                        builder.append("\"").append(val).append("\"");
                    }
                }
            }
        }
        builder.append("}");
    }

    private String showJsonResults(
                                final int scorePos,
                                final int similarityPos,
                                final NGSchema schema,
                                final MultivaluedMap<String,String> queryParams,
                                final List<String> grouped) {
        assert scorePos >= 0;
        assert similarityPos >= 0;
        assert schema != null;
        assert queryParams != null;
        assert grouped != null;

        final StringBuilder builder = new StringBuilder("{");
        boolean first = true;

        getParameters(queryParams, builder);
        builder.append(",\"total\":")
               .append(grouped.size())
               .append(",\"result\":[");
        for (String res: grouped) {
            if (first) {
                first = false;
            } else {
                builder.append(",");
            }

            final String[] split = res.split("\\|");
            final int idx = ((split.length - 2) / 2) + 2;            
            final Map<String,Integer> namesPos = schema.getNamesPos();

            builder.append("{").append("\"score\":\"")
                   .append(split[scorePos]).append("\",\"similarity\":\"")
                   .append(split[similarityPos]).append("\"");

            for (Map.Entry<String,Integer> entry: namesPos.entrySet()) {
                final String content = split[idx + 2 * entry.getValue()];
                builder.append(",").append("\"").append(entry.getKey())
                                                                 .append("\":");
                if (content.contains(OCC_SEP)) {
                    final String[] split2 = content.split(" *" + OCC_SEP
                                                                        + " *");
                    boolean first2 = true;
                    builder.append("[");
                    for (String spl : split2) {
                        if (first2) {
                            first2 = false;
                        } else {
                            builder.append(",");
                        }
                        builder.append("\"").append(spl).append("\"");
                    }
                    builder.append("]");
                } else {
                    builder.append("\"").append(content).append("\"");
                }

            }
            builder.append("}");
        }
        builder.append("]}");

        return builder.toString();
    }
}
