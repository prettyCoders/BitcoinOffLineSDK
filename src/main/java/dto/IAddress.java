package dto;

import entity.P2SHMultiSigAccount;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.script.Script;

import java.util.List;


public interface IAddress extends BitcoinBaseInterface {

    ECKey getECKey(byte[] seed, int accountIndex, int addressIndex);

    String getLegacyAddress(ECKey ecKey);

    String getSegWitAddress(ECKey ecKey);

    String getWIF(ECKey ecKey);

    String getPublicKeyAsHex(ECKey ecKey);

    String getPrivateKeyAsHex(ECKey ecKey);

    byte[] getPrivateKeyBytes(ECKey ecKey);

    ECKey publicKeyToECKey(String publicKeyHex);

    ECKey WIFToECKey(String WIF);

    P2SHMultiSigAccount generateMultiSigAddress(int threshold, List<ECKey> keys);

    String signMessage(String WIF,String message);

    boolean verifyMessage(String publicKeyHex,String message,String signatureBase64);

    boolean isLegacyAddress(String addressBase58);

    boolean isSegWitAddress(String addressBase58);

    String scriptToAddress(Script script);



}
