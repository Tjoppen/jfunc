package junit.extensions.jfunc;

import junit.framework.Test;
import junit.framework.TestListener;

public interface AssertListener extends TestListener {
    
    /**
     * An assert happened.
     **/
    public void addAssert(Test test, String msg, boolean condition);

}
