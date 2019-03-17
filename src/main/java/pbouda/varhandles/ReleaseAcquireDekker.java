package pbouda.varhandles;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;

/*

 Observed 	TC 1	    TC 2	    TC 3	    TC 4	    TC 5	    TC 6	Expectation
 0, 0	    18074216	8177036	    31235680	12147882	0	        0	    ACCEPTABLE
 0, 1	    25643615	30921384	28572001	28411882	29848387	308106	ACCEPTABLE
 1, 0	    34169115	37582696	34211131	43567817	36916047	962882	ACCEPTABLE
 1, 1	    42005	    28235	    3679	    490	        873777	    356653	ACCEPTABLE


 */
@JCStressTest
@Outcome(id = "1, 1", expect = ACCEPTABLE)
@Outcome(id = "1, 0", expect = ACCEPTABLE)
@Outcome(id = "0, 1", expect = ACCEPTABLE)
@Outcome(id = "0, 0", expect = ACCEPTABLE)
@State
public class ReleaseAcquireDekker {

    volatile int x = 0;
    volatile int y = 0;

    private static final VarHandle X;
    private static final VarHandle Y;

    static {
        try {
            X = MethodHandles.lookup()
                    .findVarHandle(ReleaseAcquireDekker.class, "x", int.class);

            Y = MethodHandles.lookup()
                    .findVarHandle(ReleaseAcquireDekker.class, "y", int.class);


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