import org.junit.Test;
import sdk.BitcoinOffLineSDK;

import java.util.List;

public class MnemonicTest {

    /**
     * 测试生成助记词
     */
    @Test
    public void generateMnemonicCode(){
        BitcoinOffLineSDK.MNEMONIC.generateMnemonicCode();
    }


    /**
     * 测试生成种子
     */
    @Test
    public void generateSeed(){
        List<String> mnemonic=BitcoinOffLineSDK.MNEMONIC.generateMnemonicCode();
        BitcoinOffLineSDK.MNEMONIC.generateSeed(mnemonic,"123456");
    }

}
