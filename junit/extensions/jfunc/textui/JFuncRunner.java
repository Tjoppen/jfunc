package junit.extensions.jfunc.textui;

import java.lang.reflect.*;
import java.text.NumberFormat;
import java.util.*;
import java.io.PrintStream;

import junit.framework.*;
import junit.runner.*;
import junit.extensions.jfunc.*;
import junit.extensions.jfunc.util.ColorWriter;
import junit.extensions.jfunc.runner.BaseTestRunner;


/**
 * A command line based tool to run tests.
 * <pre>
 * java junit.textui.TestRunner [-wait] TestCaseClass
 * </pre>
 * TestRunner expects the name of a TestCase class as argument.
 * If this class defines a static <code>suite</code> method it 
 * will be invoked and the returned test is run. Otherwise all 
 * the methods starting with "test" having no arguments are run.
 * <p>
 * When the wait command line argument is given TestRunner
 * waits until the users types RETURN.
 * <p>
 * TestRunner prints a trace as the tests are executed followed by a
 * summary at the end. 
 */
public class JFuncRunner extends BaseTestRunner {
    PrintStream fWriter= System.out;
    private boolean verbose = false;
    /**
     * Constructs a TestRunner.
     */
    public JFuncRunner() {
    }
    /**
     * Constructs a TestRunner using the given stream for all the output
     */
    public JFuncRunner(PrintStream writer) {
        this();
        if (writer == null)
            throw new IllegalArgumentException("Writer can't be null");
        fWriter= writer;
    }
        
    /**
     * Always use the StandardTestSuiteLoader. Overridden from
     * BaseTestRunner.
     */
    public TestSuiteLoader getLoader() {
        return new StandardTestSuiteLoader();
    }

                        
    /**
     * Creates the TestResult to be used for the test run.
     */
    protected TestResult createTestResult() {
        return new JFuncResult();
    }
        
    public TestResult doRun(Test suite, boolean wait) {
        TestResult result= createTestResult();
        if (verbose) 
            result.addListener(new VerboseListener());
        else
            result.addListener(new StatusListener());
        long startTime= System.currentTimeMillis();
        suite.run(result);
        long endTime= System.currentTimeMillis();
        long runTime= endTime-startTime;
        writer().println();
        writer().println("Time: "+elapsedTimeAsString(runTime));
        print(result);

        writer().println();

        pause(wait);
        return result;
    }

    protected void pause(boolean wait) {
        if (wait) {
            writer().println("<RETURN> to continue");
            try {
                System.in.read();
            }
            catch(Exception e) {
            }
        }
    }

    // <AssertListener> 

    class StatusListener implements AssertListener {
        int fColumn= 0;

        public synchronized void startTest(Test test) {
            writer().print(".");
            if (fColumn++ >= 40) {
                writer().println();
                fColumn= 0;
            }
        }

        public void endTest(Test test) {
        }

        public void addAssert(Test test, String msg, boolean condition) {
            //System.err.println(msg + condition);
        }

        public synchronized void addError(Test test, Throwable t) {
            writer().print("E");
        }
        
        public synchronized void addFailure(Test test, AssertionFailedError t) {
            writer().print("F");
        }

    }

    class VerboseListener implements AssertListener {
        ColorWriter out;

        public VerboseListener() {
            out = new ColorWriter(System.out);
        }

        public void startTest(Test test) {
            out.println(test.toString() + ": ");
        }

        public void endTest(Test test) {
            //out.println("end: " + test);
        }

        public void addError(Test test, Throwable t) {
            //status(ColorWriter.BLACK, ColorWriter.YELLOW, "ERROR");
            status(ColorWriter.YELLOW, "ERROR", t.toString());
            //out.println("error: " + test + " " + t);
        }

        public void addFailure(Test test, AssertionFailedError t) {
            status(ColorWriter.RED, "FAILED", t.getMessage());
            //out.println("failure: " + test + " " + t);
        }
        
        public void addAssert(Test test, String msg, boolean condition) {
            int color;
            String passorfail;
            if (!condition) {
                // if failed
                return;
            }
            if (condition) {
                color = ColorWriter.GREEN;
                passorfail = "PASSED";
            } else {
                color = ColorWriter.RED;
                passorfail = "FAILED";
            }
            status(color, passorfail, msg);
            //out.println("assert: " + test + " " + msg + ": " + condition);
        }

