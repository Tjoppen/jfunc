package junit.extensions.jfunc;

import junit.framework.Test;
import junit.framework.TestListener;

/**
 * Extends the TestListener to provide more information regarding assertions.
 * 
 * @version $Revision: 1.2 $
 * @author Shane Celis 
 **/
public interface AssertListener extends TestListener {
    
    /**
     * An assert happened.
     **/
    public void addAssert(Test test, String msg, boolean condition);

}
