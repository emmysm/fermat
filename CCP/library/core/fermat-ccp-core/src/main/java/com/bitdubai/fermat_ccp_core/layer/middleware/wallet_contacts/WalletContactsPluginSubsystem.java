package com.bitdubai.fermat_ccp_core.layer.middleware.wallet_contacts;

import com.bitdubai.fermat_api.layer.all_definition.common.abstract_classes.AbstractPluginDeveloper;
import com.bitdubai.fermat_api.layer.all_definition.common.abstract_classes.AbstractPluginSubsystem;
import com.bitdubai.fermat_api.layer.all_definition.common.exceptions.CantStartSubsystemException;
import com.bitdubai.fermat_api.layer.all_definition.common.interfaces.FermatPluginsEnum;
import com.bitdubai.fermat_api.layer.all_definition.common.utils.PluginReference;
import com.bitdubai.fermat_ccp_api.all_definition.enums.CCPPlugins;
import com.bitdubai.fermat_ccp_plugin.layer.middleware.wallet_contacts.developer.bitdubai.DeveloperBitDubai;

/**
 * Created by Leon Acosta - (laion.cj91@gmail.com) on 24/09/2015.
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
public class WalletContactsPluginSubsystem extends AbstractPluginSubsystem {

    public WalletContactsPluginSubsystem() {
        super(new PluginReference(CCPPlugins.BITDUBAI_WALLET_CONTACTS_MIDDLEWARE));
    }

    @Override
    public void start() throws CantStartSubsystemException {

        try {
            AbstractPluginDeveloper developerBitDubai = new DeveloperBitDubai();
            plugin = developerBitDubai.getPlugin();
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            throw new CantStartSubsystemException(e, null, null);
        }
    }

}
