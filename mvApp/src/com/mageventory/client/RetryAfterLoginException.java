/* Copyright (c) 2014 mVentory Ltd. (http://mventory.com)
 * 
* License       http://creativecommons.org/licenses/by-nc-nd/4.0/
* 
* NonCommercial � You may not use the material for commercial purposes. 
* NoDerivatives � If you compile, transform, or build upon the material,
* you may not distribute the modified material. 
* Attribution � You must give appropriate credit, provide a link to the license,
* and indicate if changes were made. You may do so in any reasonable manner, 
* but not in any way that suggests the licensor endorses you or your use. 
*/

package com.mageventory.client;

public class RetryAfterLoginException extends RuntimeException {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public RetryAfterLoginException() {
        super();
    }

    public RetryAfterLoginException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RetryAfterLoginException(String detailMessage) {
        super(detailMessage);
    }

    public RetryAfterLoginException(Throwable throwable) {
        super(throwable);
    }

}
