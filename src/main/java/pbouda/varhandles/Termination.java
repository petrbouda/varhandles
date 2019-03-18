package pbouda.varhandles;

import org.openjdk.jcstress.annotations.*;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class Termination {

    /*

        Observed    TC 1	TC 2	TC 3	TC 4	TC 5	TC 6	Expectation	            Interpretation
        STALE	    1	    1	    1  	    1	    0 	    0	    ACCEPTABLE_INTERESTING	Test hung up
        TERMINATED	4	    4	    5	    5	    12060	12564	ACCEPTABLE

    */
    @JCStressTest(Mode.Termination)
    @Outcome(id = "TERMINATED", expect = Expect.ACCEPTABLE, desc = "Signal propagated")
    @Outcome(id = "STALE", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Test hung up")
    @State
    public static class PlainTermination {

        volatile int x = 0;

        private static final VarHandle X;

        static {
            try {
                X = MethodHandles.lookup()
                        .findVarHandle(PlainTermination.class, "x", int.class);
            } catch (ReflectiveOperationException roe) {
                throw new RuntimeException("Something Wrong With Our VarHandle", roe);
            }
        }

        @Actor
        public void actor1() {
            while ((int) X.get(this) == 0) {
                // spin
            }
        }

        @Signal
        public void signal() {
             X.set(this, 1);
        }
    }

    /*

        Observed 	TC 1	TC 2	TC 3	TC 4	TC 5	TC 6	Expectation
        TERMINATED	12188	12867	12511	12153	12827	12746	ACCEPTABLE

    */
    @JCStressTest(Mode.Termination)
    @Outcome(id = "TERMINATED", expect = Expect.ACCEPTABLE, desc = "Signal propagated")
    @Outcome(id = "STALE", expect = Expect.FORBIDDEN, desc = "Test hung up")
    @State
    public static class OpaqueTermination {

        volatile int x = 0;

        private static final VarHandle X;

        static {
            try {
                X = MethodHandles.lookup()
                        .findVarHandle(OpaqueTermination.class, "x", int.class);
            } catch (ReflectiveOperationException roe) {
                throw new RuntimeException("Something Wrong With Our VarHandle", roe);
            }
        }

        @Actor
        public void actor1() {
            while ((int) X.getOpaque(this) == 0) {
                // spin
            }
        }

        @Signal
        public void signal() {
            X.setOpaque(this, 1);
        }
    }

    /*

        Observed    TC 1	TC 2	TC 3	TC 4	TC 5	TC 6	Expectation
        TERMINATED	12911	12823	11958	12542	13050	11989	ACCEPTABLE

    */
    @JCStressTest(Mode.Termination)
    @Outcome(id = "TERMINATED", expect = Expect.ACCEPTABLE, desc = "Signal propagated")
    @Outcome(id = "STALE", expect = Expect.FORBIDDEN, desc = "Test hung up")
    @State
    public static class ReleaseAcquireTermination {

        volatile int x = 0;

        private static final VarHandle X;

        static {
            try {
                X = MethodHandles.lookup()
                        .findVarHandle(ReleaseAcquireTermination.class, "x", int.class);
            } catch (ReflectiveOperationException roe) {
                throw new RuntimeException("Something Wrong With Our VarHandle", roe);
            }
        }

        @Actor
        public void actor1() {
            while ((int) X.getAcquire(this) == 0) {
                // spin
            }
        }

        @Signal
        public void signal() {
            X.setRelease(this, 1);
        }
    }
}
