import entity.P2SHMultiSigAccount;
import entity.UTXOKey;
import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sdk.BitcoinOffLineSDK;
import utils.Converter;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.util.*;

import static org.bitcoinj.core.Utils.HEX;

/**
 * 测试多种地址类型之间的转账
 * 1、P2PKH+P2WPKH---P2PKH+P2WPKH
 * 2、P2PKH+P2WPKH---MultiSign
 * 3、MultiSign---P2PKH+P2WPKH
 * 4、MultiSign---MultiSign(没写，相当于MultiSign---P2SH)
 */
public class TransactionTest {
    private byte[] seed;

    @BeforeEach
    public void setup() {
        BitcoinOffLineSDK.CONFIG.setNetworkParameters(TestNet3Params.get());
        List<String> mnemonicCode = Arrays.asList("fetch", "panda", "tide", "theory", "risk", "aerobic",
                "trust", "disease", "super", "color", "annual", "lazy");
        String passphrase = "";
        seed = BitcoinOffLineSDK.MNEMONIC.generateSeed(mnemonicCode, passphrase);
    }

    /**
     * 1、P2PKH---P2PHK
     */
    @Test
    public void P2PKH_P2PKH() {
        //HD账户n192r9XTP3WU9ofnHG3XkZbVHY3GFT48vs收到两笔转账
        //2a6864abc8102bcdc1858508f5c3203a040e11a29701858cfbb03ad1bfda0854
        //dbecf464318ed9fb61abad5fa447e93a699afe6333dbc32631378e3334558075
        //现在转给另外两个地址，剩下的找零回来
        ECKey ecKey = BitcoinOffLineSDK.ADDRESS.getECKey(seed, 0, 1);
        assertThat(BitcoinOffLineSDK.ADDRESS.getLegacyAddress(ecKey),is("n192r9XTP3WU9ofnHG3XkZbVHY3GFT48vs"));

        //接收地址和金额列表
        Map<String, Double> receiveAddressAndValue = new HashMap<>();
        receiveAddressAndValue.put("mumTsH5L1hq7y9R7cofhxySryNUf3hXQSG", 0.02);
        receiveAddressAndValue.put("mhQRcw8EVQbuC1TNbHSvBR7YdcsVtQZDhr", 0.01);
        receiveAddressAndValue.put("n192r9XTP3WU9ofnHG3XkZbVHY3GFT48vs", 0.006);

        //构建UTXOKey列表
        List<UTXOKey> utxoKeys=new ArrayList<>();

        UTXO utxo = new UTXO(
                Sha256Hash.wrap("2a6864abc8102bcdc1858508f5c3203a040e11a29701858cfbb03ad1bfda0854"),
                1,
                Coin.valueOf(Converter.bitcoinToSatoshis(0.03491879)),
                0,
                false,
                new Script(Converter.hexToByte("76a914d73d2a5b2f468ba9bff5fe1b1b6620ee33e7766c88ac"))
        );

        utxoKeys.add(new UTXOKey(utxo,ecKey));

        utxo = new UTXO(
                Sha256Hash.wrap("dbecf464318ed9fb61abad5fa447e93a699afe6333dbc32631378e3334558075"),
                0,
                Coin.valueOf(Converter.bitcoinToSatoshis(0.002)),
                0,
                false,
                new Script(Converter.hexToByte("76a914d73d2a5b2f468ba9bff5fe1b1b6620ee33e7766c88ac"))
        );

        utxoKeys.add(new UTXOKey(utxo,ecKey));

        //构建交易并签名
        Transaction transaction=BitcoinOffLineSDK.TRANSACTION.buildLegacyTransactionWithSigners(utxoKeys, receiveAddressAndValue);

        String rawTransactionHex = HEX.encode(transaction.bitcoinSerialize());
        assertThat(rawTransactionHex,is("01000000025408dabfd13ab0fb8c850197a2110e043a20c3f5088585c1cd2b10c8ab64682a010000006b483045022100c43e57e37c36635a08bcc648d60660286c2d6082b9a3d911339609cc01e4423902204b74d9dae42f92c5b1cc08a1c6fcc1c7868e01b5175595f64806c3074030263f81210388d34c1d4e8951896d2eca7d9b7eb644eefcc6fee5b2fecb409d56e977210059ffffffff75805534338e373126c3db3363fe9a693ae947a45fadab61fbd98e3164f4ecdb000000006a473044022033ccc5391935ba47dd993b119443650bfd62037f2199a2f54fa8e0ec8cd98fd902205c5d9765a52f370902e3e4ed21410b3c11268408e85b0fe1a641ad17e5dd579381210388d34c1d4e8951896d2eca7d9b7eb644eefcc6fee5b2fecb409d56e977210059ffffffff03c0270900000000001976a914d73d2a5b2f468ba9bff5fe1b1b6620ee33e7766c88ac80841e00000000001976a9149c50454569dd0567916f639e93a07e7dcdf0c74b88ac40420f00000000001976a91414b4266839352fed86052061482754ef784f06ba88ac00000000"));
    }

