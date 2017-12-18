/**
 *  ElasticsearchTableFactory
 *  Copyright 03.12.2017 by Michael Peter Christen, @0rb1t3r
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program in the file lgpl21.txt
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package net.yacy.grid.io.db;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.yacy.grid.io.index.ElasticsearchClient;

/**
 * factory for elasticsearch-based tables
 */
public class ElasticsearchTableFactory implements TableFactory {

    private ElasticsearchClient elastic;
    private Map<String, MapTable> tables;
    
    /**
     * initialize a table factory. All tables will be stored at the
     * given path as files
     * @param storageLocationPath the storage location for the tables
     */
    public ElasticsearchTableFactory(ElasticsearchClient elastic) {
        this.elastic = elastic;
        this.tables = new ConcurrentHashMap<>();
    }
    
    /**
     * get a table for a given table name. If the table does not exist, it will
     * be generated. If the table was generated before, the table is not re-generated
     * but the previously generated handle is returned
     * @param tableName the name of the table
     * @return the table
     * @throws IOException
     */
    @Override
    public Table getTable(String databaseName) throws IOException {
        MapTable table = tables.get(databaseName);
        if (table != null) return table;
        synchronized (this) {
            table = tables.get(databaseName);
            if (table != null) return table;
            table = new MapTable(new ElasticsearchHashMap(this.elastic, databaseName));
            this.tables.put(databaseName, table);
            return table;
        }
    }
    
    /**
     * close the factory and all tables that have been opened
     */
    @Override
    public void close() {
        this.tables.values().forEach(table -> table.close());
    }

}