        private void status(int color, String coloredMsg, String msg ) {
            out.print("  [ ");
            out.setColor(color);
            out.print(coloredMsg);
            out.setColor(ColorWriter.DEFAULT);
            out.print(" " + msg);
            out.println(" ]");
        }
        
    }
    // </AssertListener> 

        
    public static void main(String args[]) {
        JFuncRunner aTestRunner= new JFuncRunner();
        try {
            TestResult r= aTestRunner.start(args);
            if (!r.wasSuccessful()) 
                System.exit(-1);
            System.exit(0);
        } catch(Exception e) {
            System.err.println(e.getMessage());
            System.exit(-2);
        }
    }
    /**
     * Prints failures to the standard output
     */
    public synchronized void print(TestResult result) {
        printErrors(result);
        printFailures(result);
        printHeader(result);
    }
    /**
     * Prints the errors to the standard output
     */
    public void printErrors(TestResult result) {
        if (result.errorCount() != 0) {
            if (result.errorCount() == 1)
                writer().println("There was "+result.errorCount()+" error:");
            else
                writer().println("There were "+result.errorCount()+" errors:");

            int i= 1;
            for (Enumeration e= result.errors(); e.hasMoreElements(); i++) {
                TestFailure failure= (TestFailure)e.nextElement();
                writer().println(i+") "+failure.failedTest() + " ");
                writer().print(getFilteredTrace(failure.thrownException()));
            }
        }
    }
    /**
     * Prints failures to the standard output
     */
    public void printFailures(TestResult result) {
        if (result.failureCount() != 0) {
            if (result.failureCount() == 1)
                writer().println("There was " + result.failureCount() + " failure:");
            else
                writer().println("There were " + result.failureCount() + " failures:");
            int i = 1;
            for (Enumeration e= result.failures(); e.hasMoreElements(); i++) {
                TestFailure failure= (TestFailure) e.nextElement();
                writer().print(i + ") " + failure.failedTest() + " ");
                Throwable t= failure.thrownException();
                writer().print(getFilteredTrace(failure.thrownException(), true));
            }
        }
    }
    /**
     * Prints the header of the report
     */
    public void printHeader(TestResult result) {
        if (result.wasSuccessful()) {
            writer().println();
            writer().print("OK");
            writer().println (" (" + result.runCount() + " tests)");

        } else {
            writer().println();
            writer().println("FAILURES!!!");
            writer().println("Tests run: "+result.runCount()+ 
                             ",  Failures: "+result.failureCount()+
                             ",  Errors: "+result.errorCount());
        }
    }
    /**
     * Runs a suite extracted from a TestCase subclass.
     */
    static public void run(Class testClass) {
        run(new TestSuite(testClass));
    }
    /**
     * Runs a single test and collects its results.
     * This method can be used to start a test run
     * from your program.
     * <pre>
     * public static void main (String[] args) {
     *     test.textui.TestRunner.run(suite());
     * }
     * </pre>
     */
    static public void run(Test suite) {
        JFuncRunner aTestRunner= new JFuncRunner();
        aTestRunner.doRun(suite, false);
    }
    /**
     * Runs a single test and waits until the user
     * types RETURN.
     */
    static public void runAndWait(Test suite) {
        JFuncRunner aTestRunner= new JFuncRunner();
        aTestRunner.doRun(suite, true);
    }
    /**
     * Starts a test run. Analyzes the command line arguments
     * and runs the given test suite.
     */
    protected TestResult start(String args[]) throws Exception {
        String testCase= null;
        boolean wait= false;
        List testArgs = new ArrayList();
        for (int i= 0; i < args.length; i++) {
            if (testCase != null) {
                testArgs.add(args[i]);
                continue;
            }
            if (args[i].equals("-wait"))
                wait= true;
            else if (args[i].equals("-c")) 
                testCase= extractClassName(args[++i]);
            else if (args[i].equals("-version"))
                System.err.println("JUnit "+Version.id()+" by Kent Beck and Erich Gamma (modified by Shane Celis)");
            else if (args[i].equals("-v")) 
                verbose = true;
            else
                testCase= args[i];
        }
                
        if (testCase.equals("")) 
            throw new Exception("usage: jfuncrunner [-wait] testCaseName [args]");

        try {
            Test suite= getTest(testCase, 
                                (String[]) testArgs.toArray(new String[0]));
            return doRun(suite, wait);
        }
        catch(Exception e) {
            throw new Exception("Could not create and run test suite: "+e);
        }
    }
                
    protected void runFailed(String message) {
        System.err.println(message);
        System.exit(1);
    }
                
    protected PrintStream writer() {
        return fWriter;
    }
}
