import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.junit.Test;
import sdk.BitcoinOffLineSDK;
import utils.Converter;

import java.util.*;

public class TransactionTest {

    @Test
    public void send() {
        BitcoinOffLineSDK.CONFIG.setNetworkParameters(TestNet3Params.get());
        Map<String, Double> receiveAddressAndValue=new HashMap<>();
        receiveAddressAndValue.put("mumTsH5L1hq7y9R7cofhxySryNUf3hXQSG",0.02);
        receiveAddressAndValue.put("mhQRcw8EVQbuC1TNbHSvBR7YdcsVtQZDhr",0.01);
        receiveAddressAndValue.put("n192r9XTP3WU9ofnHG3XkZbVHY3GFT48vs",0.006);
        Transaction transaction=new Transaction(BitcoinOffLineSDK.CONFIG.getNetworkParameters());
        BitcoinOffLineSDK.TRANSACTION.addOutPut(transaction,receiveAddressAndValue);

        List<String> mnemonicCode = Arrays.asList("fetch", "panda", "tide", "theory", "risk", "aerobic",
                "trust", "disease", "super", "color", "annual", "lazy");
        String passphrase = "";
        byte[] seed=BitcoinOffLineSDK.MNEMONIC.generateSeed(mnemonicCode,passphrase);
        ECKey ecKey=BitcoinOffLineSDK.ADDRESS.getECKey(seed,0,1);

        UTXO utxo=new UTXO(
                Sha256Hash.wrap("2a6864abc8102bcdc1858508f5c3203a040e11a29701858cfbb03ad1bfda0854"),
                1,
                Coin.valueOf(Converter.bitcoinToSatoshis(0.03491879)),
                0,
                false,
                new Script(Converter.hexToByte("76a914d73d2a5b2f468ba9bff5fe1b1b6620ee33e7766c88ac"))
        );

        BitcoinOffLineSDK.TRANSACTION.addUTXOSign(transaction,utxo,ecKey);

        utxo=new UTXO(
                Sha256Hash.wrap("dbecf464318ed9fb61abad5fa447e93a699afe6333dbc32631378e3334558075"),
                0,
                Coin.valueOf(Converter.bitcoinToSatoshis(0.002)),
                0,
                false,
                new Script(Converter.hexToByte("76a914d73d2a5b2f468ba9bff5fe1b1b6620ee33e7766c88ac"))
        );
        BitcoinOffLineSDK.TRANSACTION.addUTXOSign(transaction,utxo,ecKey);
        List<TransactionOutput> transactionOutputs=transaction.getOutputs();

        //下面是交易产生的新的找零UTXO
        for(TransactionOutput transactionOutput:transactionOutputs){
            Script script=transactionOutput.getScriptPubKey();
            String address=script.getToAddress(BitcoinOffLineSDK.CONFIG.getNetworkParameters()).toString();
            if (address.equals("n192r9XTP3WU9ofnHG3XkZbVHY3GFT48vs")){
                System.out.println(transactionOutput.getIndex());
                System.out.println(address);
                System.out.println(transaction.getHashAsString());
                System.out.println(Converter.byteToHex(script.getProgram()));
            }
        }
        String rawTransactionHex = Converter.byteToHex(transaction.bitcoinSerialize());
        System.out.println("rawTransactionHex:" + rawTransactionHex);
    }
}
