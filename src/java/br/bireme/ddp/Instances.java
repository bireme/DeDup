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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Heitor Barbieri
 * date: 20151013
 */
public class Instances {
    private final Map<String, NGSchema> schemas;
    private final Map<String, NGIndex> indexes;
    private final Map<String, Set<NGIndex>> databases; 

    public Instances(final String confFile) throws ParserConfigurationException,
                                                   SAXException,
                                                   IOException {
        if (confFile == null) {
            throw new NullPointerException("confFile");
        }
        schemas = new TreeMap<String, NGSchema>();
        indexes = new TreeMap<String, NGIndex>();
        databases = new TreeMap<String, Set<NGIndex>>();

        parseConfig(confFile);
    }

    public Map<String, NGSchema> getSchemas() {
        return schemas;
    }

    public Map<String, NGIndex> getIndexes() {
        return indexes;
    }
    
    public Map<String, Set<NGIndex>> getDatabases() {
        return databases;
    }

    private void parseConfig(final String confFile) throws
                                                   ParserConfigurationException,
                                                   SAXException,
                                                   IOException {
        assert confFile != null;

        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();
        final File file = new File(confFile);
        
        if (!file.exists()) {
            throw new IOException("missing DeDup configuration file:" + confFile);
        }
        final Document doc = db.parse(new File(confFile));

        final Node configNode = doc.getFirstChild();
        if (! "config".equals(configNode.getNodeName())) {
            throw new IOException("missing 'config' node");
        }

        parseSchemas(configNode);
        parseIndexes(configNode);
        //parseDatabases(configNode);
    }

    final void parseSchemas(final Node config) throws
                                                   ParserConfigurationException,
                                                   SAXException,
                                                   IOException {
        assert config != null;

        for (Node schNode : getNodes(config, "schema")) {
            final String name = getChildContent(schNode, "name");
            if (schemas.containsKey(name)) {
                throw new IOException("duplicated schema name:" + name);
            }
            final NGSchema schema = new NGSchema(name,
                                          getChildContent(schNode, "path"),
                                          getChildContent(schNode, "encoding"));
            schemas.put(name, schema);
        }
    }

    final void parseIndexes(final Node config) throws
                                                   ParserConfigurationException,
                                                   SAXException,
                                                   IOException {
        assert config != null;

        for (Node idxNode : getNodes(config, "index")) {
            final String name = getChildContent(idxNode, "name");
            if (indexes.containsKey(name)) {
                throw new IOException("duplicated index name:" + name);
            }
            final String schemaName = getChildContent(idxNode, "schema");
            final NGSchema schema = schemas.get(schemaName);
            if (schema == null) {
                throw new IOException("missing index/schema element");
            }
            final String path = getChildContent(idxNode, "path");
            final NGIndex index = new NGIndex(name, path, schema);
            indexes.put(name, index);
        }
    }
    
    final void parseDatabases(final Node config) throws
                                                   ParserConfigurationException,
                                                   SAXException,
                                                   IOException {
        assert config != null;

        for (Node dbNode : getNodes(config, "database")) {
            final String name = getChildContent(dbNode, "name");
            if (databases.containsKey(name)) {
                throw new IOException("duplicated database name:" + name);
            }
            final Set<NGIndex> hngi = new HashSet<NGIndex>();
            databases.put(name, hngi);

            for (Node idxNode : getNodes(dbNode, "index")) {
                final NGIndex ngi = indexes.get(idxNode.getTextContent());
                if (ngi == null) {
                    throw new IOException("missing database/index element");
                }
                hngi.add(ngi);
            }
            if (hngi.isEmpty()) {
                throw new IOException("missing database/index element");
            }
        }
    }

    private List<Node> getNodes(final Node root,
                                final String nname) {
        assert root != null;
        assert nname != null;

        final List<Node> lst = new ArrayList<Node>();
        final NodeList child = root.getChildNodes();
        final int len = child.getLength();

        for (int idx = 0; idx < len; idx++) {
            final Node node = child.item(idx);
            if (node.getNodeName().equals(nname)) {
                lst.add(node);
            }
        }

        return lst;
    }

    private String getChildContent(final Node root,
                                   final String childName) throws IOException {
        assert root != null;
        assert childName != null;

        final NodeList child = root.getChildNodes();
        final int len = child.getLength();
        String content = null;

        for (int idx = 0; idx < len; idx++) {
            final Node node = child.item(idx);
            if (node.getNodeName().equals(childName)) {
                content = node.getTextContent();
                break;
            }
        }
        if (content == null) {
            throw new IOException("missing '" + childName + "' node");
        }

        return content.trim();
    }
}
