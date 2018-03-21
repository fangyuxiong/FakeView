package xfy.fakeview.library;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void testReg() throws Exception {
        final String RICH_REG = "(?<=\\([Ff][Oo][Nn][Tt])[\\s\\S]*?(?=\\(/[Ff][Oo][Nn][Tt]\\))";
        final String TEXT = "h1(font size='30px' color='black' weight='600')今天天气真好，晚上来家坐坐呀(/font)h2(font size='26px' color='red' weight='400')@王先生(/font)h3";
        Matcher matcher = Pattern.compile(RICH_REG).matcher(TEXT);
        if (matcher.find())
            Assert.assertEquals(" size='30px' color='black' weight='600')今天天气真好，晚上来家坐坐呀", matcher.group());
        while (matcher.find()) {
            log(matcher.group());
        }
    }

    private static void log(String log) {
        log("test", log);
    }

    private static void log(String tag, String log) {
        System.out.println(tag + ": " + log);
    }
}