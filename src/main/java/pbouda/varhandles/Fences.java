package pbouda.varhandles;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.I_Result;

import java.lang.invoke.VarHandle;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

public class Fences {

    @JCStressTest
    @Outcome(id = "1", expect = ACCEPTABLE)
    @Outcome(id = "0", expect = FORBIDDEN)
    @State
    public static class Reordering {

        int x = 0;
        int y = 0;

        @Actor
        public void actor1() {
            x = 1;
            VarHandle.storeStoreFence();
            y = 1;
        }

        @Actor
        public void actor2(I_Result r) {
            while (y != 1) {
                VarHandle.loadLoadFence();
            }
            r.r1 = x;
        }
    }
}