    /**
     * 2、P2WPKH---P2WPKH
     */
    @Test
    public void P2WPKH_P2WPKH(){
        //A收到一笔转账
        //0ba79528a7398cf1a4e9f2e8a4cdb014b64abfa9423d7d84249a8d4cb12e4cc5
        //现在发送0.002到B，0.0015找零回来
        ECKey ecKeyA=BitcoinOffLineSDK.ADDRESS.getECKey(seed,0,0);
        assertThat(BitcoinOffLineSDK.ADDRESS.getSegWitAddress(ecKeyA),is("tb1qn3gy23tfm5zk0yt0vw0f8gr70hxlp36td00f8g"));

        ECKey ecKeyB=BitcoinOffLineSDK.ADDRESS.getECKey(seed,0,1);
        assertThat(BitcoinOffLineSDK.ADDRESS.getSegWitAddress(ecKeyB),is("tb1q6u7j5ke0g696n0l4lcd3ke3qace7wanv9x5azh"));

        //构建A的UTXOKey
        List<UTXOKey> utxoKeys=new ArrayList<>();
        UTXO utxo=new UTXO(
                Sha256Hash.wrap("0ba79528a7398cf1a4e9f2e8a4cdb014b64abfa9423d7d84249a8d4cb12e4cc5"),
                0,
                Coin.valueOf(Converter.bitcoinToSatoshis(0.004)),
                0,
                false,
                new Script(Converter.hexToByte("0014b726650837b155c78f7ad8e0c9b18d362473970e"))
        );

        utxoKeys.add(new UTXOKey(utxo,ecKeyA));


        //接收地址和金额列表
        Map<String, Double> receiveAddressAndValue = new HashMap<>();
        receiveAddressAndValue.put("tb1q6u7j5ke0g696n0l4lcd3ke3qace7wanv9x5azh", 0.002);
        receiveAddressAndValue.put("tb1qn3gy23tfm5zk0yt0vw0f8gr70hxlp36td00f8g", 0.0015);

        //构建并签名交易
        Transaction transaction=BitcoinOffLineSDK.TRANSACTION.buildLegacyTransactionWithSigners(utxoKeys,receiveAddressAndValue);
        assertThat(HEX.encode(transaction.bitcoinSerialize()),is("01000000000101c54c2eb14c8d9a24847d3d42a9bf4ab614b0cda4e8f2e9a4f18c39a72895a70b0000000000ffffffff02400d030000000000160014d73d2a5b2f468ba9bff5fe1b1b6620ee33e7766cf0490200000000001600149c50454569dd0567916f639e93a07e7dcdf0c74b02483045022100f2e7a65e34060d420d43ac0763fa6bf2086bfe8412aa8aa3b8157a8f70ebfff502204d61d9f9ef88c71aa300e37cdd3483793396ceec1c54eb305863cc4aec8edb2501210248857ec3b6b80aa564a5ae33e0b6ff2e42f1ff9056278d279682dd1b78311fa400000000"));
    }

