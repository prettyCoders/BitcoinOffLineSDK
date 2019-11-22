import com.google.common.collect.ImmutableList;
import entity.P2SHMultiSigAccount;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import sdk.BitcoinOffLineSDK;
import utils.Converter;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AddressTest {

    @BeforeEach
    public void setup() {
        BitcoinOffLineSDK.CONFIG.setNetworkParameters(TestNet3Params.get());
    }

    /**
     * 测试创建普通地址
     */
    @Test
    public void createNewAddress(){
        BitcoinOffLineSDK.CONFIG.setNetworkParameters(MainNetParams.get());
        List<String> mnemonicCode = Arrays.asList("weasel", "street", "dutch", "vintage", "legal", "network",
                "squirrel", "sort", "stereo", "drum", "trumpet", "farm");
        String passphrase = "your passphrase";
        byte[] seed=BitcoinOffLineSDK.MNEMONIC.generateSeed(mnemonicCode,passphrase);

        ECKey ecKey=BitcoinOffLineSDK.ADDRESS.getECKey(seed,0,0);
        String address=BitcoinOffLineSDK.ADDRESS.getLegacyAddress(ecKey);
        assertThat(address, is("1jWa6a9y4eoVJtGSH5bMo2krPq2mXkJtz"));

        ecKey=BitcoinOffLineSDK.ADDRESS.getECKey(seed,1,0);
        address=BitcoinOffLineSDK.ADDRESS.getLegacyAddress(ecKey);
        assertThat(address, is("141tHfGH61XUgMqU6SuqoS8osW1sPeCZ96"));

        ecKey=BitcoinOffLineSDK.ADDRESS.getECKey(seed,1,1);
        address=BitcoinOffLineSDK.ADDRESS.getLegacyAddress(ecKey);
        assertThat(address, is("1Kwk9VW5XbvmCYy74c4Fja6KugRfHSDTa1"));

        ecKey=BitcoinOffLineSDK.ADDRESS.getECKey(seed,1,10);
        address=BitcoinOffLineSDK.ADDRESS.getLegacyAddress(ecKey);
        assertThat(address, is("1853DuTD8QPKrAyvEqKZZ4ryJ5JNbAnZwf"));
    }

    /**
     * 测试钱包可导入格式私钥的使用
     */
    @Test
    public void testWIF() {
        BitcoinOffLineSDK.CONFIG.setNetworkParameters(MainNetParams.get());
        List<String> mnemonicCode = Arrays.asList("weasel", "street", "dutch", "vintage", "legal", "network",
                "squirrel", "sort", "stereo", "drum", "trumpet", "farm");
        String passphrase = "your passphrase";
        byte[] seed=BitcoinOffLineSDK.MNEMONIC.generateSeed(mnemonicCode,passphrase);

        ECKey ecKey=BitcoinOffLineSDK.ADDRESS.getECKey(seed,1,10);

        String address=BitcoinOffLineSDK.ADDRESS.getLegacyAddress(ecKey);

        String privateKeyAsHex = BitcoinOffLineSDK.ADDRESS.getPrivateKeyAsHex(ecKey);
        String publicKeyAsHex =  BitcoinOffLineSDK.ADDRESS.getPublicKeyAsHex(ecKey);
        String WIF =  BitcoinOffLineSDK.ADDRESS.getWIF(ecKey);
        assertThat(privateKeyAsHex, is("16bfdab77dde08b329668867cd1505c78c2dfe6e277d737df8649ad8cd97a6fa"));
        assertThat(publicKeyAsHex, is("03f9fe3ecc7f5ac22c8a3361030a203230695623e198262f2a470292602f7d5afb"));
        assertThat(WIF, is("Kwyw4CddU1Lpn5mgtc17PCiPEju48hrd4zVKx8CJRT9ng3keQYDK"));
        assertThat(address, is("1853DuTD8QPKrAyvEqKZZ4ryJ5JNbAnZwf"));
    }

    /**
     * 测试创建多签地址
     */
    @Test
    public void testGenerateMultiSigAddress() {
        BitcoinOffLineSDK.CONFIG.setNetworkParameters(TestNet3Params.get());
        ECKey publicKeyA = BitcoinOffLineSDK.ADDRESS.publicKeyToECKey("0218e262023a9e32eb44cdc18a2158dc5a81c747e6e9c78e0c6a7edb8100a0147e");
        ECKey publicKeyB = BitcoinOffLineSDK.ADDRESS.publicKeyToECKey("03c19f6736ba4d7851bae2f7b95e8aa7f919dca8ab0fc4c7483b265c0ebc970e47");
        ECKey publicKeyC = BitcoinOffLineSDK.ADDRESS.publicKeyToECKey("03124af1502666ba7bf2e833ddab36a7f68e21340c97cc77ad0678291bde4c5282");
        List<ECKey> keys = ImmutableList.of(publicKeyA, publicKeyB, publicKeyC);
        P2SHMultiSigAccount p2SHMultiSigAccount = BitcoinOffLineSDK.ADDRESS.generateMultiSigAddress(2, keys);
        assertThat(Converter.byteToHex(p2SHMultiSigAccount.getRedeemScript().getProgram()), is("52210218e262023a9e32eb44cdc18a2158dc5a81c747e6e9c78e0c6a7edb8100a0147e2103124af1502666ba7bf2e833ddab36a7f68e21340c97cc77ad0678291bde4c52822103c19f6736ba4d7851bae2f7b95e8aa7f919dca8ab0fc4c7483b265c0ebc970e4753ae"));
        assertThat(p2SHMultiSigAccount.getAddress().toBase58(), is("2NA9pNXxHVvv4qV5eLNDsAggcqAiy7BAbFb"));
    }


    /**
     * 测试消息签名、验签
     */
    @Test
    public void testSignVerifyMessage(){
        BitcoinOffLineSDK.CONFIG.setNetworkParameters(TestNet3Params.get());
        String message="hello world";
        String signatureBase64=BitcoinOffLineSDK.ADDRESS.signMessage("cP7kYMKRB6D68GzQgnkfRQm3gR7dhXjgZwbZ4vhQNds6qyfkavmP",message);
        assertThat(BitcoinOffLineSDK.ADDRESS.verifyMessage("0232017d9f60b74d0409402c96e7cf220595c2e431c476bda52ef88e6ac84729ba",message,signatureBase64),is(true));
    }

    @Test
    public void testSegWitAddress(){
        BitcoinOffLineSDK.CONFIG.setNetworkParameters(TestNet3Params.get());
        List<String> mnemonicCode = Arrays.asList("weasel", "street", "dutch", "vintage", "legal", "network",
                "squirrel", "sort", "stereo", "drum", "trumpet", "farm");
        String passphrase = "your passphrase";
        byte[] seed=BitcoinOffLineSDK.MNEMONIC.generateSeed(mnemonicCode,passphrase);

        ECKey ecKey=BitcoinOffLineSDK.ADDRESS.getECKey(seed,0,0);
        String address=BitcoinOffLineSDK.ADDRESS.getSegWitAddress(ecKey);
        assertThat(address,is("tb1qpq9z0xhzjnl6ulpqjxha6argch9jmha0m68yys"));
    }

    @Test
    public void testAddressType(){
        BitcoinOffLineSDK.CONFIG.setNetworkParameters(TestNet3Params.get());
        assertThat(BitcoinOffLineSDK.ADDRESS.isLegacyAddress("2NA9pNXxHVvv4qV5eLNDsAggcqAiy7BAbFb"),is(true));
        assertThat(BitcoinOffLineSDK.ADDRESS.isLegacyAddress("mgtxRQnoUMGX5ncFp3kDFUk7xAJWg6XCSb"),is(true));
        assertThat(BitcoinOffLineSDK.ADDRESS.isLegacyAddress("tb1qpq9z0xhzjnl6ulpqjxha6argch9jmha0m68yys"),is(false));
        assertThat(BitcoinOffLineSDK.ADDRESS.isSegWitAddress("tb1qpq9z0xhzjnl6ulpqjxha6argch9jmha0m68yys"),is(true));
        assertThat(BitcoinOffLineSDK.ADDRESS.isSegWitAddress("mgtxRQnoUMGX5ncFp3kDFUk7xAJWg6XCSb"),is(false));
        assertThat(BitcoinOffLineSDK.ADDRESS.isSegWitAddress("2NA9pNXxHVvv4qV5eLNDsAggcqAiy7BAbFb"),is(false));
    }

    @Test
    public void testScriptToAddress(){
        BitcoinOffLineSDK.CONFIG.setNetworkParameters(TestNet3Params.get());
        assertThat(BitcoinOffLineSDK.ADDRESS.scriptToAddress(new Script(Converter.hexToByte("a9148e2acc223101503adec422af45bcac35ff38b8ce87"))),is("2N6CwD92GzvAgAbGUj9UGSvVPoXfRkEE1Kh"));
        assertThat(BitcoinOffLineSDK.ADDRESS.scriptToAddress(new Script(Converter.hexToByte("76a9149c50454569dd0567916f639e93a07e7dcdf0c74b88ac"))),is("mumTsH5L1hq7y9R7cofhxySryNUf3hXQSG"));
        assertThat(BitcoinOffLineSDK.ADDRESS.scriptToAddress(new Script(Converter.hexToByte("0014080a279ae294ffae7c2091afdd7468c5cb2ddfaf"))),is("tb1qpq9z0xhzjnl6ulpqjxha6argch9jmha0m68yys"));
    }
}
