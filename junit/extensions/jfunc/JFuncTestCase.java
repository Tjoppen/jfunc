package junit.extensions.jfunc;

import junit.framework.*;

import java.lang.reflect.*;

/**
 * JFuncTestCase works the same as TestCase for most purposes, but it
 * uses JFunc's assert classes which allow for other features like
 * multiple failures and verbose assertions.
 *
 * @see TestCase
 * @see JFuncResult
 * @see JFuncSuite 
 **/
// We would simply extend TestCase, but we want to use our own
// non-static Assert class
public abstract class JFuncTestCase extends VerboseAssert 
    implements Test, Cloneable {
    /**
     * the name of the test case
     */
    private String name;

    /**
     * No-arg constructor to enable serialization. This method
     * is not intended to be used by mere mortals.
     */
    public JFuncTestCase() {
        name = null;
        // XXX perhaps some fatal flag can be added here to prevent this
        // test from trying to run
    }
        
    /**
     * Constructs a test case with the given name.
     */
    public JFuncTestCase(String name) {
        this.name = name;
    }
        
    /**
     * Counts the number of test cases executed by run(TestResult result).
     */
    public int countTestCases() {
        return 1;
    }

    /**
     * Creates a default TestResult object
     *
     * @see TestResult
     */
    protected TestResult createResult() {
        //return new TestResult();
        return new JFuncResult();
    }

    /**
     * Gets the name of the test case.
     * @deprecated use getName()
     */
    public String name() {
        return name;
    }

    /**
     * A convenience method to run this test, collecting the results with a
     * default TestResult object.
     *
     * @see TestResult
     */
    public TestResult run() {
        TestResult result= createResult();
        run(result);
        return result;
    }

    /**
     * Runs the test case and collects the results in TestResult.
     */
    public void run(TestResult result) {
        // This isn't run when used with the TestletWrapper.
        // If it were, we couldn't have arguments to our code anyway

        // if we could extend junit.framework.TestCase this code would look like
        final JFuncTestCase test = this;
        //if (!isFatal()) 
        setResult(result);  // Assert needs this info to do non-fatal asserts

        result.startTest(test);
        Protectable p= new Protectable() {
                public void protect() throws Throwable {
                    test.runBare();
                }
            };
        result.runProtected(test, p);
        
        result.endTest(test);
        
        //result.run(this);
    }

    /**
     * Runs the bare test sequence.
     * @exception Throwable if any exception is thrown
     */
    public void runBare() throws Throwable {
        setUp();
        try {
            runTest();
        } finally {
            tearDown();
        }
    }
    /**
     * Override to run the test and assert its state.
     * @exception Throwable if any exception is thrown
     */
    protected void runTest() throws Throwable {
        Method runMethod= null;
        try {
            // use getMethod to get all public inherited
            // methods. getDeclaredMethods returns all
            // methods of this class but excludes the
            // inherited ones.
            runMethod = getClass().getMethod(name, null);
        } catch (NoSuchMethodException e) {
            fail("Method \"" + name + "\" not found");
        }
        if (!Modifier.isPublic(runMethod.getModifiers())) {
            fail("Method \"" + name + "\" should be public");
        }

        try {
            runMethod.invoke(this, new Class[0]);
        }
        catch (InvocationTargetException e) {
            e.fillInStackTrace();
            throw e.getTargetException();
        }
        catch (IllegalAccessException e) {
            e.fillInStackTrace();
            throw e;
        }
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
    }

    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called only once.  (Currently, this only works
     * as intended when used with JFuncSuite's oneTest option turned
     * on.)  
     **/
    protected void setUpOnce() throws Exception {
    }

    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is only called once.  (Currently, this only works
     * as intended when used with JFuncSuite's oneTest option turned
     * on.)
     */
    protected void tearDownOnce() throws Exception {
    }

    /**
     * Returns a string representation of the test case
     */
    public String toString() {
        //System.err.println("JFuncTestCase.toString()");
        return shortName(getClass()) + "." + name()+"()";
    }

    public static String shortName(Class cl) {
        String classname = cl.getName();
        return classname.substring(classname.lastIndexOf('.') + 1);
    }
    
    /**
     * Gets the name of a TestCase
     * @return returns a String (may be null in the case where proxies are used)
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of a TestCase
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

}
