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

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author Heitor Barbieri
 * date: 20150928
 */
@javax.ws.rs.ApplicationPath("")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<Class<?>>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(br.bireme.ddp.DeDup.class);
        resources.add(br.bireme.ddp.GenericResource.class);
    }

}
