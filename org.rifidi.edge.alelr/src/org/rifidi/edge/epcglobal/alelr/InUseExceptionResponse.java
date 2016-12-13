
package org.rifidi.edge.epcglobal.alelr;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 3.1.4
 * 2015-12-12T10:17:43.072-05:00
 * Generated source version: 3.1.4
 */

@WebFault(name = "InUseException", targetNamespace = "urn:epcglobal:alelr:wsdl:1")
public class InUseExceptionResponse extends Exception {
    
    private org.rifidi.edge.epcglobal.alelr.InUseException inUseException;

    public InUseExceptionResponse() {
        super();
    }
    
    public InUseExceptionResponse(String message) {
        super(message);
    }
    
    public InUseExceptionResponse(String message, Throwable cause) {
        super(message, cause);
    }

    public InUseExceptionResponse(String message, org.rifidi.edge.epcglobal.alelr.InUseException inUseException) {
        super(message);
        this.inUseException = inUseException;
    }

    public InUseExceptionResponse(String message, org.rifidi.edge.epcglobal.alelr.InUseException inUseException, Throwable cause) {
        super(message, cause);
        this.inUseException = inUseException;
    }

    public org.rifidi.edge.epcglobal.alelr.InUseException getFaultInfo() {
        return this.inUseException;
    }
}