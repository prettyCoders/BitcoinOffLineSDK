import entity.P2SHMultiSigAccount;
import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.junit.Test;
import sdk.BitcoinOffLineSDK;
import utils.Converter;

import java.util.*;

public class TransactionTest {

    @Test
    public void standardTransaction() {
        BitcoinOffLineSDK.CONFIG.setNetworkParameters(TestNet3Params.get());
        Map<String, Double> receiveAddressAndValue = new HashMap<>();
        receiveAddressAndValue.put("mumTsH5L1hq7y9R7cofhxySryNUf3hXQSG", 0.02);
        receiveAddressAndValue.put("mhQRcw8EVQbuC1TNbHSvBR7YdcsVtQZDhr", 0.01);
        receiveAddressAndValue.put("n192r9XTP3WU9ofnHG3XkZbVHY3GFT48vs", 0.006);
        Transaction transaction = new Transaction(BitcoinOffLineSDK.CONFIG.getNetworkParameters());
        BitcoinOffLineSDK.TRANSACTION.addOutPut(transaction, receiveAddressAndValue);

        List<String> mnemonicCode = Arrays.asList("fetch", "panda", "tide", "theory", "risk", "aerobic",
                "trust", "disease", "super", "color", "annual", "lazy");
        String passphrase = "";
        byte[] seed = BitcoinOffLineSDK.MNEMONIC.generateSeed(mnemonicCode, passphrase);
        ECKey ecKey = BitcoinOffLineSDK.ADDRESS.getECKey(seed, 0, 1);

        UTXO utxo = new UTXO(
                Sha256Hash.wrap("2a6864abc8102bcdc1858508f5c3203a040e11a29701858cfbb03ad1bfda0854"),
                1,
                Coin.valueOf(Converter.bitcoinToSatoshis(0.03491879)),
                0,
                false,
                new Script(Converter.hexToByte("76a914d73d2a5b2f468ba9bff5fe1b1b6620ee33e7766c88ac"))
        );

        BitcoinOffLineSDK.TRANSACTION.addUTXOSign(transaction, utxo, ecKey);

        utxo = new UTXO(
                Sha256Hash.wrap("dbecf464318ed9fb61abad5fa447e93a699afe6333dbc32631378e3334558075"),
                0,
                Coin.valueOf(Converter.bitcoinToSatoshis(0.002)),
                0,
                false,
                new Script(Converter.hexToByte("76a914d73d2a5b2f468ba9bff5fe1b1b6620ee33e7766c88ac"))
        );
        BitcoinOffLineSDK.TRANSACTION.addUTXOSign(transaction, utxo, ecKey);
        List<TransactionOutput> transactionOutputs = transaction.getOutputs();

        //下面是交易产生的新的找零UTXO
        for (TransactionOutput transactionOutput : transactionOutputs) {
            Script script = transactionOutput.getScriptPubKey();
            String address = script.getToAddress(BitcoinOffLineSDK.CONFIG.getNetworkParameters()).toString();
            if (address.equals("n192r9XTP3WU9ofnHG3XkZbVHY3GFT48vs")) {
                System.out.println(transactionOutput.getIndex());
                System.out.println(address);
                System.out.println(transaction.getHashAsString());
                System.out.println(Converter.byteToHex(script.getProgram()));
            }
        }
        String rawTransactionHex = Converter.byteToHex(transaction.bitcoinSerialize());
        System.out.println("rawTransactionHex:" + rawTransactionHex);
    }

