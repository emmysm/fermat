package com.bitdubai.fermat_cry_plugin.layer.crypto_router.incoming_crypto.developer.bitdubai.version_1.event_handlers;

import com.bitdubai.fermat_api.layer.dmp_transaction.TransactionServiceNotStartedException;
import com.bitdubai.fermat_api.layer.all_definition.enums.ServiceStatus;
import com.bitdubai.fermat_api.layer.all_definition.event.PlatformEvent;
import com.bitdubai.fermat_api.layer.pip_platform_service.event_manager.EventHandler;
import com.bitdubai.fermat_api.layer.pip_platform_service.event_manager.events.IncomingCryptoIdentifiedEvent;
import com.bitdubai.fermat_cry_plugin.layer.crypto_router.incoming_crypto.developer.bitdubai.version_1.exceptions.CantSaveEvent;
import com.bitdubai.fermat_cry_plugin.layer.crypto_router.incoming_crypto.developer.bitdubai.version_1.structure.IncomingCryptoEventRecorderService;

/**
 * Created by loui on 22/02/15.
 * Modified by Arturo Vallone 25/04/2015
 */
public class IncomingCryptoIdentifiedEventHandler implements EventHandler {
    /**
     * IncomingCryptoIdentifiedEventHandler member variables
     */
    IncomingCryptoEventRecorderService incomingCryptoEventRecorderService;


    /**
     * IncomingCryptoIdentifiedEventHandler member methods
     */
    public void setIncomingCryptoEventRecorderService(IncomingCryptoEventRecorderService incomingCryptoEventRecorderService){
        this.incomingCryptoEventRecorderService = incomingCryptoEventRecorderService;
    }


    /**
     * EventHandler interface implementation
     */
    @Override
    public void handleEvent(PlatformEvent platformEvent) throws Exception {

        if (this.incomingCryptoEventRecorderService.getStatus() == ServiceStatus.STARTED){

            try
            {
                this.incomingCryptoEventRecorderService.incomingCryptoIdentified((IncomingCryptoIdentifiedEvent) platformEvent);
            }
            catch (CantSaveEvent cantSaveEvent)
            {
                /**
                 * The main module could not handle this exception. Me neither. Will throw it again.
                 */
                System.err.println("CantSaveEvent: "+ cantSaveEvent.getMessage());
                cantSaveEvent.printStackTrace();

                throw  cantSaveEvent;
            }
            catch (ClassCastException classCastException)
            {
                /**
                 * The main module could not handle this exception. Me neither. Will throw it again.
                 */
                System.err.println("ClassCastException: "+ classCastException.getMessage());
                classCastException.printStackTrace();

                throw  new CantSaveEvent();
            }
        }
        else
        {
            throw new TransactionServiceNotStartedException();
        }
    }

}
