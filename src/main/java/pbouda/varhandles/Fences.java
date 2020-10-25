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
            //  Ensures that loads before the fence will not be reordered with loads and
            //  stores after the fence; a "LoadLoad plus LoadStore barrier".
            //
            //  Corresponds to C11 atomic_thread_fence(memory_order_acquire)
            //  (an "acquire fence").
            //
            //  A pure LoadLoad fence is not provided, since the addition of LoadStore
            //  is almost always desired, and most current hardware instructions that
            //  provide a LoadLoad barrier also provide a LoadStore barrier for free.
            //  @since 1.8
            //
            VarHandle.storeStoreFence();
            y = 1;
        }

        @Actor
        public void actor2(I_Result r) {
            while (y != 1) {
                //  Ensures that loads and stores before the fence will not be reordered with
                //  stores after the fence; a "StoreStore plus LoadStore barrier".
                //
                //  Corresponds to C11 atomic_thread_fence(memory_order_release)
                //  (a "release fence").
                //
                //  A pure StoreStore fence is not provided, since the addition of LoadStore
                //  is almost always desired, and most current hardware instructions that
                //  provide a StoreStore barrier also provide a LoadStore barrier for free.
                //  @since 1.8
                //
                VarHandle.loadLoadFence();
            }
            r.r1 = x;
        }
    }
}
