import org.junit.Test;
import sdk.BitcoinOffLineSDK;

import java.util.List;

public class MnemonicTest {

    @Test
    public void generateMnemonicCode(){
        BitcoinOffLineSDK.MNEMONIC.generateMnemonicCode();
    }


    @Test
    public void generateSeed(){
        List<String> mnemonic=BitcoinOffLineSDK.MNEMONIC.generateMnemonicCode();
        BitcoinOffLineSDK.MNEMONIC.generateSeed(mnemonic,"123456");
    }

}
