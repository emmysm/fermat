package com.bitdubai.fermat_bch_plugin_layer.watch_only_vault.developer.bitdubai.version_1.database;

import com.bitdubai.fermat_api.DealsWithPluginIdentity;
import com.bitdubai.fermat_api.layer.all_definition.developer.DeveloperDatabase;
import com.bitdubai.fermat_api.layer.all_definition.developer.DeveloperDatabaseTable;
import com.bitdubai.fermat_api.layer.all_definition.developer.DeveloperDatabaseTableRecord;
import com.bitdubai.fermat_api.layer.all_definition.developer.DeveloperObjectFactory;
import com.bitdubai.fermat_api.layer.osa_android.database_system.Database;
import com.bitdubai.fermat_api.layer.osa_android.database_system.DatabaseRecord;
import com.bitdubai.fermat_api.layer.osa_android.database_system.DatabaseTable;
import com.bitdubai.fermat_api.layer.osa_android.database_system.DatabaseTableRecord;
import com.bitdubai.fermat_api.layer.osa_android.database_system.DealsWithPluginDatabaseSystem;
import com.bitdubai.fermat_api.layer.osa_android.database_system.PluginDatabaseSystem;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantCreateDatabaseException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantLoadTableToMemoryException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantOpenDatabaseException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.DatabaseNotFoundException;
import com.bitdubai.fermat_bch_plugin_layer.watch_only_vault.developer.bitdubai.version_1.exceptions.CantInitializeBitcoinWatchOnlyCryptoVaultDatabaseException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by rodrigo on 12/30/15.
 */
public class BitcoinWatchOnlyCryptoVaultDeveloperDatabaseFactory  implements DealsWithPluginDatabaseSystem, DealsWithPluginIdentity {

    /**
     * DealsWithPluginDatabaseSystem Interface member variables.
     */
    PluginDatabaseSystem pluginDatabaseSystem;

    /**
     * DealsWithPluginIdentity Interface member variables.
     */
    UUID pluginId;


    Database database;

