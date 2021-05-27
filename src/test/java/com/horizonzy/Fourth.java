package com.horizonzy;

public class Fourth<A, B, C, D> {
    private A first;
    private B second;
    private C third;
    private D fourth;

    public Fourth(A first, B second, C third, D fourth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    public C getThird() {
        return third;
    }

    public D getFourth() {
        return fourth;
    }
}
