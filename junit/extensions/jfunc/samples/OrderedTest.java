package junit.extensions.jfunc.samples;

import junit.extensions.jfunc.*;
import junit.framework.*;

public class OrderedTest extends JFuncTestCase {

    int i = 0;
    public OrderedTest() {
    }

    public void firstTest() {
        assert(++i == 1);
    }

    public void secondTest() {
        assert(++i == 2);
    }

    public void thirdTest() {
        assert(++i == 3);
    }

    public static Test suite() throws Exception {
        JFuncSuite suite = new JFuncSuite();
        OrderedTest test = new OrderedTest();
        suite.oneInstancePerTest(false);
        test = (OrderedTest) suite.getTestProxy(test);
        test.firstTest();
        test.secondTest();
        test.thirdTest();
        return suite;
    }
}
                             
