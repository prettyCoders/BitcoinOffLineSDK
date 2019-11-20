package sdk;

import dto.IAddress;
import dto.IMnemonic;
import dto.ITransaction;
import sdk.impl.BitcoinFactory;

/**
 * SDK服务
 */
public class BitcoinOffLineSDK {

    public static final IMnemonic MNEMONIC;
    public static final IAddress ADDRESS;
    public static final ITransaction TRANSACTION;
    public static final BitcoinSDKConfig CONFIG=BitcoinSDKConfig.getInstance();

    static {
        BitcoinFactory factory=BitcoinFactory.getInstance();
        MNEMONIC = factory.getService(IMnemonic.class);
        ADDRESS=factory.getService(IAddress.class);
        TRANSACTION=factory.getService(ITransaction.class);
    }
}
