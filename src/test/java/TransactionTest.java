import entity.P2SHMultiSigAccount;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.junit.Test;
import sdk.BitcoinOffLineSDK;
import utils.Converter;

import java.util.*;

import static org.bitcoinj.core.Utils.HEX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransactionTest {

    /**
     * 测试常规交易
     */
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
    public void testSendFromSegWitAddress(){
        BitcoinOffLineSDK.CONFIG.setNetworkParameters(TestNet3Params.get());
        List<String> mnemonicCode = Arrays.asList("weasel", "street", "dutch", "vintage", "legal", "network",
                "squirrel", "sort", "stereo", "drum", "trumpet", "farm");
        String passphrase = "your passphrase";
        byte[] seed=BitcoinOffLineSDK.MNEMONIC.generateSeed(mnemonicCode,passphrase);

        ECKey ecKey=BitcoinOffLineSDK.ADDRESS.getECKey(seed,0,0);

        Transaction tx=new Transaction(BitcoinOffLineSDK.CONFIG.getNetworkParameters());

        Map<String, Double> receiveAddressAndValue = new HashMap<>();
        receiveAddressAndValue.put("mumTsH5L1hq7y9R7cofhxySryNUf3hXQSG", 0.019);
        BitcoinOffLineSDK.TRANSACTION.addOutPut(tx, receiveAddressAndValue);



        //第一种方式,用原始交易来构建新的input
//        String preTxHex="0100000001f497bc827f7c9fb7ad5d806a1f5461fb76e2e169a41223d0d1756f2262a94629000000006a47304402200486df608dcf7d05ea9570ad79727a92d9c06ae2e966eb19701969b8f55462d6022010f871ebcecba9dc3ac017b4723b9b0d04462e3d7f7569e1a9771ffa3a32c57181210248857ec3b6b80aa564a5ae33e0b6ff2e42f1ff9056278d279682dd1b78311fa4ffffffff02043d0100000000001976a9149c50454569dd0567916f639e93a07e7dcdf0c74b88ac60ae0a0000000000160014080a279ae294ffae7c2091afdd7468c5cb2ddfaf00000000";
//        Transaction preTx = new Transaction(BitcoinOffLineSDK.CONFIG.getNetworkParameters(), HEX.decode(preTxHex));
//        TransactionInput transactionInput=tx.addInput(preTx.getOutput(1));
//        tx.addSignedInput(tx0.getOutput(1),ecKey); //不好使，不取utxo的value报空指针

        //第二种方式,用UTXO构建新的input
        UTXO utxo = new UTXO(
                Sha256Hash.wrap("2946a962226f75d1d02312a469e1e276fb61541f6a805dadb79f7c7f82bc97f4"),
                1,
                Coin.valueOf(2000000),
                0,
                false,
                new Script(Converter.hexToByte("0014080a279ae294ffae7c2091afdd7468c5cb2ddfaf"))
        );

        TransactionInput transactionInput=tx.addInput(utxo.getHash(),utxo.getIndex(),utxo.getScript());

        //计算见证签名
        Script scriptCode = new ScriptBuilder().data(ScriptBuilder.createP2PKHOutputScript(ecKey).getProgram()).build();
        TransactionSignature txSig1 = tx.calculateWitnessSignature(0, ecKey,
                scriptCode, utxo.getValue(),
                Transaction.SigHash.ALL, false);

        transactionInput.setWitness(TransactionWitness.redeemP2WPKH(txSig1, ecKey));
        //隔离见证的input不需要scriptSig
        transactionInput.clearScriptBytes();
        System.out.println( HEX.encode(tx.bitcoinSerialize()));

    }

    /**
     * 测试多签交易
     */
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
                        Sha256Hash.wrap("6255f5071ae34238301d2d2e75f14f3ee6ad93e4e287bdcfa783c7468589dd81"),
                        1,
                        Coin.valueOf(2000000),
                        0,
                        false,
                        new Script(Converter.hexToByte("a9148e2acc223101503adec422af45bcac35ff38b8ce87"))
                )
        );
        receiveAddressAndValue.put("mkodx6v5kgVWYH2MMVtUChPhYGFX2q1o4j", 0.01);
        receiveAddressAndValue.put("mr8uCkt9UjyHCmAzH5dsbMi4zgggvC5uvi", 0.005);
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
