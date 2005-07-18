/* 
* $Header$
* $Revision$
* $Date$
* ------------------------------------------------------------------------------------------------------
* 
* Copyright (c) SymphonySoft Limited. All rights reserved.
* http://www.symphonysoft.com
* 
* The software in this package is published under the terms of the BSD
* style license a copy of which has been included with this distribution in
* the LICENSE.txt file. 
*
*/
package org.mule.transformers.xml;

import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;
import org.mule.util.SgmlCodec;

/**
 * Encodes a string with SGML entities
 *
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class SgmlEntityEncoder extends AbstractTransformer
{
    public SgmlEntityEncoder()
    {
        registerSourceType(String.class);
        registerSourceType(byte[].class);
        setReturnClass(String.class);
    }

    public Object doTransform(Object src) throws TransformerException
    {
        if(src instanceof byte[]) {
            return SgmlCodec.encodeString(new String((byte[])src));
        } else {
            return SgmlCodec.encodeString(src.toString());
        }
    }
}
