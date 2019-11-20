package sdk;

import lombok.Data;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;

/**
 * SDK配置类
 */
@Data
public final class BitcoinSDKConfig {
    private static final BitcoinSDKConfig INSTANCE = new BitcoinSDKConfig();
    private NetworkParameters networkParameters=MainNetParams.get();

    public static BitcoinSDKConfig getInstance() {
        return INSTANCE;
    }

    private BitcoinSDKConfig() {}


}
