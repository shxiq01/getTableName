package checkTest;

import com.sitech.csd.codecheck.sql.Check;
import org.junit.Test;

/**
 * Created by shixiaoqi on 2019/3/20.
 */
public class CheckTest {

    @Test
    public void check(){
        String[] args = new String[]{"/Users/shixiaoqi/IdeaProjects/ga-userinfo"};
        Check.main(args);
    }
    @Test
    public void aaa(){
        System.out.println("into ".indexOf("into"));
    }
}
