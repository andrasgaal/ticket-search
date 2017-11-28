package com.asg.ticket.wizz.process;

public interface Processor<I> {

    void process(I input);
}