    @Test
    public void P2WPKH_P2PKH(){
        List<String> mnemonicCode = Arrays.asList("weasel", "street", "dutch", "vintage", "legal", "network",
                "squirrel", "sort", "stereo", "drum", "trumpet", "farm");
        String passphrase = "your passphrase";
        byte[] seed=BitcoinOffLineSDK.MNEMONIC.generateSeed(mnemonicCode,passphrase);
        ECKey ecKey=BitcoinOffLineSDK.ADDRESS.getECKey(seed,0,0);
        assertThat(BitcoinOffLineSDK.ADDRESS.getSegWitAddress(ecKey),is("tb1qpq9z0xhzjnl6ulpqjxha6argch9jmha0m68yys"));
        UTXO utxo = new UTXO(
                Sha256Hash.wrap("2946a962226f75d1d02312a469e1e276fb61541f6a805dadb79f7c7f82bc97f4"),
                1,
                Coin.valueOf(2000000),
                0,
                false,
                new Script(Converter.hexToByte("0014080a279ae294ffae7c2091afdd7468c5cb2ddfaf"))
        );

        List<UTXOKey> utxoKeys=new ArrayList<>();
        utxoKeys.add(new UTXOKey(utxo,ecKey));

        Map<String, Double> receiveAddressAndValue = new HashMap<>();
        receiveAddressAndValue.put("mumTsH5L1hq7y9R7cofhxySryNUf3hXQSG", 0.019);
        Transaction transaction=BitcoinOffLineSDK.TRANSACTION.buildLegacyTransactionWithSigners(utxoKeys,receiveAddressAndValue);
        assertThat(HEX.encode(transaction.bitcoinSerialize()),is("01000000000101f497bc827f7c9fb7ad5d806a1f5461fb76e2e169a41223d0d1756f2262a946290100000000ffffffff01e0fd1c00000000001976a9149c50454569dd0567916f639e93a07e7dcdf0c74b88ac024730440220730395643e0bc2d27aea30dcf9a2338406e8f6cf3fa47e6f12e4dfb9d9abc49002202163af959aebe1d415bb2abcbb2460a7e080426625f82a86b462e73a2dd49d08012102ac0e3005c6aebeb53d7f18e1c6222f7116603f35449dff2cf03096accda4efc800000000"));
    }

    @Test
    public void P2PKH_P2WPKH_TO_P2PKH_P2WPKH(){
        //edb243ce88ba78385325b6ca62398ebb6c14c4923b87e96d517bb90fdd7272d6
        ECKey ecKeySendA = BitcoinOffLineSDK.ADDRESS.getECKey(seed, 0, 0);
        assertThat(BitcoinOffLineSDK.ADDRESS.getLegacyAddress(ecKeySendA),is("mumTsH5L1hq7y9R7cofhxySryNUf3hXQSG"));
        //7d3495159a54ae0a6fa7f0e699c3212be034d552f56cca5b530154b39f1ac098
        ECKey ecKeySendB = BitcoinOffLineSDK.ADDRESS.getECKey(seed, 0, 1);
        assertThat(BitcoinOffLineSDK.ADDRESS.getSegWitAddress(ecKeySendB),is("tb1q6u7j5ke0g696n0l4lcd3ke3qace7wanv9x5azh"));

        ECKey ecKeyRecipientA = BitcoinOffLineSDK.ADDRESS.getECKey(seed, 0, 2);
        assertThat(BitcoinOffLineSDK.ADDRESS.getLegacyAddress(ecKeyRecipientA),is("mhQRcw8EVQbuC1TNbHSvBR7YdcsVtQZDhr"));
        ECKey ecKeyRecipientB = BitcoinOffLineSDK.ADDRESS.getECKey(seed, 0, 3);
        assertThat(BitcoinOffLineSDK.ADDRESS.getSegWitAddress(ecKeyRecipientB),is("tb1qsg666g22ew3hts0gt9evgv3csurw95egej3ykx"));


        //构建A的UTXOKey
        List<UTXOKey> utxoKeys=new ArrayList<>();
        UTXO utxo=new UTXO(
                Sha256Hash.wrap("edb243ce88ba78385325b6ca62398ebb6c14c4923b87e96d517bb90fdd7272d6"),
                0,
                Coin.valueOf(Converter.bitcoinToSatoshis(0.00571588)),
                0,
                false,
                new Script(Converter.hexToByte("76a9149c50454569dd0567916f639e93a07e7dcdf0c74b88ac"))
        );

        utxoKeys.add(new UTXOKey(utxo,ecKeySendA));

        utxo=new UTXO(
                Sha256Hash.wrap("7d3495159a54ae0a6fa7f0e699c3212be034d552f56cca5b530154b39f1ac098"),
                0,
                Coin.valueOf(Converter.bitcoinToSatoshis(0.002)),
                0,
                false,
                new Script(Converter.hexToByte("0014d73d2a5b2f468ba9bff5fe1b1b6620ee33e7766c"))
        );

        utxoKeys.add(new UTXOKey(utxo,ecKeySendB));


        //接收地址和金额列表
        Map<String, Double> receiveAddressAndValue = new HashMap<>();
        receiveAddressAndValue.put("mhQRcw8EVQbuC1TNbHSvBR7YdcsVtQZDhr", 0.00571588);
        receiveAddressAndValue.put("tb1qsg666g22ew3hts0gt9evgv3csurw95egej3ykx", 0.0015);

        //构建并签名交易
        Transaction transaction=BitcoinOffLineSDK.TRANSACTION.buildLegacyTransactionWithSigners(utxoKeys,receiveAddressAndValue);
        assertThat(HEX.encode(transaction.bitcoinSerialize()),is("01000000000102d67272dd0fb97b516de9873b92c4146cbb8e3962cab625533878ba88ce43b2ed000000006a47304402202b075fb40b1b904028ed95fdda1bbaac45cf155fae62d2c8c7750d4c13ee1cdd022079972c9660fa88de16d594cb5384ae9033224020dcc5966c05f5266309eab6af81210248857ec3b6b80aa564a5ae33e0b6ff2e42f1ff9056278d279682dd1b78311fa4ffffffff98c01a9fb35401535bca6cf552d534e02b21c399e6f0a76f0aae549a1595347d0000000000ffffffff02f0490200000000001600148235ad214acba375c1e85972c432388706e2d328c4b80800000000001976a91414b4266839352fed86052061482754ef784f06ba88ac0002483045022100f3ad7aa22e8d11d4fe23fe486b64c033348f201ea54eec833a1a81cd11bf5926022059b75973cb6a683237d26de95d7c25dfac93deff75a33402e0672819b6ec904901210388d34c1d4e8951896d2eca7d9b7eb644eefcc6fee5b2fecb409d56e97721005900000000"));
        assertThat(transaction.getTxId().toString(),is("f30b74e91d8a8162a597a839adfb57a48ad0ad45e645b6f02405e85850e45385"));
    }


