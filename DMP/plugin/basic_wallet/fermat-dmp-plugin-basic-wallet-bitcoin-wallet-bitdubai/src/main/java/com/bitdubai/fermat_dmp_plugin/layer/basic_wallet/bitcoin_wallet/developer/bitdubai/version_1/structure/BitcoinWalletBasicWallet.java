package com.bitdubai.fermat_dmp_plugin.layer.basic_wallet.bitcoin_wallet.developer.bitdubai.version_1.structure;

import com.bitdubai.fermat_api.FermatException;
import com.bitdubai.fermat_api.layer.all_definition.enums.Plugins;
import com.bitdubai.fermat_api.layer.dmp_basic_wallet.bitcoin_wallet.enums.BalanceType;
import com.bitdubai.fermat_api.layer.dmp_basic_wallet.bitcoin_wallet.interfaces.BitcoinWalletTransactionSummary;
import com.bitdubai.fermat_api.layer.dmp_basic_wallet.common_exceptions.CantGetActorTransactionSummaryException;
import com.bitdubai.fermat_api.layer.dmp_basic_wallet.common_exceptions.CantListTransactionsException;
import com.bitdubai.fermat_api.layer.dmp_basic_wallet.bitcoin_wallet.interfaces.BitcoinWalletBalance;
import com.bitdubai.fermat_api.layer.dmp_basic_wallet.bitcoin_wallet.interfaces.BitcoinWalletTransaction;
import com.bitdubai.fermat_api.layer.dmp_basic_wallet.common_exceptions.CantStoreMemoException;
import com.bitdubai.fermat_api.layer.dmp_basic_wallet.common_exceptions.CantFindTransactionException;
import com.bitdubai.fermat_api.layer.dmp_basic_wallet.bitcoin_wallet.exceptions.CantInitializeBitcoinWalletBasicException;
import com.bitdubai.fermat_api.layer.dmp_basic_wallet.bitcoin_wallet.interfaces.BitcoinWalletWallet;

import com.bitdubai.fermat_api.layer.osa_android.database_system.Database;
import com.bitdubai.fermat_api.layer.osa_android.database_system.PluginDatabaseSystem;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantCreateDatabaseException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.DatabaseNotFoundException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantOpenDatabaseException;
import com.bitdubai.fermat_api.layer.osa_android.file_system.FileLifeSpan;
import com.bitdubai.fermat_api.layer.osa_android.file_system.FilePrivacy;
import com.bitdubai.fermat_api.layer.osa_android.file_system.PluginFileSystem;
import com.bitdubai.fermat_api.layer.osa_android.file_system.PluginTextFile;
import com.bitdubai.fermat_api.layer.osa_android.file_system.exceptions.CantCreateFileException;
import com.bitdubai.fermat_api.layer.osa_android.file_system.exceptions.CantLoadFileException;
import com.bitdubai.fermat_api.layer.osa_android.file_system.exceptions.CantPersistFileException;
import com.bitdubai.fermat_api.layer.osa_android.file_system.exceptions.FileNotFoundException;
import com.bitdubai.fermat_pip_api.layer.pip_platform_service.error_manager.ErrorManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.bitdubai.fermat_api.layer.dmp_basic_wallet.common_exceptions.CantCreateWalletException;
import com.bitdubai.fermat_pip_api.layer.pip_platform_service.error_manager.UnexpectedPluginExceptionSeverity;

/**
 * Created by eze on 2015.06.23..
 * Modified by Leon Acosta - (laion.cj91@gmail.com) on 18/09/15.
 */
public class BitcoinWalletBasicWallet implements BitcoinWalletWallet {

    private static final String WALLET_IDS_FILE_NAME = "walletsIds";

    /**
     * BitcoinWalletBasicWallet member variables.
     */
    private Database database;
    //private UUID internalWalletId;

    private Map<String, UUID> walletIds = new HashMap<>();

    private BitcoinWalletBasicWalletDao bitcoinWalletBasicWalletDao;

    private ErrorManager errorManager;

    private PluginDatabaseSystem pluginDatabaseSystem;

    private PluginFileSystem pluginFileSystem;

    private UUID pluginId;

    public BitcoinWalletBasicWallet(ErrorManager errorManager, PluginDatabaseSystem pluginDatabaseSystem, PluginFileSystem pluginFileSystem, UUID pluginId) {
        this.errorManager = errorManager;
        this.pluginDatabaseSystem = pluginDatabaseSystem;
        this.pluginFileSystem = pluginFileSystem;
        this.pluginId = pluginId;
    }

    //metodo create para crear la base de datos
    // metodo initialize abre la table, y si no existe da un error

