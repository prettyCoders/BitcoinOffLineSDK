package dto;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.UTXO;

import java.util.Map;

public interface ITransaction extends BitcoinBaseInterface {

    void addOutPut(Transaction transaction, Map<String, Double> receiveAddressAndValue);

    void addUTXOSign(Transaction transaction, UTXO utxo, ECKey ecKey);
}
