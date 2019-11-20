package dto.impl;

import dto.ITransaction;
import org.bitcoinj.core.*;
import sdk.BitcoinOffLineSDK;
import utils.Converter;

import java.util.Map;

public class TransactionImpl implements ITransaction {


    @Override
    public void addOutPut(Transaction transaction ,Map<String, Double> receiveAddressAndValue) {
        addOutputs(BitcoinOffLineSDK.CONFIG.getNetworkParameters(), transaction, receiveAddressAndValue);
    }

    /**
     * 添加交易输出列表
     *
     * @param networkParameters      网络参数
     * @param transaction            待添加outputs的交易
     * @param receiveAddressAndValue 接收地址和金额
     * @return 添加完成的交易
     */
    private static void addOutputs(
            NetworkParameters networkParameters, Transaction transaction, Map<String, Double> receiveAddressAndValue) {
        //构建outputs
        for (Object o : receiveAddressAndValue.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String receiveAddress = (String) entry.getKey();
            Coin value = Coin.valueOf(Converter.bitcoinToSatoshis((Double) entry.getValue()));
            //接收地址
            Address receiver = Address.fromBase58(networkParameters, receiveAddress);
            //添加OUTPUT
            transaction.addOutput(value, receiver);
        }
    }

    @Override
    public void addUTXOSign(Transaction transaction, UTXO utxo, ECKey ecKey) {
        TransactionOutPoint outPoint = new TransactionOutPoint(BitcoinOffLineSDK.CONFIG.getNetworkParameters(), utxo.getIndex(), utxo.getHash());
        transaction.addSignedInput(outPoint, utxo.getScript(), ecKey, Transaction.SigHash.ALL, true);
    }
}
