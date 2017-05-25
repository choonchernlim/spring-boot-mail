package com.github.choonchernlim.springbootmail.core;

public final class Test {

    public static void main(String[] args) {
        final MailBean mailBean = MailBean.builder().build();
        System.out.println(mailBean);
    }
}
