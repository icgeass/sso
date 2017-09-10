import com.zeroq6.sso.web.client.security.AesCrypt;
import com.zeroq6.sso.web.client.security.Base64;
import com.zeroq6.sso.web.client.utils.SsoUtils;
import org.junit.Test;

import java.util.Date;

/**
 * Created by icgeass on 2017/4/19.
 */
public class NoSpringTest {

    @Test
    public void test(){
        System.out.println("1111");
    }

    @Test
    public void testDecrypt(){
        String encrypt = "ardjqbW1cbZHZNFYZ9m7XDsGtdaF9dMh0pZF65amUYlbexMTSL5novpTJqSDpcy2";
        String cipher = "8W0WABRNDQYQYD4EVEMUKVYBFBF4JMRJP3I7C89XR4KI9UK79F1GOM0BHN94VPLM";
        AesCrypt aesCrypt = new AesCrypt(cipher, 256);
        String result = aesCrypt.decrypt(Base64.getUrlDecoder().decode(encrypt));
        System.out.println(encrypt);



    }
}