    /* This methid is called by the plug-in Root with the internal walletId as parameter
     * The wallet tries to open it's database. If it fails it is because the wallet was not
     * properly created before, so we end with an error in that case
     */
    public void initialize(UUID walletId) throws CantInitializeBitcoinWalletBasicException {
        if (walletId == null)
            throw new CantInitializeBitcoinWalletBasicException("InternalId is null", null, "Parameter walletId is null", "loadWallet didn't find the asociated id");

        try {
            database = this.pluginDatabaseSystem.openDatabase(this.pluginId, walletId.toString());
        } catch (CantOpenDatabaseException cantOpenDatabaseException) {
            throw new CantInitializeBitcoinWalletBasicException("I can't open database", cantOpenDatabaseException, "WalletId: " + walletId.toString(), "");
        } catch (DatabaseNotFoundException databaseNotFoundException) {
            throw new CantInitializeBitcoinWalletBasicException("Database does not exists", databaseNotFoundException, "WalletId: " + walletId.toString(), "");
        } catch (Exception exception) {
            throw new CantInitializeBitcoinWalletBasicException(CantInitializeBitcoinWalletBasicException.DEFAULT_MESSAGE, FermatException.wrapException(exception), null, null);
        }
    }


    /* At the moment of creation the plug-in root gives us the walletId
     * the external modules use ti identify this wallet. We will create
     * an internal walletId that we will store in a file.
     * This internal wallet Id will be used to name the database of this wallet
     * This is a security choise of implementation
     */
    // TODO: In this implementation it is not being considered how to solve problems while
    //       creating the wallet. For example, if the file persistToMemory method fails.
    //       The file insertion fail should delete the database created
    //       The internal Id generated should be checked before assigning it (check it was
    //       not previously assign)

    public UUID create(String walletId) throws CantCreateWalletException {
        try {
            // TODO: Until the Wallet MAnager create the wallets, we will use this internal id
            //       We need to change this in the near future
            UUID internalWalletId = UUID.randomUUID();
            createWalletDatabase(internalWalletId);
            PluginTextFile walletIdsFile = createIdsFile();
            loadWalletIdsMap(walletIdsFile);
            walletIds.put(walletId, internalWalletId);
            persistWalletIds(walletIdsFile);
            return internalWalletId;
        } catch (CantCreateWalletException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new CantCreateWalletException(CantCreateWalletException.DEFAULT_MESSAGE, FermatException.wrapException(exception), null, null);
        }

    }

    @Override
    public List<BitcoinWalletTransaction> listTransactions(BalanceType balanceType,
                                                           int max,
                                                           int offset) throws CantListTransactionsException {
        try {
            bitcoinWalletBasicWalletDao = new BitcoinWalletBasicWalletDao(database);
            return bitcoinWalletBasicWalletDao.listTransactions(balanceType, max, offset);
        } catch (CantListTransactionsException exception) {
            errorManager.reportUnexpectedPluginException(Plugins.BITDUBAI_BITCOIN_WALLET_BASIC_WALLET, UnexpectedPluginExceptionSeverity.DISABLES_SOME_FUNCTIONALITY_WITHIN_THIS_PLUGIN, FermatException.wrapException(exception));
            throw exception;
        } catch (Exception exception) {
            errorManager.reportUnexpectedPluginException(Plugins.BITDUBAI_BITCOIN_WALLET_BASIC_WALLET, UnexpectedPluginExceptionSeverity.DISABLES_SOME_FUNCTIONALITY_WITHIN_THIS_PLUGIN, FermatException.wrapException(exception));
            throw new CantListTransactionsException(CantListTransactionsException.DEFAULT_MESSAGE, FermatException.wrapException(exception), null, null);
        }
    }

    @Override
    public List<BitcoinWalletTransaction> listTransactionsByActor(String actorPublicKey,
                                                                  BalanceType balanceType,
                                                                  int max,
                                                                  int offset) throws CantListTransactionsException {
        try {
            bitcoinWalletBasicWalletDao = new BitcoinWalletBasicWalletDao(database);
            return bitcoinWalletBasicWalletDao.listTransactionsByActor(actorPublicKey, balanceType, max, offset);
        } catch (CantListTransactionsException exception) {
            errorManager.reportUnexpectedPluginException(Plugins.BITDUBAI_BITCOIN_WALLET_BASIC_WALLET, UnexpectedPluginExceptionSeverity.DISABLES_SOME_FUNCTIONALITY_WITHIN_THIS_PLUGIN, FermatException.wrapException(exception));
            throw exception;
        } catch (Exception exception) {
            errorManager.reportUnexpectedPluginException(Plugins.BITDUBAI_BITCOIN_WALLET_BASIC_WALLET, UnexpectedPluginExceptionSeverity.DISABLES_SOME_FUNCTIONALITY_WITHIN_THIS_PLUGIN, FermatException.wrapException(exception));
            throw new CantListTransactionsException(CantListTransactionsException.DEFAULT_MESSAGE, FermatException.wrapException(exception), null, null);
        }
    }