    @Test
    public void multiSignTransaction() {
        BitcoinOffLineSDK.CONFIG.setNetworkParameters(TestNet3Params.get());
        //创建2/3的多签地址
        ECKey ecKeyA = BitcoinOffLineSDK.ADDRESS.publicKeyToECKey("0232017d9f60b74d0409402c96e7cf220595c2e431c476bda52ef88e6ac84729ba");
        ECKey ecKeyB = BitcoinOffLineSDK.ADDRESS.publicKeyToECKey("0232bc8885f5ee0e20f53b2640534bb09c6ee3dab36563f7b4e0876f53288027d5");
        ECKey ecKeyC = BitcoinOffLineSDK.ADDRESS.publicKeyToECKey("0218e262023a9e32eb44cdc18a2158dc5a81c747e6e9c78e0c6a7edb8100a0147e");
        List<ECKey> keys = new ArrayList<>();
        keys.add(ecKeyA);
        keys.add(ecKeyB);
        keys.add(ecKeyC);
        P2SHMultiSigAccount p2SHMultiSigAccount = BitcoinOffLineSDK.ADDRESS.generateMultiSigAddress(2, keys);
        System.out.println("Address:" + p2SHMultiSigAccount.getAddress().toBase58());
        System.out.println("RedeemScript:" + Converter.byteToHex(p2SHMultiSigAccount.getRedeemScript().getProgram()));

        //消费多签地址上的资产
        List<UTXO> utxos = new ArrayList<>();
        Map<String, Double> receiveAddressAndValue = new HashMap<>();
        utxos.add(
                new UTXO(
                        Sha256Hash.wrap("bf24eab23e277810878e436b74ccaa8283538ba553ba313e82a0562b99b7e2f7"),
                        1,
                        Coin.valueOf(3000000),
                        0,
                        false,
                        new Script(Converter.hexToByte("a9148e2acc223101503adec422af45bcac35ff38b8ce87"))
                )
        );
        receiveAddressAndValue.put("mkodx6v5kgVWYH2MMVtUChPhYGFX2q1o4j", 0.015);
        receiveAddressAndValue.put("mr8uCkt9UjyHCmAzH5dsbMi4zgggvC5uvi", 0.01);
        receiveAddressAndValue.put("mjpUoWCrDhiLqUJ1H8LEDryvdUQxdimh4n", 0.004);
        //构建交易
        Transaction transaction = BitcoinOffLineSDK.TRANSACTION.buildTransactionFromMultiSigAddress(utxos, receiveAddressAndValue);
        //赎回脚本，首次签名会用到
        Script redeemScript = new Script(Converter.hexToByte("52210218e262023a9e32eb44cdc18a2158dc5a81c747e6e9c78e0c6a7edb8100a0147e210232017d9f60b74d0409402c96e7cf220595c2e431c476bda52ef88e6ac84729ba210232bc8885f5ee0e20f53b2640534bb09c6ee3dab36563f7b4e0876f53288027d553ae"));


        ECKey ecKey = BitcoinOffLineSDK.ADDRESS.WIFToECKey("cP7kYMKRB6D68GzQgnkfRQm3gR7dhXjgZwbZ4vhQNds6qyfkavmP");
        BitcoinOffLineSDK.TRANSACTION.signMultiSigTransaction(transaction, redeemScript, ecKey, true);
        System.out.println("First sign:");
        int i = 0;
        for (TransactionInput input : transaction.getInputs()) {
            System.out.println("input" + i + ":" + input.getScriptSig());
            System.out.println("input" + i + ":" + Converter.byteToHex(input.getScriptSig().getProgram()));
            i++;
        }
        String transactionHex = Converter.byteToHex(transaction.bitcoinSerialize());
        System.out.println("First signed transaction hex:" + transactionHex);

        //这里模拟第二个人在其他地方对这笔交易签名
        //从十六进制的文本格式还原交易
        transaction = new Transaction(BitcoinOffLineSDK.CONFIG.getNetworkParameters(), Converter.hexToByte(transactionHex));
        //他的ECKey
        ecKey = BitcoinOffLineSDK.ADDRESS.WIFToECKey("cPQjg3W4f5GdDqTS2YwLbzDCZTb19jvKkkXSbTdMf1pS9R5ADrxE");
        //签名
        BitcoinOffLineSDK.TRANSACTION.signMultiSigTransaction(transaction, null, ecKey, false);

        System.out.println("Second sign:");
        i = 0;
        for (TransactionInput input : transaction.getInputs()) {
            System.out.println("input" + i + ":" + input.getScriptSig());
            System.out.println("input" + i + ":" + Converter.byteToHex(input.getScriptSig().getProgram()));
            i++;
        }
        transactionHex = Converter.byteToHex(transaction.bitcoinSerialize());
        System.out.println("Second signed transaction hex:" + transactionHex);
    }
}
