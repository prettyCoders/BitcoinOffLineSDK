package dto;

import java.util.List;

public interface IMnemonic extends BitcoinBaseInterface{

    List<String> generateMnemonicCode();

    byte[] generateSeed(List<String> mnemonicCode, String passphrase);

}
