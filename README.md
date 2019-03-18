# VarHandles

- https://www.youtube.com/watch?v=w2zaqhFczjY
- http://gee.cs.oswego.edu/dl/html/j9mm.html
- https://docs.oracle.com/javase/9/docs/api/java/lang/invoke/VarHandle.html

```java
public class BasicDescription {

    /*
     * Mark VOLATILE the fields which are used by VarHandle
     * - it's something like default (strongest) mode if we forget to define weaker mode correctly
     *      => better than go with weaker mode => potential place for mistakes
     */
    volatile int x;

    /*
     * STATIC => One reference can be used for all instances
     * FINAL => Constant - helps JIT to reduce number of instructions
     *
     * It's a good practice to name VarHandle variable with the similar name or uppercase
     */
    private static final VarHandle X;

    static {
        try {
            X = MethodHandles.lookup()
                    .findVarHandle(BasicDescription.class, "x", int.class);

        } catch (ReflectiveOperationException roe) {
            throw new RuntimeException("Something Wrong With Our VarHandle", roe);
        }
    }

    void setOpaqueValue(int value) {
        X.setOpaque(this, value);
    }

    int getOpaqueValue(int value) {
        return (int) X.getOpaque(this);
    }
}
```

### Plain

```
    |===============================================================|
    | PLAIN                                                         |
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
    | No ordering and no certain visibility.                        |
    | Plain mode can skip, postpone, reorder some writes            |
    |===============================================================|
```

### Opaque

```
    |===============================================================|
    |---------------------------------------------------------------|
    | OPAQUE                                                        |
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
    | WRITES ARE EVENTUALLY VISIBLE (EVENTUAL CONSISTENCY)          |
    |                                                               |
    | EXAMPLE:                                                      |
    |                                                               |
    | Thread A                                                      |
    | --------------------                                          |
    | X.setOpaque(this, 1)                                          |
    |                                                               |
    | Thread B                                                      |
    | => So the compiler cannot hoist reads out of loops            |
    | (it knows the variable may be written by other threads)       |
    | ------------------------------                                |
    | while (X.getOpaque(this) != 1)                                |
    |                                                               |
    | Other thread will be spinning in loop but eventually be       |
    | terminated.                                                   |
    |                                                               |
    | ------------------------------------------------------------- |
    | If you need to guarantee ordering of writes across multiple   |
    | fields, this ordering mode is insufficient                    |
    | ------------------------------------------------------------- |
    |                                                               |
    | Acyclicity:                                                   |
    |                                                               |
    | If Opaque (or any stronger) mode is used for all accesses     |
    | to a variable, the antecedence relation is guaranteed to be   |
    | acyclic with respect to that variable.                        |
    |                                                               |
    | This rules out several anomalies described above with         |
    | Plain mode, including those in which future accesses appear   |
    | to impact the past, but only with respect to a single         | 
    | variable.                                                     |
    |                                                               |
    | Bitwise Atomicity:                                            |
    |                                                               |
    | If Opaque (or any stronger) mode is used for all accesses,    |
    | then reads of all types, including long and double, are       |
    | guaranteed not to mix the bits of multiple writes.            |
    |                                                               |
    | Reading a value in Opaque mode need not tell you anything     |
    | about values of any other variable. The name "opaque" stems   |
    | from the idea that shared variables need not be read or       |
    | written only by the current thread, so current values or      |
    | theiruses might not be known locally, requiring interaction   |
    | with memory systems. However, Opaque mode does not directly   |
    | impose any ordering constraints with respect to other         |
    | variables beyond Plain mode.                                  |
    |                                                               |
    | Collecting progress indicators issued by multiple threads,    |
    | it may be acceptable that results only eventually be accurate |
    | upon quiescence.                                              |
    |                                                               |
    | It is almost never a good idea to use bare spins waiting      |
    | for values of variables. Use Thread.onSpinWait, Thread.yield, |
    | and/or blocking synchronization to better cope with the fact  |
    | that "eventually" can be a long time, especially when there   |
    | are more threads than cores on a system.                      |    
    |===============================================================|
```
    
### Release / Acquire
    
```
    |===============================================================|
    | RELEASE/ACQUIRE                                               |
    |---------------------------------------------------------------|
    | VarHandle setRelease, getAcquire                              |
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
    |                                                               |
    | => It's not strong enough to guarantee sensible outomes for   |
    |    2 or more threads to write the same variable at the same   |
    |    time                                                       |
    |                                                               |
    | => OWNERSHIP -> only write can write but others can read      |
    |                                                               |
    | => After making an object accessible, the (previous) owner    |
    |    never uses it again. Automatic enforcement of this rule    |
    |    forms the basis of most differences between                |
    |    "message passing" vs "shared memory" approaches to         |
    |    concurrency.                                               |
    |                                                               |
    | class Dinner {                                                |
    |    int desc;                                                  |
    |    Dinner(int d) { desc = d; }                                |
    | }                                                             |
    |                                                               |
    | // Initially null, with VarHandle MEAL                        |
    | volatile Dinner meal;                                         |
    |                                                               |
    | Thread 1                                                      |
    | --------                                                      |
    | Dinner f = new Dinner(17);                                    |
    | MEAL.setRelease(f);                                           |
    |                                                               |
    | Thread 2                                                      |
    | --------                                                      |
    | Dinner m = MEAL.getAcquire();                                 |
    | if (m != null)                                                |
    |    int d = m.desc; // sees 17                                 |
    |                                                               |
    | The causality guarantee of RA mode is needed in producer      |
    | consumer designs, message-passing designs, unorder broadcast, |
    | and many others.                                              |
    |===============================================================|
```

### Volatile

- Difference between RA and Volatile 

```
    |===============================================================|
    | - This is sufficient for most concurrency. If a field is      |
    |   updated, the next read will read that value, but ordering   |
    |   between two such fields can be reordered.                   |
    |                                                               |
    | - If you see a new value from Thread A, then you will see     |
    |   even the writes made before in Program Order. However, it's |
    |   not sure you will see it immediately.                       |
    |       => EVENTIAL CONSISTENCY (COHERENCY)                     |
    |          but lack of SEQUENTIAL CONSISTENCY                   |
    |                                                               |
    | - Volatile ensures sequential consistency. All reads and      |
    |   writes are ordered.                                         |
    |===============================================================|
```


```
=> an Array with an effect of VOLATILE access
- atomic update is not supported

VarHandle arrayVarHandle = MethodHandles.arrayElementVarHandle(int[].class);
```
   