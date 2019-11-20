package sdk.impl;


import dto.BitcoinBaseInterface;
import dto.IAddress;
import dto.IMnemonic;
import dto.ITransaction;
import dto.impl.AddressImpl;
import dto.impl.MnemonicImpl;
import dto.impl.TransactionImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BitcoinFactory {
    private static final BitcoinFactory instance = new BitcoinFactory();
    private static Map<Class<? extends BitcoinBaseInterface>, BitcoinBaseInterface> serviceContainer = new ConcurrentHashMap();

    private BitcoinFactory() {
    }

    public static BitcoinFactory getInstance() {
        return instance;
    }

    protected BitcoinFactory register(Class<? extends BitcoinBaseInterface> interfaceType, Class<? extends BitcoinBaseInterface> serviceType) {
        try {
            serviceContainer.put(interfaceType, serviceType.newInstance());
        } catch (Exception var4) {
            ;
        }

        return this;
    }

    public <AI> AI getService(Class<? extends BitcoinBaseInterface> interfaceType) {
        try {
            return (AI) serviceContainer.get(interfaceType);
        } catch (Exception var3) {
            return null;
        }
    }

    static {
        getInstance()
                .register(IMnemonic.class, MnemonicImpl.class)
                .register(IAddress.class, AddressImpl.class)
                .register(ITransaction.class, TransactionImpl.class);
    }
}
