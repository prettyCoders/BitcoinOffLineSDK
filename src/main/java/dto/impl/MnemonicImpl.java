package dto.impl;

import dto.IMnemonic;
import io.github.novacrypto.bip39.SeedCalculator;
import io.github.novacrypto.bip39.wordlists.English;
import org.bitcoinj.wallet.DeterministicSeed;

import java.security.SecureRandom;
import java.util.List;

public class MnemonicImpl implements IMnemonic {
    public List<String> generateMnemonicCode() {
        SecureRandom secureRandom = new SecureRandom();
        DeterministicSeed ds = new DeterministicSeed(secureRandom, 128, "", System.currentTimeMillis() / 1000);
        return ds.getMnemonicCode();
    }

    public byte[] generateSeed(List<String> mnemonicCode, String passphrase) {
        return new SeedCalculator().withWordsFromWordList(English.INSTANCE).calculateSeed(mnemonicCode, passphrase);
    }
}