    @Test
    public void P2PKH_P2WPKH_TO_MultiSign(){
        //f30b74e91d8a8162a597a839adfb57a48ad0ad45e645b6f02405e85850e45385
        ECKey ecKeySendA = BitcoinOffLineSDK.ADDRESS.getECKey(seed, 0, 2);
        assertThat(BitcoinOffLineSDK.ADDRESS.getLegacyAddress(ecKeySendA),is("mhQRcw8EVQbuC1TNbHSvBR7YdcsVtQZDhr"));

        //f30b74e91d8a8162a597a839adfb57a48ad0ad45e645b6f02405e85850e45385
        ECKey ecKeySendB = BitcoinOffLineSDK.ADDRESS.getECKey(seed, 0, 3);
        assertThat(BitcoinOffLineSDK.ADDRESS.getSegWitAddress(ecKeySendB),is("tb1qsg666g22ew3hts0gt9evgv3csurw95egej3ykx"));

        //创建多签地址
        ECKey ecKeyMultiSignA = BitcoinOffLineSDK.ADDRESS.getECKey(seed, 0, 10);
        String publicKeyA=BitcoinOffLineSDK.ADDRESS.getPublicKeyAsHex(ecKeyMultiSignA);
        assertThat(publicKeyA,is("0257a59041c72e9234adffa5adaba4a49ba38de28cc6a3c1ef4883fbb02b93a6c8"));
        assertThat(BitcoinOffLineSDK.ADDRESS.getLegacyAddress(ecKeyMultiSignA),is("myfyq3MYWJNAvfWUpg92ZyCiDGKin6J8Ty"));

        ECKey ecKeyMultiSignB = BitcoinOffLineSDK.ADDRESS.getECKey(seed, 0, 11);
        String publicKeyB=BitcoinOffLineSDK.ADDRESS.getPublicKeyAsHex(ecKeyMultiSignB);
        assertThat(publicKeyB,is("030f1b97e4df51bed157b0d030dbe4356caf06e986e441d1c7077915431d13a9da"));
        assertThat(BitcoinOffLineSDK.ADDRESS.getLegacyAddress(ecKeyMultiSignB),is("mjGHmnkeMh5DgKxyAcPbMCWw1vUdYv6tAN"));

        ECKey ecKeyMultiSignC = BitcoinOffLineSDK.ADDRESS.getECKey(seed, 0, 12);
        String publicKeyC=BitcoinOffLineSDK.ADDRESS.getPublicKeyAsHex(ecKeyMultiSignC);
        assertThat(publicKeyC,is("034d7e772482631bde32b32ea46203b77cf18ed75da19ba324e1692fa28706e702"));
        assertThat(BitcoinOffLineSDK.ADDRESS.getLegacyAddress(ecKeyMultiSignC),is("mfZRBBybuzFNcj18RPfqLUJDf4Xuwp7AAo"));

        List<ECKey> ecKeys=new ArrayList<>();
        ecKeys.add(BitcoinOffLineSDK.ADDRESS.publicKeyToECKey(publicKeyA));
        ecKeys.add(BitcoinOffLineSDK.ADDRESS.publicKeyToECKey(publicKeyB));
        ecKeys.add(BitcoinOffLineSDK.ADDRESS.publicKeyToECKey(publicKeyC));
        P2SHMultiSigAccount p2SHMultiSigAccount=BitcoinOffLineSDK.ADDRESS.generateMultiSigAddress(2,ecKeys);
        assertThat(Converter.byteToHex(p2SHMultiSigAccount.getRedeemScript().getProgram()), is("52210257a59041c72e9234adffa5adaba4a49ba38de28cc6a3c1ef4883fbb02b93a6c821030f1b97e4df51bed157b0d030dbe4356caf06e986e441d1c7077915431d13a9da21034d7e772482631bde32b32ea46203b77cf18ed75da19ba324e1692fa28706e70253ae"));
        assertThat(p2SHMultiSigAccount.getAddress().toBase58(), is("2NGVEx8a2WThySFqgLCqhFdArb9qQ5eY3kY"));

        //构建A的UTXOKey
        List<UTXOKey> utxoKeys=new ArrayList<>();
        UTXO utxo=new UTXO(
                Sha256Hash.wrap("f30b74e91d8a8162a597a839adfb57a48ad0ad45e645b6f02405e85850e45385"),
                1,
                Coin.valueOf(Converter.bitcoinToSatoshis(0.00571588)),
                0,
                false,
                new Script(Converter.hexToByte("76a91414b4266839352fed86052061482754ef784f06ba88ac"))
        );

        utxoKeys.add(new UTXOKey(utxo,ecKeySendA));

        utxo=new UTXO(
                Sha256Hash.wrap("f30b74e91d8a8162a597a839adfb57a48ad0ad45e645b6f02405e85850e45385"),
                0,
                Coin.valueOf(Converter.bitcoinToSatoshis(0.0015)),
                0,
                false,
                new Script(Converter.hexToByte("00148235ad214acba375c1e85972c432388706e2d328"))
        );

        utxoKeys.add(new UTXOKey(utxo,ecKeySendB));


        //接收地址和金额列表
        Map<String, Double> receiveAddressAndValue = new HashMap<>();
        receiveAddressAndValue.put("2NGVEx8a2WThySFqgLCqhFdArb9qQ5eY3kY", 0.007);

        Transaction transaction=BitcoinOffLineSDK.TRANSACTION.buildLegacyTransactionWithSigners(utxoKeys,receiveAddressAndValue);
        assertThat(HEX.encode(transaction.bitcoinSerialize()),is("010000000001028553e45058e80524f0b645e645add08aa457fbad39a897a562818a1de9740bf3010000006b483045022100f5b3c0dbfdd3bcbca6bc4c155f3d4f31a24d1e03555849abfba88eaa4679065e02201258f38e481020c18dceb88384f7ac2e07a2a7d030c001cdbc18907d7141940f8121026e4f25e991d42176ee13c8a075d3ef2815a74719a3138cf39fad596e9ba2f7b4ffffffff8553e45058e80524f0b645e645add08aa457fbad39a897a562818a1de9740bf30000000000ffffffff0160ae0a000000000017a914fef185305033ca6b65a53e7fbe0c8c01549ead9c8700024830450221009e178d5c2f53a4fe4055315a3924bb0b1d9d7b65462f602390493458879b033402200776061c6cb5633032b17e91fe080ac4988988a4323b50fe24a498d4247bb81e0121034de6dbc1cd541e5bdf987fd70ab341dfff79cfed7d744ea83740ab24b4bf377200000000"));
        assertThat(transaction.getTxId().toString(),is("1cb5e76112142da8c2ea51f99611e9897d7f1f2ad70d85c95f56757abed19376"));
    }


