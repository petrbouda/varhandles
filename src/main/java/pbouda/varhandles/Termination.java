package pbouda.varhandles;

import org.openjdk.jcstress.annotations.*;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class Termination {

    /*
        |===============================================================|
        | PLAIN															|
        |---------------------------------------------------------------|
        | => No support for inter-thread communication                  |
        | => Does not tak into account any changes on particular        |
        | variable                                                      |
        |                                                               |
        | EXAMPLE:                                                      |
        | X.set(this, 1)                                                |
        | while (X.get(this) != 1)                                      |
        |                                                               |
        | This can be an infinite loop (and usually do) because they    |
        | don't require to notice that a write ever occured in another  |
        | thread if it wasn't seen on first encounter.                  |
        |                                                               |
        | That's because GET statement can be used as a local variable  |
        | which is not going to change anytime and just for the sake of |
        | compiler optimizations.                                       |
        |                                                               |
        | No ordering and no certain visibility.						|
        | Plain mode can skip, postpone, reorder some writes            |
        |===============================================================|

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
        |===============================================================|
        | OPAQUE														|
        |---------------------------------------------------------------|
        | Coherence:                                                    |
        |                                                               |
        | Visible overwrites to each variable are totaly ordered.       |
        | Per-variable overwrite order iss guaranteed to be consistent  |
        | to both (writes to later reads, reads to later writes)        |
        |                                                               |
        | If it's used for all accesses to a variable, updatess do not  |
        | appear out of order. This wouldn't be case if only the reads  |
        | (but not writes) were performed in Opaque mode.               |
        |                                                               |
        | Progress:                                                     |
        |                                                               |
        | WRITES ARE EVENTUALLY VISIBLE (EVENTUAL CONSISTENCY)	        |
        |                                                               |
        | EXAMPLE:                                                      |
        | X.setOpaque(this, 1)                                          |
        | while (X.getOpaque(this) != 1)                                |
        |                                                               |
        | Other thread will be spinning in loop but eventually be       |
        | terminated.                                                   |
        |===============================================================|

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
        |===============================================================|
        | RELEASE/ACQUIRE 												|
        |---------------------------------------------------------------|
        | VarHandle setRelease, getAcquire							    |
        |                                                               |
        | - Adds a causality constraint to Opaque mode                  |
        | - For each variable, the antecedence relation, restricted     |
        |	to interthread Release/Acquire (or stronger) accesses,      |
        |	is a partial order.                                         |
        |                                                               |
        | + If access A precedes interthread Release mode (or stronger) |
        | write W in source program order, then A precedes W in local   |
        | precedence order for thread T.                                |
        | + If interthread Acquire mode (or stronger) read R precedes   |
        | access A in source program order, then R precedes A in local  |
        | precedence order for Thread T.                                |
        |                                                               |
        | => This is the main idea behind causally consistent systems,  |
        | 	 including most distributed data stores.                    |
        |===============================================================|

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
