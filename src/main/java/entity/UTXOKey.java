package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.UTXO;

/**
 * UTXO和签名此UTXO的ECKey组成的数据结构，便于构建交易的时候签名
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UTXOKey {

    /**
     * 未花费输出
     */
    private UTXO utxo;

    /**
     * ECKey
     */
    private ECKey ecKey;
}
