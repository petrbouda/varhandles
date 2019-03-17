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

/*

 Observed   TC 1	    TC 2	    TC 3	    TC 4	    TC 5	    TC 6	    Expectation     Interpretation
 0, 0	    28123063	119711992	12133189	68269222	128061883	1611275	    ACCEPTABLE	    Dinner is not ready
 1, 1	    172962878	78890029	134316642	100933159	10142398	1667606	    ACCEPTABLE	    Dinner is ready

 => There is no occurrence that READY has been already set and DINNER is not seen!

*/
@JCStressTest
@Outcome(id = "0, 0", expect = ACCEPTABLE, desc = "Dinner is not ready")
@Outcome(id = "1, 1", expect = ACCEPTABLE, desc = "Dinner is ready")
@Outcome(id = "1, 0", expect = FORBIDDEN, desc = "Dinner is supposed to be ready :(")
@State
public class ReleaseAcquireDinner {

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
