package org.fermat.fermat_dap_api.layer.dap_actor.asset_user.exceptions;

import org.fermat.fermat_dap_api.layer.all_definition.exceptions.DAPException;

/**
 * Created by lcampo on 26/11/15.
 */
public class CantDeleteAssetUserGroupException extends DAPException {

    private static final String DEFAULT_MESSAGE = "There was an error while trying to delete asset user group.";

    public CantDeleteAssetUserGroupException(Exception cause) {
        this(DEFAULT_MESSAGE, cause, null, null);
    }

    /**
     * This is the constructor that every inherited DAPException must implement
     *
     * @param message        the short description of the why this exception happened, there is a public static constant called DEFAULT_MESSAGE that can be used here
     * @param cause          the exception that triggered the throwing of the current exception, if there are no other exceptions to be declared here, the cause should be null
     * @param context        a String that provides the values of the variables that could have affected the exception
     * @param possibleReason an explicative reason of why we believe this exception was most likely thrown
     */
    public CantDeleteAssetUserGroupException(String message, Exception cause, String context, String possibleReason) {
        super(message, cause, context, possibleReason);
    }
}
