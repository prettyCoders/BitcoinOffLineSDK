import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.crypto.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.UnitTestParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.KeyChain;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.*;

public class DeterministicKeyChainTest {
    private DeterministicKeyChain chain;
    private DeterministicKeyChain segWitChain;
    private DeterministicKeyChain bip44chain;
    private final byte[] ENTROPY = Sha256Hash.hash("don't use a string seed like this in real life".getBytes());
    private static final NetworkParameters UNITTEST = UnitTestParams.get();
    private static final NetworkParameters MAINNET = MainNetParams.get();
    private static final List<ChildNumber> BIP44_COIN_1_ACCOUNT_ZERO_PATH = ImmutableList.of(new ChildNumber(44, true),
            new ChildNumber(1, true), ChildNumber.ZERO_HARDENED);

    @Before
    public void setup() {
        BriefLogFormatter.init();
        // You should use a random seed instead. The secs constant comes from the unit test file, so we can compare
        // serialized data properly.
        long secs = 1389353062L;
        chain = DeterministicKeyChain.builder().entropy(ENTROPY, secs)
                .accountPath(DeterministicKeyChain.ACCOUNT_ZERO_PATH).outputScriptType(Script.ScriptType.P2PKH).build();
        chain.setLookaheadSize(10);

        segWitChain = DeterministicKeyChain.builder().entropy(ENTROPY, secs)
                .accountPath(DeterministicKeyChain.ACCOUNT_ONE_PATH).outputScriptType(Script.ScriptType.P2WPKH).build();
        segWitChain.setLookaheadSize(10);

        bip44chain = DeterministicKeyChain.builder().entropy(ENTROPY, secs).accountPath((ImmutableList<ChildNumber>) BIP44_COIN_1_ACCOUNT_ZERO_PATH)
                .outputScriptType(Script.ScriptType.P2PKH).build();
        bip44chain.setLookaheadSize(10);

    }

    @Test
    public void derive() throws Exception {
        ECKey key1 = chain.getKey(KeyChain.KeyPurpose.RECEIVE_FUNDS);
        System.out.println(((DeterministicKey) key1).getPathAsString());
        assertFalse(key1.isPubKeyOnly());
        ECKey key2 = chain.getKey(KeyChain.KeyPurpose.RECEIVE_FUNDS);
        for (int i = 0; i < 110; i++) {
//            System.out.println(chain.getKey(KeyChain.KeyPurpose.RECEIVE_FUNDS).getPathAsString());
            System.out.println(segWitChain.getKey(KeyChain.KeyPurpose.RECEIVE_FUNDS).getPathAsString());
        }

        System.out.println(chain.getMnemonicCode());
        System.out.println(((DeterministicKey) key2).getPath());
        assertFalse(key2.isPubKeyOnly());

        final Address address = LegacyAddress.fromBase58(UNITTEST, "n1bQNoEx8uhmCzzA5JPG6sFdtsUQhwiQJV");
        assertEquals(address, LegacyAddress.fromKey(UNITTEST, key1));
        assertEquals("mnHUcqUVvrfi5kAaXJDQzBb9HsWs78b42R", LegacyAddress.fromKey(UNITTEST, key2).toString());
        assertEquals(key1, chain.findKeyFromPubHash(address.getHash()));
        assertEquals(key2, chain.findKeyFromPubKey(key2.getPubKey()));

        key1.sign(Sha256Hash.ZERO_HASH);
        assertFalse(key1.isPubKeyOnly());

        ECKey key3 = chain.getKey(KeyChain.KeyPurpose.CHANGE);
        assertFalse(key3.isPubKeyOnly());
        assertEquals("mqumHgVDqNzuXNrszBmi7A2UpmwaPMx4HQ", LegacyAddress.fromKey(UNITTEST, key3).toString());
        key3.sign(Sha256Hash.ZERO_HASH);
        assertFalse(key3.isPubKeyOnly());

    }
}