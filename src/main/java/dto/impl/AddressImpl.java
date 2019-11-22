package dto.impl;

import dto.IAddress;
import entity.P2SHMultiSigAccount;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptPattern;
import org.bouncycastle.util.encoders.Hex;
import sdk.BitcoinOffLineSDK;
import utils.Converter;

import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 比特币地址相关(集成bip32,bip39)
 */
public class AddressImpl implements IAddress {

    /**
     * 通过种子和路径获取ECKey
     *
     * @param seed         种子
     * @param accountIndex 账户索引
     * @param addressIndex 地址索引
     * @return ECKey
     */
    public ECKey getECKey(byte[] seed, int accountIndex, int addressIndex) {
        DeterministicKey masterPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
        DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(masterPrivateKey);
        List<ChildNumber> parentPath = new ArrayList<>();
        parentPath.add(new ChildNumber(accountIndex));
        DeterministicKey deterministicKey = deterministicHierarchy.deriveChild(parentPath, false, true, new ChildNumber(addressIndex));
        return ECKey.fromPrivate(deterministicKey.getPrivKey());
    }

    /**
     * 通过ECKey获取P2PKH地址
     *
     * @param ecKey ECKey
     * @return Base58编码的地址文本
     */
    @Override
    public String getLegacyAddress(ECKey ecKey) {
        LegacyAddress legacyAddress = LegacyAddress.fromKey(BitcoinOffLineSDK.CONFIG.getNetworkParameters(), ecKey);
        return legacyAddress.toBase58();
    }

    /**
     * 获取隔离见证地址
     * @param ecKey ECKey
     * @return bech32编码后的地址文本
     */
    @Override
    public String getSegWitAddress(ECKey ecKey) {
        SegwitAddress segwitAddress = SegwitAddress.fromKey(BitcoinOffLineSDK.CONFIG.getNetworkParameters(), ecKey);
        return segwitAddress.toBech32();
    }

    /**
     * 通过ECKey获取钱包可导入格式的私钥
     *
     * @param ecKey ECKey
     * @return 钱包可导入格式的私钥
     */
    @Override
    public String getWIF(ECKey ecKey) {
        return ecKey.getPrivateKeyAsWiF(BitcoinOffLineSDK.CONFIG.getNetworkParameters());
    }

    /**
     * 通过ECKey获取公钥的十六进制格式文本
     *
     * @param ecKey ECKey
     * @return 公钥的十六进制格式文本
     */
    @Override
    public String getPublicKeyAsHex(ECKey ecKey) {
        return ecKey.getPublicKeyAsHex();
    }

    /**
     * 通过ECKey获取私钥的十六进制格式文本
     *
     * @param ecKey ECKey
     * @return 私钥的十六进制格式文本
     */
    @Override
    public String getPrivateKeyAsHex(ECKey ecKey) {
        return ecKey.getPrivateKeyAsHex();
    }

    /**
     * 通过ECKey获取私钥的字节数组
     *
     * @param ecKey ECKey
     * @return 私钥的字节数组
     */
    @Override
    public byte[] getPrivateKeyBytes(ECKey ecKey) {
        return ecKey.getPrivKeyBytes();
    }

    /**
     * 通过十六进制公钥文本构建ECKey，通常用于验签
     *
     * @param publicKeyHex 十六进制公钥文本
     * @return ECKey
     */
    @Override
    public ECKey publicKeyToECKey(String publicKeyHex) {
        return ECKey.fromPublicOnly(Hex.decode(publicKeyHex));
    }

    /**
     * 通过钱包可导入格式私钥构建ECKey，通常用于导入账户
     *
     * @param WIF 钱包可导入格式私钥
     * @return ECKey
     */
    @Override
    public ECKey WIFToECKey(String WIF) {
        return DumpedPrivateKey.fromBase58(BitcoinOffLineSDK.CONFIG.getNetworkParameters(), WIF).getKey();
    }

    /**
     * 生成多签地址
     *
     * @param threshold 最低签名数量
     * @param keys      公钥构建的ECKey列表
     * @return P2SHMultiSigAccount对象
     */
    @Override
    public P2SHMultiSigAccount generateMultiSigAddress(int threshold, List<ECKey> keys) {
        //创建多签赎回脚本
        Script redeemScript = ScriptBuilder.createRedeemScript(threshold, keys);
        //为给定的赎回脚本创建scriptPubKey
        Script script = ScriptBuilder.createP2SHOutputScript(redeemScript);
        //返回一个地址，该地址表示从给定的scriptPubKey中提取的脚本HASH
        byte[] scriptHash = ScriptPattern.extractHashFromP2SH(script);
        LegacyAddress multiSigAddress = LegacyAddress.fromScriptHash(BitcoinOffLineSDK.CONFIG.getNetworkParameters(), scriptHash);
//        Address multiSigAddress = Address.fromP2SHScript(BitcoinOffLineSDK.CONFIG.getNetworkParameters(), script);
        return new P2SHMultiSigAccount(redeemScript, multiSigAddress);
    }

    /**
     * 消息签名
     *
     * @param WIF     钱包可导入模式的私钥
     * @param message 待签名消息
     * @return 签名后的base64文本
     */
    @Override
    public String signMessage(String WIF, String message) {
        ECKey ecKey = WIFToECKey(WIF);
        return ecKey.signMessage(message);
    }

    /**
     * 消息验签
     *
     * @param publicKeyHex    十六进制公钥
     * @param message         消息
     * @param signatureBase64 签名base64格式文本
     * @return 结果
     */
    @Override
    public boolean verifyMessage(String publicKeyHex, String message, String signatureBase64) {
        ECKey ecKey = BitcoinOffLineSDK.ADDRESS.publicKeyToECKey(publicKeyHex);
        try {
            ecKey.verifyMessage(message, signatureBase64);
        } catch (SignatureException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 判断是否是普通地址
     * @param addressBase58 地址文本格式
     * @return 结果
     */
    @Override
    public boolean isLegacyAddress(String addressBase58) {
        try {
            byte[] versionAndDataBytes = Base58.decodeChecked(addressBase58);
            int version = versionAndDataBytes[0] & 0xFF;
            return version == BitcoinOffLineSDK.CONFIG.getNetworkParameters().getAddressHeader() ||
                    version == BitcoinOffLineSDK.CONFIG.getNetworkParameters().getP2SHHeader();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断是否是隔离见证地址
     * @param addressBase58 地址文本格式
     * @return 结果
     */
    @Override
    public boolean isSegWitAddress(String addressBase58) {
        try {
            Bech32.Bech32Data bechData = Bech32.decode(addressBase58);
            return bechData.hrp.equals(BitcoinOffLineSDK.CONFIG.getNetworkParameters().getSegwitAddressHrp());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 根据脚本计算地址文本
     * @param script 脚本
     * @return 地址
     */
    @Override
    public String scriptToAddress(Script script) {
        NetworkParameters networkParameters = BitcoinOffLineSDK.CONFIG.getNetworkParameters();
        Script.ScriptType scriptType = script.getScriptType();
        if (scriptType == Script.ScriptType.P2PK || scriptType == Script.ScriptType.P2SH || scriptType == Script.ScriptType.P2PKH) {
            return ((LegacyAddress) script.getToAddress(networkParameters)).toBase58();
        } else if (scriptType == Script.ScriptType.P2WPKH || scriptType == Script.ScriptType.P2WSH) {
            return ((SegwitAddress) script.getToAddress(networkParameters)).toBech32();
        } else {
            return null;
        }
    }

}
