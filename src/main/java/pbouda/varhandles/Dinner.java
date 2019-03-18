package pbouda.varhandles;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import static org.openjdk.jcstress.annotations.Expect.*;

public class Dinner {

    /*
        Observed	TC 1	TC 2	    TC 3	    TC 4	    TC 5	    TC 6	    Expectation
        0, 0	    86627169	10174832	60385645	8218930	    18600670	796289	ACCEPTABLE
        1, 0	    0	        5246	    0	        33	        0	        0	    ACCEPTABLE
        1, 1	    90854582	69954943	150792446	132450958	67653251	2229422	ACCEPTABLE
     */
    @JCStressTest
    @Outcome(id = "0, 0", expect = ACCEPTABLE, desc = "Dinner is not ready")
    @Outcome(id = "1, 1", expect = ACCEPTABLE, desc = "Dinner is ready")
    @Outcome(id = "1, 0", expect = ACCEPTABLE, desc = "Dinner is supposed to be ready :(")
    @State
    public static class PlainDinner {

        volatile int ready = 0;
        int dinner = 0;

        private static final VarHandle READY;

        static {
            try {
                READY = MethodHandles.lookup()
                        .findVarHandle(PlainDinner.class, "ready", int.class);


            } catch (ReflectiveOperationException roe) {
                throw new RuntimeException("Something Wrong With Our VarHandle", roe);
            }
        }

        @Actor
        public void guest(II_Result r) {
            int ready = (int) READY.get(this);
            if (ready == 1) {
                r.r1 = ready;
                r.r2 = dinner;
            }
        }

        @Actor
        public void chef() {
            dinner = 1;
            READY.set(this, 1);
        }
    }

    /*
        Observed    TC 1	    TC 2	    TC 3	    TC 4	    TC 5	    TC 6	    Expectation
        0, 0	    66424115	25849026	68389825	21972194	98288928	932099	    ACCEPTABLE
        1, 1	    141911076	109810655	126336226	90093517	31949403	1414402	    ACCEPTABLE
     */
    @JCStressTest
    @Outcome(id = "0, 0", expect = ACCEPTABLE, desc = "Dinner is not ready")
    @Outcome(id = "1, 1", expect = ACCEPTABLE, desc = "Dinner is ready")
    @Outcome(id = "1, 0", expect = ACCEPTABLE_INTERESTING, desc = "Dinner is supposed to be ready :(")
    @State
    public static class OpaqueDinner {

        volatile int ready = 0;
        int dinner = 0;

        private static final VarHandle READY;

        static {
            try {
                READY = MethodHandles.lookup()
                        .findVarHandle(OpaqueDinner.class, "ready", int.class);


            } catch (ReflectiveOperationException roe) {
                throw new RuntimeException("Something Wrong With Our VarHandle", roe);
            }
        }

        @Actor
        public void guest(II_Result r) {
            int ready = (int) READY.getOpaque(this);
            if (ready == 1) {
                r.r1 = ready;
                r.r2 = dinner;
            }
        }

        @Actor
        public void chef() {
            dinner = 1;
            READY.setOpaque(this, 1);
        }
    }

    /*
        Observed	TC 1	    TC 2	    TC 3	    TC 4	    TC 5	    TC 6	    Expectation
        0, 0	    37941121	14343253	96372753	12966321	80334902	1120215	    ACCEPTABLE
        1, 1	    154367070	99374808	115519098	120522840	23361649	1076626	    ACCEPTABLE
     */
    @JCStressTest
    @Outcome(id = "0, 0", expect = ACCEPTABLE, desc = "Dinner is not ready")
    @Outcome(id = "1, 1", expect = ACCEPTABLE, desc = "Dinner is ready")
    @Outcome(id = "1, 0", expect = FORBIDDEN, desc = "Dinner is supposed to be ready :(")
    @State
    public static class ReleaseAcquireDinner {

        volatile int ready = 0;
        int dinner = 0;

        private static final VarHandle READY;

        static {
            try {
                READY = MethodHandles.lookup()
                        .findVarHandle(ReleaseAcquireDinner.class, "ready", int.class);


            } catch (ReflectiveOperationException roe) {
                throw new RuntimeException("Something Wrong With Our VarHandle", roe);
            }
        }

        @Actor
        public void guest(II_Result r) {
            int ready = (int) READY.getAcquire(this);
            if (ready == 1) {
                r.r1 = ready;
                r.r2 = dinner;
            }
        }

        @Actor
        public void chef() {
            dinner = 1;
            READY.setRelease(this, 1);
        }
    }

    /*
        Observed    TC 1	    TC 2	    TC 3	    TC 4	    TC 5	    TC 6	    Expectation
        0, 0	    209338439	160741652	219475779	64961732	62238203	1496097	    ACCEPTABLE
        1, 1	    4059282	    10800779	3859942	    15222499	20453198	848724	    ACCEPTABLE
     */
    @JCStressTest
    @Outcome(id = "0, 0", expect = ACCEPTABLE, desc = "Dinner is not ready")
    @Outcome(id = "1, 1", expect = ACCEPTABLE, desc = "Dinner is ready")
    @Outcome(id = "1, 0", expect = FORBIDDEN, desc = "Dinner is supposed to be ready :(")
    @State
    public static class VolatileDinner {

        volatile int ready = 0;
        int dinner = 0;

        private static final VarHandle READY;

        static {
            try {
                READY = MethodHandles.lookup()
                        .findVarHandle(VolatileDinner.class, "ready", int.class);


            } catch (ReflectiveOperationException roe) {
                throw new RuntimeException("Something Wrong With Our VarHandle", roe);
            }
        }

        @Actor
        public void guest(II_Result r) {
            int ready = (int) READY.getVolatile(this);
            if (ready == 1) {
                r.r1 = ready;
                r.r2 = dinner;
            }
        }

        @Actor
        public void chef() {
            dinner = 1;
            READY.setVolatile(this, 1);
        }
    }
}
