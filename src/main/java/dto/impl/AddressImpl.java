package dto.impl;

import dto.IAddress;
import entity.P2SHMultiSigAccount;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import sdk.BitcoinOffLineSDK;
import utils.Converter;

import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;

public class AddressImpl implements IAddress {

    public ECKey getECKey(byte[] seed, int accountIndex, int addressIndex) {
        DeterministicKey masterPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
        DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(masterPrivateKey);
        List<ChildNumber> parentPath = new ArrayList<>();
        parentPath.add(new ChildNumber(accountIndex));
        DeterministicKey deterministicKey = deterministicHierarchy.deriveChild(parentPath, false, true, new ChildNumber(addressIndex));
        return ECKey.fromPrivate(deterministicKey.getPrivKey());
    }

    @Override
    public String getAddress(ECKey ecKey) {
        return ecKey.toAddress(BitcoinOffLineSDK.CONFIG.getNetworkParameters()).toBase58();
    }

    @Override
    public String getWIF(ECKey ecKey) {
        return ecKey.getPrivateKeyAsWiF(BitcoinOffLineSDK.CONFIG.getNetworkParameters());
    }

    @Override
    public String getPublicKeyAsHex(ECKey ecKey) {
        return ecKey.getPublicKeyAsHex();
    }

    @Override
    public String getPrivateKeyAsHex(ECKey ecKey) {
        return ecKey.getPrivateKeyAsHex();
    }

    @Override
    public byte[] getPrivateKeyBytes(ECKey ecKey) {
        return ecKey.getPrivKeyBytes();
    }

    @Override
    public ECKey publicKeyToECKey(String publicKeyHex) {
        return ECKey.fromPublicOnly(Converter.hexToByte(publicKeyHex));
    }

    @Override
    public ECKey WIFToECKey(String WIF) {
        return DumpedPrivateKey.fromBase58(BitcoinOffLineSDK.CONFIG.getNetworkParameters(), WIF).getKey();
    }

    @Override
    public P2SHMultiSigAccount generateMultiSigAddress(int threshold, List<ECKey> keys) {
        //创建多签赎回脚本
        Script redeemScript = ScriptBuilder.createRedeemScript(threshold, keys);
        //为给定的赎回脚本创建scriptPubKey
        Script script = ScriptBuilder.createP2SHOutputScript(redeemScript);
        //返回一个地址，该地址表示从给定的scriptPubKey中提取的脚本HASH
        Address multiSigAddress = Address.fromP2SHScript(BitcoinOffLineSDK.CONFIG.getNetworkParameters(), script);
        return new P2SHMultiSigAccount(redeemScript, multiSigAddress);
    }

    @Override
    public String signMessage(String WIF, String message) {
        ECKey ecKey=WIFToECKey(WIF);
        return ecKey.signMessage(message);
    }

    @Override
    public boolean verifyMessage(String publicKeyHex, String message, String signatureBase64) {
        ECKey ecKey = BitcoinOffLineSDK.ADDRESS.publicKeyToECKey(publicKeyHex);
        try {
            ecKey.verifyMessage(message,signatureBase64);
        } catch (SignatureException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
