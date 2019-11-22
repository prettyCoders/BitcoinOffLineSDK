package dto.impl;

import dto.ITransaction;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;

import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptChunk;
import sdk.BitcoinOffLineSDK;
import utils.Converter;

import java.util.*;

/**
 * 比特币交易相关
 */
public class TransactionImpl implements ITransaction {


    /**
     * 添加交易的输出
     *
     * @param transaction            交易
     * @param receiveAddressAndValue 接收地址和金额
     */
    @Override
    public void addOutPut(Transaction transaction, Map<String, Double> receiveAddressAndValue) {
        addOutputs(transaction, receiveAddressAndValue);
    }

    /**
     * 添加交易输出列表
     *
     * @param transaction            待添加outputs的交易
     * @param receiveAddressAndValue 接收地址和金额
     * @return 添加完成的交易
     */
    private static void addOutputs(Transaction transaction, Map<String, Double> receiveAddressAndValue) {
        //构建outputs
        for (Object o : receiveAddressAndValue.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String receiveAddress = (String) entry.getKey();
            Address address;
            if (BitcoinOffLineSDK.ADDRESS.isLegacyAddress(receiveAddress)) {
                address = LegacyAddress.fromBase58(BitcoinOffLineSDK.CONFIG.getNetworkParameters(), receiveAddress);
            } else if (BitcoinOffLineSDK.ADDRESS.isSegWitAddress(receiveAddress)) {
                address = SegwitAddress.fromBech32(BitcoinOffLineSDK.CONFIG.getNetworkParameters(), receiveAddress);
            } else {
                throw new AddressFormatException.InvalidPrefix("No network found for " + receiveAddress);
            }

            Coin value = Coin.valueOf(Converter.bitcoinToSatoshis((Double) entry.getValue()));
            //接收地址
//            LegacyAddress receiver = LegacyAddress.fromBase58(networkParameters, receiveAddress);
            //添加OUTPUT
            transaction.addOutput(value, address);

        }
    }

    /**
     * 添加UTXO(inputs)，并签名
     *
     * @param transaction 交易
     * @param utxo        utxo
     * @param ecKey       ECKey
     */
    @Override
    public void addUTXOSign(Transaction transaction, UTXO utxo, ECKey ecKey) {
        TransactionOutPoint outPoint = new TransactionOutPoint(BitcoinOffLineSDK.CONFIG.getNetworkParameters(), utxo.getIndex(), utxo.getHash());
        transaction.addSignedInput(outPoint, utxo.getScript(), ecKey, Transaction.SigHash.ALL, true);
    }

    /**
     * 构建多签转账交易
     *
     * @param utxos                  utxo列表
     * @param receiveAddressAndValue 接收地址和金额
     * @return 交易
     */
    @Override
    public Transaction buildTransactionFromMultiSigAddress
    (List<UTXO> utxos, Map<String, Double> receiveAddressAndValue) {
        //新建交易
        Transaction transaction = new Transaction(BitcoinOffLineSDK.CONFIG.getNetworkParameters());
        //构建Inputs
        for (UTXO utxo : utxos) {
            ScriptBuilder scriptBuilder = new ScriptBuilder();
            // Script of this output
            scriptBuilder.data(utxo.getScript().getProgram());
            transaction.addInput(utxo.getHash(), utxo.getIndex(), scriptBuilder.build());
        }
        //构建输出列表
        addOutputs(transaction, receiveAddressAndValue);
        return transaction;
    }

    /**
     * 多签转账交易签名
     *
     * @param transaction       多签交易
     * @param knownRedeemScript 已知的赎回脚本(创建多签地址的时候生成的)
     * @param key               签名所需的ECKey
     * @param first             首次签名(也就是第一个签名)
     */
    @Override
    public void signMultiSigTransaction(Transaction transaction, Script knownRedeemScript, ECKey key, boolean first) {
        List<TransactionInput> inputs = transaction.getInputs();
        //遍历交易的inputs
        for (int i = 0; i < inputs.size(); i++) {
            TransactionInput input = inputs.get(i);
            if (first) { //如果是第一个签名
                //计算待签名hash,也就是交易的简化形式的hash值,对这个hash值进行签名
                Sha256Hash sigHash = transaction.hashForSignature(i, knownRedeemScript, Transaction.SigHash.ALL, false);
                //签名之后得到ECDSASignature
                ECKey.ECDSASignature ecdsaSignature = key.sign(sigHash);
                //ECDSASignature转换为TransactionSignature
                TransactionSignature transactionSignature = new TransactionSignature(ecdsaSignature, Transaction.SigHash.ALL, false);

                // 创建p2sh多重签名的输入脚本
                Script inputScript = ScriptBuilder.createP2SHMultiSigInputScript(Collections.singletonList(transactionSignature), knownRedeemScript);

                //将脚本添加进input
                input.setScriptSig(inputScript);
//                input.verify();
            } else {
                // 获取输入的脚本元素列表(可以理解为操作码列表)
                Script inputScript = transaction.getInput(i).getScriptSig();
                List<ScriptChunk> scriptChunks = inputScript.getChunks();

                //创建一个空的签名列表，这个List最后会保存所有的签名
                List<TransactionSignature> signatureList = new ArrayList<>();

                //由于脚本元素列表中的最后一个签名是redeemScript，所以用过迭代的方式获取redeemScript
                //迭代过程中也将别人之前的签名添加到了上面的签名列表里
                Iterator<ScriptChunk> iterator = scriptChunks.iterator();
                Script redeemScript = null;
                while (iterator.hasNext()) {
                    ScriptChunk chunk = iterator.next();

                    if (iterator.hasNext() && chunk.opcode != 0) {
                        TransactionSignature transactionSignature = null;
                        try {
                            transactionSignature = TransactionSignature.decodeFromBitcoin(Objects.requireNonNull(chunk.data), false, false);
                        } catch (SignatureDecodeException e) {
                            e.printStackTrace();
                        }
                        signatureList.add(transactionSignature);
                    } else {
                        redeemScript = new Script(Objects.requireNonNull(chunk.data));
                    }
                }

                //计算待签名hash,也就是交易的简化形式的hash值,对这个hash值进行签名
                Sha256Hash sigHash = transaction.hashForSignature(i, Objects.requireNonNull(redeemScript), Transaction.SigHash.ALL, false);

                //签名之后得到ECDSASignature
                ECKey.ECDSASignature ecdsaSignature = key.sign(sigHash);

                //ECDSASignature转换为TransactionSignature
                TransactionSignature transactionSignature = new TransactionSignature(ecdsaSignature, Transaction.SigHash.ALL, false);

                //添加本次签名的数据
                signatureList.add(transactionSignature);

                // 重新构建p2sh多重签名的输入脚本
                inputScript = ScriptBuilder.createP2SHMultiSigInputScript(signatureList, redeemScript);

                //更新新的脚本
                input.setScriptSig(inputScript);
//                input.verify();
            }
        }
    }
}
