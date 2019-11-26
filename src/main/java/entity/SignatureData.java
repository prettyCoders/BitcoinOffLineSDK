package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bitcoinj.core.ECKey;

import java.util.ArrayList;
import java.util.List;

/**
 * 签名数据
 */
@Data
@NoArgsConstructor
public class SignatureData {
    //input下标
    private int inputIndex;

    //计算之后得出的待签名Hash
    private String simplifiedTransactionHash;

    //签名列表
    private List<ECKey.ECDSASignature> signatures=new ArrayList<>();

    public SignatureData(int inputIndex, String simplifiedTransactionHash) {
        this.inputIndex = inputIndex;
        this.simplifiedTransactionHash = simplifiedTransactionHash;
    }
}
