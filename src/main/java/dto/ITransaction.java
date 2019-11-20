package dto;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.UTXO;
import org.bitcoinj.script.Script;

import java.util.List;
import java.util.Map;

public interface ITransaction extends BitcoinBaseInterface {

    void addOutPut(Transaction transaction, Map<String, Double> receiveAddressAndValue);

    void addUTXOSign(Transaction transaction, UTXO utxo, ECKey ecKey);

    Transaction buildTransactionFromMultiSigAddress(List<UTXO> utxos, Map<String, Double> receiveAddressAndValue);

    void signMultiSigTransaction(Transaction transaction, Script knownRedeemScript, ECKey key, boolean first);
}
