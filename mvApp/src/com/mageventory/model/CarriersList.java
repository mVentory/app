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

package com.mageventory.model;

import java.io.Serializable;
import java.util.List;

import com.mageventory.MageventoryConstants;

public class CarriersList implements MageventoryConstants, Serializable {

    public String mLastUsedCarrier;

    public List<String> mCarriersList;

}
