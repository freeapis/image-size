package net.freeapis.core;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() throws Exception
    {
        System.out.println(Image.sizeOf(new File(ClassLoader.getSystemResource("source.jpg").getFile())));
    }
}