    @Override
    public void setTransactionDescription(UUID transactionID, String memo) throws CantStoreMemoException, CantFindTransactionException {
        try {
            bitcoinWalletBasicWalletDao = new BitcoinWalletBasicWalletDao(database);
            bitcoinWalletBasicWalletDao.updateMemoFiled(transactionID, memo);
        } catch (CantStoreMemoException | CantFindTransactionException exception) {
            errorManager.reportUnexpectedPluginException(Plugins.BITDUBAI_BITCOIN_WALLET_BASIC_WALLET, UnexpectedPluginExceptionSeverity.DISABLES_SOME_FUNCTIONALITY_WITHIN_THIS_PLUGIN, FermatException.wrapException(exception));
            throw exception;
        } catch (Exception exception) {
            errorManager.reportUnexpectedPluginException(Plugins.BITDUBAI_BITCOIN_WALLET_BASIC_WALLET, UnexpectedPluginExceptionSeverity.DISABLES_SOME_FUNCTIONALITY_WITHIN_THIS_PLUGIN, FermatException.wrapException(exception));
            throw new CantStoreMemoException(CantStoreMemoException.DEFAULT_MESSAGE, FermatException.wrapException(exception), null, null);
        }
    }

    @Override
    public BitcoinWalletBalance getBalance(BalanceType balanceType) {
        switch (balanceType) {
            case AVAILABLE:
                return new BitcoinWalletBasicWalletAvailableBalance(database);
            case BOOK:
                return new BitcoinWalletBasicWalletBookBalance(database);
            default:
                return new BitcoinWalletBasicWalletAvailableBalance(database);
        }
    }

    @Override
    public BitcoinWalletTransactionSummary getActorTransactionSummary(String actorPublicKey, BalanceType balanceType) throws CantGetActorTransactionSummaryException {
        try {
            bitcoinWalletBasicWalletDao = new BitcoinWalletBasicWalletDao(database);
            return bitcoinWalletBasicWalletDao.getActorTransactionSummary(actorPublicKey, balanceType);
        } catch (CantGetActorTransactionSummaryException exception) {
            errorManager.reportUnexpectedPluginException(Plugins.BITDUBAI_BITCOIN_WALLET_BASIC_WALLET, UnexpectedPluginExceptionSeverity.DISABLES_SOME_FUNCTIONALITY_WITHIN_THIS_PLUGIN, FermatException.wrapException(exception));
            throw exception;
        } catch (Exception exception) {
            errorManager.reportUnexpectedPluginException(Plugins.BITDUBAI_BITCOIN_WALLET_BASIC_WALLET, UnexpectedPluginExceptionSeverity.DISABLES_SOME_FUNCTIONALITY_WITHIN_THIS_PLUGIN, FermatException.wrapException(exception));
            throw new CantGetActorTransactionSummaryException(CantGetActorTransactionSummaryException.DEFAULT_MESSAGE, FermatException.wrapException(exception), null, null);
        }
    }

    private void createWalletDatabase(final UUID internalWalletId) throws CantCreateWalletException {
        try {
            BitcoinWalletDatabaseFactory databaseFactory = new BitcoinWalletDatabaseFactory();
            databaseFactory.setPluginDatabaseSystem(pluginDatabaseSystem);
            database = databaseFactory.createDatabase(this.pluginId, internalWalletId);
        } catch (CantCreateDatabaseException cantCreateDatabaseException) {
            throw new CantCreateWalletException("Database could not be created", cantCreateDatabaseException, "internalWalletId: " + internalWalletId.toString(), "");
        }
    }

    private PluginTextFile createIdsFile() throws CantCreateWalletException {
        try {
            return pluginFileSystem.getTextFile(pluginId, "", WALLET_IDS_FILE_NAME, FilePrivacy.PRIVATE, FileLifeSpan.PERMANENT);
        } catch (CantCreateFileException cantCreateFileException) {
            throw new CantCreateWalletException("File could not be created (?)", cantCreateFileException, "File Name: " + WALLET_IDS_FILE_NAME, "");
        } catch (FileNotFoundException e) {
            throw new CantCreateWalletException("File could not be found", e, "File Name: " + WALLET_IDS_FILE_NAME, "");
        }
    }

    private void loadWalletIdsMap(final PluginTextFile walletIdsFile) throws CantCreateWalletException {
        try {
            walletIdsFile.loadFromMedia();
            String[] stringWalletIds = walletIdsFile.getContent().split(";", -1);

            for (String stringWalletId : stringWalletIds) {

                if (!stringWalletId.equals("")) {
                    String[] idPair = stringWalletId.split(",", -1);
                    walletIds.put(idPair[0], UUID.fromString(idPair[1]));
                }
            }
        } catch (CantLoadFileException exception) {
            throw new CantCreateWalletException("Can't load file content from media", exception, "", "");
        }
    }

    private void persistWalletIds(final PluginTextFile walletIdsFile) throws CantCreateWalletException {
        StringBuilder stringBuilder = new StringBuilder(walletIds.size() * 72);
        Iterator iterator = walletIds.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();

            stringBuilder
                    .append(pair.getKey().toString())
                    .append(",")
                    .append(pair.getValue().toString())
                    .append(";");

            iterator.remove();
        }

        walletIdsFile.setContent(stringBuilder.toString());

        try {
            walletIdsFile.persistToMedia();
        } catch (CantPersistFileException cantPersistFileException) {
            throw new CantCreateWalletException("Could not persist in file", cantPersistFileException, "stringBuilder: " + stringBuilder.toString(), "");
        }
    }
}
