package com.brentcroft.gtd.utilities;

import org.junit.Test;

/**
 * Created by adobson on 28/06/2016.
 */
public class TestReplace
{
    @Test
    public void test()
    {
        System.out.println(
        "<br cd=\"12asdasd\" fg=\"123\">"
            .replaceAll(
                    "<br((?:\\s+\\w+=\"[^\"]*\")*)\\s*>",
                    "<br $1/>" )
        );

        System.out.println(
                "<br>"
                        .replaceAll(
                                "<br((?:\\s+\\w+=\"[^\"]*\")*)\\s*>",
                                "<br $1/>" )
        );
    }

}
