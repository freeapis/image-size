package net.freeapis.core;

import org.apache.commons.lang3.tuple.Pair;
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
        Pair<Integer,Integer> size = Image
                .sizeOf(new File(ClassLoader.getSystemResource("source.jpg").getFile()));
        System.out.println("width:" + size.getLeft() + " height:" + size.getRight());

        System.out.println(Image.sizeOf(new File("D:\\GraphicsMagic\\doc-convert\\bug.jpg")));
    }
}
