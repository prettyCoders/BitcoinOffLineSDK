package dto;

import entity.UTXOKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.UTXO;
import org.bitcoinj.script.Script;

import java.util.List;
import java.util.Map;

public interface ITransaction extends BitcoinBaseInterface {

    Transaction buildLegacyTransactionWithSigners(List<UTXOKey> utxoKeys, Map<String, Double> receiveAddressAndValue);

    Transaction buildTransaction(List<UTXO> utxos, Map<String, Double> receiveAddressAndValue);

    void signMultiSigTransaction(Transaction transaction, Script knownRedeemScript, ECKey key, boolean first);
}