    /**
     * Constructor
     *
     * @param pluginDatabaseSystem
     * @param pluginId
     */
    public BitcoinWatchOnlyCryptoVaultDeveloperDatabaseFactory(PluginDatabaseSystem pluginDatabaseSystem, UUID pluginId) {
        this.pluginDatabaseSystem = pluginDatabaseSystem;
        this.pluginId = pluginId;

        try {
            initializeDatabase();
        } catch (CantInitializeBitcoinWatchOnlyCryptoVaultDatabaseException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method open or creates the database i'll be working with
     *
     * @throws CantInitializeBitcoinWatchOnlyCryptoVaultDatabaseException
     */
    public void initializeDatabase() throws CantInitializeBitcoinWatchOnlyCryptoVaultDatabaseException {
        try {

             /*
              * Open new database connection
              */
            database = this.pluginDatabaseSystem.openDatabase(pluginId, pluginId.toString());

        } catch (CantOpenDatabaseException cantOpenDatabaseException) {

             /*
              * The database exists but cannot be open. I can not handle this situation.
              */
            throw new CantInitializeBitcoinWatchOnlyCryptoVaultDatabaseException(cantOpenDatabaseException.getMessage());

        } catch (DatabaseNotFoundException e) {

             /*
              * The database no exist may be the first time the plugin is running on this device,
              * We need to create the new database
              */
            BitcoinWatchOnlyCryptoVaultDatabaseFactory bitcoinWatchOnlyCryptoVaultDatabaseFactory = new BitcoinWatchOnlyCryptoVaultDatabaseFactory(pluginDatabaseSystem);

            try {
                  /*
                   * We create the new database
                   */
                database = bitcoinWatchOnlyCryptoVaultDatabaseFactory.createDatabase(pluginId, pluginId.toString());
            } catch (CantCreateDatabaseException cantCreateDatabaseException) {
                  /*
                   * The database cannot be created. I can not handle this situation.
                   */
                throw new CantInitializeBitcoinWatchOnlyCryptoVaultDatabaseException(cantCreateDatabaseException.getMessage());
            }
        }
    }


    public List<DeveloperDatabase> getDatabaseList(DeveloperObjectFactory developerObjectFactory) {
        /**
         * I only have one database on my plugin. I will return its name.
         */
        List<DeveloperDatabase> databases = new ArrayList<DeveloperDatabase>();
        databases.add(developerObjectFactory.getNewDeveloperDatabase("WatchOnlyVault", this.pluginId.toString()));
        return databases;
    }


    public List<DeveloperDatabaseTable> getDatabaseTableList(DeveloperObjectFactory developerObjectFactory) {
        List<DeveloperDatabaseTable> tables = new ArrayList<DeveloperDatabaseTable>();

        /**
         * Table key_accounts columns.
         */
        List<String> key_accountsColumns = new ArrayList<String>();

        key_accountsColumns.add(BitcoinWatchOnlyCryptoVaultDatabaseConstants.KEY_ACCOUNTS_ID_COLUMN_NAME);
        key_accountsColumns.add(BitcoinWatchOnlyCryptoVaultDatabaseConstants.KEY_ACCOUNTS_DESCRIPTION_COLUMN_NAME);
        key_accountsColumns.add(BitcoinWatchOnlyCryptoVaultDatabaseConstants.KEY_ACCOUNTS_TYPE_COLUMN_NAME);
        /**
         * Table key_accounts addition.
         */
        DeveloperDatabaseTable key_accountsTable = developerObjectFactory.getNewDeveloperDatabaseTable(BitcoinWatchOnlyCryptoVaultDatabaseConstants.KEY_ACCOUNTS_TABLE_NAME, key_accountsColumns);
        tables.add(key_accountsTable);

        /**
         * Table key_Maintenance columns.
         */
        List<String> key_MaintenanceColumns = new ArrayList<String>();

        key_MaintenanceColumns.add(BitcoinWatchOnlyCryptoVaultDatabaseConstants.KEY_MAINTENANCE_ACCOUNT_ID_COLUMN_NAME);
        key_MaintenanceColumns.add(BitcoinWatchOnlyCryptoVaultDatabaseConstants.KEY_MAINTENANCE_GENERATED_KEYS_COLUMN_NAME);
        key_MaintenanceColumns.add(BitcoinWatchOnlyCryptoVaultDatabaseConstants.KEY_MAINTENANCE_USED_KEYS_COLUMN_NAME);
        /**
         * Table key_Maintenance addition.
         */
        DeveloperDatabaseTable key_MaintenanceTable = developerObjectFactory.getNewDeveloperDatabaseTable(BitcoinWatchOnlyCryptoVaultDatabaseConstants.KEY_MAINTENANCE_TABLE_NAME, key_MaintenanceColumns);
        tables.add(key_MaintenanceTable);


        /**
         * Table key_Maintenance_detail columns.
         */
        List<String> key_Maintenance_DetailColumns = new ArrayList<String>();

        key_Maintenance_DetailColumns.add(BitcoinWatchOnlyCryptoVaultDatabaseConstants.KEY_MAINTENANCE_DETAIL_ACCOUNT_ID_COLUMN_NAME);
        key_Maintenance_DetailColumns.add(BitcoinWatchOnlyCryptoVaultDatabaseConstants.KEY_MAINTENANCE_DETAIL_KEY_DEPTH_COLUMN_NAME);
        key_Maintenance_DetailColumns.add(BitcoinWatchOnlyCryptoVaultDatabaseConstants.KEY_MAINTENANCE_DETAIL_PUBLIC_KEY_COLUMN_NAME);
        key_Maintenance_DetailColumns.add(BitcoinWatchOnlyCryptoVaultDatabaseConstants.KEY_MAINTENANCE_DETAIL_ADDRESS_COLUMN_NAME);
        key_Maintenance_DetailColumns.add(BitcoinWatchOnlyCryptoVaultDatabaseConstants.KEY_MAINTENANCE_DETAIL_BLOCKCHAIN_NETWORK_TYPE_COLUMN_NAME);

        /**
         * Table key_Maintenance_Detail addition.
         */
        DeveloperDatabaseTable key_MaintenanceDetailTable = developerObjectFactory.getNewDeveloperDatabaseTable(BitcoinWatchOnlyCryptoVaultDatabaseConstants.KEY_MAINTENANCE_DETAIL_TABLE_NAME, key_Maintenance_DetailColumns);
        tables.add(key_MaintenanceDetailTable);

        /**
         * Table key_Maintenance_Monitor columns.
         */
        List<String> key_Maintenance_MonitorColumns = new ArrayList<String>();

        key_Maintenance_MonitorColumns.add(BitcoinWatchOnlyCryptoVaultDatabaseConstants.KEY_MAINTENANCE_MONITOR_EXECUTION_NUMBER_COLUMN_NAME);
        key_Maintenance_MonitorColumns.add(BitcoinWatchOnlyCryptoVaultDatabaseConstants.KEY_MAINTENANCE_MONITOR_ACCOUNT_ID_COLUMN_NAME);
        key_Maintenance_MonitorColumns.add(BitcoinWatchOnlyCryptoVaultDatabaseConstants.KEY_MAINTENANCE_MONITOR_EXECUTION_DATE_COLUMN_NAME);
        key_Maintenance_MonitorColumns.add(BitcoinWatchOnlyCryptoVaultDatabaseConstants.KEY_MAINTENANCE_MONITOR_GENERATED_KEYS_COLUMN_NAME);
        key_Maintenance_MonitorColumns.add(BitcoinWatchOnlyCryptoVaultDatabaseConstants.KEY_MAINTENANCE_MONITOR_USED_KEYS_COLUMN_NAME);
        key_Maintenance_MonitorColumns.add(BitcoinWatchOnlyCryptoVaultDatabaseConstants.KEY_MAINTENANCE_MONITOR_THRESHOLD_COLUMN_NAME);
        /**
         * Table key_Maintenance_Monitor addition.
         */
        DeveloperDatabaseTable key_Maintenance_MonitorTable = developerObjectFactory.getNewDeveloperDatabaseTable(BitcoinWatchOnlyCryptoVaultDatabaseConstants.KEY_MAINTENANCE_MONITOR_TABLE_NAME, key_Maintenance_MonitorColumns);
        tables.add(key_Maintenance_MonitorTable);

        /**
         * Table active_Networks columns.
         */
        List<String> active_NetworksColumns = new ArrayList<String>();

        active_NetworksColumns.add(BitcoinWatchOnlyCryptoVaultDatabaseConstants.ACTIVE_NETWORKS_NETWORKTYPE_COLUMN_NAME);
        active_NetworksColumns.add(BitcoinWatchOnlyCryptoVaultDatabaseConstants.ACTIVE_NETWORKS_ACTIVATION_DATE_COLUMN_NAME);
        /**
         * Table active_Networks addition.
         */
        DeveloperDatabaseTable active_NetworksTable = developerObjectFactory.getNewDeveloperDatabaseTable(BitcoinWatchOnlyCryptoVaultDatabaseConstants.ACTIVE_NETWORKS_TABLE_NAME, active_NetworksColumns);
        tables.add(active_NetworksTable);



        return tables;
    }


    public List<DeveloperDatabaseTableRecord> getDatabaseTableContent(DeveloperObjectFactory developerObjectFactory, DeveloperDatabaseTable developerDatabaseTable) {
        /**
         * Will get the records for the given table
         */
        List<DeveloperDatabaseTableRecord> returnedRecords = new ArrayList<DeveloperDatabaseTableRecord>();
        /**
         * I load the passed table name from the SQLite database.
         */
        DatabaseTable selectedTable = database.getTable(developerDatabaseTable.getName());
        try {
            selectedTable.loadToMemory();
            List<DatabaseTableRecord> records = selectedTable.getRecords();
            for (DatabaseTableRecord row: records){
                List<String> developerRow = new ArrayList<String>();

                /**
                 * for each row in the table list
                 */
                for (DatabaseRecord field : row.getValues()){
                    /**
                     * I get each row and save them into a List<String>
                     */
                    developerRow.add(field.getValue());
                }
                /**
                 * I create the Developer Database record
                 */
                returnedRecords.add(developerObjectFactory.getNewDeveloperDatabaseTableRecord(developerRow));
            }
            /**
             * return the list of DeveloperRecords for the passed table.
             */
        } catch (CantLoadTableToMemoryException cantLoadTableToMemory) {
            /**
             * if there was an error, I will returned an empty list.
             */
            database.closeDatabase();
            return returnedRecords;
        } catch (Exception e){
            database.closeDatabase();
            return returnedRecords;
        }
        database.closeDatabase();
        return returnedRecords;
    }

    @Override
    public void setPluginDatabaseSystem(PluginDatabaseSystem pluginDatabaseSystem) {
        this.pluginDatabaseSystem = pluginDatabaseSystem;
    }

    @Override
    public void setPluginId(UUID pluginId) {
        this.pluginId = pluginId;
    }
}
