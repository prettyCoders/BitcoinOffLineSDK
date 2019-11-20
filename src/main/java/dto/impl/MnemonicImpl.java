package dto.impl;

import dto.IMnemonic;
import io.github.novacrypto.bip39.SeedCalculator;
import io.github.novacrypto.bip39.wordlists.English;
import org.bitcoinj.wallet.DeterministicSeed;

import java.security.SecureRandom;
import java.util.List;

/**
 * 比特币助记词相关
 */
public class MnemonicImpl implements IMnemonic {

    /**
     * 生成助记词
     * @return 助记词
     */
    public List<String> generateMnemonicCode() {
        SecureRandom secureRandom = new SecureRandom();
        DeterministicSeed ds = new DeterministicSeed(secureRandom, 128, "", System.currentTimeMillis() / 1000);
        return ds.getMnemonicCode();
    }

    /**
     * 通过助记词和密码生成种子
     * @param mnemonicCode 助记词
     * @param passphrase 种子
     * @return 种子
     */
    public byte[] generateSeed(List<String> mnemonicCode, String passphrase) {
        return new SeedCalculator().withWordsFromWordList(English.INSTANCE).calculateSeed(mnemonicCode, passphrase);
    }
}
