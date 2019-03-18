package pbouda.varhandles;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

public class VolatileCross {


    /*

     Observed	    TC 1	    TC 2	    TC 3	    TC 4	    TC 5	    TC 6	    Expectation
     0, 1	        18713063	35909611	64674743	34016730	30830492	1236050	    ACCEPTABLE
     1, 0	        43935390	41614007	46674945	34645648	45118566	879028	    ACCEPTABLE
     1, 1	        277718	    553633	    427893	    321553	    564153	    239703	    ACCEPTABLE

     => [0, 0] - Violates Sequential Consistency!
    */
    @JCStressTest
    @Outcome(id = "1, 1", expect = ACCEPTABLE)
    @Outcome(id = "1, 0", expect = ACCEPTABLE)
    @Outcome(id = "0, 1", expect = ACCEPTABLE)
    @State
    public static class BothAtomicCross {

        volatile int x = 0;
        volatile int y = 0;

        private static final VarHandle X;
        private static final VarHandle Y;

        static {
            try {
                X = MethodHandles.lookup()
                        .findVarHandle(BothAtomicCross.class, "x", int.class);

                Y = MethodHandles.lookup()
                        .findVarHandle(BothAtomicCross.class, "y", int.class);

            } catch (ReflectiveOperationException roe) {
                throw new RuntimeException("Something Wrong With Our VarHandle", roe);
            }
        }

        @Actor
        public void actor1(II_Result r) {
            X.setVolatile(this, 1);
            r.r2 = (int) Y.getVolatile(this);
        }

        @Actor
        public void actor2(II_Result r) {
            Y.setVolatile(this, 1);
            r.r1 = (int) X.getVolatile(this);
        }
    }

    /*

        Observed	TC 1	    TC 2	    TC 3	    TC 4	    TC 5	    TC 6	    Expectation
        0, 0	    129420036	113640565	198439588	109525537	69266555	559628	    ACCEPTABLE
        0, 1	    648864	    786680	    164301	    393237	    1065387	    553819	    ACCEPTABLE
        1, 1	    16039141	12272466	6094632	    23615717	40781409	2041374	    ACCEPTABLE

     => COHERENCY is visible
            - there are results with 1s
            - but we can also see [0, 0] which means there is no TOTAL ORDER
            - result just weren't propagated yet (EVENTUAL CONSISTENCY)
     => CAUSALITY is applied only for ordinary READS/WRITES
            - if ATOMIC WRITE is available then even all ORDINARY WRITES before are available
     => SEQUENTIAL CONSISTENCY
            - all operation are sequential consistent (immediately visible)
    */
    @JCStressTest
    @Outcome(id = "1, 1", expect = ACCEPTABLE)
    @Outcome(id = "1, 0", expect = FORBIDDEN)
    @Outcome(id = "0, 1", expect = ACCEPTABLE)
    @State
    public static class NonSynchronizedCross {

        volatile int x = 0;
        int y = 0;

        private static final VarHandle X;

        static {
            try {
                X = MethodHandles.lookup()
                        .findVarHandle(NonSynchronizedCross.class, "x", int.class);

            } catch (ReflectiveOperationException roe) {
                throw new RuntimeException("Something Wrong With Our VarHandle", roe);
            }
        }

        @Actor
        public void actor1() {
            y = 1;
            X.setVolatile(this, 1);
        }

        @Actor
        public void actor2(II_Result r) {
            r.r1 = (int) X.getVolatile(this);
            r.r2 = y;
        }
    }
}