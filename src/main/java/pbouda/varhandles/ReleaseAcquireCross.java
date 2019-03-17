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

public class ReleaseAcquireCross {


    /*

     Observed	TC 1	    TC 2	    TC 3	    TC 4	    TC 5	    TC 6	Expectation
     0, 0	    19615965	18735786	7024334	    10332977	136755	    12876	ACCEPTABLE
     0, 1	    40463436	30927745	49286768	40137598	41579632	989574	ACCEPTABLE
     1, 0	    62363900	59819926	52920649	49440063	60507243	1331892	ACCEPTABLE
     1, 1	    1020	    1304	    30	        83	        106261	    5319	ACCEPTABLE

     => [0, 0] - Violates Sequential Consistency!
    */
    @JCStressTest
    @Outcome(id = "1, 1", expect = ACCEPTABLE)
    @Outcome(id = "1, 0", expect = ACCEPTABLE)
    @Outcome(id = "0, 1", expect = ACCEPTABLE)
    @Outcome(id = "0, 0", expect = ACCEPTABLE)
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
            X.setRelease(this, 1);
            r.r2 = (int) Y.getAcquire(this);
        }

        @Actor
        public void actor2(II_Result r) {
            Y.setRelease(this, 1);
            r.r1 = (int) X.getAcquire(this);
        }
    }

    /*

     Observed	TC 1	    TC 2	    TC 3	    TC 4	    TC 5	    TC 6	    Expectation
     0, 0	    7305490 	9842654	    4766842	    2860500	    44771304	383802	    ACCEPTABLE
     0, 1	    159968	    399434  	111109	    51778	    636701	    197610	    ACCEPTABLE
     1, 1	    131890603	132865013	143404990	138931213	69634996	1645009	    ACCEPTABLE

     => COHERENCY is visible
            - there are results with 1s
            - but we can also see [0, 0] which means there is no TOTAL ORDER
            - result just weren't propagated yet (EVENTUAL CONSISTENCY)
     => CAUSALITY is applied only for ordinary READS/WRITES
            - if ATOMIC WRITE is available then even all ORDINARY WRITES before are available
    */
    @JCStressTest
    @Outcome(id = "1, 1", expect = ACCEPTABLE)
    @Outcome(id = "1, 0", expect = FORBIDDEN)
    @Outcome(id = "0, 1", expect = ACCEPTABLE)
    @Outcome(id = "0, 0", expect = ACCEPTABLE)
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
            X.setRelease(this, 1);
        }

        @Actor
        public void actor2(II_Result r) {
            r.r1 = (int) X.getAcquire(this);
            r.r2 = y;
        }
    }
}