    /**
     * 测试多签交易
     */
    @Test
    public void MultiSign_TO_P2PKH_P2WPKH() {
        //创建多签地址
        //5be214cb5ea5c0aea3772c188f94b6cc86329a196c6572bee34bebdb707e3021
        //6a6f2000f093c787b18ecc206f5c9c74809aa6383271c106e1b0ed16d9e4cce4
        ECKey ecKeyMultiSignA = BitcoinOffLineSDK.ADDRESS.getECKey(seed, 0, 10);
        String publicKeyA=BitcoinOffLineSDK.ADDRESS.getPublicKeyAsHex(ecKeyMultiSignA);
        assertThat(publicKeyA,is("0257a59041c72e9234adffa5adaba4a49ba38de28cc6a3c1ef4883fbb02b93a6c8"));
        assertThat(BitcoinOffLineSDK.ADDRESS.getLegacyAddress(ecKeyMultiSignA),is("myfyq3MYWJNAvfWUpg92ZyCiDGKin6J8Ty"));

        ECKey ecKeyMultiSignB = BitcoinOffLineSDK.ADDRESS.getECKey(seed, 0, 11);
        String publicKeyB=BitcoinOffLineSDK.ADDRESS.getPublicKeyAsHex(ecKeyMultiSignB);
        assertThat(publicKeyB,is("030f1b97e4df51bed157b0d030dbe4356caf06e986e441d1c7077915431d13a9da"));
        assertThat(BitcoinOffLineSDK.ADDRESS.getLegacyAddress(ecKeyMultiSignB),is("mjGHmnkeMh5DgKxyAcPbMCWw1vUdYv6tAN"));

        ECKey ecKeyMultiSignC = BitcoinOffLineSDK.ADDRESS.getECKey(seed, 0, 12);
        String publicKeyC=BitcoinOffLineSDK.ADDRESS.getPublicKeyAsHex(ecKeyMultiSignC);
        assertThat(publicKeyC,is("034d7e772482631bde32b32ea46203b77cf18ed75da19ba324e1692fa28706e702"));
        assertThat(BitcoinOffLineSDK.ADDRESS.getLegacyAddress(ecKeyMultiSignC),is("mfZRBBybuzFNcj18RPfqLUJDf4Xuwp7AAo"));

        List<ECKey> ecKeys=new ArrayList<>();
        ecKeys.add(BitcoinOffLineSDK.ADDRESS.publicKeyToECKey(publicKeyA));
        ecKeys.add(BitcoinOffLineSDK.ADDRESS.publicKeyToECKey(publicKeyB));
        ecKeys.add(BitcoinOffLineSDK.ADDRESS.publicKeyToECKey(publicKeyC));
        P2SHMultiSigAccount p2SHMultiSigAccount=BitcoinOffLineSDK.ADDRESS.generateMultiSigAddress(2,ecKeys);
        assertThat(Converter.byteToHex(p2SHMultiSigAccount.getRedeemScript().getProgram()), is("52210257a59041c72e9234adffa5adaba4a49ba38de28cc6a3c1ef4883fbb02b93a6c821030f1b97e4df51bed157b0d030dbe4356caf06e986e441d1c7077915431d13a9da21034d7e772482631bde32b32ea46203b77cf18ed75da19ba324e1692fa28706e70253ae"));
        assertThat(p2SHMultiSigAccount.getAddress().toBase58(), is("2NGVEx8a2WThySFqgLCqhFdArb9qQ5eY3kY"));

        //消费多签地址上的资产
        List<UTXO> utxos = new ArrayList<>();
        UTXO utxo=new UTXO(
                Sha256Hash.wrap("5be214cb5ea5c0aea3772c188f94b6cc86329a196c6572bee34bebdb707e3021"),
                1,
                Coin.valueOf(Converter.bitcoinToSatoshis( 0.0001)),
                0,
                false,
                new Script(Converter.hexToByte("a914fef185305033ca6b65a53e7fbe0c8c01549ead9c87"))
        );
        utxos.add(utxo);
        utxo=new UTXO(
                Sha256Hash.wrap("6a6f2000f093c787b18ecc206f5c9c74809aa6383271c106e1b0ed16d9e4cce4"),
                1,
                Coin.valueOf(Converter.bitcoinToSatoshis( 0.0001)),
                0,
                false,
                new Script(Converter.hexToByte("a914fef185305033ca6b65a53e7fbe0c8c01549ead9c87"))
        );
        utxos.add(utxo);
        Map<String, Double> receiveAddressAndValue = new HashMap<>();
        receiveAddressAndValue.put("mhQRcw8EVQbuC1TNbHSvBR7YdcsVtQZDhr", 0.00004);
        receiveAddressAndValue.put("tb1qsg666g22ew3hts0gt9evgv3csurw95egej3ykx", 0.00005);
        //构建交易
        Transaction transaction = BitcoinOffLineSDK.TRANSACTION.buildTransaction(utxos, receiveAddressAndValue);
        //赎回脚本，首次签名会用到
        Script redeemScript =p2SHMultiSigAccount.getRedeemScript();


        BitcoinOffLineSDK.TRANSACTION.signMultiSigTransaction(transaction, redeemScript, ecKeyMultiSignA, true);
       /* System.out.println("First sign:");
        int i = 0;
        for (TransactionInput input : transaction.getInputs()) {
            System.out.println("input" + i + ":" + input.getScriptSig());
            System.out.println("input" + i + ":" + Converter.byteToHex(input.getScriptSig().getProgram()));
            i++;
        }*/
        String transactionHex = Converter.byteToHex(transaction.bitcoinSerialize());
//        System.out.println("First signed transaction hex:" + transactionHex);

        //这里模拟第二个人在其他地方对这笔交易签名
        //从十六进制的文本格式还原交易
        transaction = new Transaction(BitcoinOffLineSDK.CONFIG.getNetworkParameters(), Converter.hexToByte(transactionHex));
        //签名
        BitcoinOffLineSDK.TRANSACTION.signMultiSigTransaction(transaction, null, ecKeyMultiSignB, false);

        /*System.out.println("Second sign:");
        i = 0;
        for (TransactionInput input : transaction.getInputs()) {
            System.out.println("input" + i + ":" + input.getScriptSig());
            System.out.println("input" + i + ":" + Converter.byteToHex(input.getScriptSig().getProgram()));
            i++;
        }*/
        transactionHex = Converter.byteToHex(transaction.bitcoinSerialize());
//        System.out.println("Second signed transaction hex:" + transactionHex);
        assertThat(transactionHex,is("010000000221307e70dbeb4be3be72656c199a3286ccb6948f182c77a3aec0a55ecb14e25b01000000fdfe0000483045022100f2c56653808f231ed5f4f7f92bab9d4884557b9823f311221f06d556cb0c612202204338d44a1fe284fda10a593a1e902f2b7967d46ce908b4624ce31e02e2700dfc01483045022100c3bf2f9f00a21e31bb83abcb78c9203e57fbf9523bf435ae5c0c4b0a9c8b6ba102201e2fcc1f61bc4d85c82067123fcfbba98ebbebb25dd15b9abca0ed7d8caae6a3014c6952210257a59041c72e9234adffa5adaba4a49ba38de28cc6a3c1ef4883fbb02b93a6c821030f1b97e4df51bed157b0d030dbe4356caf06e986e441d1c7077915431d13a9da21034d7e772482631bde32b32ea46203b77cf18ed75da19ba324e1692fa28706e70253aeffffffffe4cce4d916edb0e106c1713238a69a80749c5c6f20cc8eb187c793f000206f6a01000000fc00473044022056c5508a8f5ba6fb2a099ab3fa58c00abb770e8e3c4f48a2874055c26eeb32ae02205f86e7db1618c1c1422c910585aa74fd132f9dc5cc66de6b3f7e1f0d17121f6b0147304402203e87eea89d3ce11e92401049b8c845c1eabd2e7556447ae9b64c94670d2959b5022041967578a6a6fa407293b8b46b24b31dc3e03950c6c16f3ea169fe80496f0d24014c6952210257a59041c72e9234adffa5adaba4a49ba38de28cc6a3c1ef4883fbb02b93a6c821030f1b97e4df51bed157b0d030dbe4356caf06e986e441d1c7077915431d13a9da21034d7e772482631bde32b32ea46203b77cf18ed75da19ba324e1692fa28706e70253aeffffffff0288130000000000001600148235ad214acba375c1e85972c432388706e2d328a00f0000000000001976a91414b4266839352fed86052061482754ef784f06ba88ac00000000"));
        assertThat(transaction.getTxId().toString(),is("8a0e4aa27338291ab569eeac9a5679f75d99448e879473efb9a048861806f880"));
    }
}
